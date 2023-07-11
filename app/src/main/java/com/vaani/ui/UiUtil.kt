package com.vaani.ui

import java.util.*
import kotlin.random.Random

object UiUtil {

    //init formatter
    private var mFormatBuilder: StringBuilder = StringBuilder()
    private var mFormatter: Formatter = Formatter(mFormatBuilder, Locale.getDefault())
    var random = Random(System.currentTimeMillis())
        private set

    /**
     * convert string to time
     *
     * @param timeMs time to be formatted
     * @return 00:00:00
     */
    fun stringToTime(timeMs: Long): String {
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