package com.vaani.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.vaani.ui.favouriteList.FavouriteMediaListFragment
import com.vaani.ui.folderList.FolderListFragment

class HomePagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    private val tabConstructors = arrayOf(::FolderListFragment, ::FavouriteMediaListFragment)

    override fun getItemCount(): Int = tabConstructors.size

    override fun createFragment(position: Int): Fragment {
        return tabConstructors[position]()
    }

}