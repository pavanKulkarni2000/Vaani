package com.vaani.ui.home

import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vaani.ui.favourites.FavouriteFragment
import com.vaani.ui.folderList.FolderFragment

@UnstableApi
class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

  private val tabs = arrayOf<Fragment>(FolderFragment, FavouriteFragment)

  override fun getItemCount(): Int = tabs.size

  override fun createFragment(position: Int): Fragment {
    return tabs[position]
  }
}
