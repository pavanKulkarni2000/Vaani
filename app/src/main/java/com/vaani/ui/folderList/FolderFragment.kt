package com.vaani.ui.folderList

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FolderEntity
import com.vaani.models.SortOrder
import com.vaani.player.PlayerUtil
import com.vaani.ui.favourites.FavouriteFragment
import com.vaani.ui.files.FilesFragment
import com.vaani.ui.listUtil.AbstractListAdapter
import com.vaani.ui.listUtil.AbstractListFragment
import com.vaani.ui.listUtil.ListItemCallbacks
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
object FolderFragment : AbstractListFragment<FolderEntity>(), ListItemCallbacks {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        resetData(Files.allFolders)
    }

    private lateinit var selectedFolder: FolderEntity
    override val listAdapter = object :
        AbstractListAdapter(this, R.layout.folder_item, R.menu.fol_list_action_menu) {
        override fun setViewData(view: View, position: Int) {
            view.findViewById<TextView>(R.id.folder_text).text = displayList[position].name
            view.findViewById<TextView>(R.id.folder_subtext).text =
                String.format("%d media files", displayList[position].items)
        }

        override fun getItemCount(): Int = displayList.size
    }

    override fun fabAction(view: View) {
        if (PlayerUtil.controller?.isPlaying != true) {
            val idx = displayList.indexOfFirst { it.id == PreferenceUtil.lastPlayedFolderId }
            if(idx!=-1){
                onClick(idx)
            }
            FilesFragment.fabAction(view)
        } else {
            PlayerUtil.startPlayerActivity()
        }
    }

    override fun search(string: String?) {
        resetData(Files.allFolders)
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

    override fun onClick(position: Int) {
        FilesFragment.currentFolder = displayList[position]
        requireParentFragment().parentFragmentManager.commit {
            add(R.id.fragment_container_view, FilesFragment)
            addToBackStack(null)
        }
    }

    override fun onOptions(position: Int, menu: Menu) {
        selectedFolder = displayList[position]
        menu.findItem(R.id.folder_option_rename).apply {
            if (selectedFolder.isUri) {
                isVisible = false
            } else {
                setOnMenuItemClickListener {
                    renameFolder()
                    true
                }
            }
        }
        menu.findItem(R.id.folder_option_delete).apply {
            setOnMenuItemClickListener {
                deleteFolder()
                true
            }
        }
    }

    private fun renameFolder() {
        val renameView = LayoutInflater.from(requireContext()).inflate(R.layout.rename_popup, null)
        val editTextView = renameView?.findViewById<EditText>(R.id.exit_text)
        editTextView?.setText(selectedFolder.name)
        renameView?.findViewById<TextView>(R.id.extension_text)?.visibility = View.GONE
        AlertDialog.Builder(recyclerView.context)
            .setTitle("Rename folder")
            .setView(renameView)
            .setPositiveButton(R.string.rename) { dialogInterface: DialogInterface, i: Int ->
                CoroutineScope(Job()).launch {
                    try {
                        Files.rename(selectedFolder, editTextView?.text.toString())
                        withContext(Dispatchers.Main) {
                            listAdapter.notifyItemChanged(displayList.indexOf(selectedFolder))
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "rename folder: error", e)
                        Toast.makeText(requireContext(), "Unable to rename", Toast.LENGTH_SHORT).show()
                    }
                }
                dialogInterface.dismiss()
            }.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.cancel()
            }.show()
    }

    private fun deleteFolder() {
        AlertDialog.Builder(recyclerView.context)
            .setTitle("Do you want to permanently delete the file ${selectedFolder.name}?")
            .setPositiveButton(R.string.delete) { dialogInterface: DialogInterface, i: Int ->
                try {
                    CoroutineScope(Job()).launch {
                        Files.delete(selectedFolder)
                        withContext(Dispatchers.Main) {
                            displayList.indexOf(selectedFolder).let { idx ->
                                displayList.removeAt(idx)
                                listAdapter.notifyItemRemoved(idx)
                            }
                            FavouriteFragment.resetData(Files.favourites)
                            FavouriteFragment.listAdapter.notifyDataSetChanged()
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "deleteFolder: error", e)
                    Toast.makeText(requireContext(), "Unable to delete", Toast.LENGTH_SHORT).show()
                }
                dialogInterface.dismiss()
            }.setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.cancel()
            }.show()
    }

    override fun refreshAction() {
        CoroutineScope(Job()).launch {
            Files.exploreFolders()
            launch(Dispatchers.Main) {
                listAdapter.notifyDataSetChanged()
                refreshFinish()
            }
        }
    }

}