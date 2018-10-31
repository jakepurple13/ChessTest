package com.crestron.aurora.otherfun

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Context
import android.content.Intent
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.Source
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.net.SocketTimeoutException


class ShowCheckService : JobService() {

    companion object {
        var started = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Loged.i("Starting")
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onStopJob(p0: JobParameters?): Boolean {
        Loged.e("Stopped")
        return false
    }

    override fun onStartJob(p0: JobParameters?): Boolean {
        Loged.d("Starting!")
        if (started) {
            started = false
            //if(wifiOnly()) {
            val nStyle = NotificationCompat.InboxStyle()
            //val mNotificationManager = this@ShowCheckService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //mNotificationManager.activeNotifications.filter { it.id == 1 }[0].notification.
            val showDatabase = ShowDatabase.getDatabase(this@ShowCheckService)
            GlobalScope.launch {
                var count = 0
                val showApi = ShowApi(Source.RECENT_ANIME).showInfoList
                showApi.addAll(ShowApi(Source.RECENT_CARTOON).showInfoList)
                val filteredList = showApi.distinctBy { it.url }
                val shows = showDatabase.showDao().allShows

                val aColIds = shows.asSequence().map { it.link }.toSet()
                val bColIds = filteredList.filter { it.url in aColIds }

                for (i in bColIds) {
                    try {
                        val showList = EpisodeApi(i).episodeList.size
                        if (showDatabase.showDao().getShow(i.name).showNum < showList) {
                            nStyle.addLine("${i.name} Updated: Episode $showList")
                            val show = showDatabase.showDao().getShow(i.name)
                            show.showNum = showList
                            showDatabase.showDao().updateShow(show)
                            count++
                        }
                        Loged.wtf("${i.name} and size is $showList")
                    } catch (e: SocketTimeoutException) {
                        continue
                    }
                }
                if (count > 0) {
                    defaultSharedPreferences.edit().putInt(ConstantValues.UPDATE_COUNT,
                            defaultSharedPreferences.getInt(ConstantValues.UPDATE_COUNT, 0) + count).apply()

                    sendNotification(this@ShowCheckService,
                            android.R.mipmap.sym_def_app_icon,
                            "$count show${if (count == 1) "" else "s"} had updates!",
                            nStyle,
                            "episodeUpdate",
                            ShowListActivity::class.java,
                            1)
                }
                started = true
                jobFinished(p0, true)
            }
        } else {
            jobFinished(p0, true)
        }
        //}
        return true
    }

    /*
    private fun wifiOnly(): Boolean {
        return if (defaultSharedPreferences.getBoolean(ConstantValues.WIFI_ONLY, false))
            isWifiConnected()
        else
            true
    }

    private fun isWifiConnected(): Boolean {
        val cm = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return cm.activeNetworkInfo != null && cm.activeNetworkInfo.type == 1
    }*/


    private fun sendNotification(context: Context, smallIconId: Int, title: String, messages: NotificationCompat.Style = NotificationCompat.InboxStyle(), channel_id: String, gotoActivity: Class<*>, notification_id: Int) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, "episodeUpdate")
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setStyle(messages)
                .setChannelId(channel_id)
                .setAutoCancel(true)
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)
        resultIntent.putExtra(ConstantValues.RECENT_OR_NOT, true)
        resultIntent.putExtra(ConstantValues.SHOW_LINK, Source.RECENT_ANIME.link)

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(gotoActivity)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

}