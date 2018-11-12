package com.crestron.aurora.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.crestron.aurora.R
import com.crestron.aurora.otherfun.FetchingUtils
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2core.Func2

class PlayPauseWidgetActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_pause_widget)
        val id = intent.getIntExtra("show_to_pause_or_play", -1)
        if (id == -1) {
            finish()
        } else {
            Fetch.getDefaultInstance().getDownload(id, Func2 {
                when (it?.status) {
                    Status.DOWNLOADING -> {
                        FetchingUtils.pause(it)
                    }
                    Status.PAUSED -> {
                        FetchingUtils.resume(it)
                    }
                    else -> {
                    }
                }
            })
            finish()
        }
    }
}
