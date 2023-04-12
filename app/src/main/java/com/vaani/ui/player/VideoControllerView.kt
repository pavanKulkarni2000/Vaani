package com.vaani.ui.player

import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
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
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.slider.Slider
import com.vaani.R
import com.vaani.player.Player
import com.vaani.util.PlayBackUtil
import com.vaani.util.TAG
import java.lang.ref.WeakReference

/**
 * Created by Bruce Too
 * On 7/12/16.
 * At 16:09
 */
class VideoControllerView(
    private val mContext: Activity,
    mSurfaceView: SurfaceView,
    private val mAnchorView: ViewGroup
) : FrameLayout(mContext), VideoGestureListener, LifecycleOwner {

    /**
     * if [VideoControllerView] is visible
     *
     * @return showing or not
     */
    var isShowing = false
        private set
    private var mIsDragging = false

    // seek views
    private lateinit var slider: Slider
    private lateinit var mEndTime: TextView
    private lateinit var mCurrentTime: TextView

    @DrawableRes
    private val mPauseIcon = R.drawable.media_controller__pause

    @DrawableRes
    private val mPlayIcon = R.drawable.media_controller__play_arrow

    @DrawableRes
    private val mShrinkIcon = R.drawable.mediacontroller_fullscreen_exit_40px

    @DrawableRes
    private val mStretchIcon = R.drawable.mediacontroller_fullscreen_40px

    @DrawableRes
    private val mShuffleIcon = R.drawable.media_controller__shuffle

    @DrawableRes
    private val mShuffleDisabledIcon = R.drawable.media_controller__shuffle_disabled

    @DrawableRes
    private val mLoopIcon = R.drawable.media_controller__loop

    @DrawableRes
    private val mLoopDisabledIcon = R.drawable.media_controller__loop_disabled

    //top layout
    private lateinit var mTopLayout: View
    private lateinit var titleTextView: TextView
    private lateinit var mBackButton: ImageButton

    //center layout
    private lateinit var mCenterLayout: View
    private lateinit var mCenterImage: ImageView
    private lateinit var mCenterProgress: LinearProgressIndicator
    private var mCurBrightness = -1f
    private lateinit var mAudioManager: AudioManager
    private var mCurVolume = -1
    private var mMaxVolume = 0

    //speed selector
    private lateinit var mSpeedSelectorLayout: View
    private lateinit var mSpeedSelector: Slider

    //bottom layout
    private lateinit var mBottomLayout: View
    private lateinit var mPauseButton: ImageButton
    private lateinit var mFullscreenButton: ImageButton
    private lateinit var mSpeedButton: TextView
    private lateinit var mLoopButton: ImageButton
    private lateinit var mShuffleButton: ImageButton

    private val mHandler: Handler = ControllerViewHandler(this)
    private lateinit var lifecycleReg: LifecycleRegistry
    private lateinit var gestureDetector: GestureDetector

    init {
        initControllerView()
        initGestureListener(mSurfaceView)
    }


    /**
     * find all views inside [VideoControllerView]
     * and init params
     */
    private fun initControllerView() {
        removeAllViews()
        val rootView = (mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater)
            .inflate(R.layout.media_controller, null)
        addView(rootView)
        initTopLayout(rootView)
        initCentralLayout(rootView)
        initSpeedSelectorLayout(rootView)
        initBottomLayout(rootView)
        initObservers()
    }

    private fun initObservers() {
        lifecycleReg = LifecycleRegistry(this)
        lifecycleReg.currentState = Lifecycle.State.CREATED
        Player.state.file.observe(this) {
            updateControls()
            show()
        }
    }

    private fun updateControls() {
        titleTextView.text = Player.state.file.value?.name ?: ""
        setPlayPauseIcon()
        setShuffleButton()
        setLoopButton()
        setSpeedButton()
        setStretchShrinkScreenIcon()
    }

    private fun initTopLayout(rootView: View) {
        mTopLayout = rootView.findViewById(R.id.layout_top)
        val closeButton = rootView.findViewById<ImageButton>(R.id.close_button)
        closeButton.requestFocus()
        closeButton.setOnClickListener {
            exit()
            Player.stop()
        }
        titleTextView = mTopLayout.findViewById(R.id.controller_title)
        titleTextView.text = Player.state.file.value?.name ?: ""

        mBackButton = rootView.findViewById(R.id.back_button)
        mBackButton.setOnClickListener {
            exit()
            Player.detachPlayerView()
        }
    }

    private fun initBottomLayout(rootView: View) {
        mBottomLayout = rootView.findViewById(R.id.layout_bottom)
        mPauseButton = rootView.findViewById(R.id.bottom_pause)
        setPlayPauseIcon()
        mPauseButton.setOnClickListener {
            Player.state.updatePlaying(!Player.state.isPlaying.value!!)
            setPlayPauseIcon()
        }
        mBottomLayout.findViewById<ImageButton>(R.id.next_button).setOnClickListener { Player.playNext() }
        mBottomLayout.findViewById<ImageButton>(R.id.previous_button).setOnClickListener { Player.playPrevious() }

        mFullscreenButton = rootView.findViewById(R.id.bottom_fullscreen)
        mFullscreenButton.requestFocus()
        mFullscreenButton.setOnClickListener {
            doToggleFullscreen()
            show()
        }

        slider = rootView.findViewById(R.id.bottom_seekbar)
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
        slider.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                val duration = Player.duration.toLong()
                val newPosition = duration * value / 1000L
                Player.seekTo(newPosition.toInt())
            }
        }
        slider.setLabelFormatter { value ->
            PlayBackUtil.stringToTime((Player.duration * value / 1000).toInt())
        }
        slider.valueTo = 0.0F
        slider.valueTo = 1000.0F

        mEndTime = rootView.findViewById(R.id.bottom_time)
        mCurrentTime = rootView.findViewById(R.id.bottom_time_current)

        mSpeedButton = rootView.findViewById(R.id.speed_button)
        setSpeedButton()
        mSpeedButton.setOnClickListener {
            mSpeedSelectorLayout.visibility = VISIBLE
            mSpeedSelector.value = Player.state.speed.value!!
            mSpeedSelector.requestFocus()
        }

        mLoopButton = rootView.findViewById(R.id.loop_button)
        setLoopButton()
        mLoopButton.setOnClickListener {
            Player.state.updateLoop(!Player.state.loop.value!!)
            setLoopButton()
        }

        mShuffleButton = rootView.findViewById(R.id.shuffle_button)
        setShuffleButton()
        mShuffleButton.setOnClickListener {
            Player.state.updateShuffle(!Player.state.shuffle.value!!)
            setShuffleButton()
        }
    }

    private fun initSpeedSelectorLayout(rootView: View) {
        mSpeedSelectorLayout = rootView.findViewById(R.id.layout_speed_selector)
        mSpeedSelectorLayout.visibility = View.GONE
        mSpeedSelector = mSpeedSelectorLayout.findViewById(R.id.speed_selector)
        mSpeedSelector.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) mSpeedSelectorLayout.visibility = View.GONE
        }
        mSpeedSelector.addOnChangeListener { _, value, fromUser ->
            if (fromUser) {
                var speed = value
                if (value < 0.5) {
                    speed = 0.5f
                    mSpeedSelector.value = speed
                }
                Player.state.updateSpeed(speed)
                setSpeedButton()
            }
        }
    }

    private fun initCentralLayout(rootView: View) {
        mCenterLayout = rootView.findViewById(R.id.layout_center)
        mCenterLayout.visibility = GONE
        mCenterImage = rootView.findViewById(R.id.image_center_bg)
        mCenterProgress = rootView.findViewById(R.id.progress_center)
    }

    private fun initGestureListener(surfaceView: SurfaceView) {
        mAudioManager = mContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        gestureDetector = GestureDetector(context, ViewGestureListener(mContext, this))
        surfaceView.setOnClickListener {
            toggleControllerView()
        }
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
                        .start {
                            isShowing = true
                            lifecycleReg.currentState = Lifecycle.State.STARTED
                            mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS)
                        }
                }
        }
        //update progress
        mHandler.sendEmptyMessage(HANDLER_UPDATE_PROGRESS)
    }


    private fun setLoopButton() {
        mLoopButton.setImageResource( if(Player.state.loop.value==true) mLoopIcon else mLoopDisabledIcon)
    }

    private fun setShuffleButton() {
        mShuffleButton.setImageResource( if(Player.state.shuffle.value==true) mShuffleIcon else mShuffleDisabledIcon)
    }

    private fun setSpeedButton() {
        mSpeedButton.text = Player.state.speed.value.toString()
    }

    private fun setPlayPauseIcon() {
        mPauseButton.setImageResource( if(Player.state.isPlaying.value==true) mPauseIcon else mPlayIcon)
    }

    /**
     * toggle [VideoControllerView] show or not
     * this can be called when [View.onTouchEvent] happened
     */
    private fun toggleControllerView() {
        if (!isShowing) {
            show()
        } else {
            //remove exist one first
            mHandler.removeMessages(HANDLER_ANIMATE_OUT)
            mHandler.sendEmptyMessageDelayed(HANDLER_ANIMATE_OUT, 100)
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
                lifecycleReg.currentState = Lifecycle.State.RESUMED
            }
    }

    private fun setSeekProgress(): Int {
        if (mIsDragging) {
            return 0
        }
        val position = Player.currentPosition
        val duration = Player.duration
        if (duration > 0) {
            slider.value = (1000 * position.coerceAtLeast(0) / duration).toFloat()
        }
        mEndTime.text = PlayBackUtil.stringToTime(duration)
        mCurrentTime.text = PlayBackUtil.stringToTime(position)
        return position
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_UP -> {
                mCurVolume = -1
                mCurBrightness = -1f
                mCenterLayout.visibility = GONE
                gestureDetector.onTouchEvent(event)
            }
            else -> gestureDetector.onTouchEvent(event)
        }
        return true
    }

    /**
     * set stretch screen or shrink screen icon
     */
    private fun setStretchShrinkScreenIcon() {
        if (mContext.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mFullscreenButton.setImageResource(mShrinkIcon)
        } else {
            mFullscreenButton.setImageResource(mStretchIcon)
        }
    }

    /**
     * toggle full screen listener invoke
     */
    private fun doToggleFullscreen() {
        if (mContext.requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            mContext.requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            mContext.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
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
        var pos = Player.currentPosition
        pos -= PROGRESS_SEEK.toInt()
        Player.seekTo(pos)
        setSeekProgress()
        show()
    }

    private fun seekForWard() {
        var pos = Player.currentPosition
        pos += PROGRESS_SEEK.toInt()
        Player.seekTo(pos)
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

    override fun onDoubleTap() {
        Player.state.updatePlaying(!Player.state.isPlaying.value!!)
        setPlayPauseIcon()
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
    private class ControllerViewHandler(view: VideoControllerView) : Handler(Looper.getMainLooper()) {
        private val mView: WeakReference<VideoControllerView>

        init {
            mView = WeakReference(view)
        }

        override fun handleMessage(message: Message) {
            val view = mView.get() ?: return
            val pos: Int
            when (message.what) {
                HANDLER_ANIMATE_OUT -> view.hide()
                HANDLER_UPDATE_PROGRESS -> {
                    pos = view.setSeekProgress()
                    if (!view.mIsDragging && view.isShowing && Player.state.isPlaying.value!!) { //just in case
                        //cycle update
                        sendEmptyMessageDelayed(HANDLER_UPDATE_PROGRESS, (1000 - pos % 1000).toLong())
                    }
                }
            }
        }
    }

    fun exit() {
        mHandler.removeMessages(HANDLER_ANIMATE_OUT)
        mHandler.removeMessages(HANDLER_UPDATE_PROGRESS)
    }

    companion object {
        private const val HANDLER_ANIMATE_OUT = 1 // out animate
        private const val HANDLER_UPDATE_PROGRESS = 2 //cycle update progress
        private const val PROGRESS_SEEK: Long = 500
        private const val ANIMATE_TIME: Long = 300
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleReg
    }
}