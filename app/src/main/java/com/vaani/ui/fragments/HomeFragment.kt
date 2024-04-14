package com.vaani.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.vaani.R

@UnstableApi
class HomeFragment : Fragment(R.layout.fragment_home_nav) {

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    childFragmentManager.commit {
      setReorderingAllowed(true)
      add(R.id.home_fragment_fragment_container_view, FolderFragment)
      add(R.id.home_fragment_fragment_container_view, FavouriteFragment)
      hide(FavouriteFragment)
    }
    val bottomNav = view.findViewById<BottomNavigationView>(R.id.bottom_navigation)
    bottomNav.setOnItemSelectedListener { item ->
      when (item.itemId) {
        R.id.folders -> {
          childFragmentManager.commit {
            hide(FavouriteFragment)
            show(FolderFragment)
          }
          true
        }
        R.id.favorites -> {
          childFragmentManager.commit {
            hide(FolderFragment)
            show(FavouriteFragment)
          }
          true
        }
        else -> false
      }
    }
  }
}
