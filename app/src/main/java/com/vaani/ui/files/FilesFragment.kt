package com.vaani.ui.files

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.models.SortOrder
import com.vaani.player.PlayerUtil
import com.vaani.ui.UiUtil
import com.vaani.ui.favourites.FavouriteFragment
import com.vaani.ui.folderList.FolderFragment
import com.vaani.ui.listUtil.AbstractListAdapter
import com.vaani.ui.listUtil.AbstractListFragment
import com.vaani.ui.listUtil.ListItemCallbacks
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@UnstableApi
object FilesFragment : AbstractListFragment<FileEntity>(), ListItemCallbacks {

    private lateinit var copyLauncher: ActivityResultLauncher<Uri?>
    private lateinit var selectedFile: FileEntity
    var currentFolder: FolderEntity = FolderEntity()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        copyLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree(), this::copyFile)
        resetData(Files.getCollectionFiles(currentFolder.id))
    }

    override val listAdapter: AbstractListAdapter = object : AbstractListAdapter(
        this, R.layout.file_item, R.menu.file_list_option_menu
    ) {
        override fun setViewData(view: View, position: Int) {
            view.findViewById<TextView>(R.id.file_text).text = displayList[position].name
            view.findViewById<TextView>(R.id.file_subtext).text = UiUtil.stringToTime(displayList[position].duration)
            view.findViewById<ImageView>(R.id.file_image).setImageResource(
                when (displayList[position].isAudio) {
                    true -> R.drawable.music_note_40px
                    false -> R.drawable.movie_40px
                }
            )
        }

        override fun getItemCount(): Int = displayList.size
    }

    override fun refreshAction() {
        CoroutineScope(Job()).launch {
            val newFiles = Files.exploreFolder(currentFolder)
            resetData(newFiles)
            launch(Dispatchers.Main) {
                listAdapter.notifyDataSetChanged()
                refreshFinish()
            }
        }
    }

    override fun fabAction(view: View) {
        if (PlayerUtil.controller?.isPlaying != true || PlayerData.currentCollection != currentFolder.id) {
            PlayerUtil.playLastPlayed(currentFolder)
        } else {
            Log.d(TAG, "fabAction: already playing")
            PlayerUtil.startPlayerActivity()
        }
    }

    override fun search(string: String?) {
        resetData(Files.getCollectionFiles(currentFolder.id))
        if (string != null && string.isNotBlank()) {
            displayList.retainAll { it.name.lowercase().contains(string.lowercase()) }
        }
    }

    override fun sort() {
        when (sortOrder) {
            SortOrder.ASC, SortOrder.RANK -> displayList.sortBy { it.name.lowercase() }
            SortOrder.DSC -> {
                displayList.sortBy { it.name.lowercase() }
                displayList.reverse()
            }
        }
    }

    override fun onClick(position: Int) = PlayerUtil.play(displayList[position], currentFolder.id)

    override fun onOptions(position: Int, menu: Menu) {
        selectedFile = displayList[position]
        menu.findItem(R.id.file_list_option_add_fav).setOnMenuItemClickListener {
            val newFav = Files.addFavourite(selectedFile)
            FavouriteFragment.displayList.add(newFav)
            FavouriteFragment.listAdapter.notifyItemInserted(FavouriteFragment.displayList.size - 1)
            true
        }
        menu.findItem(R.id.file_list_option_copy).apply {
            setOnMenuItemClickListener {
                copyLauncher.launch(null)
                true
            }
        }
        menu.findItem(R.id.file_list_option_rename).apply {
            if (selectedFile.isUri) {
                isVisible = false
            } else {
                setOnMenuItemClickListener {
                    renameFile()
                    true
                }
            }
        }
        menu.findItem(R.id.file_list_option_move).apply {
            if (selectedFile.isUri) {
                isVisible = false
            } else {
                setOnMenuItemClickListener {
                    true
                }
            }
        }
        menu.findItem(R.id.file_list_option_delete).apply {
            if (selectedFile.isUri) {
                isVisible = false
            } else {
                setOnMenuItemClickListener {
                    deleteFile()
                    true
                }
            }
        }
    }

    private fun copyFile(uri: Uri?) {
        uri?.let {
            CoroutineScope(Job()).launch {
                val newFolder = Files.copyFile(selectedFile, it)
                FolderFragment.displayList.indexOf(newFolder).let {
                    if (it == -1) {
                        FolderFragment.displayList.add(newFolder)
                        FolderFragment.listAdapter.notifyItemInserted(FolderFragment.displayList.size - 1)
                    } else {
                        FolderFragment.listAdapter.notifyItemChanged(it)
                    }
                }
            }
        }
    }

    private fun renameFile() {
        val renameView = LayoutInflater.from(requireContext()).inflate(R.layout.rename_popup, null)
        val editTextView = renameView?.findViewById<EditText>(R.id.exit_text)
        val fileExtension = selectedFile.name.lastIndexOf('.').let {
            if (it == -1) "" else selectedFile.name.substring(it)
        }
        editTextView?.setText(selectedFile.name.removeSuffix(fileExtension))
        renameView?.findViewById<TextView>(R.id.extension_text)?.text = fileExtension
        AlertDialog.Builder(recyclerView.context)
            .setTitle("Rename file")
            .setView(renameView)
            .setPositiveButton(R.string.rename) { dialogInterface: DialogInterface, i: Int ->
                val newName = "${editTextView?.text}$fileExtension"
                try {
                    Files.rename(selectedFile, newName)
                    listAdapter.notifyItemChanged(displayList.indexOf(selectedFile))
                } catch (e: Exception) {
                    Log.e(TAG, "renameFile: error", e)
                    Toast.makeText(requireContext(), "Unable to rename", Toast.LENGTH_SHORT).show()
                }
                dialogInterface.dismiss()
            }.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.cancel()
            }.show()
    }

    private fun deleteFile() {
        AlertDialog.Builder(recyclerView.context)
            .setTitle("Do you want to permanently delete the file ${selectedFile.name}?")
            .setPositiveButton(R.string.rename) { dialogInterface: DialogInterface, i: Int ->
                try {
                    Files.delete(selectedFile)
                    displayList.indexOf(selectedFile).let { idx ->
                        displayList.removeAt(idx)
                        listAdapter.notifyItemRemoved(idx)
                    }
                    FavouriteFragment.displayList.indexOfFirst { fav -> fav.fileId == selectedFile.id }.let { idx ->
                        if (idx != -1) {
                            FavouriteFragment.displayList.removeAt(idx)
                            FavouriteFragment.listAdapter.notifyItemRemoved(idx)
                        }
                    }
                    FolderFragment.displayList.indexOf(currentFolder).let { idx ->
                        if (currentFolder.items == 0) {
                            FolderFragment.displayList.removeAt(idx)
                            FolderFragment.listAdapter.notifyItemRemoved(idx)
                        } else {
                            FolderFragment.listAdapter.notifyItemChanged(idx)
                        }

                    }
                } catch (e: Exception) {
                    Log.e(TAG, "deleteFile: error", e)
                    Toast.makeText(requireContext(), "Unable to delete", Toast.LENGTH_SHORT).show()
                }
                dialogInterface.dismiss()
            }.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.cancel()
            }.show()
    }

}