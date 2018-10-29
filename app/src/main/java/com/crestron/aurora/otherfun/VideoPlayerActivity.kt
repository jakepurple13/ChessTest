package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.WindowManager
import com.crestron.aurora.R
import hb.xvideoplayer.MxPlayerListener
import hb.xvideoplayer.MxVideoPlayer
import kotlinx.android.synthetic.main.activity_video_player.*


class VideoPlayerActivity : AppCompatActivity() {

    private val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            or View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

    var currentVolume: Int = 0

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

        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        currentVolume = audio.getStreamVolume(AudioManager.STREAM_MUSIC)

        mpw_video_player.autoStartPlay(path, MxVideoPlayer.FULLSCREEN_ORIENTATION, name)
        mpw_video_player.startWindowFullscreen()
        //mpw_video_player.mFullscreenButton.isEnabled = false

        mpw_video_player.playerListener = object : MxPlayerListener {
            override fun onComplete() {
                this@VideoPlayerActivity.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        val audio = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        audio.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0)
        super.onDestroy()
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
        //videoView.releasePlayer()
        //player.release()
        MxVideoPlayer.releaseAllVideos()
    }

/*override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
    return if (keyCode == KeyEvent.KEYCODE_BACK) {
        videoView.onKeyDown(keyCode, event)
    } else super.onKeyDown(keyCode, event)
}*/

    override fun onBackPressed() {
        /*if (MxVideoPlayer.backPress()) {
            return
        }*/
        //videoView.releasePlayer()
        //player.release()
        if (MxVideoPlayer.backPress()) {
            return
        }
        super.onBackPressed()
    }


}
