package com.crestron.aurora.otherfun

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crestron.aurora.*
import com.crestron.aurora.showapi.EpisodeInfo
import com.crestron.aurora.utilities.getObjectExtra
import com.crestron.aurora.views.DownloadsWidget
import com.ncorti.slidetoact.SlideToActView
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.programmersbox.flowutils.clicks
import com.programmersbox.flowutils.collectOnUi
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Error
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.android.synthetic.main.activity_super_recent_anime_episode.*
import kotlinx.coroutines.*
import org.jetbrains.anko.defaultSharedPreferences


class SuperRecentAnimeEpisodeActivity : AppCompatActivity() {

    private var animeInfo: AnimeShowApi.AnimeInfoModel? = null
    private val api = AnimeShowApi()
    private lateinit var mNotificationManager: NotificationManager
    private val fetching = FetchingUtils(this, object : FetchingUtils.FetchAction {

        override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
            super.onStarted(download, downloadBlocks, totalBlocks)
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            super.onQueued(download, waitingOnNetwork)
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
        }

        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
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
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
        }

        override fun onPaused(download: Download) {
            super.onPaused(download)
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
        }

        override fun onResumed(download: Download) {
            super.onResumed(download)
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
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
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            super.onError(download, error, throwable)
            //Crashlytics.log("${error.throwable?.message}")
            if (defaultSharedPreferences.getBoolean(ConstantValues.AUTO_RETRY, false))
                FetchingUtils.retry(download)
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
        }

        override fun onCompleted(download: Download) {
            super.onCompleted(download)
            Toast.makeText(this@SuperRecentAnimeEpisodeActivity, "Finished Downloading", Toast.LENGTH_LONG).show()
            ChoiceActivity.downloadCast(this@SuperRecentAnimeEpisodeActivity, ChoiceActivity.BroadCastInfo.KVObject("view_download_item_count", "1"))
            ViewVideosActivity.videoCast(this@SuperRecentAnimeEpisodeActivity)
            if (DownloadsWidget.isWidgetActive(this@SuperRecentAnimeEpisodeActivity))
                DownloadsWidget.sendRefreshBroadcast(this@SuperRecentAnimeEpisodeActivity)
            FetchingUtils.retryAll()
            mNotificationManager.cancel(download.id)
        }
    })
    private val adapter = RecentEpisodeAdapter(arrayListOf())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_super_recent_anime_episode)

        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationSetup()

        loadAnime()

        val dividerItemDecoration = DividerItemDecoration(this, (episode_list.layoutManager as LinearLayoutManager).orientation)
        episode_list.addItemDecoration(dividerItemDecoration)

        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
            }
        }
        episode_list.addItemDecoration(ItemOffsetDecoration(20))

        episode_list.adapter = adapter

        goto_downloads
                .clicks()
                .collectOnUi { startActivity(Intent(this@SuperRecentAnimeEpisodeActivity, DownloadViewerActivity::class.java)) }
    }

    private fun notificationSetup() {
        UtilNotification.createNotificationChannel(this@SuperRecentAnimeEpisodeActivity, ConstantValues.CHANNEL_NAME, ConstantValues.CHANNEL_DES, ConstantValues.CHANNEL_ID)
        UtilNotification.createNotificationGroup(this@SuperRecentAnimeEpisodeActivity, ConstantValues.GROUP_ID, ConstantValues.GROUP_NAME)
    }

    private fun loadAnime() {
        GlobalScope.launch {
            animeInfo = withContext(Dispatchers.Default) { intent?.getObjectExtra<AnimeShowApi.AnimeMetaModel>("animeInfo", null)?.let { api.parseAnimeInfo(it) } }

            runOnUiThread {
                Glide.with(this@SuperRecentAnimeEpisodeActivity)
                        .load(animeInfo?.imageUrl)
                        .error(R.drawable.b1fv)
                        .override((600 * .6).toInt(), (800 * .6).toInt())
                        .into(cover_image)

                titleName.text = "${animeInfo?.animeTitle}"
                download_info.text = "${animeInfo?.plotSummary}"
            }

            val list = withContext(Dispatchers.Default) { animeInfo?.let { api.fetchEpisodeList(it) } }
            runOnUiThread { list?.let { adapter.addItems(it) } }
        }
    }

    inner class RecentEpisodeAdapter(list: ArrayList<AnimeShowApi.EpisodeModel>) : DragSwipeAdapter<AnimeShowApi.EpisodeModel, ViewHolderEpisode>(list) {
        override fun onBindViewHolder(holder: ViewHolderEpisode, position: Int) {
            val item = list[position]
            holder.slideToDownload.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    //action here
                    GlobalScope.launch {
                        runOnUiThread { Toast.makeText(this@SuperRecentAnimeEpisodeActivity, "Downloading...", Toast.LENGTH_SHORT).show() }
                        withContext(Dispatchers.Default) {
                            api.fetchAnimeVideoInfo(api.parseMediaUrl(item)).firstOrNull()?.let {
                                fetching.getVideo(EpisodeInfo(it.fileName, it.fileUrl), true, if (reverse_order.isChecked) NetworkType.WIFI_ONLY else NetworkType.ALL)
                            }
                        }
                        delay(500)
                        runOnUiThread { view.resetSlider() }
                    }
                }
            }

            holder.slideToDownload.text = "Download"
            holder.watched.text = item.episodeNumber

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderEpisode =
                ViewHolderEpisode(layoutInflater.inflate(R.layout.episode_info, parent, false))

    }
}