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
import java.net.URI
import java.net.URL
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

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
    RECENT_LIVE_ACTION("https://www1.putlocker.fyi/recent-episodes/", true),
    LIVE_ACTION_MOVIES("https://www1.putlocker.fyi/a-z-movies/", movie = true);

    companion object SourceUrl {
        fun getSourceFromUrl(url: String): Source = when (url) {
            ANIME.link -> ANIME
            CARTOON.link -> CARTOON
            DUBBED.link -> DUBBED
            ANIME_MOVIES.link -> ANIME_MOVIES
            CARTOON_MOVIES.link -> CARTOON_MOVIES
            RECENT_ANIME.link -> RECENT_ANIME
            RECENT_CARTOON.link -> RECENT_CARTOON
            LIVE_ACTION.link -> LIVE_ACTION
            RECENT_LIVE_ACTION.link -> RECENT_LIVE_ACTION
            LIVE_ACTION_MOVIES.link -> LIVE_ACTION_MOVIES
            else -> ANIME
        }
    }
}

enum class ShowSource {
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
open class ShowInfo(val name: String, val url: String, internal val isMovie: Boolean = false) {
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
        var LOGGING_ENABLED = false
    }

    private var progressListener: (Double) -> Unit = {}

    private val doc: Document = Jsoup.connect(source.link).get()

    /**
     * returns a list of the show's from the wanted source
     */
    val showInfoList: List<ShowInfo> = if (source.recent) getRecentList() else getList()

    fun getShowList(progress: (Double) -> Unit): List<ShowInfo> {
        progressListener = progress
        return if (source.recent) getRecentList() else getList()
    }

    private fun toShowInfo(element: Element) = ShowInfo(element.text(), element.attr("abs:href"))

    private fun getList(): List<ShowInfo> = when (ShowSource.getSourceType(source.link)) {
        ShowSource.GOGOANIME -> if (source == Source.ANIME_MOVIES || source.movie) gogoAnimeMovies() else gogoAnimeAll()
        ShowSource.PUTLOCKER -> if (source == Source.LIVE_ACTION_MOVIES || source.movie) putlockerMovies() else putlockerShows()
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
            .map { ShowInfo(it.text(), it.select("a[href^=http]").attr("abs:href"), source.movie) }

    private fun putlockerShows() = doc.select("a.az_ls_ent").map(this::toShowInfo)
    private fun putlockerMovies(): List<ShowInfo> {
        fun getMovieFromPage(document: Document) = document.allElements.select("div.col-6").map {
            ShowInfo(it.select("span.mov_title").text(), it.select("a.thumbnail").attr("abs:href"), true)
        }
        return doc.allElements.select("ul.pagination-az").select("a.page-link").pmap { p ->
            if (LOGGING_ENABLED) println(p.attr("abs:href"))
            val page = Jsoup.connect(p.attr("abs:href")).get()
            val listPage = page.allElements.select("li.page-item")
            val lastPage = listPage[listPage.size - 2].text().toInt()
            (1..lastPage).pmap {
                progressListener((it.toDouble() / lastPage.toDouble()))
                if (it == 1) getMovieFromPage(page) else getMovieFromPage(
                        Jsoup.connect("${Source.LIVE_ACTION_MOVIES.link}page/$it/${p.attr("abs:href").split("/").last()}").get()
                )
            }.flatten()
        }.flatten()
    }

    private fun <T, R> Iterable<T>.pmap(
            numThreads: Int = Runtime.getRuntime().availableProcessors() - 2,
            exec: ExecutorService = Executors.newFixedThreadPool(numThreads),
            transform: (T) -> R): List<R> {
        // default size is just an inlined version of kotlin.collections.collectionSizeOrDefault
        val defaultSize = if (this is Collection<*>) this.size else 10
        val destination = Collections.synchronizedList(ArrayList<R>(defaultSize))
        forEach { item -> exec.submit { destination.add(transform(item)) } }
        exec.shutdown()
        exec.awaitTermination(1, TimeUnit.DAYS)
        return ArrayList<R>(destination)
    }
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
     * the genres of the show
     */
    val genres: List<String> = when (ShowSource.getSourceType(source.url)) {
        ShowSource.PUTLOCKER -> doc.select(".mov-desc").select("p:contains(Genre)")
        ShowSource.GOGOANIME -> doc.select("div.animeDetail-item:contains(Genres)")
        ShowSource.ANIMETOON -> doc.select("span.red_box")
        ShowSource.NONE -> null
    }?.select("a[href^=http]")?.eachText() ?: emptyList()

    /**
     * the description
     */
    val description: String = when (ShowSource.getSourceType(source.url)) {
        ShowSource.PUTLOCKER -> try {
            val response = OkHttpClient()
                    .newCall(okhttp3.Request.Builder().url("http://www.omdbapi.com/?t=$name&plot=full&apikey=e91b86ee").get().build()).execute()
            val jsonObj = JSONObject(response.body!!.string())
            "Years Active: ${jsonObj.getString("Year")}\nReleased: ${jsonObj.getString("Released")}\n${jsonObj.getString("Plot")}"
        } catch (e: Exception) {
            var textToReturn = ""
            val para = doc.select(".mov-desc").select("p")
            for (i in para.withIndex()) {
                textToReturn += when (i.index) {
                    1 -> "Release: "
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
            ShowSource.PUTLOCKER -> {
                if (source.isMovie) {
                    val info = "var post = \\{\"id\":\"(.*?)\"\\};".toRegex().toPattern().matcher(doc.html())
                    if (info.find()) {
                        val encodingPID = Base64.getEncoder().encodeToString("6${info.group(1)!!.reversed()}".toByteArray())
                        listOf(EpisodeInfo(name, "https://www.putlocker.fyi/embed-src-v2/$encodingPID"))
                    } else emptyList()
                } else {
                    doc.select("div.col-lg-12").select("div.row").select("a.btn-episode")
                            .map {
                                val encodingPID = Base64.getEncoder().encodeToString("6${it.attr("data-pid").reversed()}".toByteArray())
                                EpisodeInfo(it.attr("title"), "https://www.putlocker.fyi/embed-src-v2/$encodingPID")
                            }
                }
            }
            ShowSource.GOGOANIME -> doc.select("ul.check-list").select("li")
                    .map {
                        val urlInfo = it.select("a[href^=http]")
                        val epName = urlInfo.text().let { info -> if (info.contains(name)) info.substring(name.length) else info }.trim()
                        EpisodeInfo(epName, urlInfo.attr("abs:href"))
                    }.distinctBy(EpisodeInfo::name)
            ShowSource.ANIMETOON -> {
                fun getStuff(document: Document) = document.allElements.select("div#videos").select("a[href^=http]").map { EpisodeInfo(it.text(), it.attr("abs:href")) }
                getStuff(doc) + doc.allElements.select("ul.pagination").select(" button[href^=http]").map { getStuff(Jsoup.connect(it.attr("abs:href")).get()) }.flatten()
            }
            ShowSource.NONE -> emptyList()
        }

    override fun toString(): String = "$name - ${episodeList.size} eps - $description"
}

/**
 * Actual Episode info, name and url
 */
class EpisodeInfo(name: String, url: String) : ShowInfo(name, url) {

    private fun getGogoAnime() = Jsoup.connect(url).get().select("a[download^=http]").attr("abs:download")
    private fun getPutLocker(): String = try {
        val doc = Jsoup.connect(url.trim()).get()
        val mix = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().find(doc.toString())!!.groups[1]!!.value
        val doc2 = Jsoup.connect(mix.trim()).get()
        val r = "\\}\\('(.+)',(\\d+),(\\d+),'([^']+)'\\.split\\('\\|'\\)".toRegex().find(doc2.toString())!!
        fun encodeBaseN(num: Int, n: Int): String {
            var num1 = num
            val fullTable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val table = fullTable.substring(0..n)
            if (num1 == 0) return table[0].toString()
            var ret = ""
            while (num1 > 0) {
                ret = (table[num1 % n].toString() + ret)
                num1 = Math.floorDiv(num, n)
            }
            return ret
        }
        val (obfucastedCode, baseTemp, countTemp, symbolsTemp) = r.destructured
        val base = baseTemp.toInt()
        var count = countTemp.toInt()
        val symbols = symbolsTemp.split("|")
        val symbolTable = mutableMapOf<String, String>()
        while (count > 0) {
            count--
            val baseNCount = encodeBaseN(count, base)
            symbolTable[baseNCount] = if (symbols[count].isNotEmpty()) symbols[count] else baseNCount
        }
        val unpacked = "\\b(\\w+)\\b".toRegex().replace(obfucastedCode) { symbolTable[it.groups[0]!!.value].toString() }
        val search = "MDCore\\.v.*?=\"([^\"]+)".toRegex().find(unpacked)!!.groups[1]!!.value
        "https:$search"
    } catch (e: Exception) {
        ""
    }

    /**
     * returns a url link to the episodes video
     * # Use for anything but movies
     */
    fun getVideoLink(): String = when (ShowSource.getSourceType(url)) {
        ShowSource.PUTLOCKER -> getPutLocker()
        ShowSource.GOGOANIME -> getGogoAnime()
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
            ShowSource.PUTLOCKER -> return arrayListOf(getPutLocker())
            ShowSource.GOGOANIME -> return arrayListOf(getGogoAnime())
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
            ShowSource.PUTLOCKER -> return arrayListOf(Storage(link = getPutLocker(), filename = name, quality = "720", source = "PutLocker", sub = "No"))
            ShowSource.GOGOANIME -> {
                val storage = Storage(link = getGogoAnime(), source = url, quality = "Good", sub = "Yes")
                val regex = "^[^\\[]+(.*mp4)".toRegex().toPattern().matcher(storage.link!!)
                storage.filename = if (regex.find()) regex.group(1)!! else "${URI(url).path.split("/")[2]} $name.mp4"
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
        return emptyList()
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

    override fun toString(): String = "$name: $url"
}

internal class NormalLink(var normal: Normal? = null)
internal class Normal(var storage: Array<Storage>? = emptyArray())
data class Storage(var sub: String? = null, var source: String? = null, var link: String? = null, var quality: String? = null, var filename: String? = null)
