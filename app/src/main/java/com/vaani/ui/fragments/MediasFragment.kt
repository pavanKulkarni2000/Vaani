package com.vaani.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.google.gson.Gson
import com.vaani.R
import com.vaani.VaaniApplication
import com.vaani.model.Folder
import com.vaani.model.Media
import com.vaani.player.PlayerData
import com.vaani.player.PlayerUtil
import com.vaani.ui.util.Selector
import com.vaani.util.TAG
import kotlinx.coroutines.launch

@UnstableApi
class MediasFragment : BaseFragment<Media>(R.layout.fragment_medias) {

    private val viewModel: MediasViewModel by viewModels {
        val container = (requireActivity().application as VaaniApplication).container
        MediasViewModelFactory(container.mediaManager, container.fileManager)
    }
    private lateinit var currentFolder: Folder
    private var startLastPlayed: Boolean = false
    private lateinit var playerUtil: PlayerUtil

    private val selectorListener: Selector.OnSelectionChangedListener = object : Selector.OnSelectionChangedListener {
        override fun selectingChanged(selecting: Boolean) {
            if (selecting) {
                actionMode = toolbar.startActionMode(callback)
            } else {
                actionMode?.finish()
            }
        }

        override fun selectionChanged(count: Int) {
            actionMode?.title = "$count selected"
        }
    }
    private val selector: Selector<Media> = Selector(displayList, selectorListener)
    private var actionMode: ActionMode? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currentFolder = Gson().fromJson(it.getString(ARG_FOLDER), Folder::class.java)
            startLastPlayed = it.getBoolean(ARG_START_LAST_PLAYED)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerUtil = (requireActivity().application as VaaniApplication).container.playerUtil
        selector.unselectAll()
        toolbar.title = currentFolder.name
        toolbar.subtitle = currentFolder.subTitle
        toolbar.setNavigationOnClickListener {
            requireActivity().supportFragmentManager.popBackStackImmediate()
        }

        viewModel.medias.observe(viewLifecycleOwner) {
            resetData(it)
            adapter.notifyDataSetChanged()
            if (startLastPlayed) {
                playLastPlayed()
                startLastPlayed = false // To avoid re-playing on config change
            }
            stopRefreshLayout()
        }

        viewModel.loadMedias(currentFolder)
    }

    override fun onItemClick(position: Int, view: View?) {
        if (selector.selecting) {
            selector.flipSelectionAt(position)
            adapter.notifyItemChanged(position)
        } else {
            lifecycleScope.launch {
                playerUtil.play(displayList, position, currentFolder.id)
            }
        }
    }

    override fun onItemLongClick(position: Int, view: View?): Boolean {
        if (!selector.selecting) {
            selector.selectAt(position)
            adapter.notifyItemChanged(position)
            return true
        }
        return false
    }

    override fun onRefresh() {
        viewModel.refreshMedias(currentFolder)
    }

    override fun fabAction(view: View?) {
        if (playerUtil.controller?.isPlaying != true || PlayerData.currentCollection != currentFolder.id) {
            playLastPlayed()
        } else {
            Log.d(TAG, "fabAction: already playing")
            playerUtil.startPlayerActivity()
        }
    }

    private fun playLastPlayed() {
        val idx = displayList.indexOfFirst { it.id == currentFolder.lastPlayedId }
        if (idx != -1) {
            onItemClick(idx, null)
        }
    }

    private val callback =
        object : ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                requireActivity().menuInflater.inflate(R.menu.medias_action_mode_menu, menu)
                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                toolbar.visibility = View.GONE
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.medias_action_mode_add_fav -> {
                        // Create an immutable copy of the selection to pass to the coroutine
                        val selectedIdsCopy = selector.selection.toList()
                        lifecycleScope.launch {
                            val app = requireActivity().application as VaaniApplication
                            val itemsAddedCount = app.container.favoritesRepository.addFavorites(selectedIdsCopy)
                            val message = if (itemsAddedCount > 0) {
                                resources.getQuantityString(R.plurals.favorites_added, itemsAddedCount, itemsAddedCount)
                            } else {
                                getString(R.string.already_in_favorites)
                            }
                            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> return false
                }
                mode.finish()
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                selector.unselectAll()
                adapter.notifyDataSetChanged()
                actionMode = null
                toolbar.visibility = View.VISIBLE
            }
        }

    companion object {
        private const val ARG_FOLDER = "folder"
        private const val ARG_START_LAST_PLAYED = "start_last_played"

        fun newInstance(folder: Folder, startLastPlayed: Boolean): MediasFragment {
            val fragment = MediasFragment()
            val args = Bundle()
            args.putString(ARG_FOLDER, Gson().toJson(folder))
            args.putBoolean(ARG_START_LAST_PLAYED, startLastPlayed)
            fragment.arguments = args
            return fragment
        }
    }
}
