package com.vaani.ui.fragments

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchView
import com.google.android.material.search.SearchView.TransitionState
import com.vaani.R
import com.vaani.dal.Files
import com.vaani.model.Folder
import com.vaani.player.PlayerUtil
import com.vaani.ui.adapter.ItemClickProvider
import com.vaani.ui.util.GlobalMediaSearcher
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@UnstableApi
object FolderFragment : BaseFragment<Folder>(R.layout.fragment_folders) {

  override val data: List<Folder>
    get() = Files.folders

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    val searchView: SearchView = view.findViewById(R.id.fragment_searchview)
    val searchContent: RecyclerView = view.findViewById(R.id.search_view_contents)
    GlobalMediaSearcher.setUp(searchView,searchContent)
  }

  override fun onRefresh() {
    localScope.launch {
      Files.exploreFolders()
      launch(Dispatchers.Main) {
        adapter.notifyDataSetChanged()
        stopRefreshLayout()
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
        onItemClick(idx, null)
      }
      MediasFragment.fabAction(view)
    } else {
      PlayerUtil.startPlayerActivity()
    }
  }

  override fun onItemClick(position: Int, view: View?) {
    MediasFragment.currentFolder = displayList[position]
    requireActivity().supportFragmentManager.commit {
      add(R.id.main_activity_fragment_container_view, MediasFragment)
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
  //              adapter.notifyItemChanged(displayList.indexOf(selectedFolder))
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
  //                adapter.notifyItemRemoved(idx)
  //              }
  //              FavoriteFragment.resetData(Files.favourites)
  //              FavoriteFragment.adapter.notifyDataSetChanged()
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
