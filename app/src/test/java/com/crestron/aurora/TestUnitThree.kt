package com.crestron.aurora

import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.showapi.Source
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import io.ktor.utils.io.core.String
import kotlinx.coroutines.runBlocking
import org.apache.tools.ant.util.DateUtils
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.lang.Math.floorDiv
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.system.measureTimeMillis

@Suppress("SameParameterValue")
class TestUnitThree {

    @Before
    fun beforeSetup() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
        Loged.UNIT_TESTING = true
        println("Starting at ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(System.currentTimeMillis())}")
    }

    @After
    fun afterSetup() {
        println("Ending at ${SimpleDateFormat("h:mm:ss a", Locale.getDefault()).format(System.currentTimeMillis())}")
    }

    private fun getUrl(html: String): String {
        val r = "}\\('(.+)',(\\d+),(\\d+),'([^']+)'\\.split\\('\\|'\\)".toRegex().find(html)!!
        fun encodeBaseN(num: Int, n: Int): String {
            var num1 = num
            val fullTable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val table = fullTable.substring(0..n)
            if (num1 == 0) return table[0].toString()
            var ret = ""
            while (num1 > 0) {
                ret = (table[num1 % n].toString() + ret)
                num1 = floorDiv(num, n)
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
        return "https:$search"
    }

    private fun getPutLocker(url: String): String = try {
        val doc = Jsoup.connect(url.trim()).get()
        val mix = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().find(doc.toString())!!.groups[1]!!.value
        val doc2 = Jsoup.connect(mix.trim()).get()
        val r = "}\\('(.+)',(\\d+),(\\d+),'([^']+)'\\.split\\('\\|'\\)".toRegex().find(doc2.toString())!!
        fun encodeBaseN(num: Int, n: Int): String {
            var num1 = num
            val fullTable = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            val table = fullTable.substring(0..n)
            if (num1 == 0) return table[0].toString()
            var ret = ""
            while (num1 > 0) {
                ret = (table[num1 % n].toString() + ret)
                num1 = floorDiv(num, n)
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

    @Test
    fun putlo() {
        val f = ShowApi(Source.LIVE_ACTION).showInfoList
        val e = f.find { it.name.contains("HarmonQuest", true) }?.let { EpisodeApi(it) }!!
        Loged.f(e)
        val first = e.episodeList.first()
        Loged.f(first)
        Loged.f(first.getVideoLink())
        Loged.f(first.getVideoLinks())
        Loged.f(first.url)
        //val link = Jsoup.connect(first.url).get()
        //println(link.toString())
        //Loged.f(getUrl(link.toString()))
        //Loged.f(getPutLocker(first.url))
        /*
        println(Jsoup.connect(first?.url).get())
        println("-".repeat(50))
        println(getHtml(first?.url!!))
        println("-".repeat(50))*/
        //println(Jsoup.connect("https://mixdrop.co/e/ruuicksyx").get())
        val s = """
            <!DOCTYPE html><html lang="en"><head><meta http-equiv="Content-Type" content="text/html; charset=utf-8" /><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1" /><meta name="viewport" content="width=device-width; initial-scale=1.0"><meta name="csrf" content="8df0deb677f26db36ae189ab9f43a19f"><!--<link href="https://vjs.zencdn.net/7.3.0/video-js.css" rel="stylesheet">--><link rel="stylesheet" href="/player/video-js.min.css" /><link rel="stylesheet" href="/player/airplay/videojs.airplay.css" /><link rel="stylesheet" href="/player/videoplayer.min.css?v=0.18" /></head><body><script src="https://www.google.com/recaptcha/api.js?render=6LetXaoUAAAAAB6axgg4WLG9oZ_6QLTsFXZj-5sd"></script><script src="https://code.jquery.com/jquery-3.3.1.min.js" integrity="sha256-FgpCb/KJQlLNfOu91ta32o/NMZxltwRo8QtmkMRdAu8=" crossorigin="anonymous"></script><script src="https://vjs.zencdn.net/7.6.6/video.min.js"></script><script src="/player/airplay/videojs.airplay.js"></script><link rel="stylesheet" href="/js/jRange/jquery.range.css" /><script src="/js/jRange/jquery.range-min.js"></script><script src="/player/videoplayer.min.js?v=1.0.67"></script><script src="/js/ads.js"></script><script>MDCore.ref = "ruuicksyx";eval(function(p,a,c,k,e,d){e=function(c){return c.toString(36)};if(!''.replace(/^/,String)){while(c--){d[c.toString(a)]=k[c]||c.toString(a)}k=[function(e){return d[e]}];e=function(){return'\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c])}}return p}('1.p="//2-4.6.7/8/3.9";1.a="//2-4.6.7/b/3.5?2=-c&e=d";1.f="3.5";1.i="2-4";1.j="";1.k="0";1.l="m%n%o%h.g";',26,26,'|MDCore|s|e2d07893ddac08c69d194ac24d4d3c96|delivery3|mp4|mxdcontent|net|thumbs|jpg|vvsr|v|09ngjmoqiwmrWyBCntmpg|1576716665||vfile|com|2Fthewebsite|vserver|remotesub|chromeInject|referrer|http|3A|2F|poster'.split('|'),0,{}))</script><video id="videojs" class="player video-js vjs-default-skin vjs-big-play-centered" style="width: 100% !important;height: 100% !important;" preload="none" crossorigin="anonymous" controls></video><input type="file" onchange="MDCore.addSub(this)" id="subfile" accept=".vtt,.srt" style="display: none"><!-- Tssp--><!-- PopAds.net Popunder Code for mixdrop.co | 2019-12-18,3365713,0,0 --><script type="text/javascript" data-cfasync="false">/*<![CDATA[/* *//* Generated 2019-12-18 18:34:21 for "PopAds%20CGAPIL%20A", len 1917 */ (function(){ var o=window;o["\u005f\u0070\x6f\x70"]=[["s\x69\u0074eI\x64",3365713],["\x6di\x6e\u0042\x69d",0],["\u0070\u006f\u0070\u0075\u006e\u0064\x65\x72s\u0050\u0065\x72\x49P",0],["\x64\x65l\u0061y\u0042\x65\u0074wee\x6e",0],["\x64\u0065fau\u006c\u0074","h\u0074\x74p\u0073\u003a\x2f\u002f\x75\u0074\u0068\u006f\u0072\x6e\u0065\x72.\x69nf\u006f\x2f\x72\x65dir\x65\x63\x74\u003ft\u0069d\x3d\x38\u00314\u0030\x305\u0026\u0072\u0065f=mi\x78dr\u006f\x70.\x63\u006f"],["\u0064e\u0066\x61\u0075l\x74\x50e\u0072\x44ay",0],["\x74\u006fp\x6dostLa\u0079e\x72",!1]];var l=["\x2f\u002f\u00631.\u0070\u006f\u0070\x61\x64\u0073\x2e\u006e\x65\u0074/\u0070\u006fp\x2e\u006a\x73","\u002f\u002f\u0063\u0032\u002e\x70\u006f\x70\x61d\u0073\u002en\x65\u0074\x2f\x70o\u0070\u002e\x6a\u0073","\u002f/\u0077w\x77.ka\x70uaaw\x61\x73\u0065.\x63om/\u0075s\u002e\x6a\u0073","//\x77\u0077w.\u0071\x71\x71\x6d\x6eo\u0070\u0063\x69\x78\x79i.\u0063o\u006d\x2f\u006f\x72\u006a\x2ejs",""],b=0,s,r=function(){if(""==l[b])return;s=o["d\u006fc\u0075m\x65n\u0074"]["c\u0072\u0065\x61\x74\u0065\x45\u006c\u0065\u006de\x6e\u0074"]("s\u0063\u0072\u0069\x70\x74");s["\u0074\u0079\u0070\u0065"]="t\u0065\u0078\u0074\u002f\x6aa\u0076\x61\x73\x63\u0072\x69p\u0074";s["\u0061\x73\u0079nc"]=!0;var g=o["d\u006f\x63um\u0065n\x74"]["\x67\x65\u0074\u0045l\x65me\u006e\u0074\u0073\u0042y\u0054\u0061\u0067\u004e\x61\x6d\u0065"]("\x73\x63\u0072\u0069\u0070t")[0];s["\u0073\x72\x63"]=l[b];if(b<2){s["cro\u0073\x73O\x72i\u0067\x69\u006e"]="\x61\x6e\u006fn\x79\u006d\u006fu\u0073";s["i\u006e\x74\x65\u0067\x72\u0069\x74y"]="\x73\u0068a\x32\x35\u0036-\x583\x337F\x5a\u0079\u0053E\x36\x53\u00427\x58SOB\u0075\u004aw\x53\x47\x34k\x33\u0076Cq\x57\x79\u0049h\x65\x42M\x47\u002b\x6e\u0043\x4d\u0071Ww="};s["\u006f\u006e\x65\x72\u0072\x6f\x72"]=function(){b++;r()};g["\x70\u0061r\x65\x6etNo\u0064\u0065"]["i\u006e\u0073\u0065\x72\u0074B\u0065\u0066\x6f\x72e"](s,g)};r()})();/*]]>/* */</script><script>function MDinjectP3(){ ${'$'}("body").append('<script data-cfasync="false">(function(){var d={url:"https://cadsoks.com/ng7y2swh3?key=17d7f7655624c90e52f1293128c0fd22&psid=MP1P2_1043",delay:-1,max:1,period:30,perpage:-1,id:"p3",};var b=0;lastTimestamp=Math.floor(Date.now()/1000);if(document.addEventListener){document.addEventListener("click",function(g){if(g.target.className.indexOf("dtnoppu")==-1){f()}},true)}else{if(document.attachEvent){document.attachEvent("onclick",function(g){if(g.target&&g.target.className&&g.target.className.indexOf("dtnoppu")==-1){f()}})}else{var a=setInterval(function(){if(typeof(document.body)!="undefined"&&document.body){document.body.onclick=function(g){if(g.target.className.indexOf("dtnoppu")==-1){f()}};clearInterval(a)}},10)}}function f(){var g=Math.floor(Date.now()/1000);if(!localStorage["mp0p"+d.id]||g>localStorage["mp0p"+d.id]){localStorage["mp0p"+d.id]=g+d.period;localStorage["mp0p_counter"+d.id]=0}if(d.max!=-1&&localStorage["mp0p_counter"+d.id]>=d.max){return}if(d.perpage!=-1&&b>=d.perpage){return}if(d.delay>0&&g<(lastTimestamp+d.delay)){return}localStorage["mp0p_counter"+d.id]++;lastTimestamp=Math.floor(Date.now()/1000);b++;window.open(d.url)}function e(i,j,h){var g=new Date();g.setTime(g.getTime()+h);document.cookie=i+"="+j+"; expires="+g.toGMTString()+"; path=/"}function c(g){var j=document.cookie.toString().split("; ");for(var h=0;h<j.length;h++){if(j[h].split("=")[0]==g){return j[h].split("=")[1]}}return false}}());<\/script>') }</script><div style="position:absolute;top:0;left:0;width: 100%;height: 100%;z-index:2147483647" onclick="${'$'}(this).remove();MDinjectP2()"></div><script>function MDinjectP2(){ ${'$'}("body").append('<script data-cfasync="false">(function(){var d={url:"https://www.predictivdisplay.com/jump/next.php?r=2692607",delay:-1,max:1,period:30,perpage:1,id:"p2",};var b=0;lastTimestamp=Math.floor(Date.now()/1000);if(document.addEventListener){document.addEventListener("click",function(g){if(g.target.className.indexOf("dtnoppu")==-1){f()}},true)}else{if(document.attachEvent){document.attachEvent("onclick",function(g){if(g.target&&g.target.className&&g.target.className.indexOf("dtnoppu")==-1){f()}})}else{var a=setInterval(function(){if(typeof(document.body)!="undefined"&&document.body){document.body.onclick=function(g){if(g.target.className.indexOf("dtnoppu")==-1){f()}};clearInterval(a)}},10)}}function f(){var g=Math.floor(Date.now()/1000);if(!localStorage["mp0p"+d.id]||g>localStorage["mp0p"+d.id]){localStorage["mp0p"+d.id]=g+d.period;localStorage["mp0p_counter"+d.id]=0}if(d.max!=-1&&localStorage["mp0p_counter"+d.id]>=d.max){return}if(d.perpage!=-1&&b>=d.perpage){return}if(d.delay>0&&g<(lastTimestamp+d.delay)){return}localStorage["mp0p_counter"+d.id]++;lastTimestamp=Math.floor(Date.now()/1000);b++;window.open(d.url)}function e(i,j,h){var g=new Date();g.setTime(g.getTime()+h);document.cookie=i+"="+j+"; expires="+g.toGMTString()+"; path=/"}function c(g){var j=document.cookie.toString().split("; ");for(var h=0;h<j.length;h++){if(j[h].split("=")[0]==g){return j[h].split("=")[1]}}return false}}());<\/script><div style="position:absolute;top:0;left:0;width: 100%;height: 100%;z-index:2147483647" onclick="${'$'}(this).remove(); MDinjectP3()"></div>') }</script><script>eval(function(p,a,c,k,e,d){e=function(c){return(c<a?'':e(parseInt(c/a)))+((c=c%a)>35?String.fromCharCode(c+29):c.toString(36))};if(!''.replace(/^/,String)){while(c--){d[e(c)]=k[c]||e(c)}k=[function(e){return d[e]}];e=function(){return'\\w+'};c=1};while(c--){if(k[c]){p=p.replace(new RegExp('\\b'+e(c)+'\\b','g'),k[c])}}return p}('${'$'}(P).z(2(){5(2(){${'$'}("6").7("<1 8=\\"9\\" c=\\"a\\" b=\\"4://u.x.m/w-v-p/t.s?q=r\\" f=\\"g:i\\" j=\\"0-d-k 0-l 0-e 0-h\\"></1>")},3);5(2(){${'$'}("6").7("<1 8=\\"9\\" c=\\"a\\" b=\\"4://n.o/A\\" f=\\"g:i\\" j=\\"0-d-k 0-l 0-e 0-h\\"></1>")},3);5(2(){${'$'}("6").7("<1 8=\\"9\\" c=\\"a\\" b=\\"4://y.C/D?E=F&G=H.I\\" f=\\"g:i\\" j=\\"0-d-k 0-l 0-e 0-h\\"></1>")},3);5(2(){${'$'}("6").7("<1 8=\\"9\\" c=\\"a\\" b=\\"4://J.m/K/L\\" f=\\"g:i\\" j=\\"0-d-k 0-l 0-e 0-h\\"></1>")},3);5(2(){${'$'}("6").7("<1 8=\\"9\\" c=\\"a\\" b=\\"4://M.m/B/N\\" f=\\"g:i\\" j=\\"0-d-k 0-l 0-e 0-h\\"></1>")},3);5(2(){${'$'}("6").7("<1 8=\\"9\\" c=\\"a\\" b=\\"4://n.o/O\\" f=\\"g:i\\" j=\\"0-d-k 0-l 0-e 0-h\\"></1>")},3)});',52,52,'allow|iframe|function|120000|https|setTimeout|body|append|width|1366|768|src|height|same|popups|style|display|forms|none|sandbox|origin|scripts|com|zap|buzz|phone|lkid|74203065|html|pp_009309925869|www|watch|smart|gearbest|uthorner|ready|6kr3yJp|213480|info|redirect|tid|814005|ref|mixdrop|co|pmzer|213479|9189|dynamicadx|9190|Y79rkqR|document'.split('|'),0,{}))</script><script async src="https://www.googletagmanager.com/gtag/js?id=UA-142309154-2"></script><script>window.dataLayer = window.dataLayer || [];function gtag(){dataLayer.push(arguments);}gtag('js', new Date());gtag('config', 'UA-142309154-2');</script></body></html>null
        """.trimIndent()
        /*val mix = Jsoup.connect("https://mixdrop.co/e/ruuicksyx").get()//Jsoup.parse(s)
        //println(mix)
        println()
        println(getUrl(mix.toString()))*/

        //https://www.putlocker.fyi/embed-src-v2/NjgzMzk2NA==

        val s1 = """
            <iframe src="https://mixdrop.co/e/ruuicksyx" id="the_frame" width="100%" height="100%" allowfullscreen="true" webkitallowfullscreen="true" mozallowfullscreen="true" scrolling="no" frameborder="0"></iframe>
        """.trimIndent()

        /*val notMix = Jsoup.connect("https://www.putlocker.fyi/embed-src-v2/NjgzMzk2NA==").get()//Jsoup.parse(s)
        val mix1 = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().find(notMix.toString())!!.value
        println(mix1)*/

        //val notMix = Jsoup.connect("https://www.putlocker.fyi/embed-src-v2/NjgzMzk2NA==").get()//Jsoup.parse(s)
        //val mix1 = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().find(s1)!!.groups[1]!!.value
        //println(mix1)

        //val find = r.group()
        //println("$r - $count")
        //Loged.r(r.toList().map { it.destructured.toList() })

        /*val f1 = JsUnpacker(f)
        f1.detect()
        println(f1.unpack())*/
        //val next = "https://cadsoks.com/ng7y2swh3?key=17d7f7655624c90e52f1293128c0fd22&psid=MP1P2_1043"
        //println(Jsoup.connect(next).get())
        //Loged.f(first?.getVideoLinks().toString())
        //Loged.f(first?.getVideoInfo().toString())
        //println(Jsoup.connect("https://www5.putlocker.fyi/show/harmonquest/season-3/episode-8/").get())
        /*Loged.f(e.episodeList)
        Loged.f(e.episodeList.firstOrNull()?.getVideoInfo().toString())
        val firstUrl = e.episodeList.first().url
        println(firstUrl)
        println(Jsoup.connect(firstUrl).get())
        println(getHtml(firstUrl))*/
        /*val anime = ShowApi.getSources(Source.ANIME)
        val cartoon = ShowApi.getSources(Source.CARTOON, Source.CARTOON_MOVIES, Source.DUBBED)
        val liveAction = ShowApi.getSources(Source.LIVE_ACTION)

        Loged.f(EpisodeApi(anime.random()))
        Loged.f(EpisodeApi(cartoon.random()))
        Loged.f(EpisodeApi(liveAction.random()))*/

        /*val f = ShowApi(Source.LIVE_ACTION_MOVIES).showInfoList
        val e = EpisodeApi(f.random())
        Loged.f(e)*/

    }

    private fun Loged.f(
            msg: EpisodeApi, tag: String = msg.name, infoText: String = TAG,
            showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME
    ) = f(listOf(
            "Name: ${msg.name}",
            "Url: ${msg.source.url}",
            "Image: ${msg.image}",
            "Genres: ${msg.genres.joinToString(", ") { it }}",
            "Description: ${msg.description.replace("\n", " ")}",
            "Episodes:",
            *msg.episodeList.map { "      $it" }.toTypedArray()
    ), tag, infoText, showPretty, threadName)

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

    @Test
    fun putMov() {
        val f = ShowApi(Source.LIVE_ACTION_MOVIES).showInfoList
        val e = EpisodeApi(f.random())
        prettyLog(e.episodeList)
        prettyLog(e.episodeList.firstOrNull()?.getVideoInfo())
    }

    @Test
    fun putMov2() {
        val f = ShowApi(Source.LIVE_ACTION_MOVIES).getShowList { println(it) }
        prettyLog(f.size)
    }

    @Test
    fun showAptest() = runBlocking {
        //TODO: Look into multithreading
        val doc: Document = Jsoup.connect("https://www1.putlocker.fyi/a-z-movies").get()
        val alphabet = doc.allElements.select("ul.pagination-az").select("a.page-link")
        val f = alphabet.pmap { p ->
            println(p.attr("abs:href"))
            val page = Jsoup.connect(p.attr("abs:href")).get()
            val listPage = page.allElements.select("li.page-item")
            val lastPage = listPage[listPage.size - 2].text().toInt()
            fun getMovieFromPage(document: Document) = document.allElements.select("div.col-6").map {
                ShowInfo(it.select("span.mov_title").text(), it.select("a.thumbnail").attr("abs:href"))
            }
            (1..lastPage).pmap {
                if (it == 1)
                    getMovieFromPage(page)
                else
                    getMovieFromPage(Jsoup.connect("https://www1.putlocker.fyi/a-z-movies/page/$it/${p.attr("abs:href").split("/").last()}").get())
            }.flatten()
        }.flatten()
        f.showInfo()
    }

    @Test
    fun showApTest2() = runBlocking {
        val doc: Document = Jsoup.connect("https://www1.putlocker.fyi/a-z-movies").get()
        val list = arrayListOf<ShowInfo>()
        val alphabet = doc.allElements.select("ul.pagination-az").select("a.page-link")
        for (p in alphabet) {
            println(p.attr("abs:href") + " and list size is ${list.size}")
            val page = Jsoup.connect(p.attr("abs:href")).get()
            val listPage = page.allElements.select("li.page-item")
            val lastPage = listPage[listPage.size - 2].text().toInt()
            fun getMovieFromPage(document: Document) = list.addAll(document.allElements.select("div.col-6").map {
                ShowInfo(
                        it.select("span.mov_title").text(),
                        it.select("a.thumbnail").attr("abs:href"))
            })
            getMovieFromPage(page)
            for (i in 2..lastPage) {
                getMovieFromPage(
                        Jsoup.connect("https://www1.putlocker.fyi/a-z-movies/page/$i/${p.attr("abs:href").split("/").last()}").get()
                )
            }
        }
        list.showInfo()
    }

    private fun List<ShowInfo>.showInfo() {
        println("$size")
    }

    fun <T, R> Iterable<T>.pmap(
            numThreads: Int = Runtime.getRuntime().availableProcessors() - 2,
            exec: ExecutorService = Executors.newFixedThreadPool(numThreads),
            transform: (T) -> R): List<R> {
        // default size is just an inlined version of kotlin.collections.collectionSizeOrDefault
        val defaultSize = if (this is Collection<*>) this.size else 10
        val destination = Collections.synchronizedList(ArrayList<R>(defaultSize))

        for (item in this) {
            exec.submit { destination.add(transform(item)) }
        }

        exec.shutdown()
        exec.awaitTermination(1, TimeUnit.DAYS)

        return ArrayList<R>(destination)
    }


    open class Person(open var name: String? = null,
                      open var age: Int? = null,
                      open var dob: Date? = null,
                      open var address: Address? = null) {

        val friends: MutableList<Person> = mutableListOf()

        private fun getDateString() = dob?.let { ", ${SimpleDateFormat("MM-dd-yyyy").format(it)}" } ?: ""

        override fun toString(): String {
            return "$name, $age${getDateString()}. Lives at $address. His friends are ${friends.joinToString(", ") { "${it.name}" }}."
        }
    }

    data class Address(var street: String? = null,
                       var number: Int? = null,
                       var city: String? = null,
                       var hobby: Hobby? = null) {
        override fun toString(): String {
            return "$number $street, $city. His hobby is $hobby"
        }
    }

    data class Hobby(var hobbyName: String? = null) {
        override fun toString(): String {
            return "$hobbyName"
        }
    }

    fun person(block: Person.() -> Unit): Person = Person().apply(block)

    fun Person.address(block: Address.() -> Unit) {
        address = Address().apply(block)
    }

    fun Person.friend(block: Person.() -> Unit) {
        friends.add(Person().apply(block))
    }

    fun Address.hobby(block: Hobby.() -> Unit) {
        hobby = Hobby().apply(block)
    }

    fun personBuilder(block: PersonBuilder.() -> Unit): Person = PersonBuilder().apply(block).build()

    class PersonBuilder {
        var name: String = ""
        var age: Int = 0
        private var dob: Date = Date()
        var dateOfBirth: String = ""
            set(value) {
                dob = SimpleDateFormat("MM-dd-yyyy").parse(value)!!
            }
        private var address: Address? = null
        private var friendList = mutableListOf<Person>()

        fun address(block: AddressBuilder.() -> Unit) {
            address = AddressBuilder().apply(block).build()
        }

        fun friend(block: PersonBuilder.() -> Unit) {
            friendList.add(PersonBuilder().apply(block).build())
        }

        fun build(): Person = Person(name, age, dob, address).apply {
            friends.addAll(friendList)
        }
    }

    class AddressBuilder {
        var street: String = ""
        var number: Int = 0
        var city: String = ""
        private var hobby: Hobby? = null
        fun hobby(block: Hobby.() -> Unit) {
            hobby = Hobby().apply(block)
        }

        fun build(): Address = Address(street, number, city, hobby)
    }

    @Test
    fun dslTesting() {

        val person2 = personBuilder {
            name = "John"
            age = 34
            dateOfBirth = "12-4-2014"
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
                dateOfBirth = "12-31-1995"
                address {
                    street = "Bedford Rd"
                    number = 861
                    city = "Pleasantville"
                    hobby {
                        hobbyName = "Programming"
                    }
                }
            }
            friend {
                name = "Jordan"
                age = 24
                dateOfBirth = "12-4-1974"
                address {
                    street = "Main Rd"
                    number = 861
                    city = "DC"
                }
            }
        }

        prettyLog(person2)

        val person = person {
            name = "John"
            age = 25
            dob = SimpleDateFormat("MM-dd-yyyy").parse("1-1-1985")!!
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
                friends.add(this@person)
            }
        }
        prettyLog(person)
        prettyLog(person.friends[0])
    }

    @Before
    fun setUp() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
        Loged.WITH_THREAD_NAME = true
    }

    @Test
    fun sortTesting() = runBlocking {

        val endDeck = Deck(shuffler = true).getDeck().sorted().map { it.value }
        prettyLog(endDeck)

        sortThings("Default") {
            sort()
        }
        sortThings("Bubble") {
            BubbleSort().perform(this)
        }
        sortThings("Insertion") {
            InsertionSort().perform(this)
        }
        sortThings("Merge") {
            MergeSort<Card>().sort(this)
        }
        sortThings("Quick") {
            QuickSort().perform(this)
        }
        sortThings("Selection") {
            SelectionSort().perform(this)
        }
        sortThings("Shell") {
            ShellSort().perform(this)
        }

    }

    private fun sortThings(type: String, sortMethod: MutableList<Card>.() -> Unit) {
        var s = "$type\n"
        val cNum = measureTimeMillis {
            val d = Deck(shuffler = true, numberOfDecks = 50).getDeck()
            s += d.map { it.value }
            d.sortMethod()
            s += "\n" + d.map { it.value }
        }
        s += "\n$cNum milliseconds\n${cNum.toElapsed()}"
        prettyLog(s)
    }

    @Test
    fun kotlinTest() {
        val data = intArrayOf(2147483647, 2147483647, 2147483647)
        prettyLog(indexOfMax(data))
    }

    private fun indexOfMax(a: IntArray): Int? {
        return try {
            a.lastIndexOf(a.max()!!)
        } catch (e: NullPointerException) {
            null
        }
    }

    @Test
    fun searchTesting() = runBlocking {
        val c = Card.RandomCard
        prettyLog(c)
        val d = Deck().getDeck()

        val cNum = binarySearch(c, d)
        prettyLog("$cNum milliseconds")
        prettyLog(cNum.toElapsed())

        val cNum2 = linearSearch(c, d)
        prettyLog("$cNum2 milliseconds")
        prettyLog(cNum2.toElapsed())
    }

    private fun linearSearch(c: Card, d: ArrayList<Card>) = measureTimeMillis {
        var c1: Card? = null
        for (i in d) {
            if (i == c) {
                c1 = i
                break
            }
        }
        prettyLog(c1)
    }

    private fun binarySearch(c: Card, d: ArrayList<Card>) = measureTimeMillis {
        val c1 = d.binarySearch(c)
        prettyLog(c1)
    }

    private fun Long.toElapsed(): String = DateUtils.formatElapsedTime(this)

    private fun prettyLog(msg: Any?) {
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

}

fun <T> MutableList<T>.exch(i: Int, j: Int) {
    val tmp = this[i]
    this[i] = this[j]
    this[j] = tmp
}

@Retention(AnnotationRetention.SOURCE)
annotation class ComparisonSort

@Retention(AnnotationRetention.SOURCE)
annotation class StableSort

@Retention(AnnotationRetention.SOURCE)
annotation class UnstableSort

abstract class AbstractSortStrategy {
    abstract fun <T : Comparable<T>> perform(arr: MutableList<T>)
}

@ComparisonSort
@StableSort
class BubbleSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        var exchanged: Boolean
        do {
            exchanged = false
            for (i in 1 until arr.size) {
                if (arr[i] < arr[i - 1]) {
                    arr.exch(i, i - 1)
                    exchanged = true
                }
            }
        } while (exchanged)
    }
}

@ComparisonSort
@StableSort
class InsertionSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        for (i in 1 until arr.size) {
            for (j in i downTo 1) {
                if (arr[j - 1] < arr[j]) break
                arr.exch(j, j - 1)
            }
        }
    }
}

@ComparisonSort
@StableSort
class MergeSort<T : Comparable<T>> {

    fun sort(list: MutableList<T>) {
        val newList = mergeSort(list)
        list.clear()
        list.addAll(newList)
    }

    private fun mergeSort(list: List<T>): List<T> {
        if (list.size <= 1) {
            return list
        }

        val middle = list.size / 2
        val left = list.subList(0, middle)
        val right = list.subList(middle, list.size)
        return merge(mergeSort(left), mergeSort(right)).toMutableList()
    }

    private fun merge(left: List<T>, right: List<T>): List<T> {
        var indexLeft = 0
        var indexRight = 0
        val newList: MutableList<T> = mutableListOf()

        while (indexLeft < left.count() && indexRight < right.count()) {
            if (left[indexLeft] <= right[indexRight]) {
                newList.add(left[indexLeft])
                indexLeft++
            } else {
                newList.add(right[indexRight])
                indexRight++
            }
        }

        while (indexLeft < left.size) {
            newList.add(left[indexLeft])
            indexLeft++
        }

        while (indexRight < right.size) {
            newList.add(right[indexRight])
            indexRight++
        }

        return newList
    }
}

@ComparisonSort
@UnstableSort
class QuickSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        sort(arr, 0, arr.size - 1)
    }

    private fun <T : Comparable<T>> sort(arr: MutableList<T>, lo: Int, hi: Int) {
        if (hi <= lo) return
        val j = partition(arr, lo, hi)
        sort(arr, lo, j - 1)
        sort(arr, j + 1, hi)
    }

    private fun <T : Comparable<T>> partition(arr: MutableList<T>, lo: Int, hi: Int): Int {
        var i = lo
        var j = hi + 1
        val v = arr[lo]
        while (true) {
            while (arr[++i] < v) {
                if (i == hi) break
            }
            while (v < arr[--j]) {
                if (j == lo) break
            }
            if (j <= i) break
            arr.exch(j, i)
        }
        arr.exch(j, lo)
        return j
    }
}

@ComparisonSort
@UnstableSort
class SelectionSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        for (i in arr.indices) {
            var min = i
            for (j in i + 1 until arr.size) {
                if (arr[j] < arr[min]) {
                    min = j
                }
            }
            if (min != i) arr.exch(min, i)
        }
    }
}

@ComparisonSort
@StableSort
class ShellSort : AbstractSortStrategy() {
    override fun <T : Comparable<T>> perform(arr: MutableList<T>) {
        var h = 1
        while (h < arr.size / 3) {
            h = h * 3 + 1
        }

        while (h >= 1) {
            for (i in h until arr.size) {
                for (j in i downTo h step h) {
                    if (arr[j - h] < arr[j]) break
                    arr.exch(j, j - h)
                }
            }
            h /= 3
        }
    }
}

class JsUnpacker(packedJS: String?) {
    private var packedJS: String? = null
    /**
     * Detects whether the javascript is P.A.C.K.E.R. coded.
     *
     * @return true if it's P.A.C.K.E.R. coded.
     */
    fun detect(): Boolean {
        val js = packedJS!!.replace(" ", "")
        val p: Pattern = Pattern.compile("eval\\(function\\(p,a,c,k,e,(?:r|d)")
        val m: Matcher = p.matcher(js)
        return m.find()
    }

    /**
     * Unpack the javascript
     *
     * @return the javascript unpacked or null.
     */
    fun unpack(): String? {
        val js = String(packedJS!!.toByteArray())
        try {
            var p: Pattern = Pattern.compile("\\}\\s*\\('(.*)',\\s*(.*?),\\s*(\\d+),\\s*'(.*?)'\\.split\\('\\|'\\)", Pattern.DOTALL)
            var m: Matcher = p.matcher(js)
            if (m.find() && m.groupCount() == 4) {
                val payload: String = m.group(1)!!.replace("\\'", "'")
                val radixStr: String = m.group(2)!!
                val countStr: String = m.group(3)!!
                val symtab: List<String> = m.group(4)!!.split("\\|")
                var radix = 36
                var count = 0
                try {
                    radix = radixStr.toInt()
                } catch (e: Exception) {
                }
                try {
                    count = countStr.toInt()
                } catch (e: Exception) {
                }
                if (symtab.size != count) {
                    throw Exception("Unknown p.a.c.k.e.r. encoding")
                }
                val unbase = Unbase(radix)
                p = Pattern.compile("\\b\\w+\\b")
                m = p.matcher(payload)
                val decoded = java.lang.StringBuilder(payload)
                var replaceOffset = 0
                while (m.find()) {
                    val word: String = m.group(0)!!
                    val x = unbase.unbase(word)
                    var value: String? = null
                    if (x < symtab.size) {
                        value = symtab[x]
                    }
                    if (value != null && value.length > 0) {
                        decoded.replace(m.start() + replaceOffset, m.end() + replaceOffset, value)
                        replaceOffset += value.length - word.length
                    }
                }
                return decoded.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    private inner class Unbase internal constructor(private val radix: Int) {
        private val ALPHABET_62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        private val ALPHABET_95 = " !\"#$%&\\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\\\]^_`abcdefghijklmnopqrstuvwxyz{|}~"
        private var alphabet: String? = null
        private var dictionnary: HashMap<String, Int>? = null
        fun unbase(str: String): Int {
            var ret = 0
            if (alphabet == null) {
                ret = str.toInt(radix)
            } else {
                val tmp = java.lang.StringBuilder(str).reverse().toString()
                for (i in 0 until tmp.length) {
                    ret += (Math.pow(radix.toDouble(), i.toDouble()) * dictionnary!![tmp.substring(i, i + 1)]!!).toInt()
                }
            }
            return ret
        }

        init {
            if (radix > 36) {
                if (radix < 62) {
                    alphabet = ALPHABET_62.substring(0, radix)
                } else if (radix > 62 && radix < 95) {
                    alphabet = ALPHABET_95.substring(0, radix)
                } else if (radix == 62) {
                    alphabet = ALPHABET_62
                } else if (radix == 95) {
                    alphabet = ALPHABET_95
                }
                dictionnary = HashMap(95)
                for (i in 0 until alphabet!!.length) {
                    dictionnary!![alphabet!!.substring(i, i + 1)] = i
                }
            }
        }
    }

    /**
     * @param  packedJS javascript P.A.C.K.E.R. coded.
     */
    init {
        this.packedJS = packedJS
    }
}