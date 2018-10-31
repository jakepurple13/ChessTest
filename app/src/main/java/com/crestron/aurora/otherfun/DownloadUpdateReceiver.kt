package com.crestron.aurora.otherfun

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.util.Log
import com.crestron.aurora.ConstantValues
import com.google.gson.Gson
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.HttpUrlConnectionDownloader
import com.tonyodev.fetch2.Request
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2core.Func
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.net.URL

class DownloadUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra("firebase_channel_id", 0) ?: 0
        Log.d("", "NotificationBroadcastReceiver: notificationId = $notificationId")
        GlobalScope.launch {
            getNewApp(context!!)
        }
    }

    private fun getNewApp(context: Context) {

        val url = URL(ConstantValues.VERSION_URL).readText()
        val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)

        //val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        //val version = pInfo.versionName

        val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getNameFromUrl(info.link)!!.replace(".png", ".apk")
        val request = Request(info.link, filePath)

        val fetchConfiguration = FetchConfiguration.Builder(context)
                .enableAutoStart(true)
                .enableRetryOnNetworkGain(true)
                .setProgressReportingInterval(1000L)
                .setHttpDownloader(HttpUrlConnectionDownloader(Downloader.FileDownloaderType.PARALLEL))
                .setDownloadConcurrentLimit(1)
                .build()

        val fetch = fetchConfiguration.getNewFetchInstanceFromConfiguration()

        fetch.addListener(object : FetchingUtils.FetchAction {
            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
                val progress = "%.2f".format(FetchingUtils.getProgress(download.downloaded, download.total))
                val info1 = "$progress% " +
                        "at ${FetchingUtils.getDownloadSpeedString(downloadedBytesPerSecond)} " +
                        "with ${FetchingUtils.getETAString(etaInMilliSeconds)}"

                sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                        info1,
                        download.progress,
                        context,
                        DownloadViewerActivity::class.java,
                        download.id)
            }

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                super.onStarted(download, downloadBlocks, totalBlocks)
                context.defaultSharedPreferences.edit().putBoolean("delete_file", false).apply()
            }

            override fun onCompleted(download: Download) {
                super.onCompleted(download)

                val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                mNotificationManager.cancel(download.id)
                val strApkToInstall = getNameFromUrl(info.link)!!.replace(".png", ".apk")
                val path1 = File(File(Environment.getExternalStorageDirectory(), "Download"), strApkToInstall)

                val apkUri = GenericFileProvider.getUriForFile(context, context.packageName + ".otherfun.GenericFileProvider", path1)
                val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                intent.data = apkUri
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                //intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                context.startActivity(intent)
                context.defaultSharedPreferences.edit().putBoolean("delete_file", true).apply()
            }
        })

        fetch.enqueue(request, Func {

        }, Func {

        })
    }

    fun sendProgressNotification(title: String, text: String, progress: Int, context: Context, gotoActivity: Class<*>, notification_id: Int) {

        val mBuilder = NotificationCompat.Builder(context, ConstantValues.CHANNEL_ID)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setOngoing(true)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setProgress(100, progress, false)
                .setOnlyAlertOnce(true)

        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)
        resultIntent.putExtra(ConstantValues.DOWNLOAD_NOTIFICATION, false)
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

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        val mNotificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    fun getNameFromUrl(url: String): String? {
        return Uri.parse(url).lastPathSegment
    }

}