package com.vaani.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.dal.Favorites
import com.vaani.dal.Files
import com.vaani.model.Favorite
import com.vaani.model.Media
import com.vaani.player.PlayerData
import com.vaani.player.PlayerUtil
import com.vaani.ui.util.Mover
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG

@UnstableApi
object FavoriteFragment : BaseFragment<Favorite>(R.layout.fragment_favorites) {

  override val data: List<Favorite>
    get() = Favorites.all

  private val touchHelper = ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN,ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    override fun onMove(
      recyclerView: RecyclerView,
      viewHolder: RecyclerView.ViewHolder,
      target: RecyclerView.ViewHolder
    ): Boolean {
      val from = viewHolder.absoluteAdapterPosition
      val to = target.absoluteAdapterPosition
      Favorites.move(displayList[from].rank, displayList[to].rank)
      val movedItem = displayList.removeAt(from)
      displayList.add(to, movedItem)
      adapter.notifyItemMoved(from, to)
      return true
    }
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        Favorites.remove(displayList[viewHolder.absoluteAdapterPosition])
        displayList.removeAt(viewHolder.absoluteAdapterPosition)
        adapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
      // add toast
      Toast.makeText(requireContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show()
    }
  })

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
//    mover =
//      object : Mover<Favorite>(displayList, adapter, recyclerView) {
//        override fun move(from: Int, to: Int) {
//          Favorites.move(displayList[from].rank, displayList[to].rank)
//        }
//
//        override fun remove(pos: Int) {
//          Favorites.remove(displayList[pos])
//        }
//      }
//    mover.enableMove()
//    mover.enableRemove()
    touchHelper.attachToRecyclerView(recyclerView)
    super.onViewCreated(view, savedInstanceState)
  }

  override fun onItemClick(position: Int, view: View?) {
    PlayerUtil.play(getDisplayMedias(), position, FAVOURITE_COLLECTION_ID)
  }

  override fun onItemLongClick(position: Int, view: View?): Boolean {
    // TODO
    return false
  }

  override fun fabAction(view: View?) {
    if (
      PlayerUtil.controller?.isPlaying == false ||
        PlayerData.currentCollection != FAVOURITE_COLLECTION_ID
    ) {
      val lastPlayedIndex =
        displayList.indexOfFirst { it.fileId == PreferenceUtil.lastPlayedFavouriteId }
      if (lastPlayedIndex == -1) {
        onItemClick(lastPlayedIndex, null)
      }
    } else {
      PlayerUtil.startPlayerActivity()
    }
  }

  private fun getDisplayMedias(): List<Media> {
    val medFiles = Files.getMedias(displayList.map(Favorite::fileId))
    Log.d(TAG, "getDisplayMedias: $displayList $medFiles")
    return medFiles
  }
}
