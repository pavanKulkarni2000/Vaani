package com.vaani.util

import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.vaani.R
import com.vaani.data.PlayerState
import com.vaani.ui.player.PlayerFragment

object UiUtils {
    fun launchPlayerView(fragmentManager: FragmentManager){
        PlayerState.setAttached(true)
           fragmentManager.commit {
                add(R.id.fragment_container_view, PlayerFragment::class.java, null)
                addToBackStack(null)
        }
    }
}