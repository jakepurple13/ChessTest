package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.Toast
import com.abdeveloper.library.MultiSelectDialog
import com.abdeveloper.library.MultiSelectModel
import com.crashlytics.android.Crashlytics
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
import com.squareup.picasso.Picasso
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.android.synthetic.main.activity_episode.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.defaultSharedPreferences
import org.jsoup.Jsoup
import programmer.box.utilityhelper.UtilNotification
import java.util.ArrayList


class EpisodeActivity : AppCompatActivity() {

    private val listOfUrls = arrayListOf<String>()
    private val listOfNames = arrayListOf<String>()

    lateinit var mNotificationManager: NotificationManager
    lateinit var url: String
    lateinit var name: String


    fun nameUrl(s: String = ""): String {
        return "$name\n$url\n$s"
    }

    enum class StatusPlay {
        PLAY,
        PAUSE
    }

    var stats: StatusPlay = StatusPlay.PLAY

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode)

        mNotificationManager = this@EpisodeActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            UtilNotification.createNotificationChannel(this@EpisodeActivity, ConstantValues.CHANNEL_NAME, ConstantValues.CHANNEL_DES, ConstantValues.CHANNEL_ID)
            UtilNotification.createNotificationGroup(this@EpisodeActivity, ConstantValues.GROUP_ID, ConstantValues.GROUP_NAME)
        }

        url = intent.getStringExtra(ConstantValues.URL_INTENT)
        name = intent.getStringExtra(ConstantValues.NAME_INTENT)

        download_info.linksClickable = true
        download_info.text = nameUrl()

        val fetching = FetchingUtils(this, object : FetchingUtils.FetchAction {

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                super.onStarted(download, downloadBlocks, totalBlocks)
                progressBar2.max = 100
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
                val progress = "%.2f".format(FetchingUtils.getProgress(download.downloaded, download.total))
                val info = "$progress% " +
                        "at ${FetchingUtils.getDownloadSpeedString(downloadedBytesPerSecond)} " +
                        "with ${FetchingUtils.getETAString(etaInMilliSeconds)}"
                runOnUiThread {
                    progressBar2.setProgress(download.progress, true)
                    //download_info.text = nameUrl(info)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                            info,
                            download.progress,
                            this@EpisodeActivity,
                            DownloadViewerActivity::class.java,
                            download.id)
                }
            }

            override fun onCancelled(download: Download) {
                super.onCancelled(download)
                mNotificationManager.cancel(download.id)
                try {
                    deleteFile(download.file)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: java.lang.NullPointerException) {
                    e.printStackTrace()
                }
                progressBar2.progress = 0
                //download_info.text = nameUrl("0% at 0 b/s with 0 secs left")
            }

            override fun onPaused(download: Download) {
                super.onPaused(download)
                stats = StatusPlay.PAUSE
                sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                        "Paused",
                        download.progress,
                        this@EpisodeActivity,
                        DownloadViewerActivity::class.java,
                        download.id)
            }

            override fun onResumed(download: Download) {
                super.onResumed(download)
                stats = StatusPlay.PLAY
                sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                        "Resumed",
                        download.progress,
                        this@EpisodeActivity,
                        DownloadViewerActivity::class.java,
                        download.id)
            }

            override fun onDeleted(download: Download) {
                super.onDeleted(download)
                mNotificationManager.cancel(download.id)
                try {
                    deleteFile(download.file)
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                } catch (e: java.lang.NullPointerException) {
                    e.printStackTrace()
                }
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                super.onError(download, error, throwable)
                Crashlytics.log("${error.throwable?.message}")
                if (defaultSharedPreferences.getBoolean(ConstantValues.AUTO_RETRY, false))
                    FetchingUtils.retry(download)
                else
                    sendRetryNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                            "An error has occurred",
                            download.progress,
                            this@EpisodeActivity,
                            DownloadViewerActivity::class.java,
                            download.id)
            }

            override fun onCompleted(download: Download) {
                super.onCompleted(download)
                Toast.makeText(this@EpisodeActivity, "Finished Downloading", Toast.LENGTH_LONG).show()
                progressBar2.progress = 0
                mNotificationManager.cancel(download.id)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    //UtilNotification.sendNotification(this@EpisodeActivity, android.R.mipmap.sym_def_app_icon, download.file.substring(download.file.lastIndexOf("/") + 1), "All Finished!", "showDownload", ChooseActivity::class.java, download.id)
                    sendNotification(this@EpisodeActivity,
                            android.R.mipmap.sym_def_app_icon,
                            download.file.substring(download.file.lastIndexOf("/") + 1),
                            "All Finished!",
                            ConstantValues.CHANNEL_ID,
                            EpisodeActivity::class.java,
                            download.id,
                            KeyAndValue(ConstantValues.URL_INTENT, url),
                            KeyAndValue(ConstantValues.NAME_INTENT, name))
                }
            }

        })

        episode_list.layoutManager = LinearLayoutManager(this)
        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
            }
        }

        episode_list.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(episode_list.context, (episode_list.layoutManager as LinearLayoutManager).orientation)
        episode_list.addItemDecoration(dividerItemDecoration)
        episode_list.addItemDecoration(ItemOffsetDecoration(20))

        fun getList() = launch {

            fun getStuff(url: String) {
                val doc1 = Jsoup.connect(url).get()

                val stuffList = doc1.allElements.select("div#videos").select("a[href^=http]")

                for (i in stuffList) {
                    Loged.d(i.attr("abs:href"))
                    listOfUrls.add(i.attr("abs:href"))
                    listOfNames.add(i.text())
                }
            }

            val doc1 = Jsoup.connect(url).get()
            val stuffList = doc1.allElements.select("div#videos").select("a[href^=http]")

            for (i in stuffList) {
                Loged.d(i.attr("abs:href"))
                listOfUrls.add(i.attr("abs:href"))
                listOfNames.add(i.text())
            }

            val stuffLists = doc1.allElements.select("ul.pagination").select(" button[href^=http]")

            for (i in stuffLists) {
                Loged.d(i.attr("abs:href"))
                getStuff(i.attr("abs:href"))
            }

            runOnUiThread {

                val des = if (doc1.allElements.select("div#series_details").select("span#full_notes").hasText())
                    doc1.allElements.select("div#series_details").select("span#full_notes").text().removeSuffix("less")
                else {
                    val d = doc1.allElements.select("div#series_details").select("div:contains(Description:)").select("div").text()
                    try {
                        d.substring(d.indexOf("Description: ") + 13, d.indexOf("Category: "))
                    } catch (e: StringIndexOutOfBoundsException) {
                        Loged.e(e.message!!)
                        d
                    }
                }

                download_info.text = nameUrl(des)

                Picasso.get().load(doc1.select("div.left_col").select("img[src^=http]#series_image").attr("abs:src"))
                        .error(R.drawable.apk).resize((600 * .6).toInt(), (800 * .6).toInt()).into(cover_image)

                episode_list.adapter = EpisodeAdapter(listOfNames, listOfUrls, name, context = this@EpisodeActivity, action = object : EpisodeAction {
                    override fun hit(name: String, url: String) {
                        super.hit(name, url)
                        launch {
                            fetching.getVideo(url, if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL,
                                    KeyAndValue(ConstantValues.URL_INTENT, this@EpisodeActivity.url),
                                    KeyAndValue(ConstantValues.NAME_INTENT, this@EpisodeActivity.name))
                        }
                    }
                })
                Loged.d("${(episode_list.adapter!! as EpisodeAdapter).itemCount}")
            }

            episode_refresh.isRefreshing = false

        }

        getList()

        episode_refresh.setOnRefreshListener {
            listOfUrls.clear()
            listOfNames.clear()
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            getList()
        }

        reverse_order.setOnCheckedChangeListener { _, b ->
            if (b) {
                //Toast.makeText(this@EpisodeActivity, "Will only download on Wifi", Toast.LENGTH_LONG).show()
            } else {
                //Toast.makeText(this@EpisodeActivity, "Will download using Mobile Data or Wifi", Toast.LENGTH_LONG).show()
            }

            listOfNames.reverse()
            listOfUrls.reverse()

            runOnUiThread {
                episode_list.adapter = EpisodeAdapter(listOfNames, listOfUrls, name, reverse = b, context = this@EpisodeActivity, action = object : EpisodeAction {
                    override fun hit(name: String, url: String) {
                        super.hit(name, url)
                        launch {
                            fetching.getVideo(url, if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL,
                                    KeyAndValue(ConstantValues.URL_INTENT, this@EpisodeActivity.url),
                                    KeyAndValue(ConstantValues.NAME_INTENT, this@EpisodeActivity.name))
                        }
                    }
                })
                Loged.d("${(episode_list.adapter!! as EpisodeAdapter).itemCount}")
            }

        }

        goto_downloads.setOnClickListener {
            startActivity(Intent(this@EpisodeActivity, DownloadViewerActivity::class.java))
        }

        val show = ShowDatabase.getDatabase(this@EpisodeActivity)

        launch {
            if (show.showDao().isInDatabase(name) > 0) {
                fav_episode.isChecked = true
            }
        }

        batch_download.setOnClickListener {
            val multiSelectDialog = MultiSelectDialog()
                    .title("Select the Episodes to download") //setting title for dialog
                    .titleSize(25f)
                    .positiveText("Done")
                    .negativeText("Cancel")
                    .setMinSelectionLimit(0) //you can set minimum checkbox selection limit (Optional)
                    .setMaxSelectionLimit(listOfNames.size) //you can set maximum checkbox selection limit (Optional)
                    .multiSelectList(ArrayList<MultiSelectModel>().apply {
                        for (i in 0 until listOfNames.size) {
                            add(MultiSelectModel(i, listOfNames[i]))
                        }
                    }) // the multi select model list with ids and name
                    .onSubmit(object : MultiSelectDialog.SubmitCallbackListener {
                        override fun onSelected(selectedIds: java.util.ArrayList<Int>?, selectedNames: java.util.ArrayList<String>?, dataString: String?) {

                            val urlList = arrayListOf<String>().apply {
                                for (i in 0 until selectedIds!!.size) {
                                    /*Toast.makeText(this@EpisodeActivity, "Selected Ids : " + selectedIds[i] + "\n" +
                                            "Selected Names : " + selectedNames!![i] + "\n" +
                                            "DataString : " + dataString, Toast.LENGTH_SHORT).show()*/
                                    Loged.e("Selected Ids : " + selectedIds[i] + "\n" +
                                            "Selected Names : " + selectedNames!![i] + "\n" +
                                            "DataString : " + dataString)
                                    add(listOfUrls[selectedIds[i]])
                                }
                            }

                            launch {
                                fetching.getVideo(urlList, if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL,
                                        KeyAndValue(ConstantValues.URL_INTENT, this@EpisodeActivity.url),
                                        KeyAndValue(ConstantValues.NAME_INTENT, this@EpisodeActivity.name))
                            }

                        }

                        override fun onCancel() {
                            Loged.e("cancelled")
                        }

                    })

            multiSelectDialog.show(supportFragmentManager, "multiSelectDialog")
        }

        fav_episode.setOnCheckedChangeListener { _, b ->

            fun getEpisodeList(url: String) = async {
                val doc1 = Jsoup.connect(url).get()
                val stuffList = doc1.allElements.select("div#videos").select("a[href^=http]")
                stuffList.size
            }

            async {
                if (b) {
                    show.showDao().insert(Show(url, name))

                    async {
                        val s = show.showDao().getShow(name)
                        val showList = getEpisodeList(url).await()
                        if (s.showNum < showList) {
                            s.showNum = showList
                            show.showDao().updateShow(s)
                        }
                        Loged.wtf("${s.name} and size is $showList")
                    }

                } else {
                    show.showDao().deleteShow(name)
                }
            }
        }

    }

    fun sendProgressNotification(title: String, text: String, progress: Int, context: Context, gotoActivity: Class<*>, notification_id: Int) {

        val mBuilder = NotificationCompat.Builder(this@EpisodeActivity, ConstantValues.CHANNEL_ID)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setOngoing(true)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setProgress(100, progress, false)
                .setOnlyAlertOnce(true)

        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)
        resultIntent.putExtra(ConstantValues.URL_INTENT, url)
        resultIntent.putExtra(ConstantValues.NAME_INTENT, name)
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

        fun getPauseOrResumeAction(): NotificationCompat.Action {
            return when (stats) {
                StatusPlay.PLAY -> {
                    val pauseIntent = Intent(applicationContext, PauseReceiver::class.java).apply {
                        action = "fun.com.crestron.PAUSE_DOWNLOAD"
                        putExtra(ConstantValues.NOTIFICATION_ID, notification_id)
                    }
                    val pendingPauseIntent = PendingIntent.getBroadcast(applicationContext, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    NotificationCompat.Action(android.R.drawable.ic_media_pause, "Pause", pendingPauseIntent)
                }
                StatusPlay.PAUSE -> {
                    val pauseIntent = Intent(applicationContext, ResumeReceiver::class.java).apply {
                        action = "fun.com.crestron.RESUME_DOWNLOAD"
                        putExtra(ConstantValues.NOTIFICATION_ID, notification_id)
                    }
                    val pendingPauseIntent = PendingIntent.getBroadcast(applicationContext, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT)
                    NotificationCompat.Action(android.R.drawable.ic_media_play, "Resume", pendingPauseIntent)
                }
            }
        }
        mBuilder.addAction(getPauseOrResumeAction())

        val cancelIntent = Intent(applicationContext, NotificationBroadcastReceiver::class.java).apply {
            action = "fun.com.crestron.CANCEL_DOWNLOAD"
            putExtra(ConstantValues.NOTIFICATION_ID, notification_id)
        }

        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 1, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.addAction(android.R.drawable.ic_delete, "Cancel", pendingIntent)

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    fun sendRetryNotification(title: String, text: String, progress: Int, context: Context, gotoActivity: Class<*>, notification_id: Int) {

        val mBuilder = NotificationCompat.Builder(this@EpisodeActivity, ConstantValues.CHANNEL_ID)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setOngoing(true)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setProgress(100, progress, false)
                .setOnlyAlertOnce(true)

        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)
        resultIntent.putExtra(ConstantValues.URL_INTENT, url)
        resultIntent.putExtra(ConstantValues.NAME_INTENT, name)
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

        val retryIntent = Intent(applicationContext, RetryReceiver::class.java).apply {
            action = "fun.com.crestron.RETRY"
            putExtra(ConstantValues.NOTIFICATION_ID, notification_id)
        }
        val pendingIntent = PendingIntent.getBroadcast(applicationContext, 1, retryIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.addAction(android.R.drawable.ic_delete, "Retry", pendingIntent)

        val cancelIntent = Intent(applicationContext, NotificationBroadcastReceiver::class.java).apply {
            action = "fun.com.crestron.CANCEL_DOWNLOAD"
            putExtra(ConstantValues.NOTIFICATION_ID, notification_id)
        }

        val pendingIntent1 = PendingIntent.getBroadcast(applicationContext, 1, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.addAction(android.R.drawable.ic_delete, "Cancel", pendingIntent1)

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    fun sendNotification(context: Context, smallIconId: Int, title: String, message: String, channel_id: String, gotoActivity: Class<*>, notification_id: Int, vararg dataToPass: KeyAndValue) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setContentText(message)
                .setChannelId(channel_id)
        // Creates an explicit intent for an Activity in your app

        //val resultIntent = Intent(Intent.ACTION_VIEW)

        //intent.setDataAndType(Uri.parse(dataToPass.filter { it.key=="url" }[0].value), "video/*")

        val resultIntent = Intent(context, gotoActivity)

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
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    data class KeyAndValue(val key: String, val value: String)

    interface EpisodeAction {
        fun hit(name: String, url: String) {
            Loged.wtf("$name: $url")
        }
    }

}
