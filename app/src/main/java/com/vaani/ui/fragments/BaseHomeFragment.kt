package com.vaani.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.google.android.material.search.SearchBar
import com.vaani.R
import com.vaani.model.UiItem

@UnstableApi
abstract class BaseHomeFragment<T : UiItem> : BaseFragment<T>(){

  override val fragmentRes: Int = R.layout.fragment_home
  lateinit var searchBar: SearchBar

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    searchBar = view.findViewById(R.id.home_fragment_search_bar)
  }

}
