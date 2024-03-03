package com.vaani.ui.medias

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.vaani.ui.MainActivity
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FolderEntity
import com.vaani.models.MediaEntity
import com.vaani.player.PlayerUtil
import com.vaani.ui.folderList.FolderFragment
import com.vaani.ui.util.listUtil.AbstractListFragment
import com.vaani.ui.util.listUtil.Refresher
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
object MediasFragment : AbstractListFragment<MediaEntity>(listOf()) {
  override val generalMenu = R.menu.media_general_options
  override val selectedMenu = R.menu.media_selected_options
  override var subtitle = ""
  private lateinit var moveLauncher: ActivityResultLauncher<Uri?>
  private lateinit var copyLauncher: ActivityResultLauncher<Uri?>
  private lateinit var selectedFile: MediaEntity
  var currentFolder: FolderEntity = FolderEntity()
    set(value) {
      if (field != value) {
        field = value
        resetData(Files.getFolderMedias(currentFolder.id))
      }
      subtitle = "${value.items} items in ${value.name}"
    }

  override fun onItemClicked(position: Int) {
    PlayerUtil.play(displayList, position, currentFolder.id)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    //    copyLauncher =
    //      registerForActivityResult(ActivityResultContracts.OpenDocumentTree(), this::copyFile)
    //    moveLauncher =
    //      registerForActivityResult(ActivityResultContracts.OpenDocumentTree(), this::moveFile)
    object : Refresher(refreshLayout) {
      override fun onRefresh() {
        localScope.launch {
          val newFiles = Files.exploreFolder(currentFolder)
          resetData(newFiles)
          withContext(Dispatchers.Main) {
            listAdapter.notifyDataSetChanged()
            refreshFinish()
          }
        }
      }
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    MainActivity.optionsMenu = FolderFragment.generalMenu
    requireActivity().let {
      it.invalidateMenu()
      (it as AppCompatActivity).supportActionBar?.subtitle = FolderFragment.subtitle
    }
  }

  //  override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
  //    TODO
  //  }

  override fun fabAction(view: View) {
    if (
      PlayerUtil.controller?.isPlaying != true || PlayerData.currentCollection != currentFolder.id
    ) {
      val idx = displayList.indexOfFirst { it.id == currentFolder.lastPlayedId }
      if (idx != -1) {
        onItemClicked(idx)
      }
    } else {
      Log.d(TAG, "fabAction: already playing")
      PlayerUtil.startPlayerActivity()
    }
  }

  //  fun onOptions(position: Int, menu: Menu) {
  //    selectedFile = displayList[position]
  //    menu.findItem(R.id.file_list_option_add_fav).setOnMenuItemClickListener {
  //      val newFav = Files.addFavourite(selectedFile)
  //      FavouriteFragment.displayList.add(newFav)
  //      FavouriteFragment.listAdapter.notifyItemInserted(FavouriteFragment.displayList.size - 1)
  //      true
  //    }
  //    menu.findItem(R.id.file_list_option_copy).apply {
  //      setOnMenuItemClickListener {
  //        copyLauncher.launch(null)
  //        true
  //      }
  //    }
  //    menu.findItem(R.id.file_list_option_rename).apply {
  //      if (selectedFile.isUri) {
  //        isVisible = false
  //      } else {
  //        setOnMenuItemClickListener {
  //          renameFile()
  //          true
  //        }
  //      }
  //    }
  //    menu.findItem(R.id.file_list_option_move).apply {
  //      if (selectedFile.isUri) {
  //        isVisible = false
  //      } else {
  //        setOnMenuItemClickListener {
  //          moveLauncher.launch(Uri.fromFile(File(selectedFile.path)))
  //          true
  //        }
  //      }
  //    }
  //    menu.findItem(R.id.file_list_option_delete).apply {
  //      if (selectedFile.isUri) {
  //        isVisible = false
  //      } else {
  //        setOnMenuItemClickListener {
  //          deleteFile()
  //          true
  //        }
  //      }
  //    }
  //  }
  //
  //  private fun moveFile(uri: Uri?) {
  //    uri?.let {
  //      CoroutineScope(Job()).launch {
  //        val folder = Files.moveFile(selectedFile, it)
  //        FolderFragment.displayList.indexOf(folder).let {
  //          withContext(Dispatchers.Main) {
  //            if (it == -1) {
  //              FolderFragment.displayList.add(folder)
  //              FolderFragment.listAdapter.notifyItemInserted(FolderFragment.displayList.size - 1)
  //            } else {
  //              FolderFragment.listAdapter.notifyItemChanged(it)
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }
  //
  //  private fun copyFile(uri: Uri?) {
  //    uri?.let {
  //      CoroutineScope(Job()).launch {
  //        val folder = Files.copyFile(selectedFile, it)
  //        FolderFragment.displayList.indexOf(folder).let {
  //          withContext(Dispatchers.Main) {
  //            if (it == -1) {
  //              FolderFragment.displayList.add(folder)
  //              FolderFragment.listAdapter.notifyItemInserted(FolderFragment.displayList.size - 1)
  //            } else {
  //              FolderFragment.listAdapter.notifyItemChanged(it)
  //            }
  //          }
  //        }
  //      }
  //    }
  //  }
  //
  //  private fun renameFile() {
  //    val renameView = LayoutInflater.from(requireContext()).inflate(R.layout.rename_layout, null)
  //    val editTextView = renameView?.findViewById<EditText>(R.id.exit_text)
  //    val fileExtension =
  //      selectedFile.name.lastIndexOf('.').let {
  //        if (it == -1) "" else selectedFile.name.substring(it)
  //      }
  //    editTextView?.setText(selectedFile.name.removeSuffix(fileExtension))
  //    renameView?.findViewById<TextView>(R.id.extension_text)?.text = fileExtension
  //    AlertDialog.Builder(recyclerView.context)
  //      .setTitle("Rename file")
  //      .setView(renameView)
  //      .setPositiveButton(R.string.rename) { dialogInterface: DialogInterface, i: Int ->
  //        val newName = "${editTextView?.text}$fileExtension"
  //        CoroutineScope(Job()).launch {
  //          try {
  //            Files.rename(selectedFile, newName)
  //            withContext(Dispatchers.Main) {
  //              listAdapter.notifyItemChanged(displayList.indexOf(selectedFile))
  //            }
  //          } catch (e: Exception) {
  //            Log.e(TAG, "renameFile: error", e)
  //            Toast.makeText(requireContext(), "Unable to rename", Toast.LENGTH_SHORT).show()
  //          }
  //        }
  //        dialogInterface.dismiss()
  //      }
  //      .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
  //        dialogInterface.cancel()
  //      }
  //      .show()
  //  }
  //
  //  private fun deleteFile() {
  //    AlertDialog.Builder(recyclerView.context)
  //      .setTitle("Do you want to permanently delete the file ${selectedFile.name}?")
  //      .setPositiveButton(R.string.delete) { dialogInterface: DialogInterface, i: Int ->
  //        try {
  //          CoroutineScope(Job()).launch {
  //            Files.delete(selectedFile)
  //            withContext(Dispatchers.Main) {
  //              displayList.indexOf(selectedFile).let { idx ->
  //                displayList.removeAt(idx)
  //                listAdapter.notifyItemRemoved(idx)
  //              }
  //              FavouriteFragment.displayList
  //                .indexOfFirst { fav -> fav.fileId == selectedFile.id }
  //                .let { idx ->
  //                  if (idx != -1) {
  //                    FavouriteFragment.displayList.removeAt(idx)
  //                    FavouriteFragment.listAdapter.notifyItemRemoved(idx)
  //                  }
  //                }
  //              FolderFragment.displayList.indexOf(currentFolder).let { idx ->
  //                if (currentFolder.items == 0) {
  //                  FolderFragment.displayList.removeAt(idx)
  //                  FolderFragment.listAdapter.notifyItemRemoved(idx)
  //                } else {
  //                  FolderFragment.listAdapter.notifyItemChanged(idx)
  //                }
  //              }
  //            }
  //          }
  //        } catch (e: Exception) {
  //          Log.e(TAG, "deleteFile: error", e)
  //          Toast.makeText(requireContext(), "Unable to delete", Toast.LENGTH_SHORT).show()
  //        }
  //        dialogInterface.dismiss()
  //      }
  //      .setNegativeButton("Cancel") { dialogInterface: DialogInterface, i: Int ->
  //        dialogInterface.cancel()
  //      }
  //      .show()
  //  }
}
