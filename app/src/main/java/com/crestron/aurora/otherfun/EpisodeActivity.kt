package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abdeveloper.library.MultiSelectDialog
import com.abdeveloper.library.MultiSelectModel
import com.crashlytics.android.Crashlytics
import com.crestron.aurora.ChoiceActivity
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.EpisodeInfo
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.utilities.KUtility
import com.crestron.aurora.utilities.Utility
import com.crestron.aurora.views.DownloadsWidget
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import com.like.LikeButton
import com.like.OnLikeListener
import com.squareup.picasso.Picasso
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.android.synthetic.main.activity_episode.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import programmer.box.utilityhelper.UtilNotification
import java.net.SocketTimeoutException
import java.util.*


class EpisodeActivity : AppCompatActivity() {

    private val listOfUrls = arrayListOf<String>()
    private val listOfNames = arrayListOf<String>()
    private val listOfEpisodes = arrayListOf<EpisodeInfo>()

    lateinit var mNotificationManager: NotificationManager
    lateinit var url: String
    lateinit var name: String

    private var backChoice = false

    private fun nameUrl(s: String = ""): String {
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

        val show = ShowDatabase.getDatabase(this@EpisodeActivity)

        val hud = KProgressHUD.create(this@EpisodeActivity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Loading")
                .setDetailsLabel("Loading Episodes")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .setCancellable(false)

        mNotificationManager = this@EpisodeActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        UtilNotification.createNotificationChannel(this@EpisodeActivity, ConstantValues.CHANNEL_NAME, ConstantValues.CHANNEL_DES, ConstantValues.CHANNEL_ID)
        UtilNotification.createNotificationGroup(this@EpisodeActivity, ConstantValues.GROUP_ID, ConstantValues.GROUP_NAME)
        //}

        handleIntent(intent)

        download_info.linksClickable = true
        download_info.text = nameUrl()
        runOnUiThread {
            GlobalScope.launch {
                if (intent.hasExtra("is_favorited")) {
                    fav_episode.isLiked = intent.getBooleanExtra("is_favorited", false)
                } else {
                    fav_episode.isLiked = show.showDao().isUrlInDatabase(url) > 0
                }
            }
        }

        val fetching = FetchingUtils(this, object : FetchingUtils.FetchAction {

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                super.onStarted(download, downloadBlocks, totalBlocks)
                //mNotificationManager.cancelAll()
                progressBar2.max = 100
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
            }

            override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
                super.onQueued(download, waitingOnNetwork)
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
            }

            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
                /*val progress = "%.2f".format(FetchingUtils.getProgress(download.downloaded, download.total))
                val info = "$progress% " +
                        "at ${FetchingUtils.getDownloadSpeedString(downloadedBytesPerSecond)} " +
                        "with ${FetchingUtils.getETAString(etaInMilliSeconds)}"*/
                runOnUiThread {
                    progressBar2.setProgress(download.progress, true)
                    //download_info.text = nameUrl(info)
                }

                //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                /*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                        info,
                        download.progress,
                        this@EpisodeActivity,
                        DownloadViewerActivity::class.java,
                        download.id)*/
                //}
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
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
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
            }

            override fun onPaused(download: Download) {
                super.onPaused(download)
                stats = StatusPlay.PAUSE
                /*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                        "Paused",
                        download.progress,
                        this@EpisodeActivity,
                        DownloadViewerActivity::class.java,
                        download.id)*/
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
            }

            override fun onResumed(download: Download) {
                super.onResumed(download)
                stats = StatusPlay.PLAY
                /*sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                        "Resumed",
                        download.progress,
                        this@EpisodeActivity,
                        DownloadViewerActivity::class.java,
                        download.id)*/
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
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
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
            }

            override fun onError(download: Download, error: Error, throwable: Throwable?) {
                super.onError(download, error, throwable)
                Crashlytics.log("${error.throwable?.message}")
                if (defaultSharedPreferences.getBoolean(ConstantValues.AUTO_RETRY, false))
                    FetchingUtils.retry(download)
                /*else
                    sendRetryNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                            "An error has occurred",
                            download.progress,
                            this@EpisodeActivity,
                            DownloadViewerActivity::class.java,
                            download.id)*/
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
            }

            override fun onCompleted(download: Download) {
                super.onCompleted(download)
                Toast.makeText(this@EpisodeActivity, "Finished Downloading", Toast.LENGTH_LONG).show()
                progressBar2.progress = 0
                ChoiceActivity.downloadCast(this@EpisodeActivity, ChoiceActivity.BroadCastInfo.KVObject("view_download_item_count", "1"))
                ViewVideosActivity.videoCast(this@EpisodeActivity)
                if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                    DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
                FetchingUtils.retryAll()
                mNotificationManager.cancel(download.id)
                //UtilNotification.sendNotification(this@EpisodeActivity, android.R.mipmap.sym_def_app_icon, download.file.substring(download.file.lastIndexOf("/") + 1), "All Finished!", "showDownload", ChooseActivity::class.java, download.id)
                /*sendNotification(this@EpisodeActivity,
                        android.R.mipmap.sym_def_app_icon,
                        download.file.substring(download.file.lastIndexOf("/") + 1),
                        "All Finished!",
                        ConstantValues.CHANNEL_ID,
                        EpisodeActivity::class.java,
                        download.id,
                        KeyAndValue(ConstantValues.URL_INTENT, url),
                        KeyAndValue(ConstantValues.NAME_INTENT, name))*/
                if (defaultSharedPreferences.getBoolean("useNotifications", true)) {
                    sendNotification(this@EpisodeActivity,
                            android.R.mipmap.sym_def_app_icon,
                            download.file.substring(download.file.lastIndexOf("/") + 1),
                            "All Finished!",
                            ConstantValues.CHANNEL_ID,
                            StartVideoFromNotificationActivity::class.java,
                            download.id,
                            KeyAndValue("video_path", download.file),
                            KeyAndValue("video_name", name))
                    sendGroupNotification(this@EpisodeActivity,
                            android.R.mipmap.sym_def_app_icon,
                            "Finished Downloads",
                            ConstantValues.CHANNEL_ID,
                            ViewVideosActivity::class.java)
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

        fun getList() = GlobalScope.launch {
            runOnUiThread {
                hud.show()
            }
            val epApi = try {
                EpisodeApi(ShowInfo(name, url))
            } catch (e: SocketTimeoutException) {
                null
            }

            if (epApi != null)
                listOfEpisodes.addAll(epApi.episodeList)

            /*if (name == "fun.getting") {
                name = epApi?.name ?: name
            }*/
            name = epApi?.name ?: name

            runOnUiThread {

                download_info.text = if (epApi != null) nameUrl(epApi.description) else "An error has occured"

                if (epApi != null) {
                    try {
                        Picasso.get().load(epApi.image)
                                .error(R.drawable.apk)
                                .resize((600 * .6).toInt(), (800 * .6).toInt())
                                .into(cover_image)
                    } catch (e: java.lang.IllegalArgumentException) {
                        Picasso.get().load(android.R.drawable.stat_notify_error).resize((600 * .6).toInt(), (800 * .6).toInt()).into(cover_image)
                    }
                } else {
                    Picasso.get().load(android.R.drawable.stat_notify_error).resize((600 * .6).toInt(), (800 * .6).toInt()).into(cover_image)
                }

                val slideOrButton = defaultSharedPreferences.getBoolean(ConstantValues.SLIDE_OR_BUTTON, true)
                val downloadOrStream = defaultSharedPreferences.getBoolean(ConstantValues.DOWNLOAD_OR_STREAM, true)
                Loged.i("$downloadOrStream")

                episode_list.adapter = EpisodeAdapter(listOfEpisodes, name, context = this@EpisodeActivity, slideOrButton = slideOrButton, downloadOrStream = downloadOrStream, action = object : EpisodeAction {
                    override fun hit(name: String, url: String) {
                        super.hit(name, url)
                        FetchingUtils.downloadCount++
                        GlobalScope.launch {
                            if (downloadOrStream) {
                                runOnUiThread {
                                    Toast.makeText(this@EpisodeActivity, "Downloading...", Toast.LENGTH_SHORT).show()
                                }
                                fetching.getVideo(EpisodeInfo(name, url), if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL,
                                        KeyAndValue(ConstantValues.URL_INTENT, this@EpisodeActivity.url),
                                        KeyAndValue(ConstantValues.NAME_INTENT, this@EpisodeActivity.name))
                            } else {
                                val epInfo = EpisodeInfo(name, url)
                                startActivity(Intent(this@EpisodeActivity, VideoPlayerActivity::class.java).apply {
                                    putExtra("video_path", epInfo.getVideoLink())
                                    putExtra("video_name", name)
                                    putExtra("download_or_stream", false)
                                })
                            }
                        }
                    }

                    override fun hit(info: EpisodeInfo) {
                        super.hit(name, url)
                        /*FetchingUtils.downloadCount++
                        Toast.makeText(this@EpisodeActivity, "Downloading...", Toast.LENGTH_SHORT).show()
                        GlobalScope.launch {
                            fetching.getVideo(info, if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL,
                                    KeyAndValue(ConstantValues.URL_INTENT, this@EpisodeActivity.url),
                                    KeyAndValue(ConstantValues.NAME_INTENT, this@EpisodeActivity.name))
                        }*/
                        GlobalScope.launch {
                            if (reverse_order.isChecked) {
                                runOnUiThread {
                                    Toast.makeText(this@EpisodeActivity, "Getting Info...", Toast.LENGTH_SHORT).show()
                                }
                                startActivity(Intent(this@EpisodeActivity, CastingActivity::class.java).apply {
                                    putExtra("cast_info", Gson().toJson(CastVideoInfo(video_name = name, video_url = info.getVideoLink(), video_image = epApi!!.image, video_des = epApi.description)))
                                })
                            } else {
                                if (downloadOrStream) {
                                    runOnUiThread {
                                        Toast.makeText(this@EpisodeActivity, "Downloading...", Toast.LENGTH_SHORT).show()
                                    }
                                    fetching.getVideo(info, if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL,
                                            KeyAndValue(ConstantValues.URL_INTENT, this@EpisodeActivity.url),
                                            KeyAndValue(ConstantValues.NAME_INTENT, this@EpisodeActivity.name))
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@EpisodeActivity, "Getting Info...", Toast.LENGTH_SHORT).show()
                                    }
                                    startActivity(Intent(this@EpisodeActivity, VideoPlayerActivity::class.java).apply {
                                        putExtra("video_path", info.getVideoLink())
                                        putExtra("video_name", name)
                                        putExtra("download_or_stream", false)
                                    })
                                }
                            }
                        }
                    }
                })
                Loged.d("${(episode_list.adapter!! as EpisodeAdapter).itemCount}")
            }
            runOnUiThread {
                episode_refresh.isRefreshing = false
                hud.dismiss()
            }
        }

        if (Utility.isNetwork(this))
            getList()
        else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("No Internet")
            builder.setMessage("Please get connected to internet to use this section")
            // Add the buttons
            builder.setPositiveButton("Okay") { _, _ ->
                finish()
            }
            builder.setCancelable(false)
            val dialog = builder.create()
            dialog.show()
        }

        episode_refresh.setOnRefreshListener {
            listOfUrls.clear()
            listOfNames.clear()
            listOfEpisodes.clear()
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            getList()
        }

        episode_refresh.isEnabled = false

        /*reverse_order.setOnCheckedChangeListener { _, b ->
            if (b) {
                //Toast.makeText(this@EpisodeActivity, "Will only download on Wifi", Toast.LENGTH_LONG).show()
            } else {
                //Toast.makeText(this@EpisodeActivity, "Will download using Mobile Data or Wifi", Toast.LENGTH_LONG).show()
            }

            listOfNames.reverse()
            listOfUrls.reverse()
            listOfEpisodes.reverse()

            runOnUiThread {
                //val slideOrButton = defaultSharedPreferences.getBoolean(ConstantValues.SLIDE_OR_BUTTON, true)
                (episode_list.adapter as EpisodeAdapter).items.reverse()
                (episode_list.adapter as EpisodeAdapter).notifyItemRangeChanged(0, episode_list.adapter!!.itemCount)

                *//*= EpisodeAdapter(listOfEpisodes, name, reverse = b, context = this@EpisodeActivity, slideOrButton = slideOrButton, downloadOrStream = true, action = object : EpisodeAction {
                    override fun hit(name: String, url: String) {
                        super.hit(name, url)
                        FetchingUtils.downloadCount++
                        Toast.makeText(this@EpisodeActivity, "Downloading...", Toast.LENGTH_SHORT).show()
                        GlobalScope.launch {
                            fetching.getVideo(EpisodeInfo(name, url), if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL,
                                    KeyAndValue(ConstantValues.URL_INTENT, this@EpisodeActivity.url),
                                    KeyAndValue(ConstantValues.NAME_INTENT, this@EpisodeActivity.name))
                        }
                    }

                    override fun hit(info: EpisodeInfo) {
                        super.hit(name, url)
                        FetchingUtils.downloadCount++
                        Toast.makeText(this@EpisodeActivity, "Downloading...", Toast.LENGTH_SHORT).show()
                        GlobalScope.launch {
                            fetching.getVideo(info, if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL,
                                    KeyAndValue(ConstantValues.URL_INTENT, this@EpisodeActivity.url),
                                    KeyAndValue(ConstantValues.NAME_INTENT, this@EpisodeActivity.name))
                        }
                    }
                })*//*
                Loged.d("${(episode_list.adapter!! as EpisodeAdapter).itemCount}")
            }

        }*/

        goto_downloads.setOnClickListener {
            startActivity(Intent(this@EpisodeActivity, DownloadViewerActivity::class.java))
        }

        batch_download.setOnClickListener {
            val multiSelectDialog = MultiSelectDialog()
                    .title("Select the Episodes to download") //setting title for dialog
                    .titleSize(25f)
                    .positiveText("Done")
                    .negativeText("Cancel")
                    .setMinSelectionLimit(0) //you can set minimum checkbox selection limit (Optional)
                    .setMaxSelectionLimit(listOfEpisodes.size) //you can set maximum checkbox selection limit (Optional)
                    .multiSelectList(ArrayList<MultiSelectModel>().apply {
                        for (i in 0 until listOfEpisodes.size) {
                            add(MultiSelectModel(i, listOfEpisodes[i].name))
                        }
                    }) // the multi select model list with ids and name
                    .onSubmit(object : MultiSelectDialog.SubmitCallbackListener {
                        override fun onSelected(selectedIds: ArrayList<Int>?, selectedNames: ArrayList<String>?, dataString: String?) {

                            val urlList = arrayListOf<EpisodeInfo>().apply {
                                for (i in 0 until selectedIds!!.size) {
                                    /*Toast.makeText(this@EpisodeActivity, "Selected Ids : " + selectedIds[i] + "\n" +
                                            "Selected Names : " + selectedNames!![i] + "\n" +
                                            "DataString : " + dataString, Toast.LENGTH_SHORT).show()*/
                                    Loged.e("Selected Ids : " + selectedIds[i] + "\n" +
                                            "Selected Names : " + selectedNames!![i] + "\n" +
                                            "DataString : " + dataString)
                                    add(listOfEpisodes[selectedIds[i]])
                                }
                            }

                            FetchingUtils.downloadCount += urlList.size

                            GlobalScope.launch {
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

        /*fav_episode.setOnCheckedChangeListener { _, b ->

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
        }*/
        fav_episode.setOnLikeListener(object : OnLikeListener {
            override fun liked(p0: LikeButton?) {
                liked(p0!!.isLiked)
            }

            override fun unLiked(p0: LikeButton?) {
                liked(p0!!.isLiked)
            }

            fun liked(like: Boolean) {
                GlobalScope.launch {
                    if (like) {
                        show.showDao().insert(Show(url, name, listOfEpisodes.size))
                    } else {
                        show.showDao().deleteShow(name)
                    }
                }
            }

        })

        //share_button.visibility = View.GONE

        share_button.setOnClickListener {
            shareEmail()
        }

    }

    private fun shareEmail() {

        val links = url.replace("www.", "fun.")

        /*val text = "<a href=\"$links\">Check out $name</a>"

        val thisApp = ""

        val shareIntent = ShareCompat.IntentBuilder.from(this@EpisodeActivity)
                .setType("text/html")
                .setHtmlText("$text<br><br>$links<br><br>$thisApp")
                .setSubject("Definitely watch $name")
                .setChooserTitle("Share $name")
                .intent

        startActivity(shareIntent)*/

        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, links)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(sendIntent, "Share $name"))

    }

    override fun onBackPressed() {
        if (backChoice) {
            intent.putExtra("url_string", url)
            intent.putExtra("is_favorited", fav_episode.isLiked)
            setResult(111, intent)
            super.onBackPressed()
            supportFinishAfterTransition()
        } else {
            val intent = Intent(this@EpisodeActivity, ChoiceActivity::class.java)
            startActivity(intent)
            //finish()
            supportFinishAfterTransition()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent(intent!!)
    }

    private fun handleIntent(intent: Intent) {

        val action = intent.action
        val data = intent.data

        if (Intent.ACTION_VIEW == action && data != null) {
            backChoice = false
            val id = data.lastPathSegment
            //val sourceId = data.pathSegments[0]
            //val titleId = data.pathSegments[1]
            for (s in data.pathSegments) {
                Loged.e(s)
            }
            Loged.i("$data")
            url = data.toString().replace("fun.", "www.")
            name = "fun.getting"//toTitleCase(id!!.replace("-", " ").removeSuffix("online"))
            Loged.d("action: $action | data: $data | id: $id")
        } else {
            backChoice = true
            url = intent.getStringExtra(ConstantValues.URL_INTENT)!!
            name = intent.getStringExtra(ConstantValues.NAME_INTENT)!!
        }
        KUtility.removeItemFromNotiJsonList(url)
    }

    fun sendNotification(context: Context, smallIconId: Int, title: String, message: String, channel_id: String, gotoActivity: Class<*>, notification_id: Int, vararg dataToPass: KeyAndValue) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setContentText(message)
                .setChannelId(channel_id)
                .setGroup("downloaded_group")
                .setAutoCancel(true)
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
                notification_id * 2,
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        mNotificationManager.notify(notification_id * 2, mBuilder.build())
    }

    private fun sendGroupNotification(context: Context, smallIconId: Int, title: String, channel_id: String, gotoActivity: Class<*>) {

        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, channel_id)
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setChannelId(channel_id)
                .setGroupSummary(true)
                .setGroup("downloaded_group")
                .setAutoCancel(true)
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)

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
        mNotificationManager.notify(99, mBuilder.build())
    }

    data class KeyAndValue(val key: String, val value: String)

    interface EpisodeAction {
        fun hit(name: String, url: String) {
            Loged.wtf("$name: $url")
        }

        fun hit(info: EpisodeInfo) {
            Loged.wtf("$info")
        }
    }

}
