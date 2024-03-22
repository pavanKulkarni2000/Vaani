package com.vaani.ui.folders

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FolderEntity
import com.vaani.ui.MainActivity
import com.vaani.ui.common.GeneralListFragment
import com.vaani.ui.common.MyAdapter
import com.vaani.list.Selector
import com.vaani.ui.common.SelectionListFragment
import com.vaani.ui.common.UiItemViewHolder

@UnstableApi
object FolderSelectionFragment : SelectionListFragment<FolderEntity>(Files.folders,) {

  override val menuGroup: Int = R.menu.fol_selected_options
  override var subtitle = "folder"
  override fun onItemClick(position: Int, view: View?) {
    TODO("Not yet implemented")
  }


  override fun onDestroy() {
    super.onDestroy()
    MainActivity.menuGroupActiveMap[FolderFragment.menuGroup] = true
    requireActivity().let {
      it.invalidateMenu()
      (it as AppCompatActivity).supportActionBar?.subtitle = FolderFragment.subtitle
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
