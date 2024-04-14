package com.vaani.ui.fragments

import android.util.Log
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.vaani.files.Files
import com.vaani.model.Folder
import com.vaani.model.Media
import com.vaani.player.PlayerData
import com.vaani.player.PlayerUtil
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
object MediasFragment : BaseFragment<Media>() {

  var currentFolder: Folder = Folder(0, "", "", false, 0, 0, false)
    set(value) {
      if (field != value) {
        field = value
      }
    }

  override val data: List<Media>
    get() = Files.getFolderMedias(currentFolder.id)

  override fun onItemClick(position: Int, view: View?) {
    PlayerUtil.play(displayList, position, currentFolder.id)
  }

  override fun onItemLongClick(position: Int, view: View?): Boolean {
    // TODO
    return false
  }

  override fun onRefresh() {
    localScope.launch {
      Files.exploreFolder(currentFolder)
      resetData()
      withContext(Dispatchers.Main) {
        adapter.notifyDataSetChanged()
        stopRefreshLayout()
      }
    }
  }

  override fun fabAction(view: View?) {
    if (
      PlayerUtil.controller?.isPlaying != true || PlayerData.currentCollection != currentFolder.id
    ) {
      val idx = displayList.indexOfFirst { it.id == currentFolder.lastPlayedId }
      if (idx != -1) {
        onItemClick(idx, null)
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
  //      FavouriteFragment.adapter.notifyItemInserted(FavouriteFragment.displayList.size - 1)
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
  //              FolderFragment.adapter.notifyItemInserted(FolderFragment.displayList.size - 1)
  //            } else {
  //              FolderFragment.adapter.notifyItemChanged(it)
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
  //              FolderFragment.adapter.notifyItemInserted(FolderFragment.displayList.size - 1)
  //            } else {
  //              FolderFragment.adapter.notifyItemChanged(it)
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
  //              adapter.notifyItemChanged(displayList.indexOf(selectedFile))
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
  //                adapter.notifyItemRemoved(idx)
  //              }
  //              FavouriteFragment.displayList
  //                .indexOfFirst { fav -> fav.fileId == selectedFile.id }
  //                .let { idx ->
  //                  if (idx != -1) {
  //                    FavouriteFragment.displayList.removeAt(idx)
  //                    FavouriteFragment.adapter.notifyItemRemoved(idx)
  //                  }
  //                }
  //              FolderFragment.displayList.indexOf(currentFolder).let { idx ->
  //                if (currentFolder.items == 0) {
  //                  FolderFragment.displayList.removeAt(idx)
  //                  FolderFragment.adapter.notifyItemRemoved(idx)
  //                } else {
  //                  FolderFragment.adapter.notifyItemChanged(idx)
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
