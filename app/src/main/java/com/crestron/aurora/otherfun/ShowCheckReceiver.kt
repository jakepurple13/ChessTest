package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
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
import com.crestron.aurora.utilities.KUtility
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*

class ShowCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val nextTime = (System.currentTimeMillis() + (1000 * 60 * 60 * KUtility.currentUpdateTime).toLong())
        KUtility.nextCheckTime = nextTime
        val i = Intent(context, ShowCheckIntentService::class.java)
        i.putExtra("received", true)
        context!!.startService(i)
    }
}

open class ShowInfos(val name: String, val size: Int, val time: String, val url: String) {
    override fun toString(): String {
        return "$time - $name Updated: Episode $size"
    }
}

open class ShowInfosList(var list: ArrayList<ShowInfos>)

open class NameWithUrl(val name: String, val url: String)

class ShowCheckIntentService : IntentService("ShowCheckIntentService") {

    companion object {
        val updateNotiMap = arrayListOf<String>()
        val updateNotiList = arrayListOf<ShowInfos>()
    }

    @SuppressLint("SimpleDateFormat")
    override fun onHandleIntent(intent: Intent?) {

        val rec = intent!!.getBooleanExtra("received", false)
        val check = if (rec)
            KUtility.canShowUpdateCheck(this)
        else
            true
        if (check) {
            sendRunningNotification(this@ShowCheckIntentService,
                    android.R.mipmap.sym_def_app_icon,
                    "updateCheckRun", 2)
            //if(wifiOnly()) {
            //val mNotificationManager = this@ShowCheckService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //mNotificationManager.activeNotifications.filter { it.id == 1 }[0].notification.
            GlobalScope.launch {
                val showDatabase = ShowDatabase.getDatabase(this@ShowCheckIntentService)
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
                            val timeOfUpdate = SimpleDateFormat("hh:mm a").format(System.currentTimeMillis())
                            //nStyle.addLine("$timeOfUpdate - ${i.name} Updated: Episode $showList")
                            //val infoToShow = "$timeOfUpdate - ${i.name} Updated: Episode $showList"
                            //updateNotiMap.add(infoToShow)
                            updateNotiList.add(ShowInfos(i.name, showList, timeOfUpdate, i.url))
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
                //updateNotiMap.addAll(KUtility.getNotifyList())
                updateNotiList.addAll(KUtility.getNotiJsonList().list)
                //val list = updateNotiMap.distinctBy { it }
                val list = updateNotiList.distinctBy { it.url }
                if (list.isNotEmpty()) {
                    defaultSharedPreferences.edit().putInt(ConstantValues.UPDATE_COUNT,
                            defaultSharedPreferences.getInt(ConstantValues.UPDATE_COUNT, 0) + count).apply()
                    val nStyle = NotificationCompat.InboxStyle()
                    for (i in list) {
                        nStyle.addLine(i.name)
                    }
                    //updateNotiMap.clear()
                    //updateNotiMap.addAll(list)
                    updateNotiList.clear()
                    updateNotiList.addAll(list)
                    //KUtility.commitNotiList(updateNotiMap.toMutableSet())
                    KUtility.commitNotiJsonList(ShowInfosList(updateNotiList))
                    if (defaultSharedPreferences.getBoolean("useNotifications", true)) {
                        for ((j, i) in list.withIndex()) {
                            sendNotification(this@ShowCheckIntentService,
                                    android.R.mipmap.sym_def_app_icon,
                                    i.name,
                                    i.toString(),
                                    "episodeUpdate",
                                    EpisodeActivity::class.java,
                                    j + 3,
                                    NameWithUrl(i.name, i.url))
                        }
                        sendGroupNotification(this@ShowCheckIntentService,
                                android.R.mipmap.sym_def_app_icon,
                                "${list.size} show${if (list.size == 1) "" else "s"} had updates!",
                                nStyle,
                                "episodeUpdate",
                                ShowListActivity::class.java)

                    }
                }
                sendFinishedCheckingNotification(this@ShowCheckIntentService,
                        android.R.mipmap.sym_def_app_icon,
                        "updateCheckRun", 2)
            }
        }
    }

    private fun sendRunningNotification(context: Context, smallIconId: Int, channel_id: String, notification_id: Int) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, "updateCheckRun")
                .setSmallIcon(smallIconId)
                .setContentTitle("Checking for Show Updates")
                .setChannelId(channel_id)
                .setProgress(0, 100, true)
                .setOngoing(true)
                .setVibrate(longArrayOf(0L))
                .setSubText("Checking for Show Updates")
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    private fun sendFinishedCheckingNotification(context: Context, smallIconId: Int, channel_id: String, notification_id: Int) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, "updateCheckRun")
                .setSmallIcon(smallIconId)
                .setContentTitle("Finished Checking")
                .setChannelId(channel_id)
                .setVibrate(longArrayOf(0L))
                .setSubText("Finished Checking")
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setTimeoutAfter(500)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    private fun sendNotification(context: Context, smallIconId: Int, title: String, text: String, channel_id: String, gotoActivity: Class<*>, notification_id: Int, nameUrl: NameWithUrl) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, "episodeUpdate")
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true)
                .setChannelId(channel_id)
                .setGroup("episode_group")
                .setAutoCancel(true)
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)
        resultIntent.putExtra(ConstantValues.URL_INTENT, nameUrl.url)
        resultIntent.putExtra(ConstantValues.NAME_INTENT, nameUrl.name)

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
        //mBuilder.setDeleteIntent(createOnDismissedIntent(context, notification_id))
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    private fun sendGroupNotification(context: Context, smallIconId: Int, title: String, messages: NotificationCompat.Style = NotificationCompat.InboxStyle(), channel_id: String, gotoActivity: Class<*>) {

        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, "episodeUpdate")
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setStyle(messages)
                .setChannelId(channel_id)
                .setOnlyAlertOnce(true)
                .setGroupSummary(true)
                .setGroup("episode_group")
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
        //mBuilder.setDeleteIntent(createOnDismissedIntent(context, notification_id))
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(0, mBuilder.build())
    }

    private fun createOnDismissedIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, NotificationDismissedReceiver::class.java)
        //intent.putExtra("com.my.app.notificationId", notificationId)
        return PendingIntent.getBroadcast(context.applicationContext,
                notificationId, intent, 0)
    }

}

class NotificationDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //val notificationId = intent.extras!!.getInt("com.my.app.notificationId")
        /* Your code to handle the event here */
        //ShowCheckIntentService.updateNotiMap.clear()
    }
}