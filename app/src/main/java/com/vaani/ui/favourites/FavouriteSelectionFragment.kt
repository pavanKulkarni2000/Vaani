package com.vaani.ui.favourites

import android.os.Bundle
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FavouriteEntity
import com.vaani.ui.common.GeneralListFragment
import com.vaani.list.Selector
import com.vaani.ui.common.SelectionListFragment

@UnstableApi
object FavouriteSelectionFragment : SelectionListFragment<FavouriteEntity>(Files.favourites,) {

  override val menuGroup = R.menu.fav_selected_options
  override var subtitle = "fragment"
  override fun onItemClick(position: Int, view: View?) {
    TODO("Not yet implemented")
  }

}
