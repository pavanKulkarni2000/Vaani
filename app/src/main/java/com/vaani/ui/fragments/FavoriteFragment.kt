package com.vaani.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.VaaniApplication
import com.vaani.model.Favorite
import com.vaani.model.Media
import com.vaani.player.PlayerData
import com.vaani.player.PlayerUtil
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import kotlinx.coroutines.launch

@UnstableApi
class FavoriteFragment : BaseFragment<Favorite>(R.layout.fragment_favorites) {

    private val viewModel: FavoriteViewModel by viewModels {
        val app = requireActivity().application as VaaniApplication
        FavoriteViewModelFactory(app.container.favoritesRepository, app.container.fileManager)
    }
    private lateinit var playerUtil: PlayerUtil

    private val touchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition

            val movedItem = displayList.removeAt(from)
            displayList.add(to, movedItem)
            adapter.notifyItemMoved(from, to)

            lifecycleScope.launch {
                val app = requireActivity().application as VaaniApplication
                app.container.favoritesRepository.move(from, to)
            }
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.absoluteAdapterPosition
            val favoriteToRemove = displayList[position]

            displayList.removeAt(position)
            adapter.notifyItemRemoved(position)

            lifecycleScope.launch {
                val app = requireActivity().application as VaaniApplication
                app.container.favoritesRepository.remove(favoriteToRemove)
                Toast.makeText(requireContext(), "Removed from Favorites", Toast.LENGTH_SHORT).show()
            }
        }
    })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerUtil = (requireActivity().application as VaaniApplication).container.playerUtil
        touchHelper.attachToRecyclerView(recyclerView)

        viewModel.favorites.observe(viewLifecycleOwner) {
            resetData(it)
            adapter.notifyDataSetChanged()
            stopRefreshLayout()
        }
    }

    override fun onItemClick(position: Int, view: View?) {
        lifecycleScope.launch {
            val medias = viewModel.getMedias(displayList)
            playerUtil.play(medias, position, FAVOURITE_COLLECTION_ID)
        }
    }

    override fun onItemLongClick(position: Int, view: View?): Boolean {
        // TODO
        return false
    }

    override fun fabAction(view: View?) {
        if (playerUtil.controller?.isPlaying == false || PlayerData.currentCollection != FAVOURITE_COLLECTION_ID) {
            val lastPlayedIndex = displayList.indexOfFirst { it.fileId == PreferenceUtil.lastPlayedFavouriteId }
            if (lastPlayedIndex != -1) {
                onItemClick(lastPlayedIndex, null)
            }
        } else {
            playerUtil.startPlayerActivity()
        }
    }

    companion object {
        fun newInstance() = FavoriteFragment()
    }
}
