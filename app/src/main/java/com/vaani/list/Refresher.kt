package com.vaani.list

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout

abstract class Refresher(private val refreshLayout: SwipeRefreshLayout) :
  ListAction(true), SwipeRefreshLayout.OnRefreshListener {
  init {
    refreshLayout.setOnRefreshListener(this@Refresher)
  }

  abstract override fun onRefresh()

  fun refreshFinish() {
    refreshLayout.isRefreshing = false
  }
}
