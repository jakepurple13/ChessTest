package com.crestron.aurora.utilities

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.R
import com.crestron.aurora.otherfun.*
import com.tonyodev.fetch2.*
import org.jetbrains.anko.defaultSharedPreferences

open class CustomFetchNotiManager(context: Context) : FetchNotificationManager {

    private val context: Context = context.applicationContext
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val downloadNotificationsMap = mutableMapOf<Int, DownloadNotification>()
    private val downloadNotificationsBuilderMap = mutableMapOf<Int, NotificationCompat.Builder>()
    override var progressReportingIntervalInMillis: Long = 0L

    init {
        initialize()
    }

    private fun initialize() {
        createNotificationChannels(context, notificationManager)
    }

    override fun createNotificationChannels(context: Context, notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = context.getString(R.string.fetch_notification_default_channel_id)
            var channel: NotificationChannel? = notificationManager.getNotificationChannel(channelId)
            if (channel == null) {
                val channelName = context.getString(R.string.fetch_notification_default_channel_name)
                channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    override fun getChannelId(notificationId: Int, context: Context): String {
        return context.getString(R.string.fetch_notification_default_channel_id)
    }

    override fun updateGroupSummaryNotification(groupId: Int,
                                                notificationBuilder: NotificationCompat.Builder,
                                                downloadNotifications: List<DownloadNotification>,
                                                context: Context): Boolean {
        val style = NotificationCompat.InboxStyle()
        for (downloadNotification in downloadNotifications) {
            val title = getContentTitle(downloadNotification)
            val contentTitle = getContentText(context, downloadNotification)
            style.addLine("$title $contentTitle")
        }
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .setContentTitle(context.getString(R.string.fetch_notification_default_channel_name))
                .setContentText("")
                .setStyle(style)
                .setGroup(groupId.toString())
                .setGroupSummary(true)
        return false
    }

    override fun updateNotification(notificationBuilder: NotificationCompat.Builder,
                                    downloadNotification: DownloadNotification,
                                    context: Context) {
        val smallIcon = if (downloadNotification.isDownloading) {
            android.R.drawable.stat_sys_download
        } else {
            android.R.drawable.stat_sys_download_done
        }

        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, DownloadViewerActivity::class.java)
        resultIntent.putExtra(ConstantValues.DOWNLOAD_NOTIFICATION, false)
        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(DownloadViewerActivity::class.java)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        fun getPauseOrResumeAction(downloadNotification: DownloadNotification): NotificationCompat.Action? {
            var text = ""
            var icon = 0
            val pauseIntent: Intent = when {
                downloadNotification.isDownloading -> {
                    text = "Pause"
                    icon = android.R.drawable.ic_media_pause
                    Intent(context, PauseReceiver::class.java).apply {
                        action = "fun.com.crestron.PAUSE_DOWNLOAD"
                        putExtra(ConstantValues.NOTIFICATION_ID, downloadNotification.notificationId)
                    }
                }
                downloadNotification.isPaused -> {
                    text = "Resume"
                    icon = android.R.drawable.ic_media_play
                    Intent(context, ResumeReceiver::class.java).apply {
                        action = "fun.com.crestron.RESUME_DOWNLOAD"
                        putExtra(ConstantValues.NOTIFICATION_ID, downloadNotification.notificationId)
                    }
                }
                downloadNotification.isFailed -> {
                    text = "Retry"
                    icon = android.R.drawable.ic_delete
                    Intent(context, RetryReceiver::class.java).apply {
                        action = "fun.com.crestron.RETRY"
                        putExtra(ConstantValues.NOTIFICATION_ID, downloadNotification.notificationId)
                    }
                }
                else -> {
                    null
                }
            } ?: return null
            val pendingPauseIntent = PendingIntent.getBroadcast(context, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            return NotificationCompat.Action(icon, text, pendingPauseIntent)
        }

        val currentProgress = "%.2f".format(FetchingUtils.getProgress(downloadNotification.download.downloaded, downloadNotification.download.total))
        val speed = FetchingUtils.getDownloadSpeedString(downloadNotification.downloadedBytesPerSecond)
        notificationBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSmallIcon(smallIcon)
                .setContentTitle(getContentTitle(downloadNotification))
                .setContentText(getContentText(context, downloadNotification))
                .setOngoing(downloadNotification.isOnGoingNotification)
                .setContentIntent(resultPendingIntent)
                .setSubText("$speed at $currentProgress%")
                .setGroup(downloadNotification.groupId.toString())
                .setGroupSummary(false)
                .setOnlyAlertOnce(true)
        if (downloadNotification.isFailed) {
            notificationBuilder.setProgress(0, 0, false)
        } else {
            val progressIndeterminate = downloadNotification.progressIndeterminate
            val maxProgress = if (downloadNotification.progressIndeterminate) 0 else 100
            val download = downloadNotification.download
            val progress = if (download.progress < 0) 0 else download.progress
            notificationBuilder.setProgress(maxProgress, progress, progressIndeterminate)
        }
        val actions = getPauseOrResumeAction(downloadNotification)
        if (actions != null)
            notificationBuilder.addAction(actions)
        /*when {
            downloadNotification.isDownloading -> {
                notificationBuilder.addAction(R.drawable.fetch_notification_pause,
                        context.getString(R.string.fetch_notification_download_pause),
                        getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.PAUSE))
                        .addAction(R.drawable.fetch_notification_cancel,
                                context.getString(R.string.fetch_notification_download_cancel),
                                getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.CANCEL))
            }
            downloadNotification.isPaused -> {
                notificationBuilder.addAction(R.drawable.fetch_notification_resume,
                        context.getString(R.string.fetch_notification_download_resume),
                        getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.RESUME))
                        .addAction(R.drawable.fetch_notification_cancel,
                                context.getString(R.string.fetch_notification_download_cancel),
                                getActionPendingIntent(downloadNotification, DownloadNotification.ActionType.CANCEL))
            }
        }*/
        val cancelIntent = Intent(context, NotificationBroadcastReceiver::class.java).apply {
            action = "fun.com.crestron.CANCEL_DOWNLOAD"
            putExtra(ConstantValues.NOTIFICATION_ID, downloadNotification.notificationId)
        }

        val pendingIntent = PendingIntent.getBroadcast(context, 1, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.addAction(android.R.drawable.ic_delete, "Cancel", pendingIntent)
    }

    override fun getActionPendingIntent(downloadNotification: DownloadNotification,
                                        actionType: DownloadNotification.ActionType): PendingIntent {
        synchronized(downloadNotificationsMap) {
            val intent = Intent(ACTION_NOTIFICATION_ACTION)
            intent.putExtra(EXTRA_NAMESPACE, downloadNotification.download.namespace)
            intent.putExtra(EXTRA_DOWNLOAD_ID, downloadNotification.download.id)
            intent.putExtra(EXTRA_NOTIFICATION_ID, downloadNotification.notificationId)
            intent.putExtra(EXTRA_GROUP_ACTION, false)
            intent.putExtra(EXTRA_NOTIFICATION_GROUP_ID, downloadNotification.groupId)
            val action = when (actionType) {
                DownloadNotification.ActionType.CANCEL -> ACTION_TYPE_CANCEL
                DownloadNotification.ActionType.DELETE -> ACTION_TYPE_DELETE
                DownloadNotification.ActionType.RESUME -> ACTION_TYPE_RESUME
                DownloadNotification.ActionType.PAUSE -> ACTION_TYPE_PAUSE
                DownloadNotification.ActionType.RETRY -> ACTION_TYPE_RETRY
                else -> ACTION_TYPE_INVALID
            }
            intent.putExtra(EXTRA_ACTION_TYPE, action)
            return PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }

    override fun getGroupActionPendingIntent(groupId: Int,
                                             downloadNotifications: List<DownloadNotification>,
                                             actionType: DownloadNotification.ActionType): PendingIntent {
        synchronized(downloadNotificationsMap) {
            val intent = Intent(ACTION_NOTIFICATION_ACTION)
            intent.putExtra(EXTRA_NOTIFICATION_GROUP_ID, groupId)
            intent.putExtra(EXTRA_DOWNLOAD_NOTIFICATIONS, ArrayList(downloadNotifications))
            intent.putExtra(EXTRA_GROUP_ACTION, true)
            val action = when (actionType) {
                DownloadNotification.ActionType.CANCEL_ALL -> ACTION_TYPE_CANCEL_ALL
                DownloadNotification.ActionType.DELETE_ALL -> ACTION_TYPE_DELETE_ALL
                DownloadNotification.ActionType.RESUME_ALL -> ACTION_TYPE_RESUME_ALL
                DownloadNotification.ActionType.PAUSE_ALL -> ACTION_TYPE_PAUSE_ALL
                DownloadNotification.ActionType.RETRY_ALL -> ACTION_TYPE_RETRY_ALL
                else -> ACTION_TYPE_INVALID
            }
            intent.putExtra(EXTRA_ACTION_TYPE, action)
            return PendingIntent.getBroadcast(context, System.currentTimeMillis().toInt(), intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }

    override fun cancelNotification(notificationId: Int) {
        synchronized(downloadNotificationsMap) {
            notificationManager.cancel(notificationId)
            downloadNotificationsBuilderMap.remove(notificationId)
            val downloadNotification = downloadNotificationsMap[notificationId]
            if (downloadNotification != null) {
                downloadNotificationsMap.remove(notificationId)
                notify(downloadNotification.groupId)
            }
        }
    }

    override fun cancelOngoingNotifications() {
        synchronized(downloadNotificationsMap) {
            val iterator = downloadNotificationsMap.values.iterator()
            var downloadNotification: DownloadNotification
            while (iterator.hasNext()) {
                downloadNotification = iterator.next()
                if (downloadNotification.isActive) {
                    notificationManager.cancel(downloadNotification.notificationId)
                    downloadNotificationsBuilderMap.remove(downloadNotification.notificationId)
                    iterator.remove()
                    notify(downloadNotification.groupId)
                }
            }
        }
    }

    override fun notify(groupId: Int) {
        synchronized(downloadNotificationsMap) {
            val groupedDownloadNotifications = downloadNotificationsMap.values.filter { it.groupId == groupId }
            val ongoingNotification = groupedDownloadNotifications.any { it.isOnGoingNotification }
            val groupSummaryNotificationBuilder = getNotificationBuilder(groupId, groupId)
            val useGroupNotification = updateGroupSummaryNotification(groupId, groupSummaryNotificationBuilder, groupedDownloadNotifications, context)
            var notificationId: Int
            var notificationBuilder: NotificationCompat.Builder
            val notificationIdList = mutableListOf<Int>()
            val notificationOngoingList = mutableListOf<Boolean>()
            for (downloadNotification in groupedDownloadNotifications) {
                notificationId = downloadNotification.notificationId
                notificationBuilder = getNotificationBuilder(notificationId, groupId)
                updateNotification(notificationBuilder, downloadNotification, context)
                notificationIdList.add(notificationId)
                notificationOngoingList.add(downloadNotification.isOnGoingNotification)
                notificationManager.notify(notificationId, notificationBuilder.build())
            }
            if (useGroupNotification) {
                notificationIdList.add(groupId)
                notificationOngoingList.add(ongoingNotification)
                if (context.defaultSharedPreferences.getBoolean("useNotifications", true))
                    notificationManager.notify(groupId, groupSummaryNotificationBuilder.build())
            }
            for (index in 0 until notificationIdList.size) {
                handleNotificationOngoingDismissal(notificationIdList[index], groupId, notificationOngoingList[index])
            }
        }
    }

    override fun postNotificationUpdate(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long): Boolean {
        return synchronized(downloadNotificationsMap) {
            val downloadNotification = downloadNotificationsMap[download.id]
                    ?: DownloadNotification(download)
            downloadNotification.notificationId = download.id
            downloadNotification.groupId = download.group
            downloadNotification.download = download
            downloadNotification.downloadedBytesPerSecond = downloadedBytesPerSecond
            downloadNotification.etaInMilliSeconds = etaInMilliSeconds
            downloadNotificationsMap[download.id] = downloadNotification
            if (downloadNotification.isCancelledNotification) {
                cancelNotification(downloadNotification.notificationId)
            } else {
                notify(download.group)
            }
            true
        }
    }

    override fun getNotificationBuilder(notificationId: Int, groupId: Int): NotificationCompat.Builder {
        synchronized(downloadNotificationsMap) {
            val notificationBuilder = downloadNotificationsBuilderMap[notificationId]
                    ?: NotificationCompat.Builder(context, getChannelId(notificationId, context))
            downloadNotificationsBuilderMap[notificationId] = notificationBuilder
            notificationBuilder
                    .setGroup(notificationId.toString())
                    .setStyle(null)
                    .setProgress(0, 0, false)
                    .setContentTitle(null)
                    .setContentText(null)
                    .setContentIntent(null)
                    .setGroupSummary(false)
                    .setOnlyAlertOnce(true)
                    .setOngoing(false)
                    .setGroup(groupId.toString())
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .mActions.clear()
            return notificationBuilder
        }
    }

    override fun handleNotificationOngoingDismissal(notificationId: Int, groupId: Int, ongoingNotification: Boolean) {
        synchronized(downloadNotificationsMap) {
            if (progressReportingIntervalInMillis > 0) {
                val alarmIntent = Intent(ACTION_NOTIFICATION_CHECK)
                alarmIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId)
                val pendingIntent = PendingIntent.getBroadcast(context, notificationId, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT)
                alarmManager.cancel(pendingIntent)
                if (ongoingNotification) {
                    val alarmTimeMillis = SystemClock.elapsedRealtime() + progressReportingIntervalInMillis + getOngoingDismissalDelay(notificationId, groupId)
                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, alarmTimeMillis, pendingIntent)
                }
            }
        }
    }

    override fun getOngoingDismissalDelay(notificationId: Int, groupId: Int): Long {
        return 3000
    }

    private fun getContentTitle(downloadNotification: DownloadNotification): String {
        val download = downloadNotification.download
        return download.fileUri.lastPathSegment ?: Uri.parse(download.url).lastPathSegment
        ?: download.url
    }

    private fun getContentText(context: Context, downloadNotification: DownloadNotification): String {
        return when {
            downloadNotification.isCompleted -> context.getString(R.string.fetch_notification_download_complete)
            downloadNotification.isFailed -> context.getString(R.string.fetch_notification_download_failed)
            downloadNotification.isPaused -> context.getString(R.string.fetch_notification_download_paused)
            downloadNotification.isQueued -> context.getString(R.string.fetch_notification_download_starting)
            downloadNotification.etaInMilliSeconds < 0 -> context.getString(R.string.fetch_notification_download_downloading)
            else -> getEtaText(context, downloadNotification.etaInMilliSeconds)
        }
    }

    private fun getEtaText(context: Context, etaInMilliSeconds: Long): String {
        var seconds = (etaInMilliSeconds / 1000)
        val hours = (seconds / 3600)
        seconds -= (hours * 3600)
        val minutes = (seconds / 60)
        seconds -= (minutes * 60)
        return when {
            hours > 0 -> context.getString(R.string.fetch_notification_download_eta_hrs, hours, minutes, seconds)
            minutes > 0 -> context.getString(R.string.fetch_notification_download_eta_min, minutes, seconds)
            else -> context.getString(R.string.fetch_notification_download_eta_sec, seconds)
        }
    }

}