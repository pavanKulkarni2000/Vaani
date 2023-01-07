package com.vaani.ui.player

import android.app.Activity
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.annotation.DrawableRes
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import com.vaani.R
import com.vaani.ui.player.ViewAnimator.Listeners
import com.vaani.util.TAG
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created by Bruce Too
 * On 7/12/16.
 * At 16:09
 */
class VideoControllerView(
    private val mContext: Activity,
    mSurfaceView: SurfaceView,
    private val mAnchorView: ViewGroup,
    private val mMediaPlayerControlListener: MediaPlayerControlListener ) : FrameLayout(mContext), VideoGestureListener {

    /**
     * if [VideoControllerView] is visible
     *
     * @return showing or not
     */
    var isShowing = false
        private set
    private var mIsDragging  = false

    //init formatter
    private var mFormatBuilder: StringBuilder =  StringBuilder()
    private var mFormatter: Formatter = Formatter(mFormatBuilder, Locale.getDefault())

    // Root view
    private lateinit var mRootView: View

    // seek views
    private lateinit var slider: Slider
    private lateinit var mEndTime: TextView
    private lateinit var mCurrentTime: TextView

    // gestures
    private lateinit var mGestureDetector : GestureDetector

    @DrawableRes
    private val mPauseIcon = R.drawable.mediacontroller_pause_40px

    @DrawableRes
    private val mPlayIcon = R.drawable.mediacontroller_play_arrow_40px

    @DrawableRes
    private val mShrinkIcon = R.drawable.mediacontroller_fullscreen_exit_40px

    @DrawableRes
    private val mStretchIcon = R.drawable.mediacontroller_fullscreen_40px

    //top layout
    private lateinit var mTopLayout: View

    //center layout
    private lateinit var mCenterLayout: View
    private lateinit var mCenterImage: ImageView
    private lateinit var mCenterProgress: LinearProgressIndicator
    private var mCurBrightness = -1f
    private lateinit var mAudioManager: AudioManager
    private var mCurVolume = -1
    private var mMaxVolume = 0

    //bottom layout
    private lateinit var mBottomLayout: View
    private lateinit var mPauseButton: ImageButton
    private lateinit var mFullscreenButton: ImageButton
    private val mHandler: Handler = ControllerViewHandler(this)

    init {
        initControllerView()
        initGestureListener()
        mSurfaceView.setOnTouchListener { v: View?, event: MotionEvent? ->
            toggleControllerView()
            false
        }
    }


    /**
     * find all views inside [VideoControllerView]
     * and init params
     */
    private fun initControllerView() {
        removeAllViews()
        mRootView = (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.media_controller, null)
        addView(mRootView)
        //top layout
        mTopLayout = mRootView.findViewById(R.id.layout_top)
        val closeButton = mRootView.findViewById<ImageButton>(R.id.close_button)
        closeButton.requestFocus()
        closeButton.setOnClickListener{
            mMediaPlayerControlListener.exit()
        }
        //center layout
        mCenterLayout = mRootView.findViewById(R.id.layout_center)
        mCenterLayout.visibility = GONE
        mCenterImage = mRootView.findViewById(R.id.image_center_bg)
        mCenterProgress = mRootView.findViewById(R.id.progress_center)

        //bottom layout
        mBottomLayout = mRootView.findViewById(R.id.layout_bottom)
        mPauseButton = mRootView.findViewById(R.id.bottom_pause)
        mPauseButton.requestFocus()
        mPauseButton.setOnClickListener{
            doPauseResume()
            show()
        }

        mFullscreenButton = mRootView.findViewById(R.id.bottom_fullscreen)
        mFullscreenButton.requestFocus()
        mFullscreenButton.setOnClickListener {
            doToggleFullscreen()
            show()
        }

        slider = mRootView.findViewById(R.id.bottom_seekbar)
        slider.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {
                show()
                mIsDragging = true
                mHandler.removeMessages(HANDLER_UPDATE_PROGRESS)
            }

            override fun onStopTrackingTouch(slider: Slider) {
                mIsDragging = false
                setSeekProgress()
                setPlayPauseIcon()
                show()
                mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS)
            }
        })
        slider.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                val duration = mMediaPlayerControlListener.duration.toLong()
                val newPosition = duration * value / 1000L
                mMediaPlayerControlListener.seekTo(newPosition.toInt())
            }
        }
        slider.setLabelFormatter {
            value -> stringToTime((mMediaPlayerControlListener.duration * value / 1000).toInt())
        }
        slider.valueTo = 0.0F
        slider.valueTo = 1000.0F

        mEndTime = mRootView.findViewById(R.id.bottom_time)
        mCurrentTime = mRootView.findViewById(R.id.bottom_time_current)

    }

    /**
     * set gesture listen to control media player
     * include screen brightness and volume of video
     * and seek video play
     */
    private fun initGestureListener() {
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        mGestureDetector = GestureDetector(mContext, ViewGestureListener(mContext, this))
    }

    /**
     * show controller view
     */
    private fun show() {
        if (!isShowing) {

            //add controller view to bottom of the AnchorView
            val tlp = LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            mAnchorView.addView(this@VideoControllerView, tlp)
            ViewAnimator.putOn(mTopLayout)
                .waitForSize { viewAnimator ->
                    viewAnimator.animate()
                        .translationY(-mTopLayout.height.toFloat(), 0f)
                        .duration(ANIMATE_TIME)
                        .andAnimate(mBottomLayout)
                        .translationY(mBottomLayout.height.toFloat(), 0f)
                        .duration(ANIMATE_TIME)
                        .start(object : Listeners.Start {
                            override fun onStart() {
                                isShowing = true
                                mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS)
                            }
                        })
                }
        }
        setSeekProgress()
        mPauseButton.requestFocus()
        setPlayPauseIcon()
        setStretchShrinkScreenIcon()
        //update progress
        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS)
    }

    /**
     * toggle [VideoControllerView] show or not
     * this can be called when [View.onTouchEvent] happened
     */
    private fun toggleControllerView() {
        if (!isShowing) {
            show()
        } else {
            //animate out controller view
            val msg = mHandler.obtainMessage(HANDLER_ANIMATE_OUT)
            //remove exist one first
            mHandler.removeMessages(HANDLER_ANIMATE_OUT)
            mHandler.sendMessageDelayed(msg, 100)
        }
    }

    /**
     * hide controller view with animation
     * With custom animation
     */
    private fun hide() {
        ViewAnimator.putOn(mTopLayout)
            .animate()
            .translationY(-mTopLayout.height.toFloat())
            .duration(ANIMATE_TIME)
            .andAnimate(mBottomLayout)
            .translationY(mBottomLayout.height.toFloat())
            .duration(ANIMATE_TIME)
            .end {
                mAnchorView.removeView(this@VideoControllerView)
                mHandler.removeMessages(HANDLER_UPDATE_PROGRESS)
                isShowing = false
            }
    }

    /**
     * convert string to time
     *
     * @param timeMs time to be formatted
     * @return 00:00:00
     */
    private fun stringToTime(timeMs: Int): String {
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

    /**
     * set [.mSeekBar] progress
     * and video play time [.mCurrentTime]
     *
     * @return current play position
     */
    private fun setSeekProgress(): Int {
        if (mIsDragging) {
            return 0
        }
        val position = mMediaPlayerControlListener.currentPosition
        val duration = mMediaPlayerControlListener.duration
        if (duration > 0) {
            // use long to avoid overflow
            slider.value = (1000 * position / duration).toFloat()
        }
        mEndTime.text = stringToTime(duration)
        Log.e(TAG, "position:$position -> duration:$duration")
        mCurrentTime.text = stringToTime(position)
        if (mMediaPlayerControlListener.isComplete) {
            mCurrentTime.text = stringToTime(duration)
        }
        return position
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                mCurVolume = -1
                mCurBrightness = -1f
                mCenterLayout.visibility = GONE
                mGestureDetector.onTouchEvent(event)
            }
            else -> mGestureDetector.onTouchEvent(event)
        }
        return true
    }

    /**
     * set pause or play icon
     */
    private fun setPlayPauseIcon() {
        if (mMediaPlayerControlListener.isPlaying) {
            mPauseButton.setImageResource(mPauseIcon)
        } else {
            mPauseButton.setImageResource(mPlayIcon)
        }
    }

    /**
     * set stretch screen or shrink screen icon
     */
    private fun setStretchShrinkScreenIcon() {
        if (mMediaPlayerControlListener.isFullScreen) {
            mFullscreenButton.setImageResource(mShrinkIcon)
        } else {
            mFullscreenButton.setImageResource(mStretchIcon)
        }
    }

    /**
     * play or pause listener invoke
     */
    private fun doPauseResume() {
        if (mMediaPlayerControlListener.isPlaying) {
            mMediaPlayerControlListener.pause()
        } else {
            mMediaPlayerControlListener.start()
        }
        setPlayPauseIcon()
    }

    /**
     * toggle full screen listener invoke
     */
    private fun doToggleFullscreen() {
        mMediaPlayerControlListener.toggleFullScreen()
    }

    override fun setEnabled(enabled: Boolean) {
        mPauseButton.isEnabled = enabled
        slider.isEnabled = enabled
        super.setEnabled(enabled)
    }


    override fun onSingleTap() {
        toggleControllerView()
    }

    override fun onHorizontalScroll(seekForward: Boolean) {
        if (seekForward) { // seek forward
            seekForWard()
        } else {  //seek backward
            seekBackWard()
        }
    }

    private fun seekBackWard() {
        var pos = mMediaPlayerControlListener.currentPosition
        pos -= PROGRESS_SEEK.toInt()
        mMediaPlayerControlListener.seekTo(pos)
        setSeekProgress()
        show()
    }

    private fun seekForWard() {
        var pos = mMediaPlayerControlListener.currentPosition
        pos += PROGRESS_SEEK.toInt()
        mMediaPlayerControlListener.seekTo(pos)
        setSeekProgress()
        show()
    }

    override fun onVerticalScroll(percent: Float, direction: Int) {
        if (direction == ViewGestureListener.SWIPE_LEFT) {
            mCenterImage.setImageResource(R.drawable.mediacontroller_brightness_medium_48px)
            updateBrightness(percent)
        } else {
            mCenterImage.setImageResource(R.drawable.mediacontroller_volume_up_48px)
            updateVolume(percent)
        }
    }

    /**
     * update volume by seek percent
     *
     * @param percent seek percent
     */
    private fun updateVolume(percent: Float) {
        mCenterLayout.visibility = VISIBLE
        if (mCurVolume == -1) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (mCurVolume < 0) {
                mCurVolume = 0
            }
        }
        var volume = (percent * mMaxVolume).toInt() + mCurVolume
        if (volume > mMaxVolume) {
            volume = mMaxVolume
        }
        if (volume < 0) {
            volume = 0
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0)
        val progress = volume * 100 / mMaxVolume
        mCenterProgress.progress = progress
    }

    /**
     * update brightness by seek percent
     *
     * @param percent seek percent
     */
    private fun updateBrightness(percent: Float) {
        if (mCurBrightness == -1f) {
            mCurBrightness = mContext.window.attributes.screenBrightness
            if (mCurBrightness <= 0.01f) {
                mCurBrightness = 0.01f
            }
        }
        mCenterLayout.visibility = VISIBLE
        val attributes = mContext.window.attributes
        attributes.screenBrightness = mCurBrightness + percent
        if (attributes.screenBrightness >= 1.0f) {
            attributes.screenBrightness = 1.0f
        } else if (attributes.screenBrightness <= 0.01f) {
            attributes.screenBrightness = 0.01f
        }
        mContext.window.attributes = attributes
        val p = attributes.screenBrightness * 100
        mCenterProgress.progress = p.toInt()
    }



    /**
     * Handler prevent leak memory.
     */
    private class ControllerViewHandler(view: VideoControllerView) : Handler() {
        private val mView: WeakReference<VideoControllerView>

        init {
            mView = WeakReference(view)
        }

        override fun handleMessage(msg: Message) {
            var msg = msg
            val view = mView.get() ?: return
            val pos: Int
            when (msg.what) {
                HANDLER_ANIMATE_OUT -> view.hide()
                HANDLER_UPDATE_PROGRESS -> {
                    pos = view.setSeekProgress()
                    if (!view.mIsDragging && view.isShowing && view.mMediaPlayerControlListener.isPlaying) { //just in case
                        //cycle update
                        msg = obtainMessage(HANDLER_UPDATE_PROGRESS)
                        sendMessageDelayed(msg, (1000 - pos % 1000).toLong())
                    }
                }
            }
        }
    }

    /**
     * Interface of Media Controller View Which can be callBack
     * when [android.media.MediaPlayer] or some other media
     * players work
     */
    interface MediaPlayerControlListener {
        /**
         * start play video
         */
        fun start()

        /**
         * pause video
         */
        fun pause()

        /**
         * get video total time
         *
         * @return total time
         */
        val duration: Int

        /**
         * get video current position
         *
         * @return current position
         */
        val currentPosition: Int

        /**
         * seek video to exactly position
         *
         * @param position position
         */
        fun seekTo(position: Int)

        /**
         * video is playing state
         *
         * @return is video playing
         */
        val isPlaying: Boolean

        /**
         * video is complete
         * @return complete or not
         */
        val isComplete: Boolean

        /**
         * get buffer percent
         *
         * @return percent
         */
        val bufferPercentage: Int

        /**
         * video is full screen
         * in order to control image src...
         *
         * @return fullScreen
         */
        val isFullScreen: Boolean

        /**
         * toggle fullScreen
         */
        fun toggleFullScreen()

        /**
         * exit media player
         */
        fun exit()
    }

    companion object {
        private const val HANDLER_ANIMATE_OUT = 1 // out animate
        private const val HANDLER_UPDATE_PROGRESS = 2 //cycle update progress
        private const val PROGRESS_SEEK: Long = 500
        private const val ANIMATE_TIME: Long = 300
    }
}