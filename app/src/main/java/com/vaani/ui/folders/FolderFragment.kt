package com.vaani.ui.folders

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FolderEntity
import com.vaani.player.PlayerUtil
import com.vaani.ui.common.GeneralListFragment
import com.vaani.list.Refresher
import com.vaani.ui.medias.MediasFragment
import com.vaani.ui.common.MyAdapter
import com.vaani.ui.common.UiItemViewHolder
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@UnstableApi
object FolderFragment : GeneralListFragment<FolderEntity>(Files.folders) {

  override val menuGroup: Int = R.id.folders_group
  override var subtitle = "folder"

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    object : Refresher(refreshLayout) {
      override fun onRefresh() {
        localScope.launch {
          Files.exploreFolders()
          launch(Dispatchers.Main) {
            listAdapter.notifyDataSetChanged()
            refreshFinish()
          }
        }
      }
    }
  }

  override fun onItemLongClick(position: Int, view: View?): Boolean {
    // TODO
    return false
  }


  override fun fabAction(view: View?) {
    if (PlayerUtil.controller?.isPlaying != true) {
      val idx = displayList.indexOfFirst { it.id == PreferenceUtil.lastPlayedFolderId }
      if (idx != -1) {
        onItemClick(idx,null)
      }
      MediasFragment.fabAction(view)
    } else {
      PlayerUtil.startPlayerActivity()
    }
  }

  override fun onItemClick(position: Int, view: View?) {
    MediasFragment.currentFolder = displayList[position]
    requireActivity().supportFragmentManager.commit {
      add(R.id.fragment_container_view, MediasFragment)
      addToBackStack(MediasFragment.TAG)
    }
  }

//  override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
//    return false
//  }

  //  fun onOptions(position: Int, menu: Menu) {
  //    selectedFolder = displayList[position]
  //    menu.findItem(R.id.folder_option_rename).apply {
  //      if (selectedFolder.isUri) {
  //        isVisible = false
  //      } else {
  //        setOnMenuItemClickListener {
  //          renameFolder()
  //          true
  //        }
  //      }
  //    }
  //    menu.findItem(R.id.folder_option_delete).apply {
  //      setOnMenuItemClickListener {
  //        deleteFolder()
  //        true
  //      }
  //    }
  //  }
  //
  //  private fun renameFolder() {
  //    val renameView = LayoutInflater.from(requireContext()).inflate(R.layout.rename_layout, null)
  //    val editTextView = renameView?.findViewById<EditText>(R.id.exit_text)
  //    editTextView?.setText(selectedFolder.name)
  //    renameView?.findViewById<TextView>(R.id.extension_text)?.visibility = View.GONE
  //    AlertDialog.Builder(recyclerView.context)
  //      .setTitle("Rename folder")
  //      .setView(renameView)
  //      .setPositiveButton(R.string.rename) { dialogInterface: DialogInterface, i: Int ->
  //        CoroutineScope(Job()).launch {
  //          try {
  //            Files.rename(selectedFolder, editTextView?.text.toString())
  //            withContext(Dispatchers.Main) {
  //              listAdapter.notifyItemChanged(displayList.indexOf(selectedFolder))
  //            }
  //          } catch (e: Exception) {
  //            Log.e(TAG, "rename folder: error", e)
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
  //  private fun deleteFolder() {
  //    AlertDialog.Builder(recyclerView.context)
  //      .setTitle("Do you want to permanently delete the file ${selectedFolder.name}?")
  //      .setPositiveButton(R.string.delete) { dialogInterface: DialogInterface, i: Int ->
  //        try {
  //          CoroutineScope(Job()).launch {
  //            Files.delete(selectedFolder)
  //            withContext(Dispatchers.Main) {
  //              displayList.indexOf(selectedFolder).let { idx ->
  //                displayList.removeAt(idx)
  //                listAdapter.notifyItemRemoved(idx)
  //              }
  //              FavouriteFragment.resetData(Files.favourites)
  //              FavouriteFragment.listAdapter.notifyDataSetChanged()
  //            }
  //          }
  //        } catch (e: Exception) {
  //          Log.e(TAG, "deleteFolder: error", e)
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
