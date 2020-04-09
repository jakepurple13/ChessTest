package com.crestron.aurora.otherfun

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.crestron.aurora.R
import com.kaopiz.kprogresshud.KProgressHUD
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.programmerbox.dragswipeex.plusAssign
import com.programmersbox.flowutils.RecyclerViewScroll
import com.programmersbox.flowutils.clicks
import com.programmersbox.flowutils.collectOnUi
import com.programmersbox.flowutils.scrollReached
import com.programmersbox.gsonutils.putExtra
import kotlinx.android.synthetic.main.activity_super_recent_anime.*
import kotlinx.android.synthetic.main.super_recent_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.util.regex.Pattern

class SuperRecentAnimeActivity : AppCompatActivity() {

    private val adapter = RecentAnimeAdapter(arrayListOf())
    private var pageNumber = 1
    private val api = AnimeShowApi()
    private lateinit var hud: KProgressHUD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_super_recent_anime)

        hud = KProgressHUD.create(this@SuperRecentAnimeActivity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Loading")
                .setDetailsLabel("Loading Shows")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .setCancellable(false)

        superRecentAnimeRV.adapter = adapter
        superRecentAnimeRV
                .scrollReached()
                .collectOnUi {
                    if (it == RecyclerViewScroll.END) loadNewAnime()
                }

        loadNewAnime()
    }

    private fun loadNewAnime() {
        hud.show()
        GlobalScope.launch {
            try {
                val info = withContext(Dispatchers.Default) { api.parseRecentSubOrDub(pageNumber) }
                runOnUiThread { adapter += info }
                pageNumber++
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                runOnUiThread { hud.dismiss() }
            }
        }
    }

    inner class RecentAnimeAdapter(dataList: ArrayList<AnimeShowApi.AnimeMetaModel>) : DragSwipeAdapter<AnimeShowApi.AnimeMetaModel, RecentHolder>(dataList) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentHolder =
                RecentHolder(layoutInflater.inflate(R.layout.super_recent_item, parent, false))

        override fun onBindViewHolder(holder: RecentHolder, position: Int) {
            val item = list[position]
            Glide.with(this@SuperRecentAnimeActivity)
                    .load(item.imageUrl)
                    .error(R.drawable.b1fv)
                    .override((600 * .6).toInt(), (800 * .6).toInt())
                    .into(holder.image)
            holder.title.text = item.title
            holder.itemView
                    .clicks()
                    .collectOnUi {
                        startActivity(Intent(this@SuperRecentAnimeActivity, SuperRecentAnimeEpisodeActivity::class.java).apply {
                            putExtra("animeInfo", item)
                        })
                    }
        }

    }

    class RecentHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image: ImageView = itemView.animeImage
        val title: TextView = itemView.animeTitle
    }

}

class AnimeShowApi {

    private val episodeLoadUrl = { anime: AnimeInfoModel ->
        "https://ajax.gogocdn.net/ajax/load-list-episode?ep_start=0&ep_end=${anime.endEpisode}&id=${anime.id}&default_ep=0&alias=${anime.alias}"
    }
    private val baseUrl = "https://www.gogoanime.io"
    private val urlLink get() = "https://ajax.gogocdn.net/ajax/page-recent-release.html?page="

    data class AnimeMetaModel(
            var ID: Int = 0,
            //var typeValue: Int? = null,
            var imageUrl: String = "",
            var categoryUrl: String? = null,
            var episodeUrl: String? = null,
            var title: String = "",
            var episodeNumber: String? = null,
            //var genreList: List<GenreModel>? =null,
            var timestamp: Long = System.currentTimeMillis(),
            var insertionOrder: Int = -1,
            var releasedDate: String? = null
    )

    fun parseRecentSubOrDub(pageNumber: Int): MutableList<AnimeMetaModel> = Jsoup.connect("$urlLink$pageNumber").get()
            .getElementsByClass("items").first().select("li")?.map { anime ->
                val animeInfo = anime.getElementsByClass("name").first().select("a")
                val title = animeInfo.attr("title")
                val episodeUrl = animeInfo.attr("href")
                val episodeNumber = anime.getElementsByClass("episode").first().text()
                val animeImageInfo = anime.selectFirst("a")
                val imageUrl = animeImageInfo.select("img").first().absUrl("src")
                AnimeMetaModel(
                        ID = title.hashCode(),
                        title = title,
                        episodeNumber = episodeNumber,
                        episodeUrl = episodeUrl,
                        categoryUrl = getCategoryUrl(imageUrl),
                        imageUrl = imageUrl
                )
            }?.toMutableList() ?: mutableListOf()

    data class AnimeInfoModel(
            var id: String,
            var animeTitle: String,
            var imageUrl: String,
            var type: String,
            var releasedTime: String,
            var status: String,
            //var genre: ArrayList<GenreModel>,
            var plotSummary: String,
            var alias: String,
            var endEpisode: String
    )

    fun parseAnimeInfo(meta: AnimeMetaModel): AnimeInfoModel {
        val document = Jsoup.connect("$baseUrl${meta.categoryUrl}").get()
        val animeInfo = document.getElementsByClass("anime_info_body_bg")
        val animeUrl = animeInfo.select("img").first().absUrl("src")
        val animeTitle = animeInfo.select("h1").first().text()
        val lists = document.getElementsByClass("type")
        lateinit var type: String
        lateinit var releaseTime: String
        lateinit var status: String
        lateinit var plotSummary: String
        //val genre: ArrayList<GenreModel> = ArrayList()
        lists.forEachIndexed { index, element ->
            when (index) {
                0 -> type = element.text()
                1 -> plotSummary = element.text()
                //2-> genre.addAll(getGenreList(element.select("a")))
                3 -> releaseTime = element.text()
                4 -> status = element.text()
            }
        }
        val episodeInfo = document.getElementById("episode_page")
        val episodeList = episodeInfo.select("a").last()
        val endEpisode = episodeList.attr("ep_end")
        val alias = document.getElementById("alias_anime").attr("value")
        val id = document.getElementById("movie_id").attr("value")
        return AnimeInfoModel(
                id = id,
                animeTitle = animeTitle,
                imageUrl = animeUrl,
                type = formatInfoValues(type),
                releasedTime = formatInfoValues(releaseTime),
                status = formatInfoValues(status),
                //genre = genre,
                plotSummary = formatInfoValues(plotSummary).trim(),
                alias = alias,
                endEpisode = endEpisode
        )

    }

    data class EpisodeInfoModel(
            var vidcdnUrl: String? = null,
            var nextEpisodeUrl: String? = null,
            var previousEpisodeUrl: String? = null,
            var downloadUrl: String? = null
    )

    fun parseMediaUrl(model: EpisodeModel): EpisodeInfoModel {
        val mediaUrl: String?
        val document = Jsoup.connect("$baseUrl${model.episodeurl}").get()
        val info = document.getElementsByClass("anime").first().select("a")
        mediaUrl = info.attr("data-video").toString()
        val nextEpisodeUrl = document.getElementsByClass("anime_video_body_episodes_r")?.select("a")?.first()?.attr("href")
        val previousEpisodeUrl = document.getElementsByClass("anime_video_body_episodes_l")?.select("a")?.first()?.attr("href")
        val downloadEpisodeUrl = document.select("a:has(span.btndownload)")?.attr("abs:href")
        return EpisodeInfoModel(
                nextEpisodeUrl = nextEpisodeUrl,
                previousEpisodeUrl = previousEpisodeUrl,
                vidcdnUrl = mediaUrl,
                downloadUrl = downloadEpisodeUrl
        )
    }

    private val USER_AGENT = "user-agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36"
    private val ORIGIN = "origin: https://www16.gogoanime.io"
    private val REFERER = "referer: https://www16.gogoanime.io/"

    fun parseM3U8Url(response: EpisodeInfoModel): String? {
        var m3u8Url: String? = null
        val document = Jsoup.connect("https:${response.vidcdnUrl}")
                .referrer("https://www16.gogoanime.io/")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36")
                .headers(mutableMapOf("Origin" to "https://vidstreaming.io"))
                .get()
        println(document)
        val info = document.getElementsByClass("videocontent")
        println(info)
        val pattern = Pattern.compile("(http|https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?")
        val matcher = pattern.matcher(info.html())
        return try {
            while (matcher.find()) {
                if (matcher.group(0)!!.contains("m3u8")) {
                    m3u8Url = matcher.group(0)
                }
                break
            }
            m3u8Url
        } catch (npe: NullPointerException) {
            m3u8Url
        }

    }

    data class EpisodeModel(
            var episodeNumber: String,
            var episodeurl: String,
            var episodeType: String
    )

    fun fetchEpisodeList(anime: AnimeInfoModel): ArrayList<EpisodeModel> {
        val episodeList = ArrayList<EpisodeModel>()
        val document = Jsoup.connect(episodeLoadUrl(anime)).get()
        val lists = document.select("li")
        lists.forEach {
            val episodeUrl = it.select("a").first().attr("href").trim()
            val episodeNumber = it.getElementsByClass("name").first().text()
            val episodeType = it.getElementsByClass("cate").first().text()
            episodeList.add(
                    EpisodeModel(
                            episodeNumber = episodeNumber,
                            episodeType = episodeType,
                            episodeurl = episodeUrl
                    )
            )
        }
        return episodeList
    }

    data class AnimeShowVideoInfo(val fileName: String, val size: String, val duration: String, val fileUrl: String)

    fun fetchAnimeVideoInfo(episodeModel: EpisodeInfoModel): List<AnimeShowVideoInfo> {
        val f = Jsoup.connect(episodeModel.downloadUrl).get()
        return f.select("div.dowload").select("a").map {
            AnimeShowVideoInfo(
                    fileName = f.select("span#title").text(),
                    size = f.select("span#filesize").text(),
                    duration = f.select("span#duration").first().text(),
                    fileUrl = it.attr("abs:href")
            )
        }
    }

    private fun getCategoryUrl(url: String): String {
        var categoryUrl = url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'))
        categoryUrl = "/category/$categoryUrl"
        return categoryUrl
    }

    private fun formatInfoValues(infoValue: String): String = infoValue.substring(infoValue.indexOf(':') + 1, infoValue.length)
}