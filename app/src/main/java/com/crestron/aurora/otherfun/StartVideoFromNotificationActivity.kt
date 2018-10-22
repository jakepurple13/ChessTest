package com.crestron.aurora.otherfun

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crestron.aurora.R
import org.jetbrains.anko.defaultSharedPreferences

class StartVideoFromNotificationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_video_from_notification)

        val path = intent.getStringExtra("video_path")
        val name = intent.getStringExtra("video_name")

        if (defaultSharedPreferences.getBoolean("videoPlayer", false)) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(path))
            intent.setDataAndType(Uri.parse(path), "video/mp4")
            startActivity(intent)
        } else {
            startActivity(Intent(this, VideoPlayerActivity::class.java).apply {
                putExtra("video_path", path)
                putExtra("video_name", name)
            })
        }
    }
}
