@file:Suppress("SameParameterValue")

package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.showapi.Source
import com.crestron.aurora.utilities.KUtility
import com.crestron.aurora.utilities.intersect
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat

class ShowCheckReceiver : BroadcastReceiver() {
    @SuppressLint("SimpleDateFormat")
    override fun onReceive(context: Context?, intent: Intent?) {
        val nextTime = (System.currentTimeMillis() + KUtility.currentDurationTime)
        KUtility.nextCheckTime = nextTime
        Loged.d(SimpleDateFormat("MM/dd/yyyy E hh:mm:ss a").format(KUtility.nextCheckTime))
        val i = Intent(context, ShowCheckIntentService::class.java)
        i.putExtra("received", true)
        try {
            context!!.startService(i)
        } catch (e: IllegalStateException) {

        }
    }
}

class BootUpReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action == "android.intent.action.BOOT_COMPLETED") {
            Loged.i("Setting from boot")
            // Set the alarm here.
            //if (KUtility.getSharedPref(context!!).getBoolean("run_update_check", true))
            KUtility.setAlarmUp(context!!)
        }
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
                //gets the list from the source
                val showApi = ShowApi(Source.RECENT_ANIME).showInfoList as ArrayList<ShowInfo>
                showApi.addAll(ShowApi(Source.RECENT_CARTOON).showInfoList)
                //this part filters the two lists with the list in the database
                val filteredList = showApi.distinctBy { it.url }
                val shows = showDatabase.showDao().allShows
                //in order to only search for the shows that are in the database
                //val aColIds = shows.asSequence().map { it.link }.toSet()
                //val bColIds = filteredList.filter { it.url in aColIds }

                val filtered = filteredList.intersect(shows) { one, two ->
                    one.url == two.link
                }

                for ((prog, i) in filtered.withIndex()) {
                    //for ((prog, i) in bColIds.withIndex()) {
                    sendRunningNotification(this@ShowCheckIntentService,
                            android.R.mipmap.sym_def_app_icon,
                            "updateCheckRun", 2, prog + 1, filtered.size, i.name)
                    try {
                        Loged.i("Checking ${i.name}")
                        val showList = EpisodeApi(i).episodeList.size
                        //if (showDatabase.showDao().getShow(i.name).showNum < showList) {
                        val show = showDatabase.showDao().getShowByURL(i.url)
                        if (show.showNum < showList) {
                            Loged.i("Checking ${i.name} with size ${show.showNum}")
                            val timeOfUpdate = SimpleDateFormat("MM/dd hh:mm a").format(System.currentTimeMillis())
                            val infoToShow = "$timeOfUpdate - ${i.name} Updated: Episode $showList"
                            Loged.wtf(infoToShow)
                            updateNotiList.add(ShowInfos(i.name, showList, timeOfUpdate, i.url))
                            show.showNum = showList
                            showDatabase.showDao().updateShow(show)
                            count++
                        }
                        Loged.wtf("${i.name} and size is $showList")
                    } catch (e: SocketTimeoutException) {
                        Loged.i("${i.name} Took too long")
                        continue
                    }
                }
                //updateNotiMap.addAll(KUtility.getNotifyList())
                updateNotiList.addAll(KUtility.getNotiJsonList().list)
                //updateNotiList.add(ShowInfos("This is the name", 4, "01:30 AM", "http://www.animetoon.org/watch-archer"))
                //updateNotiList.add(ShowInfos("Soccer One", 4, "02:30 AM", "http://www.animeplus.tv/captain-tsubasa-2018-online"))
                //updateNotiList.add(ShowInfos("Cute One With a really really long name", 4, "03:30 AM", "http://www.animeplus.tv/jingai-san-no-yome-episode-9-online"))
                //val list = updateNotiMap.distinctBy { it }
                val list = updateNotiList.distinctBy { it.url }
                if (list.isNotEmpty()) {
                    defaultSharedPreferences.edit().putInt(ConstantValues.UPDATE_COUNT,
                            defaultSharedPreferences.getInt(ConstantValues.UPDATE_COUNT, 0) + count).apply()
                    //updateNotiMap.clear()
                    //updateNotiMap.addAll(list)
                    updateNotiList.clear()
                    updateNotiList.addAll(list)
                    //KUtility.commitNotiList(updateNotiMap.toMutableSet())
                    KUtility.commitNotiJsonList(ShowInfosList(updateNotiList))

                    if (defaultSharedPreferences.getBoolean("useNotifications", true)) {
                        val links = arrayListOf<String>()
                        val names = arrayListOf<String>()
                        dismissCurrentNotis(this@ShowCheckIntentService)
                        @Suppress("IMPLICIT_CAST_TO_ANY") val nStyle = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Notification.InboxStyle()
                        } else {
                            NotificationCompat.InboxStyle()
                        }
                        for ((j, i) in list.withIndex()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                (nStyle as Notification.InboxStyle).addLine(i.name)
                            } else {
                                (nStyle as NotificationCompat.InboxStyle).addLine(i.name)
                            }
                            links.add(i.url)
                            names.add(i.name)
                            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                sendBubbleNotification(this@ShowCheckIntentService,
                                        android.R.mipmap.sym_def_app_icon,
                                        i.name,
                                        i.toString(),
                                        "episodeUpdate",
                                        EpisodeActivity::class.java,
                                        j + 3 + (Math.random() * 50).toInt(),
                                        NameWithUrl(i.name, i.url))
                            } else {*/
                                sendNotification(this@ShowCheckIntentService,
                                        android.R.mipmap.sym_def_app_icon,
                                        i.name,
                                        i.toString(),
                                        "episodeUpdate",
                                        EpisodeActivity::class.java,
                                        j + 3 + (Math.random() * 50).toInt(),
                                        NameWithUrl(i.name, i.url))
                            //}
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            (nStyle as Notification.InboxStyle).addLine("${list.size} show${if (list.size == 1) "" else "s"} had updates!")
                        } else {
                            (nStyle as NotificationCompat.InboxStyle).addLine("${list.size} show${if (list.size == 1) "" else "s"} had updates!")
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendBubbleGroupNotification(this@ShowCheckIntentService,
                                    android.R.mipmap.sym_def_app_icon,
                                    "${list.size} show${if (list.size == 1) "" else "s"} had updates!",
                                    nStyle as Notification.InboxStyle,
                                    "episodeUpdate",
                                    ShowListActivity::class.java, links, names)
                        } else {
                            sendGroupNotification(this@ShowCheckIntentService,
                                    android.R.mipmap.sym_def_app_icon,
                                    "${list.size} show${if (list.size == 1) "" else "s"} had updates!",
                                    nStyle as NotificationCompat.InboxStyle,
                                    "episodeUpdate",
                                    ShowListActivity::class.java)
                        }
                    }
                }
                sendFinishedCheckingNotification(this@ShowCheckIntentService,
                        android.R.mipmap.sym_def_app_icon,
                        "updateCheckRun", 2)
            }
        }
    }

    private fun sendRunningNotification(context: Context, smallIconId: Int, channel_id: String, notification_id: Int, prog: Int = 0, max: Int = 100, showCheckName: String = "") {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, "updateCheckRun")
                .setSmallIcon(smallIconId)
                .setContentTitle("Checking for Show Updates${if (prog != 0) ": $prog/$max" else ""}")
                .setChannelId(channel_id)
                .setProgress(max, prog, prog == 0)
                .setOngoing(true)
                .setVibrate(longArrayOf(0L))
                .setContentText(showCheckName)
                .setSubText("Checking for Show Updates")
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    private fun sendFinishedCheckingNotification(context: Context, smallIconId: Int, channel_id: String, notification_id: Int, title: String = "Finished Checking", subText: String = "Finished Checking") {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, "updateCheckRun")
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setSubText(subText)
                .setChannelId(channel_id)
                .setVibrate(longArrayOf(0L))
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setTimeoutAfter(750)

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    private fun dismissCurrentNotis(context: Context) {
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.cancel(0)
    }

    @RequiresApi(Build.VERSION_CODES.P)
    fun setUpBubble(context: Context, notification: Notification.Builder, notification_id: Int = 0, bubbleIntentSetup: (Intent) -> Unit) {
        // Create bubble intent
        val target = Intent(context, NotificationBubbleActivity::class.java)
        bubbleIntentSetup(target)
        val bubbleIntent = PendingIntent.getActivity(context, notification_id, target, 0 /* flags */)

        // Create bubble metadata
        val bubbleData = Notification.BubbleMetadata.Builder()
                .setDesiredHeight(600)
                // Note: although you can set the icon is not displayed in Q Beta 2
                .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
                .setIntent(bubbleIntent)
                //.setSuppressNotification(true)
                //.setDeleteIntent(createOnGroupDismissedIntent(context, notification_id))
                .build()

        // Create notification
        val chatBot = Person.Builder()
                .setBot(true)
                .setName("BubbleBot")
                .setImportant(true)
                .build()

        notification.setBubbleMetadata(bubbleData)
                .addPerson(chatBot)

    }

    private fun sendNotification(context: Context, smallIconId: Int, title: String, text: String, channel_id: String, gotoActivity: Class<*>, notification_id: Int, nameUrl: NameWithUrl) {

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
                notification_id,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mBuilder = NotificationCompat.Builder(context, "episodeUpdate")
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text).setBigContentTitle(title))
                .setChannelId(channel_id)
                .setGroup("episode_group")

        mBuilder.setContentIntent(resultPendingIntent)
        mBuilder.setDeleteIntent(createOnDismissedIntent(context, notification_id, nameUrl.url))

        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendBubbleNotification(context: Context, smallIconId: Int, title: String, text: String, channel_id: String, gotoActivity: Class<*>, notification_id: Int, nameUrl: NameWithUrl) {

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
                notification_id,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        val mBuilder = Notification.Builder(context, "episodeUpdate")
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setContentText(text)
                .setOnlyAlertOnce(true)
                //.setGroupAlertBehavior(Notification.GROUP_ALERT_SUMMARY)
                .setStyle(Notification.BigTextStyle().bigText(text).setBigContentTitle(title))
                .setChannelId(channel_id)
                .setGroup("episode_group")


        mBuilder.setContentIntent(resultPendingIntent)
        mBuilder.setDeleteIntent(createOnDismissedIntent(context, notification_id, nameUrl.url))

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
        mBuilder.setDeleteIntent(createOnGroupDismissedIntent(context, 0))
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(0, mBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendBubbleGroupNotification(context: Context, smallIconId: Int, title: String, messages: Notification.Style = Notification.InboxStyle(), channel_id: String, gotoActivity: Class<*>, links: ArrayList<String>, names: ArrayList<String>) {

        // The id of the channel.
        val mBuilder = Notification.Builder(context, "episodeUpdate")
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
        mBuilder.setDeleteIntent(createOnGroupDismissedIntent(context, 0))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            setUpBubble(context, mBuilder, 50) {
                val list = arrayListOf<ShowInfos>()
                for (i in links.indices) {
                    list += ShowInfos(names[i], 0, "", links[i])
                }
                it.putExtra(NotificationBubbleActivity.BUBBLE_LINKS, Gson().toJson(ShowInfosList(list)))
            }
        }
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(0, mBuilder.build())
    }

    private fun createOnDismissedIntent(context: Context, notificationId: Int, url: String): PendingIntent {
        val intent = Intent(context, NotificationDismissedReceiver::class.java)
        intent.putExtra(ConstantValues.URL_INTENT, url)
        //intent.putExtra("com.my.app.notificationId", notificationId)
        return PendingIntent.getBroadcast(context.applicationContext,
                notificationId, intent, 0)
    }

    private fun createOnGroupDismissedIntent(context: Context, notificationId: Int): PendingIntent {
        val intent = Intent(context, NotificationGroupDismissedReceiver::class.java)
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
        val url = intent.extras!!.getString(ConstantValues.URL_INTENT)
        if (url != null)
            KUtility.removeItemFromNotiJsonList(url)
    }
}

class NotificationGroupDismissedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        //val notificationId = intent.extras!!.getInt("com.my.app.notificationId")
        /* Your code to handle the event here */
        //ShowCheckIntentService.updateNotiMap.clear()
        KUtility.clearNotiJsonList()
    }
}