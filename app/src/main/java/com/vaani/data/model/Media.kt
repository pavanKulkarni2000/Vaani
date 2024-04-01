package com.vaani.data.model

import com.vaani.R
import com.vaani.ui.util.UiUtil

data class Media(
    override val id: Long,
    override val name: String,
    val path: String,
    val isUri: Boolean,
    val isAudio: Boolean,
    val duration: Long,
    val folderId: Long,
    var playBackProgress: Float,
    override var selected: Boolean = false,
) : UiItem {
    override val subTitle: String
        get() = UiUtil.stringToTime(duration)
    override val image: Int
        get() =
            if(selected)
                R.drawable.check_circle_40px
            else
                when (isAudio) {
                    true -> R.drawable.music_note_40px
                    false -> R.drawable.movie_40px
                }
    override val rank: Int
        get() = 0
}