package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import com.crestron.aurora.R
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.jarvanmo.exoplayerview.media.SimpleMediaSource
import com.jarvanmo.exoplayerview.ui.ExoVideoPlaybackControlView
import kotlinx.android.synthetic.main.activity_video_player.*
import kotlinx.android.synthetic.main.custom_player_view.view.*


class VideoPlayerActivity : AppCompatActivity() {

    private val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

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
        val name = intent.getStringExtra("video_name")
        val path = intent.getStringExtra("video_path")
        val mediaSource = SimpleMediaSource(path)//uri also supported
        mediaSource.setDisplayName(name)
        videoView.play(mediaSource, true)
        videoView.isPortrait = false
        videoView.setFastForwardIncrementMs(1000)
        videoView.setRewindIncrementMs(1000)
        videoView.controllerAutoShow = true
        videoView.controllerHideOnTouch = true
        videoView.controllerShowTimeoutMs = 2500
        videoView.setShowMultiWindowTimeBar(true)
        val view = layoutInflater.inflate(R.layout.custom_player_view, null, false)
        view.skip_forward.apply {
            setOnClickListener {
                try {
                    videoView.player.seekTo(videoView.player.currentPosition + 90000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            text = "1:30 >>"
        }
        view.half_for.apply {
            setOnClickListener {
                try {
                    videoView.player.seekTo(videoView.player.currentPosition + 15000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            text = "15s >"
        }
        view.half_back.apply {
            setOnClickListener {
                try {
                    videoView.player.seekTo(videoView.player.currentPosition - 15000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            text = "< 15s"
        }
        view.skip_backward.apply {
            setOnClickListener {
                try {
                    videoView.player.seekTo(videoView.player.currentPosition - 90000)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            text = "<< 1:30"
        }
        videoView.addCustomView(ExoVideoPlaybackControlView.CUSTOM_VIEW_TOP_LANDSCAPE, view)
        videoView.setBackListener { _, _ ->
            this@VideoPlayerActivity.onBackPressed()
            true
        }
        videoView.player.addListener(object : Player.EventListener {
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onSeekProcessed() {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPlayerError(error: ExoPlaybackException?) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPositionDiscontinuity(reason: Int) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {
                //To change body of created functions use File | Settings | File Templates.
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                //To change body of created functions use File | Settings | File Templates.
                when (playbackState) {
                    Player.STATE_ENDED -> this@VideoPlayerActivity.onBackPressed()
                }
            }

        })
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = flags
        }
    }

    override fun onPause() {
        super.onPause()
        //MxVideoPlayer.releaseAllVideos()
        videoView.releasePlayer()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            videoView.onKeyDown(keyCode, event)
        } else super.onKeyDown(keyCode, event)
    }

    override fun onBackPressed() {
        /*if (MxVideoPlayer.backPress()) {
            return
        }*/
        videoView.releasePlayer()
        super.onBackPressed()
    }

}
