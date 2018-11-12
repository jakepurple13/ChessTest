package com.crestron.aurora.views

import android.app.IntentService
import android.content.Intent
import com.crestron.aurora.otherfun.FetchingUtils
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2core.Func2

class PlayPauseWidgetService : IntentService("PlayPauseWidgetService") {

    override fun onHandleIntent(intent: Intent?) {
        val id = intent?.getIntExtra("show_to_pause_or_play", -1)
        if (id == -1 || id == null) {
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
        }
    }
}
