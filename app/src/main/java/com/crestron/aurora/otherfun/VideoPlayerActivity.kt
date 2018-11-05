package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.drawable.Icon
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.annotation.DrawableRes
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.util.Rational
import android.view.View
import android.view.WindowManager
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import hb.xvideoplayer.MxPlayerListener
import hb.xvideoplayer.MxVideoPlayer
import kotlinx.android.synthetic.main.activity_video_player.*
import org.jetbrains.anko.defaultSharedPreferences


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
                when (controlType) {
                    CONTROL_TYPE_PLAY -> mpw_video_player.playVideo()
                    CONTROL_TYPE_PAUSE -> mpw_video_player.pauseVideo()
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

    var currentVolume: Int = 0

    lateinit var path: String
    lateinit var name: String

    var currentPos: Long = 0

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
        name = intent.getStringExtra("video_name")
        path = intent.getStringExtra("video_path")

        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

        mpw_video_player.hideFullScreenButton = true
        mpw_video_player.autoStartPlay(path, MxVideoPlayer.SCREEN_WINDOW_FULLSCREEN, name)

        val pos = defaultSharedPreferences.getLong(path, 0)
        Loged.wtf("$path at $pos")

        mpw_video_player.playerListener = object : MxPlayerListener {
            override fun onComplete() {
                try {
                    this@VideoPlayerActivity.onBackPressed()
                    //finish()
                } catch (e: NullPointerException) {

                }
            }

            override fun onStarted() {
                mpw_video_player.seekToPosition(pos)
                updatePictureInPictureActions(R.drawable.ic_media_pause_dark, labelPause,
                        CONTROL_TYPE_PAUSE, REQUEST_PAUSE)
            }

            override fun onStopped() {
                currentPos = mpw_video_player.currentPositionInVideo
                updatePictureInPictureActions(R.drawable.ic_media_play_dark, labelPlay,
                        CONTROL_TYPE_PLAY, REQUEST_PLAY)
            }

            override fun onBackPress() {
                //currentPos = mpw_video_player.currentPositionInVideo
                finish()
            }
        }

    }

    override fun onUserLeaveHint() {
        //super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            minimize()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    internal fun minimize() {
        // Hide the controls in picture-in-picture mode.
        mpw_video_player.hideControls()
        // Calculate the aspect ratio of the PiP screen.
        mPictureInPictureParamsBuilder.setAspectRatio(Rational(mpw_video_player.width, mpw_video_player.height))
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
            if (!mpw_video_player.isPlaying) {
                //.showControls()
            }
        }
    }

    override fun onDestroy() {
        val position = currentPos
        Loged.wtf("$path at $position")
        defaultSharedPreferences.edit().putLong(path, position).apply()
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
        MxVideoPlayer.backPress()
        MxVideoPlayer.releaseAllVideos()
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
        mpw_video_player.pauseVideo()
        super.onStop()
    }

    override fun onBackPressed() {
        currentPos = mpw_video_player.currentPositionInVideo
        if (MxVideoPlayer.backPress()) {
            return
        }
        super.onBackPressed()
    }


}
