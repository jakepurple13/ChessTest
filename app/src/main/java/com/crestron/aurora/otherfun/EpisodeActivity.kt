package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
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
import com.crestron.aurora.db.Episode
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.firebaseserver.FirebaseDB
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.EpisodeInfo
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.utilities.KUtility
import com.crestron.aurora.utilities.Utility
import com.crestron.aurora.utilities.intersect
import com.crestron.aurora.utilities.otherWise
import com.crestron.aurora.views.DownloadsWidget
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

    lateinit var adapter: EpisodeAdapter

    val fetching = FetchingUtils(this, object : FetchingUtils.FetchAction {

        override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
            super.onStarted(download, downloadBlocks, totalBlocks)
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
            runOnUiThread {
                progressBar2.setProgress(download.progress, true)
            }
            if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
        }

        override fun onCancelled(download: Download) {
            super.onCancelled(download)
            mNotificationManager.cancel(download.id)
            try {
                deleteFile(download.file)
            } catch (e: IllegalArgumentException) {
                Loged.w(e.message!!)
            } catch (e: java.lang.NullPointerException) {
                Loged.w(e.message!!)
            }
            progressBar2.progress = 0
            if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
        }

        override fun onPaused(download: Download) {
            super.onPaused(download)
            stats = StatusPlay.PAUSE
            if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
        }

        override fun onResumed(download: Download) {
            super.onResumed(download)
            stats = StatusPlay.PLAY
            if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
        }

        override fun onDeleted(download: Download) {
            super.onDeleted(download)
            mNotificationManager.cancel(download.id)
            try {
                deleteFile(download.file)
            } catch (e: IllegalArgumentException) {
                Loged.w(e.message!!)
            } catch (e: java.lang.NullPointerException) {
                Loged.w(e.message!!)
            }
            if (DownloadsWidget.isWidgetActive(this@EpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@EpisodeActivity)
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            Crashlytics.log("${error.throwable?.message}")
            if (defaultSharedPreferences.getBoolean(ConstantValues.AUTO_RETRY, false))
                FetchingUtils.retry(download)
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
        }
    })

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

        UtilNotification.createNotificationChannel(this@EpisodeActivity, ConstantValues.CHANNEL_NAME, ConstantValues.CHANNEL_DES, ConstantValues.CHANNEL_ID)
        UtilNotification.createNotificationGroup(this@EpisodeActivity, ConstantValues.GROUP_ID, ConstantValues.GROUP_NAME)

        handleIntent(intent)

        download_info.linksClickable = true
        download_info.text = nameUrl()
        runOnUiThread {
            GlobalScope.launch {
                if (intent.hasExtra("is_favorited")) {
                    fav_episode.isLiked = intent.getBooleanExtra("is_favorited", false)
                } else {
                    fav_episode.isLiked = show.showDao().isUrlInDatabase(url) > 0 || FirebaseDB(this@EpisodeActivity).getShowSync(url) != null
                }
            }
        }

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

                GlobalScope.launch {
                    val des = if (epApi != null) "${epApi.source.url}\n${epApi.description}" else "An error has occurred"
                    runOnUiThread {
                        download_info.text = des
                    }
                }

                titleName.text = epApi?.name

                epApi?.genres?.forEach {
                    genreList.addView(Chip(this@EpisodeActivity).apply {
                        text = it
                        isCheckable = false
                        isClickable = false
                    })
                }

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

                GlobalScope.launch {
                    val slideOrButton = defaultSharedPreferences.getBoolean(ConstantValues.SLIDE_OR_BUTTON, true)
                    val downloadOrStream = defaultSharedPreferences.getBoolean(ConstantValues.DOWNLOAD_OR_STREAM, true)
                    Loged.i("$downloadOrStream")

                    val fire = FirebaseDB.getShowSync(url, this@EpisodeActivity)//FirebaseDB(this@EpisodeActivity).getShowSync(url)
                    Loged.r(fire)

                    adapter = EpisodeAdapter(listOfEpisodes, url, context = this@EpisodeActivity, slideOrButton = slideOrButton, downloadOrStream = downloadOrStream, action = object : EpisodeAction {
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
                    }, firebaseEps = fire)
                    runOnUiThread {
                        episode_list.adapter = adapter
                        Loged.d("${(episode_list.adapter!! as EpisodeAdapter).itemCount}")
                    }
                }
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
            listOfEpisodes.clear()
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            getList()
        }

        episode_refresh.isEnabled = false

        reverse_order.setOnCheckedChangeListener { _, b ->
            adapter.casting = b
            adapter.notifyDataSetChanged()
        }

        goto_downloads.setOnClickListener {
            startActivity(Intent(this@EpisodeActivity, DownloadViewerActivity::class.java))
        }

        batch_download.setOnLongClickListener {
            GlobalScope.launch {
                val eList = show.showDao().getEpisodesByUrl(url).map { it.showUrl }
                        .intersect(FirebaseDB(this@EpisodeActivity).getShowSync(url)?.episodeInfo?.map { it.url } ?: emptyList()) { one, two ->
                            one == two.otherWise { one }
                        }
                val b = BooleanArray(adapter.itemCount) { adapter.items[it].url in eList }
                val m = MaterialAlertDialogBuilder(this@EpisodeActivity)
                        .setMultiChoiceItems(adapter.items.map { it.name }.toTypedArray(), b) { _, which, isChecked ->
                            GlobalScope.launch {
                                try {
                                    if (isChecked) {
                                        Loged.e("Inserted ${adapter.items[which]}")
                                        show.showDao().insertEpisode(Episode(which, url, adapter.items[which].url))
                                        FirebaseDB(this@EpisodeActivity).addEpisode(url, Episode(which, url, adapter.items[which].url))
                                    } else {
                                        Loged.e("Deleted")
                                        show.showDao().deleteEpisode(adapter.items[which].url)
                                        FirebaseDB(this@EpisodeActivity).removeEpisode(url, Episode(which, url, adapter.items[which].url))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    runOnUiThread {
                                        Toast.makeText(this@EpisodeActivity, "Please Favorite Show if you plan on Checking the Episodes", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                        .setTitle("Already Watched")
                        .setPositiveButton("Done") { _, _ -> }

                runOnUiThread {
                    m.show()
                }
            }
            true
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
                        FirebaseDB(this@EpisodeActivity).addShow(Show(url, name))
                    } else {
                        show.showDao().deleteShowByUrl(url)
                        FirebaseDB(this@EpisodeActivity).removeShow(Show(url, name))
                    }
                }
            }

        })

        share_button.setOnClickListener {
            shareEmail()
        }

    }

    private fun shareEmail() {
        val links = url.replace("www.", "fun.")

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
