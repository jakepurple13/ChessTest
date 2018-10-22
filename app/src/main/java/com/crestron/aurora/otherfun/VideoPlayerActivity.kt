package com.crestron.aurora.otherfun

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.WindowManager
import com.crestron.aurora.R
import hb.xvideoplayer.MxVideoPlayer
import kotlinx.android.synthetic.main.activity_video_player.*


class VideoPlayerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_video_player)
        val name = intent.getStringExtra("video_name")
        val path = intent.getStringExtra("video_path")
        mpw_video_player.autoStartPlay(path, MxVideoPlayer.SCREEN_WINDOW_FULLSCREEN, name)
    }

    override fun onPause() {
        super.onPause()
        MxVideoPlayer.releaseAllVideos()
    }

    override fun onBackPressed() {
        /*if (MxVideoPlayer.backPress()) {
            return
        }*/
        super.onBackPressed()
    }
}
