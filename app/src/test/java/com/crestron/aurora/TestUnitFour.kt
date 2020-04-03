package com.crestron.aurora

import org.jsoup.Jsoup
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

class TestUnitFour {
    @Before
    fun beforeSetup() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
        Loged.UNIT_TESTING = true
        Loged.OTHER_CLASS_FILTER = { !it.contains("Framing") }
        println("Starting at ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(System.currentTimeMillis())}")
    }

    @After
    fun afterSetup() {
        println("Ending at ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(System.currentTimeMillis())}")
    }

    @Test
    fun other20() {
        //val s = Jsoup.connect("https://ajax.gogocdn.net/ajax/page-recent-release.html?page=1").get()
        //println(s.html())
        val f = parseRecentSubOrDub("https://ajax.gogocdn.net/ajax/page-recent-release.html?page=1")
        Loged.f(f)
    }

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

    fun parseRecentSubOrDub(url: String): ArrayList<AnimeMetaModel> {
        val animeMetaModelList: ArrayList<AnimeMetaModel> = ArrayList()
        val document = Jsoup.connect(url).get()
        val lists = document.getElementsByClass("items").first().select("li")
        var i = 0
        lists?.forEach { anime ->
            val animeInfo = anime.getElementsByClass("name").first().select("a")
            val title = animeInfo.attr("title")
            val episodeUrl = animeInfo.attr("href")
            val episodeNumber = anime.getElementsByClass("episode").first().text()
            val animeImageInfo = anime.selectFirst("a")
            val imageUrl = animeImageInfo.select("img").first().absUrl("src")

            animeMetaModelList.add(
                    AnimeMetaModel(
                            ID = "$title".hashCode(),
                            title = title,
                            episodeNumber = episodeNumber,
                            episodeUrl = episodeUrl,
                            //categoryUrl = getCategoryUrl(imageUrl),
                            imageUrl = imageUrl,
                            //typeValue = typeValue,
                            insertionOrder = i
                    )
            )
            i++
        }
        return animeMetaModelList
    }
}