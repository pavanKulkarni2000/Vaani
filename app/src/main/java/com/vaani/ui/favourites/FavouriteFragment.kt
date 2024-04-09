package com.vaani.ui.favourites

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.vaani.files.Files
import com.vaani.model.Favourite
import com.vaani.model.Media
import com.vaani.player.PlayerData
import com.vaani.player.PlayerUtil
import com.vaani.ui.common.MyBaseListFragment
import com.vaani.ui.util.Mover
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG

@UnstableApi
object FavouriteFragment : MyBaseListFragment<Favourite>() {

  override val data: List<Favourite>
    get() = Files.favourites

  private lateinit var mover: Mover<Favourite>

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mover =
      object : Mover<Favourite>(displayList, listAdapter, recyclerView) {
        override fun move(from: Int, to: Int) {
          Files.moveFavourite(displayList[from].rank, displayList[to].rank)
        }

        override fun remove(pos: Int) {
          Files.remove(displayList[pos])
        }
      }
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
    val medFiles = Files.getFiles(displayList.map(Favourite::fileId))
    Log.d(TAG, "getDisplayMedias: $displayList $medFiles")
    return medFiles
  }
}
