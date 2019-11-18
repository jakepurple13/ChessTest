package com.crestron.aurora.showapi

import com.google.gson.Gson
import okhttp3.OkHttpClient
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

enum class Source(val link: String, val recent: Boolean = false, var movie: Boolean = false) {
    //ANIME("http://www.animeplus.tv/anime-list"),
    ANIME("https://www.gogoanime1.com/home/anime-list"),
    CARTOON("http://www.animetoon.org/cartoon"),
    DUBBED("http://www.animetoon.org/dubbed-anime"),
    //ANIME_MOVIES("http://www.animeplus.tv/anime-movies"),
    ANIME_MOVIES("https://www.gogoanime1.com/home/anime-list", movie = true),
    CARTOON_MOVIES("http://www.animetoon.org/movies", movie = true),
    //RECENT_ANIME("http://www.animeplus.tv/anime-updates", true),
    RECENT_ANIME("https://www.gogoanime1.com/home/latest-episodes", true),
    RECENT_CARTOON("http://www.animetoon.org/updates", true),
    LIVE_ACTION("https://www.putlocker.fyi/a-z-shows/"),
    RECENT_LIVE_ACTION("https://www1.putlocker.fyi/recent-episodes/", true);

    companion object SourceUrl {
        fun getSourceFromUrl(url: String): Source {
            return when (url) {
                ANIME.link -> ANIME
                CARTOON.link -> CARTOON
                DUBBED.link -> DUBBED
                ANIME_MOVIES.link -> ANIME_MOVIES
                CARTOON_MOVIES.link -> CARTOON_MOVIES
                RECENT_ANIME.link -> RECENT_ANIME
                RECENT_CARTOON.link -> RECENT_CARTOON
                LIVE_ACTION.link -> LIVE_ACTION
                RECENT_LIVE_ACTION.link -> RECENT_LIVE_ACTION
                else -> ANIME
            }
        }
    }
}

internal enum class ShowSource {
    GOGOANIME, ANIMETOON, PUTLOCKER, NONE;

    companion object {
        fun getSourceType(url: String) = when {
            url.contains("gogoanime") -> GOGOANIME
            url.contains("animetoon") -> ANIMETOON
            url.contains("putlocker") -> PUTLOCKER
            else -> NONE
        }
    }
}

/**
 * Info about the show, name and url
 */
open class ShowInfo(val name: String, val url: String) {
    override fun toString(): String = "$name: $url"
}

/**
 * The actual api!
 */
class ShowApi(private val source: Source) {
    companion object {
        fun getAll() = getSources(Source.ANIME, Source.CARTOON, Source.CARTOON_MOVIES, Source.DUBBED, Source.LIVE_ACTION)
        fun getAllRecent(): List<ShowInfo> = getSources(Source.RECENT_ANIME, Source.RECENT_CARTOON, Source.RECENT_LIVE_ACTION)
        fun getSources(vararg source: Source): List<ShowInfo> = source.map { ShowApi(it).showInfoList }.flatten()
    }

    private val doc: Document = Jsoup.connect(source.link).get()
    /**
     * returns a list of the show's from the wanted source
     */
    val showInfoList: List<ShowInfo> = if (source.recent) getRecentList() else getList()

    private fun toShowInfo(element: Element) = ShowInfo(element.text(), element.attr("abs:href"))

    private fun getList(): List<ShowInfo> = when (ShowSource.getSourceType(source.link)) {
        ShowSource.GOGOANIME -> if (source == Source.ANIME_MOVIES || source.movie) gogoAnimeMovies() else gogoAnimeAll()
        ShowSource.PUTLOCKER -> doc.select("a.az_ls_ent").map(this::toShowInfo)
        ShowSource.ANIMETOON -> doc.allElements.select("td").select("a[href^=http]").map(this::toShowInfo)
        ShowSource.NONE -> emptyList()
    }.sortedBy(ShowInfo::name)

    private fun getRecentList(): List<ShowInfo> = when (ShowSource.getSourceType(source.link)) {
        ShowSource.GOGOANIME -> doc.allElements.select("div.dl-item").map {
            val tempUrl = it.select("div.name").select("a[href^=http]").attr("abs:href")
            ShowInfo(it.select("div.name").text(), tempUrl.substring(0, tempUrl.indexOf("/episode")))
        }
        ShowSource.PUTLOCKER -> doc.allElements.select("div.col-6").map {
            val url = it.select("a.thumbnail").attr("abs:href")
            ShowInfo(it.select("span.mov_title").text(), url.substring(0, url.indexOf("season")))
        }
        ShowSource.ANIMETOON -> {
            var listOfStuff = doc.allElements.select("div.left_col").select("table#updates").select("a[href^=http]")
            if (listOfStuff.size == 0) listOfStuff = doc.allElements.select("div.s_left_col").select("table#updates").select("a[href^=http]")
            listOfStuff.map(this::toShowInfo).filter { !it.name.contains("Episode") }
        }
        ShowSource.NONE -> emptyList()
    }

    private fun gogoAnimeMovies(): List<ShowInfo> = gogoAnimeAll().filter { it.name.contains("movie", ignoreCase = true) }
    private fun gogoAnimeAll(): List<ShowInfo> = doc.allElements.select("ul.arrow-list").select("li")
            .map { ShowInfo(it.text(), it.select("a[href^=http]").attr("abs:href")) }
}

/**
 * If you want to get the Show with all the information now rather than passing it into [EpisodeApi] yourself
 */
fun List<ShowInfo>.getEpisodeApi(index: Int): EpisodeApi = EpisodeApi(this[index])

/**
 * Actual Show information
 */
class EpisodeApi(val source: ShowInfo, timeOut: Int = 10000) {
    private var doc: Document = Jsoup.connect(source.url).timeout(timeOut).get()

    /**
     * The name of the Show
     */
    val name: String = when (ShowSource.getSourceType(source.url)) {
        ShowSource.PUTLOCKER -> doc.select("li.breadcrumb-item").last().text()
        ShowSource.GOGOANIME -> doc.select("div.anime-title").text()
        ShowSource.ANIMETOON -> doc.select("div.right_col h1").text()
        ShowSource.NONE -> ""
    }

    /**
     * The url of the image
     */
    val image: String = when (ShowSource.getSourceType(source.url)) {
        ShowSource.PUTLOCKER -> doc.select("div.thumb").select("img[src^=http]")
        ShowSource.GOGOANIME -> doc.select("div.animeDetail-image").select("img[src^=http]")
        ShowSource.ANIMETOON -> doc.select("div.left_col").select("img[src^=http]#series_image")
        ShowSource.NONE -> null
    }?.attr("abs:src") ?: ""

    /**
     * the description
     */
    val description: String = when (ShowSource.getSourceType(source.url)) {
        ShowSource.PUTLOCKER -> try {
            val request = okhttp3.Request.Builder().url("http://www.omdbapi.com/?t=$name&plot=full&apikey=e91b86ee").get().build()
            val response = OkHttpClient().newCall(request).execute()
            val jsonObj = JSONObject(response.body()!!.string())
            "Years Active: ${jsonObj.getString("Year")}\nReleased: ${jsonObj.getString("Released")}\n${jsonObj.getString("Plot")}"
        } catch (e: Exception) {
            var textToReturn = ""
            val para = doc.select(".mov-desc").select("p")
            for (i in para.withIndex()) {
                textToReturn += when (i.index) {
                    1 -> "Release: "
                    2 -> "Genre: "
                    3 -> "Director: "
                    4 -> "Stars: "
                    5 -> "Synopsis: "
                    else -> ""
                } + i.value.text() + "\n"
            }
            textToReturn
        }
        ShowSource.GOGOANIME -> doc.select("p.anime-details").text()
        ShowSource.ANIMETOON -> doc.allElements.select("div#series_details").let { element ->
            if (element.select("span#full_notes").hasText())
                element.select("span#full_notes").text().removeSuffix("less")
            else
                element.select("div:contains(Description:)").select("div").text().let {
                    try {
                        it.substring(it.indexOf("Description: ") + 13, it.indexOf("Category: "))
                    } catch (e: StringIndexOutOfBoundsException) {
                        it
                    }
                }
        }
        ShowSource.NONE -> ""
    }.let { if (it.isNullOrBlank()) "Sorry, an error has occurred" else it }.trim()

    /**
     * The episode list
     */
    val episodeList: List<EpisodeInfo>
        get() = when (ShowSource.getSourceType(source.url)) {
            ShowSource.PUTLOCKER -> doc.select("div.col-lg-12").select("div.row").select("a.btn-episode")
                    .map { EpisodeInfo(it.attr("title"), "https://www.putlocker.fyi/embed-src/${it.attr("data-pid")}") }
            ShowSource.GOGOANIME -> doc.select("ul.check-list").select("li")
                    .map {
                        val urlInfo = it.select("a[href^=http]")
                        val epName = urlInfo.text().let { info -> if (info.contains(name)) info.substring(name.length) else info }.trim()
                        EpisodeInfo(epName, urlInfo.attr("abs:href"))
                    }.distinctBy(EpisodeInfo::name)
            ShowSource.ANIMETOON -> {
                fun getStuff(url: String) = Jsoup.connect(url).get().allElements.select("div#videos").select("a[href^=http]").map { EpisodeInfo(it.text(), it.attr("abs:href")) }
                getStuff(source.url) + doc.allElements.select("ul.pagination").select(" button[href^=http]").map { getStuff(it.attr("abs:href")) }.flatten()
            }
            ShowSource.NONE -> emptyList()
        }

    override fun toString(): String = "$name - ${episodeList.size} eps - $description"
}

/**
 * Actual Episode info, name and url
 */
class EpisodeInfo(name: String, url: String) : ShowInfo(name, url) {

    /**
     * returns a url link to the episodes video
     * # Use for anything but movies
     */
    fun getVideoLink(): String = when (ShowSource.getSourceType(url)) {
        ShowSource.PUTLOCKER -> {
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(getHtml(url))
            if (d.find()) {
                val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(getHtml(d.group(1)!!))
                if (a.find()) "https://verystream.com/gettoken/${a.group(1)!!}?mime=true" else ""
            } else ""
        }
        ShowSource.GOGOANIME -> Jsoup.connect(url).get().select("a[download^=http]").attr("abs:download")
        ShowSource.ANIMETOON -> {
            val matcher = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern().matcher(getHtml(url))
            var list = ""
            if (matcher.find()) list = matcher.group(1)!!
            val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(getHtml(list))
            if (reg.find()) Gson().fromJson(reg.group(1), NormalLink::class.java).normal!!.storage!![0].link!! else ""
        }
        ShowSource.NONE -> ""
    }

    /**
     * returns a url link to the episodes video
     * # Use for movies
     */
    fun getVideoLinks(): List<String> {
        when (ShowSource.getSourceType(url)) {
            ShowSource.PUTLOCKER -> {
                val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(getHtml(url))
                if (d.find()) {
                    val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(getHtml(d.group(1)!!))
                    if (a.find()) {
                        val link = getFinalURL(URL("https://verystream.com/gettoken/${a.group(1)!!}?mime=true"))!!.toExternalForm()
                        return arrayListOf(link)
                    }
                }
                return arrayListOf("N/A")
            }
            ShowSource.GOGOANIME -> return arrayListOf(Jsoup.connect(url).get().select("a[download^=http]").attr("abs:download"))
            ShowSource.ANIMETOON -> {
                val m = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern().matcher(getHtml(url))
                val list = arrayListOf<String>()
                while (m.find()) list.add(m.group(1)!!)
                val regex = "(http|https):\\/\\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\%\\&\\-\\_\\?\\.\\=\\/])+(part[0-9])+.(\\w*)"
                when (val htmlc = if (regex.toRegex().toPattern().matcher(list[0]).find()) list else getHtml(list[0])) {
                    is ArrayList<*> -> {
                        val urlList = arrayListOf<String>()
                        for (info in htmlc) {
                            val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(getHtml(info.toString()))
                            while (reg.find()) urlList.add(Gson().fromJson(reg.group(1), NormalLink::class.java).normal!!.storage!![0].link!!)
                        }
                        return urlList
                    }
                    is String -> {
                        val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(htmlc)
                        while (reg.find()) return arrayListOf(Gson().fromJson(reg.group(1), NormalLink::class.java).normal!!.storage!![0].link!!)
                    }
                }
            }
            ShowSource.NONE -> return emptyList()
        }
        return emptyList()
    }

    /**
     * returns video information
     * this includes link to video and filename
     * # You can use this for anything. This just returns some extra information.
     */
    fun getVideoInfo(): List<Storage> {
        when (ShowSource.getSourceType(url)) {
            ShowSource.PUTLOCKER -> {
                val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(getHtml(url))
                if (d.find()) {
                    val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(getHtml(d.group(1)!!))
                    if (a.find()) {
                        val stor = Storage()
                        stor.link = "https://verystream.com/gettoken/${a.group(1)!!}?mime=true"
                        stor.filename = name
                        stor.quality = "720"
                        stor.source = "PutLocker"
                        stor.sub = "No"
                        return arrayListOf(stor)
                    }
                }
                return arrayListOf()
            }
            ShowSource.GOGOANIME -> {
                val doc = Jsoup.connect(url).get()
                val storage = Storage()
                storage.link = doc.select("a[download^=http]").attr("abs:download")
                val regex = "^[^\\[]+(.*mp4)".toRegex().toPattern().matcher(storage.link!!)
                storage.filename = if (regex.find()) {
                    regex.group(1)!!
                } else {
                    val segments = URI(url).path.split("/")
                    "${segments[2]} $name.mp4"
                }
                storage.source = url
                storage.quality = "Good"
                storage.sub = "Yes"
                return arrayListOf(storage)
            }
            ShowSource.ANIMETOON -> {
                val m = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern().matcher(getHtml(url))
                val list = arrayListOf<String>()
                while (m.find()) list.add(m.group(1)!!)
                val regex = "(http|https):\\/\\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\%\\&\\-\\_\\?\\.\\=\\/])+(part[0-9])+.(\\w*)"
                when (val htmlc = if (regex.toRegex().toPattern().matcher(list[0]).find()) list else getHtml(list[0])) {
                    is ArrayList<*> -> {
                        val urlList = arrayListOf<Storage>()
                        for (info in htmlc) {
                            val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(getHtml(info.toString()))
                            while (reg.find()) urlList.add(Gson().fromJson(reg.group(1), NormalLink::class.java).normal!!.storage!![0])
                        }
                        return urlList
                    }
                    is String -> {
                        val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(htmlc)
                        while (reg.find()) return arrayListOf(Gson().fromJson(reg.group(1), NormalLink::class.java).normal!!.storage!![0])
                    }
                }
            }
            ShowSource.NONE -> return emptyList()
        }
        return arrayListOf()
    }

    @Throws(IOException::class)
    private fun getHtml(url: String): String {
        // Build and set timeout values for the request.
        val connection = URL(url).openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
        connection.addRequestProperty("Accept-Language", "en-US,en;q=0.5")
        connection.addRequestProperty("Referer", "http://thewebsite.com")
        connection.connect()

        // Read and store the result line by line then return the entire string.
        val in1 = connection.getInputStream()
        val reader = BufferedReader(InputStreamReader(in1))
        val html = StringBuilder()
        var line: String? = ""
        while (line != null) {
            line = reader.readLine()
            html.append(line)
        }
        in1.close()

        return html.toString()
    }

    private fun getFinalURL(url: URL): URL? {
        try {
            val con = url.openConnection() as HttpURLConnection
            con.instanceFollowRedirects = false
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0")
            con.addRequestProperty("Accept-Language", "en-US,en;q=0.5")
            con.addRequestProperty("Referer", "http://thewebsite.com")
            con.connect()
            //con.getInputStream();
            val resCode = con.responseCode
            if (resCode == HttpURLConnection.HTTP_SEE_OTHER
                    || resCode == HttpURLConnection.HTTP_MOVED_PERM
                    || resCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                var location = con.getHeaderField("Location")
                if (location.startsWith("/")) {
                    location = url.protocol + "://" + url.host + location
                }
                return getFinalURL(URL(location))
            }
        } catch (e: Exception) {
            println(e.message)
        }
        return url
    }

    override fun toString(): String = "$name: $url"
}

internal class NormalLink {
    var normal: Normal? = null
    override fun toString(): String = "ClassPojo [normal = " + normal!!.toString() + "]"
}

internal class Normal {
    var storage: Array<Storage>? = null
    override fun toString(): String = "ClassPojo [storage = $storage]"
}

class Storage {
    var sub: String? = null
    var source: String? = null
    var link: String? = null
    var quality: String? = null
    var filename: String? = null
    override fun toString(): String = "ClassPojo [sub = $sub, source = $source, link = $link, quality = $quality, filename = $filename]"
}
