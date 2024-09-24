package com.vaani.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.dal.Favorites
import com.vaani.dal.Files
import com.vaani.dal.Medias
import com.vaani.model.Folder
import com.vaani.model.Media
import com.vaani.player.PlayerData
import com.vaani.player.PlayerUtil
import com.vaani.ui.util.Selector
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
class MediasFragment(private val currentFolder: Folder, val startLastPlayed: Boolean) :
  BaseFragment<Media>(R.layout.fragment_medias) {
    private val selectorListener : Selector.OnSelectionChangedListener =object : Selector.OnSelectionChangedListener {
      override fun selectingChanged(selecting: Boolean) {
        if (selecting) {
          //start action mode overlapping on toolbar
          actionMode = toolbar.startActionMode(callback)
        } else {
          actionMode?.finish()
        }
      }

      override fun selectionChanged(count: Int) {
        actionMode?.title = "$count selected"
      }
    }
  private val selector:Selector<Media> = Selector(displayList,selectorListener)
  private var actionMode: ActionMode? = null

  override val data: List<Media>
    get() = Medias.getFolderMedias(currentFolder.id)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    selector.unselectAll()
    toolbar.setTitle(currentFolder.name)
    toolbar.setSubtitle(currentFolder.subTitle)
    toolbar.setNavigationOnClickListener {
      requireActivity().supportFragmentManager.popBackStackImmediate()
    }
    if (startLastPlayed) {
      playLastPlayed()
    }
  }

  override fun onItemClick(position: Int, view: View?) {
    if (selector.selecting) {
      selector.flipSelectionAt(position)
    } else {
      PlayerUtil.play(displayList, position, currentFolder.id)
    }
  }

  override fun onItemLongClick(position: Int, view: View?): Boolean {
    if (!selector.selecting) {
      selector.selectAt(position)
      adapter.notifyItemChanged(position)
      return true
    }
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
      playLastPlayed()
    } else {
      Log.d(TAG, "fabAction: already playing")
      PlayerUtil.startPlayerActivity()
    }
  }

  private fun playLastPlayed() {
    val idx = displayList.indexOfFirst { it.id == currentFolder.lastPlayedId }
    if (idx != -1) {
      onItemClick(idx, null)
    }
  }

  private val callback =
    object : ActionMode.Callback {

      override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        requireActivity().menuInflater.inflate(R.menu.medias_action_mode_menu, menu)
        return true
      }

      override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
        toolbar.visibility = View.GONE
        return false
      }

      override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
         when (item?.itemId) {
          R.id.medias_action_mode_add_fav -> {
            Favorites.addFavorites(selector.selection)
            FavoriteFragment.resetData()
            Toast.makeText(requireContext(), "Added to favorites", Toast.LENGTH_SHORT).show()
          }
          else -> return false
        }
        mode?.finish()
        return true
      }

      override fun onDestroyActionMode(mode: ActionMode?) {
        selector.unselectAll()
        adapter.notifyDataSetChanged()
        actionMode = null
        toolbar.visibility = View.VISIBLE
      }
    }

  override fun onDestroy() {
    super.onDestroy()
    if (selector.selecting) {
      selector.unselectAll()
    }
  }

  //  fun onOptions(position: Int, menu: Menu) {
  //    selectedFile = displayList[position]
  //    menu.findItem(R.id.file_list_option_add_fav).setOnMenuItemClickListener {
  //      val newFav = Files.addFavourite(selectedFile)
  //      FavoriteFragment.displayList.add(newFav)
  //      FavoriteFragment.adapter.notifyItemInserted(FavoriteFragment.displayList.size - 1)
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
  //              FavoriteFragment.displayList
  //                .indexOfFirst { fav -> fav.fileId == selectedFile.id }
  //                .let { idx ->
  //                  if (idx != -1) {
  //                    FavoriteFragment.displayList.removeAt(idx)
  //                    FavoriteFragment.adapter.notifyItemRemoved(idx)
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
