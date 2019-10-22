package com.crestron.aurora

import android.os.Handler
import com.crestron.aurora.boardgames.yahtzee.Dice
import com.crestron.aurora.boardgames.yahtzee.YahtzeeScores
import com.crestron.aurora.showapi.*
import com.crestron.aurora.utilities.KUtility
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Suit
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import okhttp3.OkHttpClient
import okhttp3.Response
import org.apache.tools.ant.util.DateUtils
import org.json.JSONObject
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.measureTimeMillis


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

typealias cad = Card

fun cadTest() {
    val d1 = cad(Suit.DIAMONDS, 3)
}

//@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Before
    fun setUp() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
        Loged.WITH_THREAD_NAME = true
    }

    @Test
    fun searchTesting() = runBlocking {
        val c = Card.RandomCard
        prettyLog(c)
        val d = Deck().getDeck()
        val cNum = measureTimeMillis {
            delay(1000L)
            val c1 = d.binarySearch(c)
            prettyLog(c1)
        }
        prettyLog("$cNum milliseconds")
        prettyLog(cNum.toElapsed())

    }

    private fun Long.toElapsed(): String = DateUtils.formatElapsedTime(this)

    @Test
    fun handleTest() {
        /*Handler().postDelayed(object : Runnable {
            override fun run() {
                prettyLog("Hello")
                Handler().postDelayed(this, 5000)
            }
        }, 5000)*/
        val s = object {
            val d = 5
        }

        val f = Loged
        f.TAG
    }

    private fun doActionOnRepeat(delay: Long = 2500, action: () -> Boolean) {
        Thread(object : Runnable {
            override fun run() {
                Thread.sleep(delay)
                if (action())
                    Thread(this).start()
            }
        }).start()
    }

    private fun doActionRepeatedly(delay: Long = 2500, action: () -> Boolean) {
        Handler().postDelayed(object : Runnable {
            override fun run() {
                if (action())
                    Handler().postDelayed(this, delay)
            }
        }, delay)
    }

    private fun getFinalURL(url: URL): URL? {
        //TODO: Try this out when you get home
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

    private fun Collection<ShowInfo>.randomEpisode(): EpisodeApi = EpisodeApi(random())

    @Test
    fun putLock2() {

        val show = ShowApi(Source.LIVE_ACTION).showInfoList
        val ep = show.getEpisodeApi(0)//randomEpisode()
        //prettyLog("${ep.name} with ${ep.source.url} with ${ep.episodeList[0].url}")
        //prettyLog(ep.episodeList[0].getVideoLink())
        //prettyLog(ep.episodeList[0].getVideoLinks()[0])
        //val epUrl = ep.episodeList[0].getVideoLink()
        //prettyLog(epUrl)
        //val url = getFinalURL(URL(epUrl))!!
        //prettyLog(url.toExternalForm())
        //prettyLog("$url")
        //prettyLog(url.toString())
        //prettyLog("${url.toURI()}")

        val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(Jsoup.connect(ep.episodeList[0].url).get().html())
        if (d.find()) {
            val d1 = d.group(1)!!
            prettyLog(d1)
            prettyLog(Jsoup.connect(d1).get().html())
            /*val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(Jsoup.connect(d1).get().html())
            if (a.find()) {
                val a1 = a.group(1)!!
                prettyLog(a1)
                prettyLog("https://verystream.com/gettoken/$a1?mime=true")
            }*/
        }

    }

    @Test
    fun putLock3() {
        val show = ShowApi(Source.LIVE_ACTION).showInfoList
        val ep = show.getEpisodeApi(0)
        //prettyLog(ep.description)
        //To get Main Json object
        /*val jsonObjectRequest = JsonObjectRequest(Request.Method.GET,
                "http://www.omdbapi.com/?i=" + ep.name + "",
                Response.Listener<JSONObject> { response ->
                    try {
                        //To get JSON array inside the Json object
                        // val movieArray = response.getJSONArray("Search")

                        prettyLog("Plot - " + response.getString("Plot"))

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }, Response.ErrorListener { error -> VolleyLog.d("MainActivityError ", error.message) })*/
        //val queue = Volley.newRequestQueue()

        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
                .url("http://www.omdbapi.com/?t=${ep.name}&apikey=e91b86ee")
                .get()
                .build()
        val response = client.newCall(request).execute()
        val resString = response.body()!!.string()
        prettyLog(resString)
        val jsonObj = JSONObject(resString)
        prettyLog(jsonObj.getString("Plot"))

    }

    private fun getApiCalls(url: String, onError: (Response) -> Unit = {}, retrieve: (String) -> Unit) {
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
                .url(url)
                .get()
                .build()
        val response = client.newCall(request).execute()
        if (response.code() == 200) {
            val resString = response.body()!!.string()
            //retrieve(JSONObject(resString))
            retrieve(resString)
        } else {
            onError(response)
        }
    }

    data class TrackInfo(
            @SerializedName("message") val message: Message
    )

    data class Message(
            @SerializedName("header") val header: Header,
            @SerializedName("body") val body: Body
    )

    data class Body(
            @SerializedName("track") val track: Track
    )

    data class Track(
            @SerializedName("track_id") val trackID: Long,
            @SerializedName("track_name") val trackName: String,
            @SerializedName("track_name_translation_list") val trackNameTranslationList: List<Any?>,
            @SerializedName("track_rating") val trackRating: Long,
            @SerializedName("common_track_id") val commontrackID: Long,
            @SerializedName("instrumental") val instrumental: Long,
            @SerializedName("explicit") val explicit: Long,
            @SerializedName("has_lyrics") val hasLyrics: Long,
            @SerializedName("has_subtitles") val hasSubtitles: Long,
            @SerializedName("has_rich_sync") val hasRichsync: Long,
            @SerializedName("num_favourite") val numFavourite: Long,
            @SerializedName("album_id") val albumID: Long,
            @SerializedName("album_name") val albumName: String,
            @SerializedName("artist_id") val artistID: Long,
            @SerializedName("artist_name") val artistName: String,
            @SerializedName("track_share_url") val trackShareURL: String,
            @SerializedName("track_edit_url") val trackEditURL: String,
            @SerializedName("restricted") val restricted: Long,
            @SerializedName("updated_time") val updatedTime: String,
            @SerializedName("primary_genres") val primaryGenres: PrimaryGenres
    )

    data class PrimaryGenres(
            @SerializedName("music_genre_list") val musicGenreList: List<Any?>
    )

    data class Header(
            @SerializedName("status_code") val statusCode: Long,
            @SerializedName("execute_time") val executeTime: Double,
            @SerializedName("confidence") val confidence: Long,
            @SerializedName("mode") val mode: String,
            @SerializedName("cached") val cached: Long
    )

    data class TrackInfoLyric(
            @SerializedName("message") val message: MessageLyric
    )

    data class MessageLyric(
            @SerializedName("header") val header: HeaderLyric,
            @SerializedName("body") val body: BodyLyric
    )

    data class BodyLyric(
            @SerializedName("lyrics") val lyrics: Lyrics
    )

    data class HeaderLyric(
            @SerializedName("status_code") val statusCode: Long,
            @SerializedName("execute_time") val executeTime: Double
    )

    data class Lyrics(
            @SerializedName("lyrics_id") val lyricsID: Long,
            //@SerializedName("restricted") val restricted: Long,
            //@SerializedName("instrumental") val instrumental: Long,
            @SerializedName("lyrics_body") val lyricsBody: String,
            @SerializedName("lyrics_language") val lyricsLanguage: String,
            @SerializedName("script_tracking_url") val scriptTrackingURL: String,
            @SerializedName("pixel_tracking_url") val pixelTrackingURL: String,
            @SerializedName("lyrics_copyright") val lyricsCopyright: String
            //@SerializedName("backlink_url") val backlinkURL: String,
            //@SerializedName("updated_time") val updatedTime: String
    )

    @Test
    fun restTest() {
        /*val client = OkHttpClient.Builder()
                .build()
        val request = okhttp3.Request.Builder()
                .url("https://www.mwtestconsultancy.co.uk/auth")
                .post(RequestBody.create(okhttp3.MediaType.get("application/json; charset=utf-8"), "{\"username\":\"admin\", \"password\":\"password123\"}"))
                .build()
        val response = client.newCall(request).execute()
        val code = response.code()
        val resString = response.body()!!.string()
        prettyLog("Code: $code\n$resString")*/
        /*val client = OkHttpClient.Builder()
                .build()
        val request = okhttp3.Request.Builder()
                .url("https://restful-booker.herokuapp.com/booking/2")
                .build()
        val response = client.newCall(request).execute()
        val code = response.code()
        val resString = response.body()!!.string()
        prettyLog("Code: $code\n$resString\n$response")*/
        getApiCalls("https://restful-booker.herokuapp.com/booking/2&token=b788c7935947355", onError = {
            prettyLog("Code is ${it.code()}\n$it")
        }) {
            prettyLog(it)
        }

        val songName = "Hurt"

        //getApiCalls("https://api.musixmatch.com/ws/1.1/matcher.track.get?q_track=$songName&q_artist=Johnny Cash&apikey=67053f507ef88fc99c544f4d7052dfa8", onError = {
        getApiCalls("https://api.musixmatch.com/ws/1.1/matcher.track.get?q_track=starlight brigade&q_artist=twrp&apikey=67053f507ef88fc99c544f4d7052dfa8", onError = {
            prettyLog("Code is ${it.code()}\n$it")
        }) {
            prettyLog(it)
            val obj = Gson().fromJson(it, TrackInfo::class.java)
            prettyLog(obj.toString())
            prettyLog(obj.message.body.track.trackName)
            prettyLog(obj.message.body.track.artistName)
            prettyLog(obj.message.body.toString())
            prettyLog(obj.message.header.toString())
            getApiCalls("https://api.musixmatch.com/ws/1.1/track.lyrics.get?track_id=${obj.message.body.track.trackID}&apikey=67053f507ef88fc99c544f4d7052dfa8") { it2 ->
                prettyLog(it2)
                val obj2 = Gson().fromJson(it2, TrackInfoLyric::class.java)
                //prettyLog(obj2.toString())
                prettyLog(obj2.message.body.lyrics.lyricsBody)
            }
        }
    }

    @Test
    fun putlocktest2() {
        //This is what I get from the app
        //https://verystream.com/gettoken/cnjoAVarrQ2~1565007281~173.3.0.0~VBMxlX7W?mime=true
        //This is what I get when I put the link above into the browser url
        //https://wowsoamaze.verystream.net/stream/cnjoAVarrQ2/lw7ID5F1CsOe5n7T/rnL9Tc3WHG5amPKmGnyAkp5Tc5EMccRfcERY4eGnapJ2_vrzDY-V-u40wEUqDnk1CF5UJR
        // kB3STundcN3pXO21_JhKeiUpO9E521zJUENTzpFjFML_mWR9Q3goPjcw9XpBZ4s9pkV1bkE6kbomIR1sc3W7m7GNY6gXcsJdTmqLEsfgUNUqC64K7O5YIICWLrbT9FRYDSe3zNPQicD
        // HUgxwSbi6hOhrwHnncHLoRHtgtyy3V75MVMb7-uPXKW9lb0iJRXa_3EOspJlOi6clPALBm0UPjeKd7tJuBr2D0lAgXKnH-ejxkOBS2ke7Ikbbc9rZ5kmVJz0S2Kkxm8_lDpL99g1-58
        // wLDec0DOG6aEhd2UDHht6wgWAwXqMf6EY0Lv8Rab2RM9cc__sulWx0Fy1JqyoRj_LIWZz7W7zMZaT2GiNlgBqs5Gs9uhqREpa_ph47v6Alq85KCVxszvNkvXqw/Marvels.Agents.o
        // f.S.H.I.E.L.D.S06E13.HDTV.x264-KILLERS.mp4?mime=true
        val link = "https://www.putlocker.fyi/show/marvels-agents-of-s-h-i-e-l-d/"
        val epApi = EpisodeApi(ShowInfo("asdf", link))
        val show = epApi.episodeList[0]
        log(show.name)
        val links = show.getVideoLink()
        log(links)
        val ep = EpisodeInfo("Shield", "https://www.putlocker.fyi/show/marvels-agents-of-s-h-i-e-l-d/season-6/episode-13/")
        log("${ep.getVideoLink()}")
    }

    @Test
    fun putLockTV() {
        val link = "https://www.putlocker.fyi/a-z-shows/"
        val s = Jsoup.connect(link).get()
        val d = s.select("a.az_ls_ent")//s.select("tr")//s.select("a.az_ls_ent")

        data class Showsa(val title: String, val url: String) {
            override fun toString(): String {
                return "$title - $url"
            }
        }

        val listOfShows = arrayListOf<Showsa>()
        for (i in d) {
            listOfShows += Showsa(i.text(), i.attr("abs:href"))
        }

        //listOfShows.sortBy { it.title }

        log("${listOfShows.subList(0, 10)}")

        val newLink = "https://www.putlocker.fyi/"

        log(listOfShows[10].title)
        getShowStuff(listOfShows.random().url)
    }

    fun getShowStuff(url: String) {
        val s = Jsoup.connect(url).get()
        //val s = Jsoup.connect("https://www.putlocker.fyi/show/holey-moley/").get()
        //log("$s")
        val name = s.select("li.breadcrumb-item").last().text()
        log(name)

        val image = s.select("div.thumb").select("img[src^=http]").attr("abs:src")
        log(image)

        /*val rowList = s.select("div.col-lg-12").select("div.row")

        val seasons = rowList.select("a.btn-season")
        for(i in seasons) {
            //log(i.text())
        }
        val episodes = rowList.select("a.btn-episode")
        data class Eps(var title: String, var link: String, var vidUrl: String) {
            override fun toString(): String {
                return "$title: $link and $vidUrl"
            }
        }

        val epList = arrayListOf<Eps>()
        for(i in episodes) {
            val ep = Eps(i.text(), i.attr("abs:href"), "https://www.putlocker.fyi/embed-src/${i.attr("data-pid")}")
            epList+=ep
        }
        log("$epList")
        log(getVidUrl(epList.random().vidUrl))*/
    }

    fun getVidUrl(secondUrl: String): String {
        val doc = Jsoup.connect(secondUrl).get()
        val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(doc.toString())
        if (d.find()) {
            val f = Jsoup.connect(d.group(1)!!).get()
            val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(f.toString())
            if (a.find()) {
                return "https://verystream.com/gettoken/${a.group(1)!!}?mime=true"
            }
        }
        return "N/A"
    }

    @Test
    fun putLocker() {
        val s = Jsoup.connect("https://www.putlocker.fyi/show/silicon-valley/").get()
        //val s = Jsoup.connect("https://www.putlocker.fyi/show/holey-moley/").get()
        //log("$s")
        val name = s.select("li.breadcrumb-item").last().text()
        log(name)
        //'div', attrs={'class': 'col-lg-12'})[2].find_all('div', attrs={'class': 'row'})
        val rowList = s.select("div.col-lg-12").select("div.row")
        //for(i in rowList) {
        //   log(i.text())
        //}
        val seasons = rowList.select("a.btn-season")
        for (i in seasons) {
            //log(i.text())
        }
        val episodes = rowList.select("a.btn-episode")

        data class Eps(var title: String, var link: String, var vidUrl: String) {
            override fun toString(): String {
                return "$title: $link and $vidUrl"
            }
        }
        //"link": e['href'],
        //            "vid_url": self._get_embed_src(e['data-pid']),
        //            "title": self._parse_title(e['title'])
        for (i in episodes) {
            val secondUrl = "https://www.putlocker.fyi/embed-src/${i.attr("data-pid")}"
            val doc = Jsoup.connect(secondUrl).get()
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(doc.toString())
            if (d.find()) {
                val f = Jsoup.connect(d.group(1)!!).get()
                val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(f.toString())
                if (a.find()) {
                    val lastUrl = "https://verystream.com/gettoken/${a.group(1)!!}?mime=true"
                    val ep = Eps(i.text(), i.attr("abs:href"), lastUrl)
                    log("$ep")
                }
            }
        }
    }

    @Test
    fun quickD() {
        val d = Deck()
        d.addCard(d[0])
        log(d.toArrayPrettyString())
    }

    @Test
    fun yahtzeeTest() {
        val diceList = arrayListOf<Dice>()
        diceList.add(Dice(1))
        diceList.add(Dice(1))
        diceList.add(Dice(1))
        diceList.add(Dice(1))
        diceList.add(Dice(1))
        val scores = YahtzeeScores()
        log("${scores.getThreeOfAKind(diceList)}")
        log("${scores.getFourOfAKind(diceList)}")
        log("${scores.getYahtzee(diceList)}")
        log("${scores.getOnes(diceList)}")
        log("${scores.getTwos(diceList)}")
        diceList.clear()
        diceList.add(Dice(2))
        diceList.add(Dice(2))
        diceList.add(Dice(1))
        diceList.add(Dice(1))
        diceList.add(Dice(1))
        log("${scores.getFullHouse(diceList)}")
        diceList.clear()
        diceList.add(Dice(5))
        diceList.add(Dice(2))
        diceList.add(Dice(2))
        diceList.add(Dice(2))
        diceList.add(Dice(4))
        log("${scores.getLargeStraight(diceList)}")
        log("${scores.getSmallStraight(diceList)}")

    }

    @Test
    fun cipherTest() {
        //BTFHGVEHNWGBGX
        val text = "BTFHGVEHNWGBGX"

        fun freq(texts: String) {
            val letterMap = texts.filter { it in 'a'..'z' }.groupBy { it }.toSortedMap()
            for (letter in letterMap)
                println("${letter.key} = ${letter.value.size}")
            val sum = letterMap.values.sumBy { it.size }
            println("\nTotal letters = $sum")
        }

        freq(text.toLowerCase())

        fun num(i: Int): Double {
            return 0.2143 * (9 - i) +
                    0.1429 * (1 - i) +
                    0.1429 * (7 - i) +
                    0.714 * (19 - i) +
                    0.714 * (4 - i) +
                    0.714 * (5 - i) +
                    0.714 * (21 - i) +
                    0.714 * (13 - i) +
                    0.714 * (22 - i) +
                    0.714 * (23 - i)

            //#N : 10    Σ = 14.000    Σ = 99.990
        }
        for (i in 0..25) {
            System.out.println("${(i + 65).toChar()} = ${num(i)}")
        }
    }

    @Test
    fun socketting() {
        /*val ssc = ServerSocketChannel.open()
        val s = InetSocketAddress("127.0.0.1", 80)
        ssc.socket().bind(s)
        val sc = SocketChannel.open()
        sc.connect(s)
        ssc.accept().close()
        val buf = arrayOf<ByteBuffer>(ByteBuffer.allocate(10))
        val num = sc.read(buf)
        Loged.wtf("And num is $num")
        assertEquals(-1, num)
        ssc.close()
        sc.close()*/

        val d = Deck()

        System.out.println("$d")

        var count = 0

        tailrec fun findFixPoint(x: Double = 1.0): Double {
            System.out.println("X: $x and Count: ${++count}")
            log("X: ")
            return if (x == Math.cos(x))
                x
            else
                findFixPoint(Math.cos(x))
        }

        System.out.println("${findFixPoint(10.0)}")
    }

    fun addition(num: Int): Int {
        return if (num == 1) {
            1
        } else {
            addition(num - 1)
        }
    }

    private fun log(msg: String) {
        val stackTraceElement = Thread.currentThread().stackTrace
        var currentIndex = -1
        for (i in stackTraceElement.indices) {
            if (stackTraceElement[i].methodName.compareTo("log") == 0) {
                currentIndex = i + 1
                break
            }
        }
        //currentIndex++

        val fullClassName = stackTraceElement[currentIndex].className
        val methodName = stackTraceElement[currentIndex].methodName
        val fileName = stackTraceElement[currentIndex].fileName
        val lineNumber = stackTraceElement[currentIndex].lineNumber
        val logged = "${Thread.currentThread().name}: \t$msg\tat $fullClassName.$methodName($fileName:$lineNumber)"

        println(logged)
    }

    fun prettyLog(msg: Any?) {
        //the main message to be logged
        var logged = msg.toString()
        //the arrow for the stack trace
        val arrow = "${9552.toChar()}${9655.toChar()}\t"
        //the stack trace
        val stackTraceElement = Thread.currentThread().stackTrace

        val elements = listOf(*stackTraceElement)
        val wanted = elements.filter { it.className.contains(Loged.FILTER_BY_CLASS_NAME) && !it.methodName.contains("prettyLog") }

        var loc = "\n"

        for (i in wanted.indices.reversed()) {
            val fullClassName = wanted[i].className
            //get the method name
            val methodName = wanted[i].methodName
            //get the file name
            val fileName = wanted[i].fileName
            //get the line number
            val lineNumber = wanted[i].lineNumber
            //add this to location in a format where we can click on the number in the console
            loc += "$fullClassName.$methodName($fileName:$lineNumber)"

            if (wanted.size > 1 && i - 1 >= 0) {
                val typeOfArrow: Char =
                        if (i - 1 > 0)
                            9568.toChar() //middle arrow
                        else
                            9562.toChar() //ending arrow
                loc += "\n\t$typeOfArrow$arrow"
            }
        }

        logged += loc

        println(logged + "\n")
    }

    @Test
    fun funTimeTesting() {
        val time = SimpleDateFormat("MM/dd/yyyy E hh:mm:ss a").format(System.currentTimeMillis() + KUtility.timeToNextHourOrHalf())
        log("${KUtility.timeToNextHour()}")
        log(time)
    }

    @Test
    fun logedTest() {
        //Loged.wtf("${addition(10)}")

        System.out.appendHTML().html {
            body {
                div {
                    a("http://kotlinlang.org") {
                        target = ATarget.blank
                        +"Main site"
                    }
                }
            }
        }

        val h = createHTML().html {
            body {
                div {
                    a("http://kotlinlang.org") {
                        target = ATarget.blank
                        +"Main site"
                    }
                }
            }
        }

        System.out.println(h)

        System.out.appendHTML().html {
            head {
                "asdkfjlh"
            }
            body {
                p {
                    "adskfa;sdlkf"
                    script {
                        "alert(\"asdfadsf\");"
                    }
                }
                p {
                    a("http://www.google.com") {
                        target = ATarget.blank + "google"
                    }
                }
            }
        }

    }

    @Test
    fun showTest() {
        val result = runBlocking {

            val show = ShowApi(Source.RECENT_ANIME)

            val list = show.showInfoList

            log("${list.size}")

            val pieced = list.find { it.name == "Conception" }

            val episodeApi = EpisodeApi(pieced!!)

            log(episodeApi.name)

            val episodeList = episodeApi.episodeList

            log("${episodeList.size}")

            //assertEquals("One Piece Episode Count",350, episodeList.size)

            log(episodeApi.description)

        }
        result
        log("Hello")
    }

    @Test
    fun videoLinkTest() {
        log("Here")
        log("Hello")
        /*
        val result = runBlocking {
            //work from webpage   http://st5.anime1.com/[HorribleSubs]%20Tate%20no%20Yuusha%20no%20Nariagari%20-%2007%20[720p]_af.mp4?st=4z3NqVs6tOHbs84kwibNyw&e=1551392688
            //work from here      http://st8.anime1.com/[HorribleSubs] Tate no Yuusha no Nariagari - 07 [720p]_af.mp4?st=3VnviVU26QuVFQPUGv25fg&e=1551394918
            //not work from phone http://st3.anime1.com/[HorribleSubs] Tate no Yuusha no Nariagari - 07 [720p]_af.mp4?st=lkB7Cofu_L4ZEr4Zb-DRrw&e=1551395194
            //not work from phone http://st8.anime1.com/[HorribleSubs] Tate no Yuusha no Nariagari - 07 [720p]_af.mp4?st=gcZaYE5NzbD8KEwL4wqpiQ&e=1551395397
            val urlToUse = "https://www.gogoanime1.com/watch/tate-no-yuusha-no-nariagari/episode/episode-7"
            log(urlToUse)
            val doc = Jsoup.connect(urlToUse).get()
            //val vid = doc.select("div.vmn-video").select("script")
            val htmld = doc.html()//getHtml(urlToUse)
            //Loged.w(vid[1].data())
            //val js = vid[1].data()
            val m = "file: \\\"([^\\\"]+)\\\"," //"(file:(\\s*))+(\"(.*?)\")"
                    .toRegex().toPattern().matcher(htmld)
            while (m.find()) {
                val s = m.group(1)
                log(s)
                //Loged.d(URLEncoder.encode(s, "UTF-8").replace("\\+", "%20"))
            }
        }*/
        //result
        log("Yup")
        val res = runBlocking {
            log("I is here")
            val listOfShows = arrayListOf<String>()
            val url = "https://www.gogoanime1.com/watch/watashi-ni-tenshi-ga-maiorita"
            val doc = Jsoup.connect(url).get()
            val name = doc.select("div.anime-title").text()
            val stuffList = doc.select("ul.check-list").select("li")
            for (i in stuffList) {
                //if(!i.select("a[href^=http]").text().contains(doc.select("div.anime-title").text()))
                val episodeName = i.select("a").text()
                val epName = if (episodeName.contains(name)) {
                    episodeName.substring(name.length)
                } else {
                    episodeName
                }.trim()
                log(epName)
                listOfShows.add(epName)
                //listOfShows.add(ShowInfo(i.select("a[href^=http]").text(), i.select("a[href^=http]").attr("abs:href")))
            }
            log("$listOfShows")
            val c = listOfShows.distinct()
            log("$c")
            //val downloadLink = doc.select("a[download^=http]").attr("abs:download")
            //log(downloadLink)
            log("here too")
        }
        res
        log("Here")
    }

    open class Person(open var name: String? = null,
                      open var age: Int? = null,
                      open var address: Address? = null,
                      open var friend: Friend? = null) {
        override fun toString(): String {
            return "$name, $age\nLives at $address\n${friend ?: ""}"
        }
    }

    data class Address(var street: String? = null,
                       var number: Int? = null,
                       var city: String? = null,
                       var hobby: Hobby? = null) {
        override fun toString(): String {
            return "$number $street, $city\n$hobby"
        }
    }

    data class Hobby(var hobbyName: String? = null) {
        override fun toString(): String {
            return "$hobbyName"
        }
    }

    data class Friend(override var name: String? = null,
                      override var age: Int? = null,
                      override var address: Address? = null,
                      override var friend: Friend? = null) : Person(name, age, address, friend) {

        override fun toString(): String {
            return "\nHis friend is ${super.toString()}"
        }
    }

    //need it
/*fun person(block: (Person) -> Unit): Person {
    val p = Person()
    block(p)
    return p
}*/
//no it
    fun person(block: Person.() -> Unit): Person = Person().apply(block)

    fun Person.address(block: Address.() -> Unit) {
        address = Address().apply(block)
    }

    fun Person.friend(block: Friend.() -> Unit) {
        friend = Friend().apply(block)
    }

    fun Address.hobby(block: Hobby.() -> Unit) {
        hobby = Hobby().apply(block)
    }

    data class PersonB(val name: String,
                       val dateOfBirth: Date,
                       var address: AddressA?) {
        override fun toString(): String {
            return "$name, $dateOfBirth\nLives at $address"
        }
    }

    data class AddressA(val street: String,
                        val number: Int,
                        val city: String) {
        override fun toString(): String {
            return "$number $street, $city"
        }
    }


    fun personB(block: PersonBuilder.() -> Unit): PersonB = PersonBuilder().apply(block).build()


    class PersonBuilder {

        var name: String = ""

        private var dob: Date = Date()
        var dateOfBirth: String = ""
            set(value) {
                dob = SimpleDateFormat("yyyy-MM-dd").parse(value)!!
            }

        private var address: AddressA? = null

        fun addressA(block: AddressBuilder.() -> Unit) {
            address = AddressBuilder().apply(block).build()
        }

        fun build(): PersonB = PersonB(name, dob, address)

    }

    class AddressBuilder {

        var street: String = ""
        var number: Int = 0
        var city: String = ""

        fun build(): AddressA = AddressA(street, number, city)

    }

    // The model now has a non-nullable list
    data class PersonC(val name: String,
                       val dateOfBirth: Date,
                       val addresses: List<AddressA>)

    class PersonBuilderB {

        // ... other properties
        var name = ""
        private var dob: Date = Date()
        var dateOfBirth: String = ""
            set(value) {
                dob = SimpleDateFormat("yyyy-MM-dd").parse(value)!!
            }

        private val addresses = mutableListOf<AddressA>()

        fun address(block: AddressBuilder.() -> Unit) {
            addresses.add(AddressBuilder().apply(block).build())
        }

        fun build(): PersonC = PersonC(name, dob, addresses)

    }

    fun personC(block: PersonBuilderB.() -> Unit): PersonC = PersonBuilderB().apply(block).build()

    @Test
    fun dslTest() {

        val personC = personC {
            name = "John"
            dateOfBirth = "1980-12-01"
            address {
                street = "Main Street"
                number = 12
                city = "London"
            }
            address {
                street = "Dev Avenue"
                number = 42
                city = "Paris"
            }
        }

        val personB = personB {
            name = "John"
            dateOfBirth = "1980-12-01"
            addressA {
                street = "Main Street"
                number = 12
                city = "London"
            }
        }

        System.out.println(personB.toString())

        val person = person {
            name = "John"
            age = 25
            address {
                street = "Main Street"
                number = 42
                city = "London"
                hobby {
                    hobbyName = "Tennis"
                }
            }
            friend {
                name = "Jacob"
                age = 22
                address {
                    street = "Bedford Rd"
                    number = 861
                    city = "Pleasantville"
                    hobby {
                        hobbyName = "Programming"
                    }
                }
            }
        }
        person.name = "Jimmy"
        System.out.println(person.toString())

        val d = Deck.deck {
            card {
                value = 1
                suit = Suit.SPADES
            }
            card {
                value = 2
                suit = Suit.SPADES
            }
            card {
                value = 3
                suit = Suit.SPADES
            }
            card {
                value = 4
                suit = Suit.SPADES
            }
            card {
                value = 5
                suit = Suit.SPADES
            }
            card { card = Card(Suit.SPADES, 6) }
            card(Suit.SPADES, 7)
            card(Suit.HEARTS, 7)
            card(Suit.DIAMONDS, 7)
            card(Suit.CLUBS, 7)
            //randomCard()
        }
        d - Suit.CLUBS
        d - Suit.DIAMONDS

        System.out.println(d.toArrayString())

    }

    @Test
    fun test5() {
        log("Main Thread?")
        runBlocking {
            log("Launch Thread?")
        }
        runBlocking(Dispatchers.Default) {
            log("Default Thread?")
        }
        runBlocking(Dispatchers.IO) {
            log("IO Thread?")
        }
        runBlocking(Dispatchers.Unconfined) {
            log("Unconfined Thread?")
            coroutineContext[Job]
        }
        runBlocking(newSingleThreadContext("asdf")) {
            log("asdf Thread?")
        }
        runBlocking(CoroutineName("Co-name")) {
            parentResponsibilities()
            log("My own name")
        }
        runBlocking(Dispatchers.Default + CoroutineName("test")) {
            println("I'm working in thread ${Thread.currentThread().name}")
            log("Where am I now?")
        }
        runBlocking {
            threadLocalStuff()
        }
        runBlocking {
            channelTest()
        }

    }

    suspend fun channelTest() {
        val channel = Channel<Int>()
        GlobalScope.launch {
            // this might be heavy CPU-consuming computation or async logic, we'll just send five squares
            for (x in 1..5) {
                channel.send(x * x)
            }
        }
        // here we print five received integers:
        repeat(5) { println(channel.receive()) }
        println("Done!")
    }

    suspend fun threadLocalStuff() {
        val threadLocal = ThreadLocal<String?>()
        threadLocal.set("NewThread")
        println("Pre-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        val job = GlobalScope.launch(Dispatchers.Default + threadLocal.asContextElement(value = "launch")) {
            println("Launch start, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
            yield()
            println("After yield, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
        }
        job.join()
        println("Post-main, current thread: ${Thread.currentThread()}, thread local value: '${threadLocal.get()}'")
    }

    suspend fun parentResponsibilities() {
        // launch a coroutine to process some kind of incoming request
        val request = GlobalScope.launch {
            repeat(3) { i ->
                // launch a few children jobs
                launch {
                    delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                    println("Coroutine $i is done")
                }
            }
            println("request: I'm done and I don't explicitly join my children that are still active")
        }
        request.join() // wait for completion of the request, including all its children
        println("Now processing of the request is complete")
    }

    @Test
    fun strangeTest() {

        fun joinWithSpace(vararg args: String): String = String.format("%s ".repeat(args.size).trimEnd(), *args)

        fun joinWith(vararg args: String, separator: String = " "): String = String.format("%s$separator".repeat(args.size).removeSuffix(separator).trimEnd(), *args)

        fun <T> joinWith(vararg args: T, separator: String = " ", block: (T) -> String = { it.toString() }): String =
                String.format("%s$separator".repeat(args.size).removeSuffix(separator).trimEnd(), *args.map(block).toTypedArray())

        prettyLog(joinWith(" ", "asdf", "asdf"))
        prettyLog(joinWith(" ", "asdf", "asdf", separator = "ertw"))

        prettyLog(joinWith(Card.RandomCard, Card.RandomCard, Card.RandomCard, separator = "ertw") { "$it |" })
        prettyLog(joinWith(Card.RandomCard, Card.RandomCard, Card.RandomCard, separator = "ertw"))

    }

    @Test
    fun listTest() {
        val d = Deck().getDeck().toMutableList()
        removeHearts(d)
        prettyLog(d.toString())
    }

    private fun removeHearts(deck: MutableList<Card>) {
        deck.removeAll { it.suit == Suit.HEARTS }
    }

}

