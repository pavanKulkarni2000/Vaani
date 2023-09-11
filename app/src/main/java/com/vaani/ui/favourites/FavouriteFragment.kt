package com.vaani.ui.favourites

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FavouriteEntity
import com.vaani.models.SortOrder
import com.vaani.player.PlayerUtil
import com.vaani.ui.UiUtil
import com.vaani.ui.listUtil.AbstractListAdapter
import com.vaani.ui.listUtil.AbstractListFragment
import com.vaani.ui.listUtil.ListItemCallbacks
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID

@UnstableApi
object FavouriteFragment : AbstractListFragment<FavouriteEntity>(), ListItemCallbacks {

    override val listAdapter: AbstractListAdapter = object : AbstractListAdapter(
        this, R.layout.file_item, R.menu.fav_list_option_menu
    ) {
        override fun setViewData(view: View, position: Int) {
            displayList[position].let { item ->
                view.findViewById<TextView>(R.id.file_text).text = item.name
                view.findViewById<TextView>(R.id.file_subtext).text = UiUtil.stringToTime(item.duration)
                view.findViewById<ImageView>(R.id.file_image).setImageResource(
                    when (item.isAudio) {
                        true -> R.drawable.music_note_40px
                        false -> R.drawable.movie_40px
                    }
                )
            }
        }

        override fun getItemCount(): Int = displayList.size
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sortOrder = SortOrder.RANK
        resetData(Files.favourites)
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)
    }

    override fun fabAction(view: View) {
        if (PlayerUtil.controller?.isPlaying == false || PlayerData.currentCollection != FAVOURITE_COLLECTION_ID) {
            PlayerUtil.playLastPlayed(Files.favouriteFolder)
        } else {
            PlayerUtil.startPlayerActivity()
        }
    }

    override fun search(string: String?) {
        resetData(Files.favourites)
        if (string != null && string.isNotBlank()) {
            displayList.retainAll { it.name.lowercase().contains(string.lowercase()) }
        }
    }


    override fun sort() {
        when (sortOrder) {
            SortOrder.ASC -> displayList.sortBy { it.name.lowercase() }
            SortOrder.DSC -> {
                displayList.sortBy { it.name.lowercase() }
                displayList.reverse()
            }
            SortOrder.RANK -> displayList.sortBy(FavouriteEntity::rank)
        }
    }


    override fun onClick(position: Int) =
        PlayerUtil.play(Files.getFile(displayList[position].fileId), FAVOURITE_COLLECTION_ID)


    override fun onOptions(position: Int, menu: Menu) {
        menu.findItem(R.id.fav_list_option_del_fav).setOnMenuItemClickListener {
            Files.remove(displayList[position])
            displayList.removeAt(position)
            listAdapter.notifyItemRemoved(position)
            true
        }
    }

    private val touchHelper = object : ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP or ItemTouchHelper.DOWN,
        ItemTouchHelper.END
    ) {
        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            Files.moveFavourite(displayList[from].rank, displayList[to].rank)
            listAdapter.notifyItemMoved(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (direction == ItemTouchHelper.END) {
                Files.remove(displayList[viewHolder.absoluteAdapterPosition])
                displayList.removeAt(viewHolder.absoluteAdapterPosition)
                listAdapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
            }
        }
    }

    override fun refreshAction() {
        resetData(Files.favourites)
        refreshFinish()
    }
}