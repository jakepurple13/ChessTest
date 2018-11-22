package com.crestron.aurora.otherfun

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import com.abdeveloper.library.MultiSelectDialog
import com.abdeveloper.library.MultiSelectModel
import com.crashlytics.android.Crashlytics
import com.crestron.aurora.ChoiceActivity
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.views.DownloadsWidget
import com.tonyodev.fetch2.AbstractFetchListener
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2core.Func
import github.nisrulz.recyclerviewhelper.RVHItemTouchHelperCallback
import kotlinx.android.synthetic.main.activity_download_viewer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.annotations.NotNull
import java.util.*


class DownloadViewerActivity : AppCompatActivity(), ActionListener {

    private val UNKNOWN_REMAINING_TIME: Long = -1
    private val UNKNOWN_DOWNLOADED_BYTES_PER_SECOND: Long = 0
    private var fetch: Fetch? = null
    private var fileAdapter: FileAdapter? = null

    private var stats: EpisodeActivity.StatusPlay = EpisodeActivity.StatusPlay.PLAY

    var backChoice = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_download_viewer)

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        backChoice = intent.getBooleanExtra(ConstantValues.DOWNLOAD_NOTIFICATION, true)

        fetch = Fetch.getDefaultInstance()

        download_list.layoutManager = LinearLayoutManager(this)
        fileAdapter = FileAdapter(this, this)
        download_list.adapter = fileAdapter

        val callback = RVHItemTouchHelperCallback(fileAdapter
                , false
                , true
                , true)
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(download_list)

        multiple_download_delete.setOnClickListener {

            GlobalScope.launch {

                val downloadList = fileAdapter!!.downloads

                /*fetch!!.getDownloads(Func {
                    Loged.wtf(it.joinToString { "," })
                    downloadList.addAll(it)
                })*/

                val multiSelectDialog = MultiSelectDialog()
                        .title("Select the Downloads to Cancel") //setting title for dialog
                        .titleSize(25f)
                        .positiveText("Done")
                        .negativeText("Cancel")
                        .setMinSelectionLimit(0) //you can set minimum checkbox selection limit (Optional)
                        .setMaxSelectionLimit(downloadList.size) //you can set maximum checkbox selection limit (Optional)
                        .multiSelectList(ArrayList<MultiSelectModel>().apply {
                            for (i in 0 until downloadList.size) {
                                add(MultiSelectModel(downloadList[i].id, Uri.parse(downloadList[i].download!!.url).lastPathSegment))
                            }
                        }) // the multi select model list with ids and name
                        .onSubmit(object : MultiSelectDialog.SubmitCallbackListener {
                            override fun onSelected(selectedIds: java.util.ArrayList<Int>?, selectedNames: java.util.ArrayList<String>?, dataString: String?) {
                                launch {
                                    FetchingUtils.downloadCount -= selectedIds!!.size
                                    fetch!!.cancel(selectedIds)
                                    fetch!!.delete(selectedIds)
                                    fetch!!.remove(selectedIds)
                                }
                            }

                            override fun onCancel() {
                                Loged.e("cancelled")
                            }

                        })

                runOnUiThread {

                    multiSelectDialog.show(supportFragmentManager, "multiSelectDialog")

                }
            }
        }

    }

    override fun onBackPressed() {
        if (backChoice)
            super.onBackPressed()
        else {
            val intent = Intent(this@DownloadViewerActivity, ChoiceActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        fetch!!.getDownloads(Func { downloads ->
            val list = ArrayList(downloads)
            list.sortWith(Comparator { first, second -> java.lang.Long.compare(first.created, second.created) })
            for (download in list) {
                fileAdapter!!.addDownload(download)
            }
        }).addListener(fetchListener)
    }

    override fun onPause() {
        super.onPause()
        //fetch!!.removeListener(fetchListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        //fetch!!.close()
    }

    private val fetchListener = object : AbstractFetchListener() {
        override fun onQueued(@NotNull download: Download, waitingOnNetwork: Boolean) {
            fileAdapter!!.addDownload(download)
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
        }

        override fun onCompleted(@NotNull download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            val mNotificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(download.id)
            //mNotificationManager.cancelAll()
            FetchingUtils.remove(download)
            ChoiceActivity.downloadCast(this@DownloadViewerActivity, ChoiceActivity.BroadCastInfo.KVObject("view_download_item_count", "1"))
            ViewVideosActivity.videoCast(this@DownloadViewerActivity)
            //FetchingUtils.downloadCount+=1
            /*sendNotification(this@DownloadViewerActivity, android.R.mipmap.sym_def_app_icon,
                    download.file.substring(download.file.lastIndexOf("/") + 1),
                    "All Finished!",
                    ConstantValues.CHANNEL_ID,
                    EpisodeActivity::class.java, download.id,
                    EpisodeActivity.KeyAndValue(ConstantValues.URL_INTENT, "${download.extras.map[ConstantValues.URL_INTENT]}"),
                    EpisodeActivity.KeyAndValue(ConstantValues.NAME_INTENT, "${download.extras.map[ConstantValues.NAME_INTENT]}"))*/
            if (defaultSharedPreferences.getBoolean("useNotifications", true))
                sendNotification(this@DownloadViewerActivity, android.R.mipmap.sym_def_app_icon,
                        download.file.substring(download.file.lastIndexOf("/") + 1),
                        "All Finished!",
                        ConstantValues.CHANNEL_ID,
                        StartVideoFromNotificationActivity::class.java, download.id,
                        EpisodeActivity.KeyAndValue("video_path", download.file),
                        EpisodeActivity.KeyAndValue("video_name", download.file))
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            Crashlytics.log("${error.throwable?.message}")
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            if (defaultSharedPreferences.getBoolean(ConstantValues.AUTO_RETRY, false))
                FetchingUtils.retry(download)
            /*else
                sendRetryNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                        "An error has occurred",
                        download.progress,
                        this@DownloadViewerActivity,
                        DownloadViewerActivity::class.java,
                        download.id)*/
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
        }

        override fun onProgress(@NotNull download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            fileAdapter!!.update(download, etaInMilliSeconds, downloadedBytesPerSecond)
            /*val progress = "%.2f".format(FetchingUtils.getProgress(download.downloaded, download.total))
            val info = "$progress% " +
                    "at ${FetchingUtils.getDownloadSpeedString(downloadedBytesPerSecond)} " +
                    "with ${FetchingUtils.getETAString(etaInMilliSeconds)}"*/
            /*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                    info,
                    download.progress,
                    this@DownloadViewerActivity,
                    DownloadViewerActivity::class.java,
                    download.id)*/
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
            //DefaultFetchNotificationManager(this@DownloadViewerActivity).postNotificationUpdate(download, etaInMilliSeconds, downloadedBytesPerSecond)
        }

        override fun onPaused(@NotNull download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            //val mNotificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //mNotificationManager.cancel(download.id)
            stats = EpisodeActivity.StatusPlay.PAUSE
            /*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                    "Paused",
                    download.progress,
                    this@DownloadViewerActivity,
                    DownloadViewerActivity::class.java,
                    download.id)*/
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
        }

        override fun onResumed(@NotNull download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            stats = EpisodeActivity.StatusPlay.PLAY
            /*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                    "Resumed",
                    download.progress,
                    this@DownloadViewerActivity,
                    DownloadViewerActivity::class.java,
                    download.id)*/
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
        }

        override fun onCancelled(@NotNull download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            val mNotificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(download.id)
            try {
                deleteFile(download.file)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
        }

        override fun onRemoved(@NotNull download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            //val mNotificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //mNotificationManager.cancel(download.id)
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
        }

        override fun onDeleted(@NotNull download: Download) {
            fileAdapter!!.update(download, UNKNOWN_REMAINING_TIME, UNKNOWN_DOWNLOADED_BYTES_PER_SECOND)
            val mNotificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager.cancel(download.id)
            try {
                deleteFile(download.file)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }
            DownloadsWidget.sendRefreshBroadcast(this@DownloadViewerActivity)
        }
    }

    fun sendNotification(context: Context, smallIconId: Int, title: String, message: String, channel_id: String, gotoActivity: Class<*>, notification_id: Int, vararg dataToPass: EpisodeActivity.KeyAndValue) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setContentText(message)
                .setGroup("downloaded_group")
                .setChannelId(channel_id)
                .setAutoCancel(true)

        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)
        resultIntent.putExtra(ConstantValues.DOWNLOAD_NOTIFICATION, false)

        for (i in dataToPass) {
            resultIntent.putExtra(i.key, i.value)
        }

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your app to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(gotoActivity)
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)
        //stackBuilder.addNextIntent(Intent.createChooser(resultIntent, "Complete action using"))
        val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id * 2, mBuilder.build())

    }

    override fun onPauseDownload(id: Int) {
        fetch!!.pause(id)
    }

    override fun onResumeDownload(id: Int) {
        fetch!!.resume(id)
    }

    override fun onRemoveDownload(id: Int) {
        fetch!!.remove(id)
    }

    override fun onRetryDownload(id: Int) {
        fetch!!.retry(id)
    }

}