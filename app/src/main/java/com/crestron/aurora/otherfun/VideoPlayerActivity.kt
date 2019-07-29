package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.mikepenz.fontawesome_typeface_library.FontAwesome
import com.mikepenz.iconics.IconicsDrawable
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.coroutines.Runnable
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.math.abs


class VideoPlayerActivity : AppCompatActivity() {

    companion object {

        /** Intent action for media controls from Picture-in-Picture mode.  */
        private val ACTION_MEDIA_CONTROL = "media_control"

        /** Intent extra for media controls from Picture-in-Picture mode.  */
        private val EXTRA_CONTROL_TYPE = "control_type"

        /** The request code for play action PendingIntent.  */
        private val REQUEST_PLAY = 1

        /** The request code for pause action PendingIntent.  */
        private val REQUEST_PAUSE = 2

        /** The request code for info action PendingIntent.  */
        private val REQUEST_INFO = 3

        /** The intent extra value for play action.  */
        private val CONTROL_TYPE_PLAY = 1

        /** The intent extra value for pause action.  */
        private val CONTROL_TYPE_PAUSE = 2

    }

    /** The arguments to be used for Picture-in-Picture mode.  */
    @SuppressLint("NewApi")
    private val mPictureInPictureParamsBuilder = PictureInPictureParams.Builder()

    private val mReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent?) {
            StringBuilder().apply {
                append("Action: ${intent?.action}\n")
                append("URI: ${intent?.toUri(Intent.URI_INTENT_SCHEME)}\n")
                toString().also { log ->
                    Loged.d(log)
                    //Toast.makeText(context, log, Toast.LENGTH_LONG).show()
                }
            }
            intent?.let { intent ->
                if (intent.action != ACTION_MEDIA_CONTROL) {
                    return
                }

                // This is where we are called back from Picture-in-Picture action items.
                val controlType = intent.getIntExtra(EXTRA_CONTROL_TYPE, 0)
                /*when (controlType) {
                    CONTROL_TYPE_PLAY -> mpw_video_player.playVideo()
                    CONTROL_TYPE_PAUSE -> mpw_video_player.pauseVideo()
                }*/
                when (controlType) {
                    CONTROL_TYPE_PLAY -> playerView.player!!.playWhenReady = true
                    CONTROL_TYPE_PAUSE -> playerView.player!!.playWhenReady = false
                }
            }
        }
    }

    private val labelPlay: String by lazy { "Play" }
    private val labelPause: String by lazy { "Pause" }

    @TargetApi(Build.VERSION_CODES.O)
    internal fun updatePictureInPictureActions(@DrawableRes iconId: Int, title: String,
                                               controlType: Int, requestCode: Int) {

        Loged.wtf("$title and $controlType")

        val actions = ArrayList<RemoteAction>()

        // This is the PendingIntent that is invoked when a user clicks on the action item.
        // You need to use distinct request codes for play and pause, or the PendingIntent won't
        // be properly updated.
        val intent = PendingIntent.getBroadcast(this@VideoPlayerActivity,
                requestCode, Intent(ACTION_MEDIA_CONTROL)
                .putExtra(EXTRA_CONTROL_TYPE, controlType), 0)
        val icon = Icon.createWithResource(this@VideoPlayerActivity, iconId)
        actions.add(RemoteAction(icon, title, title, intent))
        mPictureInPictureParamsBuilder.setActions(actions)
        // This is how you can update action items (or aspect ratio) for Picture-in-Picture mode.
        // Note this call can happen even when the app is not in PiP mode. In that case, the
        // arguments will be used for at the next call of #enterPictureInPictureMode.
        setPictureInPictureParams(mPictureInPictureParamsBuilder.build())
    }


    private val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

    private var currentVolume: Int = 0

    lateinit var path: String
    lateinit var name: String

    private var currentPos: Long = 0

    private lateinit var player: SimpleExoPlayer

    private var locked = false

    private lateinit var gesture: GestureDetector

    private var lockTimer = TimerStuff({
        //video_lock.animate().setDuration(500).alpha(0f)
        //video_back.animate().setDuration(500).alpha(0f)
        video_info_layout.animate().setDuration(500).alpha(0f)
    })

    private var mDownX: Float = 0.toFloat()
    private var mDownY: Float = 0.toFloat()
    private var mChangeLight: Boolean = false
    private var mChangeVolume: Boolean = false
    private var mGestureDownVolume: Int = 0
    private var mGestureDownBrightness: Int = 0

    private var mScreenWidth: Int = 0
    private var mScreenHeight: Int = 0

    private lateinit var mAudioManager: AudioManager

    @Suppress("PrivatePropertyName")
    private val THRESHOLD = 70
    @Suppress("PrivatePropertyName")
    private val SCREEN_WINDOW_FULLSCREEN = 2
    private var mCurrentScreen = SCREEN_WINDOW_FULLSCREEN

    private var mVolumeDialog: Dialog? = null
    private var mDialogVolumeProgressBar: ProgressBar? = null

    private var mBrightnessDialog: Dialog? = null
    private lateinit var mDialogBrightnessProgressBar: ProgressBar

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_video_player)
        window.decorView.systemUiVisibility = flags
        val decorView = window.decorView
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility.and(View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                decorView.systemUiVisibility = flags
            }
        }
        name = intent.getStringExtra("video_name")!!
        path = intent.getStringExtra("video_path")!!

        if(path.isEmpty()) {
            finish()
        }

        video_name.text = name

        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

        mScreenWidth = this.resources.displayMetrics.widthPixels
        mScreenHeight = this.resources.displayMetrics.heightPixels

        mAudioManager = audio

        player = ExoPlayerFactory.newSimpleInstance(this)

        playerView.player = player
        val dataSourceFactory = DefaultDataSourceFactory(this, Util.getUserAgent(this, "Fun"))
        val videoSource = ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(path.toUri())
        player.prepare(videoSource)
        playerView.controllerAutoShow = true
        //playerView.controllerHideOnTouch = true
        playerView.controllerShowTimeoutMs = 2000
        playerView.player!!.playWhenReady = true

        video_lock.setImageDrawable(IconicsDrawable(this).icon(FontAwesome.Icon.faw_unlock).sizeDp(24))
        video_lock.setOnClickListener {
            locked = !locked
            video_lock.setImageDrawable(IconicsDrawable(this).icon(if (locked) FontAwesome.Icon.faw_lock else FontAwesome.Icon.faw_unlock).sizeDp(24))
        }
        video_back.setOnClickListener {
            onBackPressed()
        }

        initVideoPlayer()

        //mpw_video_player.hideFullScreenButton = true
        //mpw_video_player.autoStartPlay(path, MxVideoPlayer.SCREEN_WINDOW_FULLSCREEN, name)

        val pos = defaultSharedPreferences.getLong(path, 0)
        Loged.wtf("$path at $pos")
        playerView.player!!.seekTo(pos)
        //mpw_video_player.seekToPosition(pos)

        /*mpw_video_player.playerListener = object : MxPlayerListener {
            override fun onComplete() {
                try {
                    this@VideoPlayerActivity.onBackPressed()
                    //finish()
                } catch (e: NullPointerException) {

                }
            }

            override fun onStarted() {
                updatePictureInPictureActions(android.R.drawable.ic_media_pause, labelPause,
                        CONTROL_TYPE_PAUSE, REQUEST_PAUSE)
            }

            override fun onStopped() {
                currentPos = mpw_video_player.currentPositionInVideo
                updatePictureInPictureActions(android.R.drawable.ic_media_play, labelPlay,
                        CONTROL_TYPE_PLAY, REQUEST_PLAY)
            }

            override fun onBackPress() {
               //currentPos = mpw_video_player.currentPositionInVideo
                finish()
            }

            override fun onPrepared() {
                mpw_video_player.seekToPosition(pos)
            }
        }*/

    }

    override fun onUserLeaveHint() {
        //super.onUserLeaveHint()
        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        minimize()
        //}
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal fun minimize() {
        // Hide the controls in picture-in-picture mode.
        //mpw_video_player.hideControls()
        // Calculate the aspect ratio of the PiP screen.
        //mPictureInPictureParamsBuilder.setAspectRatio(Rational(mpw_video_player.width, mpw_video_player.height))
        enterPictureInPictureMode(mPictureInPictureParamsBuilder.build())
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration?) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        if (isInPictureInPictureMode) {
            // Starts receiving events from action items in PiP mode.
            registerReceiver(mReceiver, IntentFilter(ACTION_MEDIA_CONTROL))
        } else {
            // We are out of PiP mode. We can stop receiving events from it.
            unregisterReceiver(mReceiver)
            // Show the video controls if the video is not playing
            //if (!mpw_video_player.isPlaying) {
            //.showControls()
            //}
        }
    }

    override fun onDestroy() {
        val position = currentPos
        Loged.wtf("$path at $position")
        defaultSharedPreferences.edit().putLong(path, position).apply()
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
        //MxVideoPlayer.backPress()
        //MxVideoPlayer.releaseAllVideos()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = flags
        } else {

        }
    }

    override fun onStop() {
        //mpw_video_player.pauseVideo()
        try {
            playerView.player!!.playWhenReady = false
            playerView.player.release()
        } catch(e: IllegalStateException) {

        }
        lockTimer.stopLock()
        super.onStop()
    }

    override fun onBackPressed() {
        //currentPos = mpw_video_player.currentPositionInVideo
        currentPos = playerView.player!!.currentPosition
        /*if (MxVideoPlayer.backPress()) {
            return
        }*/
        playerView.player!!.release()
        super.onBackPressed()
    }

    private fun initVideoPlayer() {
        gesture = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {

        })
        gesture.setOnDoubleTapListener(object : GestureDetector.OnDoubleTapListener {
            override fun onDoubleTap(p0: MotionEvent?): Boolean {
                val play = playerView.findViewById<ImageButton>(R.id.exo_play)
                val pause = playerView.findViewById<ImageButton>(R.id.exo_pause)
                if (play.visibility == View.GONE)
                    pause.performClick()
                else
                    play.performClick()
                return true
            }

            override fun onDoubleTapEvent(p0: MotionEvent?): Boolean {
                return false
            }

            override fun onSingleTapConfirmed(p0: MotionEvent?): Boolean {
                return false
            }

        })
        playerView.setOnTouchListener(onTouch)
    }

    private val onTouch = View.OnTouchListener { v, event ->
        lockTimer.restartLock()
        //Loged.v("$event")
        if (!locked) {
            gesture.onTouchEvent(event!!)
            val x = event.x
            val y = event.y
            val id = v.id
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    //Loged.i("onTouch: surfaceContainer actionDown [" + this.hashCode() + "] ")
                    mDownX = x
                    mDownY = y
                    mChangeLight = false
                    mChangeVolume = false
                }
                MotionEvent.ACTION_MOVE -> {
                    //Loged.i("onTouch: surfaceContainer actionMove [" + this.hashCode() + "] ")
                    val deltaX = x - mDownX
                    var deltaY = y - mDownY
                    val absDeltaX = abs(deltaX)
                    val absDeltaY = abs(deltaY)
                    if (mCurrentScreen == SCREEN_WINDOW_FULLSCREEN) {
                        if (!mChangeVolume && !mChangeLight) {
                            if (absDeltaX > THRESHOLD || absDeltaY > THRESHOLD) {
                                //cancelProgressTimer()
                                if (x <= playerView.videoSurfaceView.width / 2) {  // adjust the volume
                                    mChangeVolume = true
                                    mGestureDownVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                                } else {  // adjust the light
                                    mChangeLight = true
                                    mGestureDownBrightness = getScreenBrightness(this)
                                }

                            }
                        }
                    }
                    if (mChangeVolume) {
                        deltaY = -deltaY  // up is -, down is +
                        val maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                        val deltaV = (maxVolume.toFloat() * deltaY * 3f / mScreenHeight).toInt()
                        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, mGestureDownVolume + deltaV, 0)
                        val volumePercent = (mGestureDownVolume * 100 / maxVolume + deltaY * 3f * 100f / mScreenHeight).toInt()
                        showVolumeDialog(-deltaY, volumePercent)
                    }
                    if (mChangeLight) {
                        deltaY = -deltaY  // up is -, down is +
                        val deltaV = (255f * deltaY * 3f / mScreenHeight).toInt()
                        val brightnessValue = mGestureDownBrightness + deltaV
                        if (brightnessValue in 0..255) {
                            setWindowBrightness(this@VideoPlayerActivity, brightnessValue.toFloat())
                        }
                        val brightnessPercent = (mGestureDownBrightness + deltaY * 255f * 3f / mScreenHeight).toInt()
                        showBrightnessDialog(-deltaY, brightnessPercent)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    Loged.i("onTouch: surfaceContainer actionUp [" + this.hashCode() + "] ")
                    dismissVolumeDialog()
                    dismissBrightnessDialog()
                    if (mChangeVolume) {
                        //onActionEvent(MxUserAction.ON_TOUCH_SCREEN_SEEK_VOLUME)
                    }
                    if (mChangeLight) {
                        //onActionEvent(MxUserAction.ON_TOUCH_SCREEN_SEEK_BRIGHTNESS)
                    }
                    //startProgressTimer()
                }
                else -> {
                }
            }
        }

        playerView.useController = !locked

        video_info_layout.animate().setDuration(500).alpha(1f)
        //video_back.animate().setDuration(500).alpha(1f)
        lockTimer.startLock()

        false
    }

    private fun showVolumeDialog(v: Float, volumePercent: Int) {
        if (mVolumeDialog == null) {
            val localView = View.inflate(this, mxvideoplayer.app.com.xvideoplayer.R.layout.mx_mobile_volume_dialog, null)
            mDialogVolumeProgressBar = localView.findViewById<View>(mxvideoplayer.app.com.xvideoplayer.R.id.volume_progressbar) as ProgressBar
            mVolumeDialog = Dialog(this, mxvideoplayer.app.com.xvideoplayer.R.style.mx_style_dialog_progress)
            mVolumeDialog!!.setContentView(localView)
            if (mVolumeDialog!!.window != null) {
                mVolumeDialog!!.window!!.addFlags(8)
                mVolumeDialog!!.window!!.addFlags(32)
                mVolumeDialog!!.window!!.addFlags(16)
                mVolumeDialog!!.window!!.setLayout(-2, -2)
            }
            val params = mVolumeDialog!!.window!!.attributes
            params.gravity = 49
            params.y = resources
                    .getDimensionPixelOffset(mxvideoplayer.app.com.xvideoplayer.R.dimen.mx_volume_dialog_margin_top)
            params.width = resources
                    .getDimensionPixelOffset(mxvideoplayer.app.com.xvideoplayer.R.dimen.mx_mobile_dialog_width)
            mVolumeDialog!!.window!!.attributes = params
        }
        if (!mVolumeDialog!!.isShowing) {
            mVolumeDialog!!.show()
        }
        mDialogVolumeProgressBar!!.progress = volumePercent
    }

    private fun dismissVolumeDialog() {
        if (mVolumeDialog != null) {
            mVolumeDialog!!.dismiss()
        }
    }

    private fun showBrightnessDialog(v: Float, brightnessPercent: Int) {
        if (mBrightnessDialog == null) {
            val localView = View.inflate(this, mxvideoplayer.app.com.xvideoplayer.R.layout.mx_mobile_brightness_dialog, null)
            mDialogBrightnessProgressBar = localView.findViewById<View>(mxvideoplayer.app.com.xvideoplayer.R.id.brightness_progressbar) as ProgressBar
            mBrightnessDialog = Dialog(this, mxvideoplayer.app.com.xvideoplayer.R.style.mx_style_dialog_progress)
            mBrightnessDialog!!.setContentView(localView)
            if (mBrightnessDialog!!.window != null) {
                mBrightnessDialog!!.window!!.addFlags(8)
                mBrightnessDialog!!.window!!.addFlags(32)
                mBrightnessDialog!!.window!!.addFlags(16)
                mBrightnessDialog!!.window!!.setLayout(-2, -2)
            }
            val params = mBrightnessDialog!!.window!!.attributes
            params.gravity = 49
            params.y = resources
                    .getDimensionPixelOffset(mxvideoplayer.app.com.xvideoplayer.R.dimen.mx_volume_dialog_margin_top)
            params.width = resources
                    .getDimensionPixelOffset(mxvideoplayer.app.com.xvideoplayer.R.dimen.mx_mobile_dialog_width)
            mBrightnessDialog!!.window!!.attributes = params
        }
        if (!mBrightnessDialog!!.isShowing) {
            mBrightnessDialog!!.show()
        }
        mDialogBrightnessProgressBar.progress = brightnessPercent
    }

    private fun dismissBrightnessDialog() {
        if (mBrightnessDialog != null) {
            mBrightnessDialog!!.dismiss()
        }
    }

    private fun setWindowBrightness(activity: Activity, brightness: Float) {
        val lp = activity.window.attributes
        lp.screenBrightness = brightness / 255.0f
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1f
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = 0.1.toFloat()
        }
        activity.window.attributes = lp
    }

    private fun getScreenBrightness(activity: Activity): Int {
        var nowBrightnessValue = 0
        val resolver = activity.contentResolver
        try {
            nowBrightnessValue = Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return nowBrightnessValue
    }

    class TimerStuff(var action: () -> Unit, private val TIME_TO_WAIT: Long = 2000) {
        private var myRunnable: Runnable = Runnable {
            action()
        }

        private var myHandler = Handler()

        fun startLock() {
            myHandler.postDelayed(myRunnable, TIME_TO_WAIT.toLong())
        }

        fun stopLock() {
            myHandler.removeCallbacks(myRunnable)
        }

        fun restartLock() {
            myHandler.removeCallbacks(myRunnable)
            myHandler.postDelayed(myRunnable, TIME_TO_WAIT.toLong())
        }
    }

}