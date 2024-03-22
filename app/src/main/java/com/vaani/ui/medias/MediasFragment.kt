package com.vaani.ui.medias

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import com.vaani.ui.MainActivity
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FolderEntity
import com.vaani.models.MediaEntity
import com.vaani.player.PlayerUtil
import com.vaani.ui.folders.FolderFragment
import com.vaani.ui.common.MyAdapter
import com.vaani.ui.common.GeneralListFragment
import com.vaani.list.Refresher
import com.vaani.ui.common.UiItemViewHolder
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@UnstableApi
object MediasFragment : GeneralListFragment<MediaEntity>(listOf(),) {
  override val menuGroup = R.menu.media_general_options
  override var subtitle = ""
  var currentFolder: FolderEntity = FolderEntity()
    set(value) {
      if (field != value) {
        field = value
        resetData(Files.getFolderMedias(currentFolder.id))
      }
      subtitle = "${value.items} items in ${value.name}"
    }

   override fun onItemClick(position: Int,view: View?) {
    PlayerUtil.play(displayList, position, currentFolder.id)
  }

  override fun onItemLongClick(position: Int, view: View?): Boolean {
    // TODO
    return false
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
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
    MainActivity.menuGroupActiveMap[FolderFragment.menuGroup]=true
    requireActivity().let {
      it.invalidateMenu()
      (it as AppCompatActivity).supportActionBar?.subtitle = FolderFragment.subtitle
    }
  }

   override fun fabAction(view: View?) {
    if (
      PlayerUtil.controller?.isPlaying != true || PlayerData.currentCollection != currentFolder.id
    ) {
      val idx = displayList.indexOfFirst { it.id == currentFolder.lastPlayedId }
      if (idx != -1) {
        onItemClick(idx,null)
      }
    } else {
      Log.d(TAG, "fabAction: already playing")
      PlayerUtil.startPlayerActivity()
    }
  }
}
