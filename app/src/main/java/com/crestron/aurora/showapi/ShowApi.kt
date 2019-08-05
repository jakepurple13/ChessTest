package com.crestron.aurora.showapi

import com.google.gson.Gson
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
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
    LIVE_ACTION("https://www.putlocker.fyi/a-z-shows/");

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
                else -> ANIME
            }
        }
    }
}

/**
 * Info about the show, name and url
 */
open class ShowInfo(val name: String, val url: String) {
    override fun toString(): String {
        return "$name: $url"
    }
}

/**
 * The actual api!
 */
class ShowApi(private val source: Source) {
    private var doc: Document = Jsoup.connect(source.link).get()

    /**
     * returns a list of the show's from the wanted source
     */
    val showInfoList: List<ShowInfo>
        get() {
            return if (source.recent)
                getRecentList()
            else
                getList()
        }

    private fun getList(): ArrayList<ShowInfo> {
        return if (source.link.contains("gogoanime")) {
            if (source == Source.ANIME_MOVIES || source.movie)
                gogoAnimeMovies()
            else
                gogoAnimeAll()
        } else if (source.link.contains("putlocker")) {
            val d = doc.select("a.az_ls_ent")
            val listOfShows = arrayListOf<ShowInfo>()
            for (i in d) {
                listOfShows += ShowInfo(i.text(), i.attr("abs:href"))
            }
            return listOfShows
        } else {
            val lists = doc.allElements
            val listOfStuff = lists.select("td").select("a[href^=http]")
            val listOfShows = arrayListOf<ShowInfo>()
            for (element in listOfStuff) {
                listOfShows.add(
                        ShowInfo(
                                element.text(),
                                element.attr("abs:href")
                        )
                )
            }
            listOfShows.sortBy { it.name }
            listOfShows
        }
    }

    private fun gogoAnimeAll(): ArrayList<ShowInfo> {
        val listOfShows = arrayListOf<ShowInfo>()
        val lists = doc.allElements
        val listOfStuff = lists.select("ul.arrow-list").select("li")
        for (element in listOfStuff) {
            listOfShows.add(
                    ShowInfo(
                            element.text(),
                            element.select("a[href^=http]").attr("abs:href")
                    )
            )
        }
        listOfShows.sortBy { it.name }
        return listOfShows
    }

    private fun gogoAnimeMovies(): ArrayList<ShowInfo> {
        val list = gogoAnimeAll().filter {
            it.name.contains(
                    "movie",
                    ignoreCase = true
            )
        } as ArrayList<ShowInfo>
        list.sortBy { it.name }
        return list
    }

    private fun getRecentList(): ArrayList<ShowInfo> {
        return if (source.link.contains("gogoanime")) {
            gogoAnimeRecent()
        } else {
            var listOfStuff = doc.allElements.select("div.left_col").select("table#updates")
                    .select("a[href^=http]")
            if (listOfStuff.size == 0) {
                listOfStuff = doc.allElements.select("div.s_left_col").select("table#updates")
                        .select("a[href^=http]")
            }
            val listOfShows = arrayListOf<ShowInfo>()
            for (element in listOfStuff) {
                val showInfo =
                        ShowInfo(element.text(), element.attr("abs:href"))
                if (!element.text().contains("Episode"))
                    listOfShows.add(showInfo)
            }
            listOfShows
        }
    }

    private fun gogoAnimeRecent(): ArrayList<ShowInfo> {
        val listOfStuff =
                doc.allElements.select("div.dl-item")
        val listOfShows = arrayListOf<ShowInfo>()
        for (element in listOfStuff) {
            val tempUrl = element.select("div.name").select("a[href^=http]").attr("abs:href")
            val showInfo = ShowInfo(
                    element.select("div.name").text(),
                    tempUrl.substring(0, tempUrl.indexOf("/episode"))
            )
            listOfShows.add(showInfo)
        }
        return listOfShows
    }

}

/**
 * If you want to get the Show with all the information now rather than passing it into [EpisodeApi] yourself
 */
fun List<ShowInfo>.getEpisodeApi(index: Int): EpisodeApi = EpisodeApi(this[index])

/**
 * Actual Show information
 */
class EpisodeApi(private val source: ShowInfo, timeOut: Int = 10000) {
    private var doc: Document = Jsoup.connect(source.url).timeout(timeOut).get()

    /**
     * The name of the Show
     */
    val name: String
        get() {
            return if(source.url.contains("putlocker")) {
                doc.select("li.breadcrumb-item").last().text()
            } else if (source.url.contains("gogoanime")) {
                doc.select("div.anime-title").text()
            } else {
                doc.select("div.right_col h1").text()
            }
        }

    /**
     * The url of the image
     */
    val image: String
        get() {
            return if (source.url.contains("putlocker")) {
                doc.select("div.thumb").select("img[src^=http]").attr("abs:src")
            } else if (source.url.contains("gogoanime")) {
                doc.select("div.animeDetail-image").select("img[src^=http]").attr("abs:src")
            } else {
                doc.select("div.left_col").select("img[src^=http]#series_image").attr("abs:src")
            }

        }

    /**
     * the description
     */
    val description: String
        get() {
            if(source.url.contains("putlocker")) {
                return "There is none here"
            } else if (source.url.contains("gogoanime")) {
                val des = doc.select("p.anime-details").text()
                return if (des.isNullOrBlank()) "Sorry, an error has occurred" else des
            } else {
                val des =
                        if (doc.allElements.select("div#series_details").select("span#full_notes").hasText())
                            doc.allElements.select("div#series_details").select("span#full_notes").text().removeSuffix(
                                    "less"
                            )
                        else {
                            val d = doc.allElements.select("div#series_details")
                                    .select("div:contains(Description:)").select("div").text()
                            try {
                                d.substring(d.indexOf("Description: ") + 13, d.indexOf("Category: "))
                            } catch (e: StringIndexOutOfBoundsException) {
                                d
                            }
                        }
                return if (des.isNullOrBlank()) "Sorry, an error has occurred" else des
            }
        }

    /**
     * The episode list
     */
    val episodeList: List<EpisodeInfo>
        get() {
            var listOfShows = arrayListOf<EpisodeInfo>()
            if (source.url.contains("putlocker")) {
                val rowList = doc.select("div.col-lg-12").select("div.row")

                //val seasons = rowList.select("a.btn-season")
                /*for (i in seasons) {
                    //log(i.text())
                }*/
                val episodes = rowList.select("a.btn-episode")

                for (i in episodes) {
                    val ep = EpisodeInfo(i.text(), "https://www.putlocker.fyi/embed-src/${i.attr("data-pid")}")//i.attr("abs:href"))
                    listOfShows.add(ep)
                }
            } else if (source.url.contains("gogoanime")) {
                val stuffList = doc.select("ul.check-list").select("li")
                val showList = arrayListOf<EpisodeInfo>()
                for (i in stuffList) {
                    val urlInfo = i.select("a[href^=http]")
                    val epName = if (urlInfo.text().contains(name)) {
                        urlInfo.text().substring(name.length)
                    } else {
                        urlInfo.text()
                    }.trim()
                    showList.add(EpisodeInfo(epName, urlInfo.attr("abs:href")))
                }
                listOfShows = showList.distinctBy { it.name } as ArrayList<EpisodeInfo>
            } else {
                fun getStuff(url: String) {
                    val doc1 = Jsoup.connect(url).get()
                    val stuffList = doc1.allElements.select("div#videos").select("a[href^=http]")
                    for (i in stuffList) {
                        listOfShows.add(
                                EpisodeInfo(
                                        i.text(),
                                        i.attr("abs:href")
                                )
                        )
                    }
                }
                getStuff(source.url)
                val stuffLists =
                        doc.allElements.select("ul.pagination").select(" button[href^=http]")
                for (i in stuffLists) {
                    getStuff(i.attr("abs:href"))
                }
            }
            return listOfShows
        }

    override fun toString(): String {
        return "$name - ${episodeList.size} eps - $description"
    }

}

/**
 * Actual Episode info, name and url
 */
class EpisodeInfo(name: String, url: String) : ShowInfo(name, url) {

    /**
     * returns a url link to the episodes video
     * # Use for anything but movies
     */
    fun getVideoLink(): String {
        if (url.contains("putlocker")) {
            //TODO: Try to see why I might be getting error 302 (aka Redirection)
            val doc = Jsoup.connect(url).get()
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(doc.toString())
            if (d.find()) {
                val f = Jsoup.connect(d.group(1)!!).get()
                val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(f.toString())
                if (a.find()) {
                    return "https://verystream.com/gettoken/${a.group(1)!!}?mime=true"
                }
            }
            return "N/A"
        } else if (url.contains("gogoanime")) {
            val doc = Jsoup.connect(url).get()
            return doc.select("a[download^=http]").attr("abs:download")
        } else {
            val episodeHtml = getHtml(url)
            val matcher = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern()
                    .matcher(episodeHtml)
            val list = arrayListOf<String>()
            while (matcher.find()) {
                list.add(matcher.group(1)!!)
            }

            val videoHtml = getHtml(list[0])
            val reg =
                    "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(videoHtml)
            if (reg.find()) {
                val d = reg.group(1)
                val g = Gson()
                val d1 = g.fromJson(d, NormalLink::class.java)

                return d1.normal!!.storage!![0].link!!
            }
        }
        return ""
    }

    /**
     * returns a url link to the episodes video
     * # Use for movies
     */
    fun getVideoLinks(): List<String> {
        if (url.contains("putlocker")) {
            val doc = Jsoup.connect(url).get()
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(doc.toString())
            if (d.find()) {
                val f = Jsoup.connect(d.group(1)!!).get()
                val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(f.toString())
                if (a.find()) {
                    return arrayListOf("https://verystream.com/gettoken/${a.group(1)!!}?mime=true")
                }
            }
            return arrayListOf("N/A")
        } else if (url.contains("gogoanime")) {
            val doc = Jsoup.connect(url).get()
            return arrayListOf(doc.select("a[download^=http]").attr("abs:download"))
        } else {
            val htmld = getHtml(url)
            val m = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern().matcher(htmld)
            var s = ""
            val list = arrayListOf<String>()
            while (m.find()) {
                val g = m.group(1)!!
                s += g + "\n"
                list.add(g)
            }

            val regex =
                    "(http|https):\\/\\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\%\\&\\-\\_\\?\\.\\=\\/])+(part[0-9])+.(\\w*)"

            val htmlc = if (regex.toRegex().toPattern().matcher(list[0]).find()) {
                list
            } else {
                getHtml(list[0])
            }

            when (htmlc) {
                is ArrayList<*> -> {
                    val urlList = arrayListOf<String>()
                    for (info in htmlc) {
                        val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern()
                                .matcher(getHtml(info.toString()))
                        while (reg.find()) {
                            val d = reg.group(1)
                            val g = Gson()
                            val d1 = g.fromJson(d, NormalLink::class.java)
                            urlList.add(d1.normal!!.storage!![0].link!!)

                        }
                    }
                    return urlList
                }
                is String -> {
                    val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(htmlc)
                    while (reg.find()) {
                        val d = reg.group(1)
                        val g = Gson()
                        val d1 = g.fromJson(d, NormalLink::class.java)
                        return arrayListOf(d1.normal!!.storage!![0].link!!)
                    }
                }
            }
        }
        return arrayListOf()
    }

    /**
     * returns video information
     * this includes link to video and filename
     * # You can use this for anything. This just returns some extra information.
     */
    fun getVideoInfo(): List<Storage> {
        if (url.contains("putlocker")) {
            val doc = Jsoup.connect(url).get()
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(doc.toString())
            if (d.find()) {
                val f = Jsoup.connect(d.group(1)!!).get()
                val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(f.toString())
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
        } else if (url.contains("gogoanime")) {
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
        } else {
            val htmld = getHtml(url)
            val m = "<iframe src=\"([^\"]+)\"[^<]+<\\/iframe>".toRegex().toPattern().matcher(htmld)
            var s = ""
            val list = arrayListOf<String>()
            while (m.find()) {
                val g = m.group(1)!!
                s += g + "\n"
                list.add(g)
            }

            val regex =
                    "(http|https):\\/\\/([\\w+?\\.\\w+])+([a-zA-Z0-9\\~\\%\\&\\-\\_\\?\\.\\=\\/])+(part[0-9])+.(\\w*)"

            val htmlc = if (regex.toRegex().toPattern().matcher(list[0]).find()) {
                list
            } else {
                getHtml(list[0])
            }

            when (htmlc) {
                is ArrayList<*> -> {
                    val urlList = arrayListOf<Storage>()
                    for (info in htmlc) {
                        val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern()
                                .matcher(getHtml(info.toString()))
                        while (reg.find()) {
                            val d = reg.group(1)
                            val g = Gson()
                            val d1 = g.fromJson(d, NormalLink::class.java)
                            urlList.add(d1.normal!!.storage!![0])
                        }
                    }
                    return urlList
                }
                is String -> {
                    val reg = "var video_links = (\\{.*?\\});".toRegex().toPattern().matcher(htmlc)
                    while (reg.find()) {
                        val d = reg.group(1)
                        val g = Gson()
                        val d1 = g.fromJson(d, NormalLink::class.java)
                        return arrayListOf(d1.normal!!.storage!![0])
                    }
                }
            }
        }
        return arrayListOf()
    }

    @Throws(IOException::class)
    internal fun getHtml(url: String): String {
        // Build and set timeout values for the request.
        val connection = URL(url).openConnection()
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
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

    override fun toString(): String {
        return "$name: $url"
    }
}

internal class NormalLink {
    var normal: Normal? = null

    override fun toString(): String {
        return "ClassPojo [normal = " + normal!!.toString() + "]"
    }
}

internal class Normal {
    var storage: Array<Storage>? = null

    override fun toString(): String {
        return "ClassPojo [storage = $storage]"
    }
}

class Storage {
    var sub: String? = null

    var source: String? = null

    var link: String? = null

    var quality: String? = null

    var filename: String? = null

    override fun toString(): String {
        return "ClassPojo [sub = $sub, source = $source, link = $link, quality = $quality, filename = $filename]"
    }
}