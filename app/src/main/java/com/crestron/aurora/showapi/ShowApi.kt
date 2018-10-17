package com.crestron.aurora.showapi

import com.crestron.aurora.Loged
import org.jsoup.Jsoup
import org.jsoup.nodes.Document

enum class Source(val link: String, val recent: Boolean = false) {
    ANIME("http://www.animeplus.tv/anime-list"),
    CARTOON("http://www.animetoon.org/cartoon"),
    DUBBED("http://www.animetoon.org/dubbed-anime"),
    ANIME_MOVIES("http://www.animeplus.tv/anime-movies"),
    CARTOON_MOVIES("http://www.animetoon.org/movies"),
    RECENT_ANIME("http://www.animeplus.tv/anime-updates", true),
    RECENT_CARTOON("http://www.animetoon.org/updates", true);

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
                else -> ANIME
            }
        }
    }
}

class ShowInfo(val name: String, val url: String)

class ShowApi(private val source: Source) {
    private var doc: Document = Jsoup.connect(source.link).get()

    val showInfoList: ArrayList<ShowInfo>
        get() {
            return if (source.recent)
                getRecentList()
            else
                getList()
        }

    private fun getList(): ArrayList<ShowInfo> {
        val lists = doc.allElements
        val listOfStuff = lists.select("td").select("a[href^=http]")
        val listOfShows = arrayListOf<ShowInfo>()
        for (element in listOfStuff) {
            listOfShows.add(ShowInfo(element.text(), element.attr("abs:href")))
        }
        listOfShows.sortBy { it.name }
        return listOfShows
    }

    private fun getRecentList(): ArrayList<ShowInfo> {
        var listOfStuff = doc.allElements.select("div.left_col").select("table#updates").select("a[href^=http]")
        if (listOfStuff.size == 0) {
            listOfStuff = doc.allElements.select("div.s_left_col").select("table#updates").select("a[href^=http]")
        }
        val listOfShows = arrayListOf<ShowInfo>()
        for (element in listOfStuff) {
            val showInfo = ShowInfo(element.text(), element.attr("abs:href"))
            if (!element.text().contains("Episode"))
                listOfShows.add(showInfo)
        }
        return listOfShows
    }
}

class EpisodeApi(private val source: ShowInfo) {
    private var doc: Document = Jsoup.connect(source.url).get()

    val name: String
        get() = doc.select("div.right_col h1").text()

    val image: String
        get() = doc.select("div.left_col").select("img[src^=http]#series_image").attr("abs:src")

    val description: String
        get() {
            val des = if (doc.allElements.select("div#series_details").select("span#full_notes").hasText())
                doc.allElements.select("div#series_details").select("span#full_notes").text().removeSuffix("less")
            else {
                val d = doc.allElements.select("div#series_details").select("div:contains(Description:)").select("div").text()
                try {
                    d.substring(d.indexOf("Description: ") + 13, d.indexOf("Category: "))
                } catch (e: StringIndexOutOfBoundsException) {
                    Loged.e(e.message!!)
                    d
                }
            }
            return if (des.isNullOrBlank()) "Sorry, an error has occurred" else des
        }

    val episodeList: ArrayList<ShowInfo>
        get() {
            val listOfShows = arrayListOf<ShowInfo>()
            fun getStuff(url: String) {
                val doc1 = Jsoup.connect(url).get()
                val stuffList = doc1.allElements.select("div#videos").select("a[href^=http]")
                for (i in stuffList) {
                    listOfShows.add(ShowInfo(i.text(), i.attr("abs:href")))
                }
            }
            getStuff(source.url)
            val stuffLists = doc.allElements.select("ul.pagination").select(" button[href^=http]")
            for (i in stuffLists) {
                getStuff(i.attr("abs:href"))
            }
            return listOfShows
        }
}