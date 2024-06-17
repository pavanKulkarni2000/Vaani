package com.vaani.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.media3.common.util.UnstableApi
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

  private lateinit var mover: Mover<Favorite>

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mover =
      object : Mover<Favorite>(displayList, adapter, recyclerView) {
        override fun move(from: Int, to: Int) {
          Favorites.move(displayList[from].rank, displayList[to].rank)
        }

        override fun remove(pos: Int) {
          Favorites.remove(displayList[pos])
        }
      }
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
