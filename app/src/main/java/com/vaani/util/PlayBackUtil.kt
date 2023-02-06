package com.vaani.util

import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import com.vaani.MainActivity
import com.vaani.models.File
import java.util.*

object PlayBackUtil {

    //init formatter
    private var mFormatBuilder: StringBuilder = StringBuilder()
    private var mFormatter: Formatter = Formatter(mFormatBuilder, Locale.getDefault())
    /**
     * convert string to time
     *
     * @param timeMs time to be formatted
     * @return 00:00:00
     */
     fun stringToTime(timeMs: Int): String {
        val totalSeconds = timeMs / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        mFormatBuilder.setLength(0)
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }
}