package com.vaani.ui.favourites

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FavouriteEntity
import com.vaani.models.MediaEntity
import com.vaani.player.PlayerUtil
import com.vaani.ui.common.MyGeneralListFragment
import com.vaani.list.Mover
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.TAG

@UnstableApi
object FavouriteFragment : MyGeneralListFragment<FavouriteEntity>(Files.favourites,) {

  private lateinit var mover: Mover<FavouriteEntity>
  override val menuGroup = R.id.favourites_group
  override var subtitle = "fragment"

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    mover =
      object : Mover<FavouriteEntity>(displayList, listAdapter, recyclerView) {
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
        displayList.indexOfFirst { it.fileId == Files.favouriteFolder.lastPlayedId }
      if (lastPlayedIndex == -1) {
        onItemClick(lastPlayedIndex, null)
      }
    } else {
      PlayerUtil.startPlayerActivity()
    }
  }

  private fun getDisplayMedias(): List<MediaEntity> {
    val medFiles = Files.getFiles(displayList.map(FavouriteEntity::fileId))
    Log.d(TAG, "getDisplayMedias: $displayList $medFiles")
    return medFiles
  }
}
