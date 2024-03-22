package com.vaani.ui.common

import android.os.Bundle
import android.service.controls.ControlsProviderService.TAG
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.util.UnstableApi
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.models.UiItem
import com.vaani.ui.MainActivity
import com.vaani.ui.medias.MediasFragment
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

@UnstableApi
abstract class GeneralListFragment<T : UiItem>(initialItems: List<T>) : MyListFragment<T>(R.layout.list_fragment, initialItems) {

  internal val localScope = CoroutineScope(Dispatchers.Default)
  internal lateinit var refreshLayout: SwipeRefreshLayout
  internal lateinit var fab: FloatingActionButton

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    fab = view.findViewById(R.id.play_fab)
    fab.setOnClickListener(::fabAction)
    refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
  }

  abstract fun fabAction(view: View?)

  override fun onPause() {
    super.onPause()
    MainActivity.menuGroupActiveMap[menuGroup] = false
  }

  override fun onResume() {
    super.onResume()
    MainActivity.menuGroupActiveMap[menuGroup] = true
    requireActivity().let {
      it.invalidateMenu()
      (it as AppCompatActivity).supportActionBar?.subtitle = subtitle
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    localScope.cancel(CancellationException("List View destroyed"))
  }

}
