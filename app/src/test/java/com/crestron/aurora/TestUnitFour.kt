package com.crestron.aurora

import com.crestron.aurora.otherfun.AnimeShowApi
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

    private val episodeLoadUrl = "https://ajax.gogocdn.net/ajax/load-list-episode"
    private val otherUrl = "https://www.gogoanime.io"

    @Test
    fun other20() {
        //val s = Jsoup.connect("https://ajax.gogocdn.net/ajax/page-recent-release.html?page=1").get()
        //println(s.html())
        val api = AnimeShowApi()
        val f = api.parseRecentSubOrDub(1)
        //Loged.f(f)
        val e = api.parseAnimeInfo(f[0])
        //Loged.f(e)
        val b = api.fetchEpisodeList(e)
        //Loged.f(b)
        val v = api.parseMediaUrl(b[0])
        //Loged.f(v)
        val m = api.parseM3U8Url("https://${v.vidcdnUrl}")
        Loged.f(m)
    }
}