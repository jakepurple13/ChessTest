package com.crestron.aurora

import com.crestron.aurora.boardgames.yahtzee.Dice
import com.crestron.aurora.boardgames.yahtzee.YahtzeeScores
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.Source
import com.crestron.aurora.utilities.KUtility
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Suit
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

typealias cad = Card

fun cadTest() {
    val d1 = cad(Suit.DIAMONDS, 3)
}

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Before
    fun setUp() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
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
        for(i in d) {
            listOfShows+=Showsa(i.text(), i.attr("abs:href"))
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
        if(d.find()) {
            val f = Jsoup.connect(d.group(1)!!).get()
            val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(f.toString())
            if(a.find()) {
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
        for(i in seasons) {
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
        for(i in episodes) {
            val secondUrl = "https://www.putlocker.fyi/embed-src/${i.attr("data-pid")}"
            val doc = Jsoup.connect(secondUrl).get()
            val d = "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().toPattern().matcher(doc.toString())
            if(d.find()) {
                val f = Jsoup.connect(d.group(1)!!).get()
                val a = "<p[^>]+id=\"videolink\">([^>]*)<\\/p>".toRegex().toPattern().matcher(f.toString())
                if(a.find()) {
                    val lastUrl = "https://verystream.com/gettoken/${a.group(1)!!}?mime=true"
                    val ep = Eps(i.text(), i.attr("abs:href"), lastUrl)
                    log("$ep")
                }
            }
        }
    }

    val putLockHtml = "<!doctype html>\n" +
            "<html lang=\"en\">\n" +
            " <head> \n" +
            "  <meta charset=\"utf-8\"> \n" +
            "  <meta name=\"robots\" content=\"index, follow\"> \n" +
            "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\"> \n" +
            "  <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/css/bootstrap.min.css\"> \n" +
            "  <link rel=\"stylesheet\" href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css\"> \n" +
            "  <link href=\"https://www.putlocker.fyi/images/favicon.ico\" rel=\"icon\"> \n" +
            "  <link href=\"https://www.putlocker.fyi/images/favicon.png\" rel=\"apple-touch-icon\"> \n" +
            "  <link href=\"https://www.putlocker.fyi/images/favicon.png\" rel=\"fluid-icon\"> \n" +
            "  <title>Watch Silicon Valley Online Free | Putlocker</title> \n" +
            "  <!-- This site is optimized with the Yoast SEO plugin v11.5 - https://yoast.com/wordpress/plugins/seo/ --> \n" +
            "  <meta name=\"description\" content=\"Watch Silicon Valley Online on Putlocker. Put locker is the way to watch Silicon Valley movie in HD. Watch Silicon Valley in HD.\"> \n" +
            "  <link rel=\"canonical\" href=\"https://www.putlocker.fyi/show/silicon-valley/\"> \n" +
            "  <meta property=\"og:locale\" content=\"en_US\"> \n" +
            "  <meta property=\"og:type\" content=\"article\"> \n" +
            "  <meta property=\"og:title\" content=\"Watch Silicon Valley Online Free | Putlocker\"> \n" +
            "  <meta property=\"og:description\" content=\"Watch Silicon Valley Online on Putlocker. Put locker is the way to watch Silicon Valley movie in HD. Watch Silicon Valley in HD.\"> \n" +
            "  <meta property=\"og:url\" content=\"https://www.putlocker.fyi/show/silicon-valley/\"> \n" +
            "  <meta property=\"og:site_name\" content=\"Putlocker\"> \n" +
            "  <meta name=\"twitter:card\" content=\"summary\"> \n" +
            "  <meta name=\"twitter:description\" content=\"Watch Silicon Valley Online on Putlocker. Put locker is the way to watch Silicon Valley movie in HD. Watch Silicon Valley in HD.\"> \n" +
            "  <meta name=\"twitter:title\" content=\"Watch Silicon Valley Online Free | Putlocker\"> \n" +
            "  <meta name=\"twitter:image\" content=\"https://www.putlocker.fyi/uploads/watch-silicon-valley-tv-show-online-putlocker.jpg\"> \n" +
            "  <script type=\"application/ld+json\" class=\"yoast-schema-graph yoast-schema-graph--main\">{\"@context\":\"https://schema.org\",\"@graph\":[{\"@type\":\"Organization\",\"@id\":\"https://www.putlocker.fyi/#organization\",\"name\":\"\",\"url\":\"https://www.putlocker.fyi/\",\"sameAs\":[]},{\"@type\":\"WebSite\",\"@id\":\"https://www.putlocker.fyi/#website\",\"url\":\"https://www.putlocker.fyi/\",\"name\":\"Putlocker\",\"publisher\":{\"@id\":\"https://www.putlocker.fyi/#organization\"},\"potentialAction\":{\"@type\":\"SearchAction\",\"target\":\"https://www.putlocker.fyi/?s={search_term_string}\",\"query-input\":\"required name=search_term_string\"}},{\"@type\":\"ImageObject\",\"@id\":\"https://www.putlocker.fyi/show/silicon-valley/#primaryimage\",\"url\":\"https://www.putlocker.fyi/uploads/watch-silicon-valley-tv-show-online-putlocker.jpg\",\"width\":182,\"height\":268},{\"@type\":\"WebPage\",\"@id\":\"https://www.putlocker.fyi/show/silicon-valley/#webpage\",\"url\":\"https://www.putlocker.fyi/show/silicon-valley/\",\"inLanguage\":\"en-US\",\"name\":\"Watch Silicon Valley Online Free | Putlocker\",\"isPartOf\":{\"@id\":\"https://www.putlocker.fyi/#website\"},\"primaryImageOfPage\":{\"@id\":\"https://www.putlocker.fyi/show/silicon-valley/#primaryimage\"},\"datePublished\":\"2016-11-16T22:44:20+00:00\",\"dateModified\":\"2017-02-05T12:22:59+00:00\",\"description\":\"Watch Silicon Valley Online on Putlocker. Put locker is the way to watch Silicon Valley movie in HD. Watch Silicon Valley in HD.\"}]}</script> \n" +
            "  <!-- / Yoast SEO plugin. --> \n" +
            "  <link rel=\"dns-prefetch\" href=\"//ajax.googleapis.com\"> \n" +
            "  <link rel=\"dns-prefetch\" href=\"//s.w.org\"> \n" +
            "  <link rel=\"alternate\" type=\"application/rss+xml\" title=\"Putlocker » Silicon Valley Comments Feed\" href=\"https://www.putlocker.fyi/show/silicon-valley/feed/\"> \n" +
            "  <link rel=\"stylesheet\" id=\"putlocker-style-css\" href=\"https://www.putlocker.fyi/wp-content/themes/putlocker/style.css?ver=0.1.3\" type=\"text/css\" media=\"all\"> \n" +
            "  <link rel=\"https://api.w.org/\" href=\"https://www.putlocker.fyi/wp-json/\"> \n" +
            "  <link rel=\"EditURI\" type=\"application/rsd+xml\" title=\"RSD\" href=\"https://www.putlocker.fyi/xmlrpc.php?rsd\"> \n" +
            "  <meta name=\"generator\" content=\"WordPress 5.2.2\"> \n" +
            "  <link rel=\"shortlink\" href=\"https://www.putlocker.fyi/?p=90942\"> \n" +
            "  <link rel=\"alternate\" type=\"application/json+oembed\" href=\"https://www.putlocker.fyi/wp-json/oembed/1.0/embed?url=https%3A%2F%2Fwww.putlocker.fyi%2Fshow%2Fsilicon-valley%2F\"> \n" +
            "  <link rel=\"alternate\" type=\"text/xml+oembed\" href=\"https://www.putlocker.fyi/wp-json/oembed/1.0/embed?url=https%3A%2F%2Fwww.putlocker.fyi%2Fshow%2Fsilicon-valley%2F&amp;format=xml\"> \n" +
            "  <style type=\"text/css\">.recentcomments a{display:inline !important;padding:0 !important;margin:0 !important;}</style> \n" +
            " </head> \n" +
            " <body> \n" +
            "  <header> \n" +
            "   <nav class=\"navbar navbar-default navbar-toggleable-lg navbar-expand-lg fixed-top navbar-inverse\" id=\"navbar-site\"> \n" +
            "    <div class=\"container\"> \n" +
            "     <div class=\"navbar-header\"> \n" +
            "      <a class=\"navbar-brand site-logo-img\" href=\"/\"><img src=\"https://www.putlocker.fyi/images/logo-2.png\" class=\"img-fluid\" id=\"logo-img\"></a> \n" +
            "      <button class=\"navbar-toggler pull-right\" type=\"button\" data-toggle=\"collapse\" data-target=\"#top-page-search\" aria-controls=\"top-page-search\" aria-expanded=\"false\" aria-label=\"Toggle Search\"> <i class=\"fa fa-search search-icon\"></i> </button> \n" +
            "      <button class=\"navbar-toggler pull-right\" type=\"button\" data-toggle=\"collapse\" data-target=\"#top-page-nav\" aria-controls=\"top-page-nav\" aria-expanded=\"false\" aria-label=\"Toggle navigation\"> <i class=\"fa fa-bars search-icon\"></i> </button> \n" +
            "     </div> \n" +
            "     <div class=\"collapse navbar-collapse\" id=\"top-page-nav\"> \n" +
            "      <ul class=\"nav navbar-nav mr-auto\"> \n" +
            "       <li class=\"nav-item\"><a class=\"nav-link\" href=\"/putlocker/\" title=\"Popular Movies\">Home</a></li> \n" +
            "       <li class=\"nav-item\"><a class=\"nav-link\" href=\"/featured-movies/\" title=\"Popular Movies\">Popular Movies</a></li> \n" +
            "       <li class=\"nav-item\"><a class=\"nav-link\" href=\"/featured-tv/\" title=\"Popular Shows\">Popular Shows</a></li> \n" +
            "       <li class=\"nav-item\"><a class=\"nav-link\" href=\"/recent-episodes/\" title=\"New Series\">New Series</a></li> \n" +
            "       <li class=\"nav-item\"><a class=\"nav-link\" href=\"/a-z-movies/\" title=\"A-Z Movies\">A-Z Movies</a></li> \n" +
            "       <li class=\"nav-item\"><a class=\"nav-link\" href=\"/a-z-shows/\" title=\"A-Z Shows\">A-Z Shows</a></li> \n" +
            "      </ul> \n" +
            "     </div> \n" +
            "     <div class=\"collapse navbar-collapse\" id=\"top-page-search\"> \n" +
            "      <form action=\"https://www.putlocker.fyi/\" method=\"get\" class=\"navbar-form navbar-right\"> \n" +
            "       <fieldset> \n" +
            "        <div class=\"input-group\"> \n" +
            "         <input type=\"text\" name=\"s\" id=\"search\" placeholder=\"Search\" value=\"\" class=\"form-control\" maxlength=\"99\" autocomplete=\"off\"> \n" +
            "         <span class=\"input-group-btn\"> <button type=\"submit\" class=\"btn btn-light\">Search</button> </span> \n" +
            "        </div> \n" +
            "       </fieldset> \n" +
            "       <div class=\"srch-sgst\" id=\"srch-sgst-menu\" style=\"display: block;\"> \n" +
            "       </div> \n" +
            "      </form> \n" +
            "     </div> \n" +
            "     <style type=\"text/css\">\n" +
            "                                        .navbar-collapse {\n" +
            "                        width: auto !important;\n" +
            "                    }\n" +
            "                </style> \n" +
            "    </div> \n" +
            "   </nav> \n" +
            "  </header> \n" +
            "  <div id=\"wrapper\" class=\"container\"> \n" +
            "   <div class=\"row\"> \n" +
            "    <div class=\"col-12 mb-0 mt-2\"> \n" +
            "     <div class=\"alert alert-dark alert-dismissible fade show\" role=\"alert\"> \n" +
            "      <span><i class=\"fa fa-blink fa-bullseye\" style=\"margin: 0 5px;\"></i><strong>www.putlocker.fyi</strong> is our new domain, Bookmark it &amp; Share it with your friends!</span> \n" +
            "      <button type=\"button\" class=\"close\" data-dismiss=\"alert\" aria-label=\"Close\"> <span aria-hidden=\"true\">×</span> </button> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </div> \n" +
            "   <div class=\"addthis_inline_share_toolbox\" style=\"margin: -5px 0 5px 0 !important;\"></div> \n" +
            "   <div class=\"row\"> \n" +
            "    <div class=\"col-lg-12\"> \n" +
            "     <h1 class=\"page-header\">Watch Silicon Valley Online</h1> \n" +
            "    </div> \n" +
            "    <div class=\"col-lg-12\"> \n" +
            "     <ol class=\"breadcrumb\"> \n" +
            "      <li class=\"breadcrumb-item\"><a href=\"/\">Home</a></li> \n" +
            "      <li class=\"breadcrumb-item\"><a href=\"/a-z-shows/\">Series</a></li> \n" +
            "      <li class=\"breadcrumb-item\"><a href=\"https://www.putlocker.fyi/show/silicon-valley/\">Silicon Valley</a></li> \n" +
            "     </ol> \n" +
            "     <div class=\"thumb pull-left\"> \n" +
            "      <img src=\"https://www.putlocker.fyi/uploads/watch-silicon-valley-tv-show-online-putlocker.jpg\" alt=\"\"> \n" +
            "     </div> \n" +
            "     <div class=\"mov-desc\"> \n" +
            "      <p>IMDB Rating: 8.5/10 from 64,030 votes</p> \n" +
            "      <p>Release: <a href=\"https://www.putlocker.fyi/showyear/2014/\">2014</a> / <a href=\"https://www.putlocker.fyi/show/silicon-valley/\">Silicon Valley</a></p> \n" +
            "      <p>Genre: <a href=\"https://www.putlocker.fyi/showgenre/comedy/\" rel=\"tag\">Comedy</a></p> \n" +
            "      <p>Director: <a href=\"https://www.putlocker.fyi/showdirector/john-altschuler/\" rel=\"tag\">John Altschuler</a></p> \n" +
            "      <p>Stars: <a href=\"https://www.putlocker.fyi/showstar/josh-brener/\" rel=\"tag\">Josh Brener</a>, <a href=\"https://www.putlocker.fyi/showstar/martin-starr/\" rel=\"tag\">Martin Starr</a>, <a href=\"https://www.putlocker.fyi/showstar/t-j-miller/\" rel=\"tag\">T.J. Miller</a>, <a href=\"https://www.putlocker.fyi/showstar/thomas-middleditch/\" rel=\"tag\">Thomas Middleditch</a></p> \n" +
            "      <p>Synopsis: Watch Silicon Valley online free. In Silicon Valley Putlocker Full Episodes, In the high-tech gold rush of modern Silicon Valley, the people most qualified to succeed are the least capable of handling success. Partially inspired by Mike Judge’s own experiences as a Silicon Valley engineer in the late ‘80s, Silicon Valley is an American sitcom that centers around six programmers who are living together and trying to make it big in the Silicon Valley.</p> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"col-lg-12\"> \n" +
            "     <div class=\"row\">\n" +
            "      <div class=\"col-lg-12\">\n" +
            "       <h2> <a class=\"btn-season\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/\" title=\"Silicon Valley Season 5\"><strong>Silicon Valley Season 5</strong></a></h2>\n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\"> \n" +
            "      <div class=\"col-lg-12\"> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/episode-8/\" title=\"Silicon Valley Season 5 Episode 8 - Fifty-One Percent\" data-pid=\"390341\"><strong>Silicon Valley Season 5 Episode 8</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/episode-7/\" title=\"Silicon Valley Season 5 Episode 7 - Initial Coin Offering\" data-pid=\"386235\"><strong>Silicon Valley Season 5 Episode 7</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/episode-6/\" title=\"Silicon Valley Season 5 Episode 6 - Artificial Emotional Intelligence\" data-pid=\"386238\"><strong>Silicon Valley Season 5 Episode 6</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/episode-5/\" title=\"Silicon Valley Season 5 Episode 5 - Facial Recognition\" data-pid=\"384769\"><strong>Silicon Valley Season 5 Episode 5</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/episode-4/\" title=\"Silicon Valley Season 5 Episode 4 - Tech Evangelist\" data-pid=\"383974\"><strong>Silicon Valley Season 5 Episode 4</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/episode-3/\" title=\"Silicon Valley Season 5 Episode 3 - Chief Operating Officer\" data-pid=\"383042\"><strong>Silicon Valley Season 5 Episode 3</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/episode-2/\" title=\"Silicon Valley Season 5 Episode 2 - Reorientation\" data-pid=\"381692\"><strong>Silicon Valley Season 5 Episode 2</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-5/episode-1/\" title=\"Silicon Valley Season 5 Episode 1 - Grow Fast or Die Slow\" data-pid=\"380018\"><strong>Silicon Valley Season 5 Episode 1</strong></a> \n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\">\n" +
            "      <div class=\"col-lg-12\">\n" +
            "       <h2> <a class=\"btn-season\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/\" title=\"Silicon Valley Season 4\"><strong>Silicon Valley Season 4</strong></a></h2>\n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\"> \n" +
            "      <div class=\"col-lg-12\"> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-10/\" title=\"Silicon Valley Season 4 Episode 10 - Server Error\" data-pid=\"312825\"><strong>Silicon Valley Season 4 Episode 10</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-9/\" title=\"Silicon Valley Season 4 Episode 9 - Hooli-Con\" data-pid=\"311913\"><strong>Silicon Valley Season 4 Episode 9</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-8/\" title=\"Silicon Valley Season 4 Episode 8 - The Keenan Vortex\" data-pid=\"307872\"><strong>Silicon Valley Season 4 Episode 8</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-7/\" title=\"Silicon Valley Season 4 Episode 7 - The Patent Troll\" data-pid=\"306604\"><strong>Silicon Valley Season 4 Episode 7</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-6/\" title=\"Silicon Valley Season 4 Episode 6 - Customer Service\" data-pid=\"305747\"><strong>Silicon Valley Season 4 Episode 6</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-5/\" title=\"Silicon Valley Season 4 Episode 5 - The Blood Boy\" data-pid=\"305749\"><strong>Silicon Valley Season 4 Episode 5</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-4/\" title=\"Silicon Valley Season 4 Episode 4 - Teambuilding Exercise\" data-pid=\"303561\"><strong>Silicon Valley Season 4 Episode 4</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-3/\" title=\"Silicon Valley Season 4 Episode 3 - Episode #4.3\" data-pid=\"302169\"><strong>Silicon Valley Season 4 Episode 3</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-2/\" title=\"Silicon Valley Season 4 Episode 2 - Terms of Service\" data-pid=\"300889\"><strong>Silicon Valley Season 4 Episode 2</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-4/episode-1/\" title=\"Silicon Valley Season 4 Episode 1 - Episode #4.1\" data-pid=\"299365\"><strong>Silicon Valley Season 4 Episode 1</strong></a> \n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\">\n" +
            "      <div class=\"col-lg-12\">\n" +
            "       <h2> <a class=\"btn-season\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/\" title=\"Silicon Valley Season 3\"><strong>Silicon Valley Season 3</strong></a></h2>\n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\"> \n" +
            "      <div class=\"col-lg-12\"> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-10/\" title=\"Silicon Valley Season 3 Episode 10 - The Uptick\" data-pid=\"90973\"><strong>Silicon Valley Season 3 Episode 10</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-9/\" title=\"Silicon Valley Season 3 Episode 9 - Daily Active Users\" data-pid=\"90972\"><strong>Silicon Valley Season 3 Episode 9</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-8/\" title=\"Silicon Valley Season 3 Episode 8 - Bachman's Earning's Over-Ride\" data-pid=\"90971\"><strong>Silicon Valley Season 3 Episode 8</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-7/\" title=\"Silicon Valley Season 3 Episode 7 - To Build a Better Beta\" data-pid=\"90970\"><strong>Silicon Valley Season 3 Episode 7</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-6/\" title=\"Silicon Valley Season 3 Episode 6 - Bachmanity Insanity\" data-pid=\"90969\"><strong>Silicon Valley Season 3 Episode 6</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-5/\" title=\"Silicon Valley Season 3 Episode 5 - The Empty Chair\" data-pid=\"90968\"><strong>Silicon Valley Season 3 Episode 5</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-4/\" title=\"Silicon Valley Season 3 Episode 4 - Maleant Data Systems Solutions\" data-pid=\"90967\"><strong>Silicon Valley Season 3 Episode 4</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-3/\" title=\"Silicon Valley Season 3 Episode 3 - Meinertzhagen's Haversack\" data-pid=\"90966\"><strong>Silicon Valley Season 3 Episode 3</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-2/\" title=\"Silicon Valley Season 3 Episode 2 - Two in the Box\" data-pid=\"90965\"><strong>Silicon Valley Season 3 Episode 2</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-3/episode-1/\" title=\"Silicon Valley Season 3 Episode 1 - Founder Friendly\" data-pid=\"90963\"><strong>Silicon Valley Season 3 Episode 1</strong></a> \n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\">\n" +
            "      <div class=\"col-lg-12\">\n" +
            "       <h2> <a class=\"btn-season\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/\" title=\"Silicon Valley Season 2\"><strong>Silicon Valley Season 2</strong></a></h2>\n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\"> \n" +
            "      <div class=\"col-lg-12\"> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-10/\" title=\"Silicon Valley Season 2 Episode 10 - Two Days of the Condor\" data-pid=\"90962\"><strong>Silicon Valley Season 2 Episode 10</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-9/\" title=\"Silicon Valley Season 2 Episode 9 - Binding Arbitration\" data-pid=\"90961\"><strong>Silicon Valley Season 2 Episode 9</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-8/\" title=\"Silicon Valley Season 2 Episode 8 - White Hat/Black Hat\" data-pid=\"90960\"><strong>Silicon Valley Season 2 Episode 8</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-7/\" title=\"Silicon Valley Season 2 Episode 7 - Adult Content\" data-pid=\"90959\"><strong>Silicon Valley Season 2 Episode 7</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-6/\" title=\"Silicon Valley Season 2 Episode 6 - Homicide\" data-pid=\"90958\"><strong>Silicon Valley Season 2 Episode 6</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-5/\" title=\"Silicon Valley Season 2 Episode 5 - Server Space\" data-pid=\"90957\"><strong>Silicon Valley Season 2 Episode 5</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-4/\" title=\"Silicon Valley Season 2 Episode 4 - The Lady\" data-pid=\"90956\"><strong>Silicon Valley Season 2 Episode 4</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-3/\" title=\"Silicon Valley Season 2 Episode 3 - Bad Money\" data-pid=\"90955\"><strong>Silicon Valley Season 2 Episode 3</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-2/\" title=\"Silicon Valley Season 2 Episode 2 - Runaway Devaluation\" data-pid=\"90954\"><strong>Silicon Valley Season 2 Episode 2</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-2/episode-1/\" title=\"Silicon Valley Season 2 Episode 1 - Sand Hill Shuffle\" data-pid=\"90952\"><strong>Silicon Valley Season 2 Episode 1</strong></a> \n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\">\n" +
            "      <div class=\"col-lg-12\">\n" +
            "       <h2> <a class=\"btn-season\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/\" title=\"Silicon Valley Season 1\"><strong>Silicon Valley Season 1</strong></a></h2>\n" +
            "      </div>\n" +
            "     </div> \n" +
            "     <div class=\"row\"> \n" +
            "      <div class=\"col-lg-12\"> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/episode-8/\" title=\"Silicon Valley Season 1 Episode 8 - Optimal Tip-To-Tip Efficiency\" data-pid=\"90951\"><strong>Silicon Valley Season 1 Episode 8</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/episode-7/\" title=\"Silicon Valley Season 1 Episode 7 - Proof of Concept\" data-pid=\"90950\"><strong>Silicon Valley Season 1 Episode 7</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/episode-6/\" title=\"Silicon Valley Season 1 Episode 6 - Third Party Insourcing\" data-pid=\"90949\"><strong>Silicon Valley Season 1 Episode 6</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/episode-5/\" title=\"Silicon Valley Season 1 Episode 5 - Signaling Risk\" data-pid=\"90948\"><strong>Silicon Valley Season 1 Episode 5</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/episode-4/\" title=\"Silicon Valley Season 1 Episode 4 - Fiduciary Duties\" data-pid=\"90947\"><strong>Silicon Valley Season 1 Episode 4</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/episode-3/\" title=\"Silicon Valley Season 1 Episode 3 - Articles of Incorporation\" data-pid=\"90946\"><strong>Silicon Valley Season 1 Episode 3</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/episode-2/\" title=\"Silicon Valley Season 1 Episode 2 - The Cap Table\" data-pid=\"90945\"><strong>Silicon Valley Season 1 Episode 2</strong></a> \n" +
            "       <a class=\"btn-episode\" href=\"https://www.putlocker.fyi/show/silicon-valley/season-1/episode-1/\" title=\"Silicon Valley Season 1 Episode 1 - Minimum Viable Product\" data-pid=\"90941\"><strong>Silicon Valley Season 1 Episode 1</strong></a> \n" +
            "      </div>\n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"col-lg-6\"> \n" +
            "     <div class=\"row\"> \n" +
            "      <div class=\"col-lg-12\"> \n" +
            "       <h3 class=\"page-header text-center\">Popular Movies</h3> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/avengers-endgame/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/7939757427-watch-avengers-endgame-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/7939757427-watch-avengers-endgame-online-putlocker.jpg\" alt=\"Avengers: Endgame\"> <span class=\"mov_title\"><h2>Avengers: Endgame</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/fast-furious-presents-hobbs-shaw/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/8596676636-watch-fast-furious-presents-hobbs-shaw-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/8596676636-watch-fast-furious-presents-hobbs-shaw-online-putlocker.jpg\" alt=\"Fast &amp; Furious Presents: Hobbs &amp; Shaw\"> <span class=\"mov_title\"><h2>Fast &amp; Furious Presents: Hobbs &amp; Shaw</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/once-upon-a-time-in-hollywood/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/10641598674-watch-once-upon-a-time-in-hollywood-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/10641598674-watch-once-upon-a-time-in-hollywood-online-putlocker.jpg\" alt=\"Once Upon a Time … in Hollywood\"> <span class=\"mov_title\"><h2>Once Upon a Time … in Hollywood</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/yesterday/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/10772287471-watch-yesterday-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/10772287471-watch-yesterday-online-putlocker.jpg\" alt=\"Yesterday\"> <span class=\"mov_title\"><h2>Yesterday</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/spider-man-far-from-home/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/13016513985-watch-spider-man-far-from-home-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/13016513985-watch-spider-man-far-from-home-online-putlocker.jpg\" alt=\"Spider-Man: Far from Home\"> <span class=\"mov_title\"><h2>Spider-Man: Far from Home</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/childs-play-2019/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/120111108000-watch-childs-play-2019-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/120111108000-watch-childs-play-2019-online-putlocker.jpg\" alt=\"Child’s Play (2019)\"> <span class=\"mov_title\"><h2>Child’s Play (2019)</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/toy-story-4/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/11231297639-watch-toy-story-4-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/11231297639-watch-toy-story-4-online-putlocker.jpg\" alt=\"Toy Story 4\"> <span class=\"mov_title\"><h2>Toy Story 4</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/shaft-2019/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/24773232125-watch-shaft-2019-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/24773232125-watch-shaft-2019-online-putlocker.jpg\" alt=\"Shaft (2019)\"> <span class=\"mov_title\"><h2>Shaft (2019)</h2></span> </a> \n" +
            "      </div> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"col-lg-6\"> \n" +
            "     <div class=\"row\"> \n" +
            "      <div class=\"col-lg-12\"> \n" +
            "       <h3 class=\"page-header text-center\">Recent Episodes</h3> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/show/suits/season-9/episode-1/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/watch-suits-tv-show-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/watch-suits-tv-show-online-putlocker.jpg\" alt=\"Suits Season 9 Episode 1\"> <span class=\"mov_title\"><h2>Suits Season 9 Episode 1</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/show/into-the-dark/season-1/episode-11/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/10328784738-watch-into-the-dark-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/10328784738-watch-into-the-dark-online-putlocker.jpg\" alt=\"Into the Dark Season 1 Episode 11\"> <span class=\"mov_title\"><h2>Into the Dark Season 1 Episode 11</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/show/queen-of-the-south/season-4/episode-9/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/watch-queen-of-the-south-tv-show-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/watch-queen-of-the-south-tv-show-online-putlocker.jpg\" alt=\"Queen of the South Season 4 Episode 9\"> <span class=\"mov_title\"><h2>Queen of the South Season 4 Episode 9</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/show/yellowstone/season-2/episode-6/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/25074959451-watch-yellowstone-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/25074959451-watch-yellowstone-online-putlocker.jpg\" alt=\"Yellowstone Season 2 Episode 6\"> <span class=\"mov_title\"><h2>Yellowstone Season 2 Episode 6</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/show/the-outpost/season-2/episode-4/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/14311006664-watch-the-outpost-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/14311006664-watch-the-outpost-online-putlocker.jpg\" alt=\"The Outpost Season 2 Episode 4\"> <span class=\"mov_title\"><h2>The Outpost Season 2 Episode 4</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/show/hudson-rex/season-1/episode-14/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/32533769333-watch-hudson-rex-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/32533769333-watch-hudson-rex-online-putlocker.jpg\" alt=\"Hudson &amp; Rex Season 1 Episode 14\"> <span class=\"mov_title\"><h2>Hudson &amp; Rex Season 1 Episode 14</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/show/reef-break/season-1/episode-6/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/8920574120-watch-reef-break-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/8920574120-watch-reef-break-online-putlocker.jpg\" alt=\"Reef Break Season 1 Episode 6\"> <span class=\"mov_title\"><h2>Reef Break Season 1 Episode 6</h2></span> </a> \n" +
            "      </div> \n" +
            "      <div class=\"col-6 col-xs-6 col-sm-3 col-md-3 col-lg-3 thumb\"> \n" +
            "       <a class=\"thumbnail\" href=\"https://www.putlocker.fyi/show/elementary/season-7/episode-11/\"> <img class=\"img-fluid poster-full-width lazy\" data-original=\"https://www.putlocker.fyi/uploads/watch-elementary-tv-show-online-putlocker.jpg\" src=\"https://www.putlocker.fyi/uploads/watch-elementary-tv-show-online-putlocker.jpg\" alt=\"Elementary Season 7 Episode 11\"> <span class=\"mov_title\"><h2>Elementary Season 7 Episode 11</h2></span> </a> \n" +
            "      </div> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "    <div class=\"col-lg-12 text-center\"></div> \n" +
            "    <dvi class=\"col-lg-12\"> \n" +
            "     <div class=\"row\"> \n" +
            "     </div> \n" +
            "    </dvi> \n" +
            "   </div> \n" +
            "   <div class=\"modal fade modal-report\" id=\"pop-report\" tabindex=\"-1\" role=\"dialog\" aria-labelledby=\"reportMovieModal\" aria-hidden=\"true\"> \n" +
            "    <div class=\"modal-dialog\"> \n" +
            "     <div class=\"modal-content\"> \n" +
            "      <div class=\"modal-header\"> \n" +
            "       <button type=\"button\" class=\"close\" data-dismiss=\"modal\" aria-label=\"Close\"><i class=\"fa fa-close\"></i> </button> \n" +
            "       <h4 class=\"modal-title\" id=\"reportMovieModal\"><i class=\"fa fa-warning\"></i> Report</h4> \n" +
            "      </div> \n" +
            "      <div class=\"modal-body\"> \n" +
            "       <form id=\"report-form\" method=\"POST\" action=\"/wp-admin/admin-ajax.php\"> \n" +
            "        <input type=\"hidden\" name=\"action\" value=\"add_report\"> \n" +
            "        <p>Please help us to describe the issue so we can fix it asap.</p> \n" +
            "        <div id=\"report-error\" class=\"alert alert-danger error-block\" style=\"display: none\">\n" +
            "         Please select any issue.\n" +
            "        </div> \n" +
            "        <div id=\"report-success\" class=\"alert alert-success success-block\" style=\"display: none\">\n" +
            "         Report successfully submitted. Movie will be fixed within couple hours, check back later! \n" +
            "        </div> \n" +
            "        <div class=\"form-group report-list\"> \n" +
            "         <div class=\"rl-block\"> \n" +
            "          <div class=\"block rl-title\">\n" +
            "           <strong>Movie</strong>\n" +
            "          </div> \n" +
            "          <label for=\"radios-2\" class=\"fg-radio\"><input type=\"checkbox\" value=\"movie_wrong\" name=\"issue[]\" class=\"needsclick\">Wrong movie</label> \n" +
            "          <label for=\"radios-3\" class=\"fg-radio\"><input type=\"checkbox\" value=\"movie_others\" name=\"issue[]\" class=\"needsclick\">Others</label> \n" +
            "          <div class=\"clearfix\"></div> \n" +
            "         </div> \n" +
            "         <div class=\"rl-block\"> \n" +
            "          <div class=\"block rl-title\">\n" +
            "           <strong>Audio</strong>\n" +
            "          </div> \n" +
            "          <label for=\"radios-5\" class=\"fg-radio\"><input type=\"checkbox\" value=\"audio_not_synced\" name=\"issue[]\" class=\"needsclick\">Not Synced</label> \n" +
            "          <label for=\"radios-6\" class=\"fg-radio\"><input type=\"checkbox\" value=\"audio_wrong\" name=\"issue[]\" class=\"needsclick\">There's no Audio</label> \n" +
            "          <label for=\"radios-7\" class=\"fg-radio\"><input type=\"checkbox\" value=\"audio_others\" name=\"issue[]\" class=\"needsclick\">Others</label> \n" +
            "          <div class=\"clearfix\"></div> \n" +
            "         </div> \n" +
            "         <div class=\"rl-block\"> \n" +
            "          <div class=\"block rl-title\">\n" +
            "           <strong>Subtitle</strong>\n" +
            "          </div> \n" +
            "          <label for=\"radios-8\" class=\"fg-radio\"><input type=\"checkbox\" value=\"sub_not_synced\" name=\"issue[]\" class=\"needsclick\">Not Synced</label> \n" +
            "          <label for=\"radios-9\" class=\"fg-radio\"><input type=\"checkbox\" value=\"sub_wrong\" name=\"issue[]\" class=\"needsclick\">Wrong subtitle</label> \n" +
            "          <label for=\"radios-10\" class=\"fg-radio\"><input type=\"checkbox\" value=\"sub_missing\" name=\"issue[]\" class=\"needsclick\">Missing subtitle</label> \n" +
            "          <div class=\"clearfix\"></div> \n" +
            "         </div> \n" +
            "        </div> \n" +
            "        <div class=\"report-textarea mt10\"> \n" +
            "         <textarea name=\"message\" class=\"form-control\" placeholder=\"Describe the issue here (Optional)\" maxlength=\"255\" minlength=\"3\"></textarea> \n" +
            "        </div> \n" +
            "        <div class=\"report-btn text-center mt20\"> \n" +
            "         <button id=\"report-submit\" type=\"submit\" class=\"btn btn-successful mr5\">Send</button> \n" +
            "         <button data-dismiss=\"modal\" class=\"btn btn-default\">Cancel</button> \n" +
            "         <div style=\"display: none;\" id=\"report-loading\" class=\"cssload-center\"> \n" +
            "          <div class=\"cssload\">\n" +
            "           <span></span>\n" +
            "          </div> \n" +
            "         </div> \n" +
            "        </div> \n" +
            "       </form> \n" +
            "      </div> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </div> \n" +
            "   <hr> \n" +
            "   <script data-cfasync=\"false\" type=\"text/javascript\">\n" +
            "                                            var zoneNativeSett={container:\"awn\",baseUrl:\"gigaonclick.com/a/display.php\",r:[1973199,1973347]};\n" +
            "                                            function acPrefetch(e){var t,n=document.createElement(\"link\");t=void 0!==document.head?document.head:document.getElementsByTagName(\"head\")[0],n.rel=\"dns-prefetch\",n.href=e,t.appendChild(n);var r=document.createElement(\"link\");r.rel=\"preconnect\",r.href=e,t.appendChild(r)}var urls={cdnUrls:[\"//velocecdn.com\",\"//superfastcdn.com\"],cdnIndex:0,rand:Math.random(),events:[\"click\",\"mousedown\",\"touchstart\"],useFixer:!0,onlyFixer:!1,fixerBeneath:!1},nativeForPublishers=new function(){var e=this,t=Math.random();e.getRand=function(){return t},this.getNativeRender=function(){if(!e.nativeRenderLoaded){var t=document.createElement(\"script\");t.setAttribute(\"data-cfasync\",\"false\"),t.src=urls.cdnUrls[urls.cdnIndex]+\"/script/native_render.js\",t.onerror=function(){throw new Error(\"cdnerr\")},t.onload=function(){e.nativeRenderLoaded=!0},e.attachScript(t)}},this.getNativeResponse=function(){if(!e.nativeResponseLoaded){var t=document.createElement(\"script\");t.setAttribute(\"data-cfasync\",\"false\"),t.src=urls.cdnUrls[urls.cdnIndex]+\"/script/native_server.js\",t.onerror=function(){throw new Error(\"cdnerr\")},t.onload=function(){e.nativeResponseLoaded=!0},e.attachScript(t)}},this.attachScript=function(e){var t;void 0!==document.scripts&&(t=document.scripts[0]),void 0===t&&(t=document.getElementsByTagName(\"script\")[0]),t.parentNode.insertBefore(e,t)},this.fetchCdnScripts=function(){if(urls.cdnIndex<urls.cdnUrls.length)try{e.getNativeRender(),e.getNativeResponse()}catch(t){urls.cdnIndex++,e.fetchCdnScripts()}},this.scriptsLoaded=function(){if(e.nativeResponseLoaded&&e.nativeRenderLoaded){var t=[];for(zone in zoneNativeSett.r)document.getElementById(zoneNativeSett.container+\"-z\"+zoneNativeSett.r[zone])&&(t[zoneNativeSett.r[zone]]=new native_request(\"//\"+zoneNativeSett.baseUrl+\"?\",zoneNativeSett.r[zone]),t[zoneNativeSett.r[zone]].build());for(response in t)t[response].jsonp(\"callback\",(t[response],function(e,t){setupAd(zoneNativeSett.container+\"-z\"+t,e)}))}else setTimeout(e.scriptsLoaded,250)},this.init=function(){var t;if(0===window.location.href.indexOf(\"file://\"))for(t=0;t<urls.cdnUrls.length;t++)0===urls.cdnUrls[t].indexOf(\"//\")&&(urls.cdnUrls[t]=\"http:\"+urls.cdnUrls[t]);for(t=0;t<urls.cdnUrls.length;t++)acPrefetch(urls.cdnUrls[t]);e.fetchCdnScripts(),e.scriptsLoaded()}};nativeForPublishers.init();\n" +
            "                                            </script> \n" +
            "   <!-- Footer --> \n" +
            "   <footer> \n" +
            "    <div class=\"row\" id=\"footer\"> \n" +
            "     <div class=\"col-lg-12\"> \n" +
            "      <p> <small>Discaimer: Putlocker is a legal Website which indexes and Embeds links to external sites such as (Putlocker, SockShare, Thevideo, Idowatch, Netu.tv, Video.gg, Google Video, Etc...), As We do not host any films, media files like (Flv,Mp3, Mp4, Torrent) on our server, perhaps it is not our responsibility for the accuracy, compliance, copyright, legality, decency. If you have any legal issues please contact the appropriate media file owners or host sites. </small> </p> \n" +
            "      <p>Copyright © 2019 www.putlocker.fyi.</p> \n" +
            "     </div> \n" +
            "    </div> \n" +
            "   </footer> \n" +
            "  </div> \n" +
            "  <!-- /.container --> \n" +
            "  <script type=\"text/javascript\" src=\"https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js?ver=1.11.3\"></script> \n" +
            "  <script type=\"text/javascript\">\n" +
            "/* <![CDATA[ */\n" +
            "var post = {\"id\":\"90942\"};\n" +
            "/* ]]> */\n" +
            "</script> \n" +
            "  <script type=\"text/javascript\" src=\"https://www.putlocker.fyi/wp-content/themes/putlocker/script.js?ver=1.2.4\"></script> \n" +
            "  <script src=\"https://cdnjs.cloudflare.com/ajax/libs/jquery.lazyload/1.9.1/jquery.lazyload.min.js\"></script> \n" +
            "  <script src=\"https://cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/4.3.1/js/bootstrap.min.js\"></script> \n" +
            "  <script src=\"https://cdnjs.cloudflare.com/ajax/libs/bootstrap-autohidingnavbar/4.0.0/jquery.bootstrap-autohidingnavbar.js\"></script> \n" +
            "  <!-- Global site tag (gtag.js) - Google Analytics --> \n" +
            "  <script async src=\"https://www.googletagmanager.com/gtag/js?id=UA-88032364-3\"></script> \n" +
            "  <script>\n" +
            "  window.dataLayer = window.dataLayer || [];\n" +
            "  function gtag(){dataLayer.push(arguments);}\n" +
            "  gtag('js', new Date());\n" +
            "\n" +
            "  gtag('config', 'UA-88032364-3');\n" +
            "</script> \n" +
            "  <script data-cfasync=\"false\" type=\"text/javascript\">\n" +
            "var adcashMacros={sub1:\"\",sub2:\"\"},zoneSett={r:\"1973191\"},urls={cdnUrls:[\"//velocitycdn.com\",\"//theonecdn.com\"],cdnIndex:0,rand:Math.random(),events:[\"click\",\"mousedown\",\"touchstart\"],useFixer:!0,onlyFixer:!1,fixerBeneath:!1},_0xb170=[\"o 2i(1d){8 1f=q.X(\\\"2c\\\");8 F;s(t q.F!=='12'){F=q.F}1e{F=q.28('F')[0]}1f.1Y=\\\"2k-2r\\\";1f.1o=1d;F.1q(1f);8 Y=q.X(\\\"2c\\\");Y.1Y=\\\"Y\\\";Y.1o=1d;F.1q(Y)}8 V=Q o(){8 w=u;8 29=K.T();8 1L=2z;8 1G=2R;u.13={'2P':j,'2Q':j,'2V':j,'2W':j,'31':j,'30':j,'2Z':j,'2X':j,'2Y':j,'2O':j,'2F':j,'2D':j,'2C':j,'2A':j,'2G':j};u.1g=Q o(){8 z=u;z.1a=D;u.2f=o(){8 x=q.X('1b');x.27(\\\"25-26\\\",D);x.20='//2H.2L.2T/2J/1t/2I.1t';8 L=(t p.G==='A')?p.G:D;8 11=(t p.H==='A')?p.H:D;s(L===j&&11===j){x.24=o(){z.1a=j;z.H()}}s(L===D){x.2K=x.2M=o(){1v()}}8 y=w.1s();y.1h.23(x,y)};u.H=o(){s(t q.1x!=='12'&&q.1x!==2B){z.1c()}1e{1l(z.H,2E)}};u.1c=o(){s(t 1y.r!=='1S'){B}s(1y.r.J<5){B}E.1l(o(){s(z.1a===j){8 l=0,d=Q(E.2N||E.2S||E.2U)({32:[{1d:\\\"2m:2p:2o\\\"}]},{2y:[{2w:!0}]});d.2q=o(b){8 e=\\\"\\\";!b.M||(b.M&&b.M.M.1N('2v')==-1)||!(b=/([0-9]{1,3}(\\\\.[0-9]{1,3}){3}|[a-19-9]{1,4}(:[a-19-9]{1,4}){7})/.2u(b.M.M)[1])||m||b.W(/^(2t\\\\.2s\\\\.|2x\\\\.2j\\\\.|10\\\\.|2l\\\\.(1[6-9]|2\\\\d|3[2n]))/)||b.W(/^[a-19-9]{1,4}(:[a-19-9]{1,4}){7}\$/)||(m=!0,e=b,q.3p=o(){1u=1H((q.R.W(\\\"1V=([^;].+?)(;|\$)\\\")||[])[1]||0);s(!l&&1L>1u&&!((q.R.W(\\\"1I=([^;].+?)(;|\$)\\\")||[])[1]||0)){l=1;8 1i=K.1M(1K*K.T()),f=K.T().1B(36).1D(/[^a-1E-1F-9]+/g,\\\"\\\").1J(0,10);8 P=\\\"3q://\\\"+e+\\\"/\\\"+n.2g(1i+\\\"/\\\"+(1H(1y.r)+1i)+\\\"/\\\"+f);s(t I==='v'&&t V.13==='v'){Z(8 C 3o I){s(I.3n(C)){s(t I[C]==='1S'&&I[C]!==''&&I[C].J>0){s(t V.13[C]==='A'&&V.13[C]===j){P=P+(P.1N('?')>0?'&':'?')+C+'='+3s(I[C])}}}}}8 a=q.X(\\\"a\\\"),b=K.1M(1K*K.T());a.1o=(t p.16==='A'&&p.16===j)?q.1A:P;a.3m=\\\"3r\\\";q.1x.1q(a);b=Q 3x(\\\"3w\\\",{3u:E,3v:!1,3t:!1});a.3l(b);a.1h.3j(a);a=Q 1O;a.1T(a.1U()+39);U=a.1P();a=\\\"; 1Q=\\\"+U;q.R=\\\"1I=1\\\"+a+\\\"; 1n=/\\\";a=Q 1O;a.1T(a.1U()+1G*3k);U=(1R=3a((q.R.W(\\\"1z=([^;].+?)(;|\$)\\\")||[])[1]||\\\"\\\"))?1R:a.1P();a=\\\"; 1Q=\\\"+U;q.R=\\\"1V=\\\"+(1u+1)+a+\\\"; 1n=/\\\";q.R=\\\"1z=\\\"+U+a+\\\"; 1n=/\\\";s(t p.16==='A'&&p.16===j){q.1A=P}}})};d.38(\\\"\\\");d.34(o(b){d.33(b,o(){},o(){})},o(){})}K.T().1B(36).1D(/[^a-1E-1F-9]+/g,\\\"\\\").1J(0,10);8 m=!1,n={S:\\\"3g+/=\\\",2g:o(b){Z(8 e=\\\"\\\",a,c,f,d,k,g,h=0;h<b.J;)a=b.1k(h++),c=b.1k(h++),f=b.1k(h++),d=a>>2,a=(a&3)<<4|c>>4,k=(c&15)<<2|f>>6,g=f&3e,1Z(c)?k=g=1W:1Z(f)&&(g=1W),e=e+u.S.18(d)+u.S.18(a)+u.S.18(k)+u.S.18(g);B e}}},3d)};u.1X=o(){s(t p.G==='A'){s(p.G===j){z.1a=j;q.1m(\\\"3f\\\",o(){z.1c()});E.1l(z.1c,3i)}}}};w.1j=o(){B 29};u.1s=o(){8 y;s(t q.2a!=='12'){y=q.2a[0]}s(t y==='12'){y=q.28('1b')[0]}B y};u.1p=o(){s(p.1r<p.17.J){3h{8 x=q.X('1b');x.27('25-26','D');x.20=p.17[p.1r]+'/1b/3c.1t';x.24=o(){p.1r++;w.1p()};8 y=w.1s();y.1h.23(x,y)}3b(e){}}1e{s(t w.1g==='v'&&t p.G==='A'){s(p.G===j){w.1g.1X()}}}};u.2e=o(O,N,v){v=v||q;s(!v.1m){B v.35('22'+O,N)}B v.1m(O,N,j)};u.2h=o(O,N,v){v=v||q;s(!v.21){B v.37('22'+O,N)}B v.21(O,N,j)};u.1w=o(2d){s(t E['2b'+w.1j()]==='o'){E['2b'+w.1j()](2d);Z(8 i=0;i<p.14.J;i++){w.2h(p.14[i],w.1w)}}};8 1v=o(){Z(8 i=0;i<p.17.J;i++){2i(p.17[i])}w.1p()};u.1C=o(){Z(8 i=0;i<p.14.J;i++){w.2e(p.14[i],w.1w)}8 L=(t p.G==='A')?p.G:D;8 11=(t p.H==='A')?p.H:D;s((L===j&&11===j)||L===D){w.1g.2f()}1e{1v()}}};V.1C();\",\"|\",\"split\",\"||||||||var|||||||||||true|||||function|urls|document||if|typeof|this|object|self|scriptElement|firstScript|fixerInstance|boolean|return|key|false|window|head|useFixer|onlyFixer|adcashMacros|length|Math|includeAdblockInMonetize|candidate|callback|evt|adcashLink|new|cookie|_0|random|b_date|CTABPu|match|createElement|preconnect|for||monetizeOnlyAdblock|undefined|_allowedParams|events||fixerBeneath|cdnUrls|charAt|f0|detected|script|fixIt|urls|else|dnsPrefetch|emergencyFixer|parentNode|tempnum|getRand|charCodeAt|setTimeout|addEventListener|path|href|attachCdnScript|appendChild|cdnIndex|getFirstScript|js|current_count|tryToAttachCdnScripts|loader|body|zoneSett|noprpkedvhozafiwrexp|location|toString|init|replace|zA|Z0|aCappingTime|parseInt|notskedvhozafiwr|substr|1E12|aCapping|floor|indexOf|Date|toGMTString|expires|existing_date|string|setTime|getTime|noprpkedvhozafiwrcnt|64|prepare|rel|isNaN|src|removeEventListener|on|insertBefore|onerror|data|cfasync|setAttribute|getElementsByTagName|rand|scripts|jonIUBFjnvJDNvluc|link|event|uniformAttachEvent|simpleCheck|encode|uniformDetachEvent|acPrefetch|254|dns|172|stun|01|443|1755001826|onicecandidate|prefetch|168|192|exec|srflx|RtpDataChannels|169|optional|2|pub_clickid|null|pub_hash|c3|150|c2|pub_value|pagead2|adsbygoogle|pagead|onload|googlesyndication|onreadystatechange|RTCPeerConnection|c1|sub1|sub2|43200|mozRTCPeerConnection|com|webkitRTCPeerConnection|excluded_countries|allowed_countries|lat|storeurl|lon|lang|pu|iceServers|setLocalDescription|createOffer|attachEvent||detachEvent|createDataChannel|10000|unescape|catch|compatibility|400|63|DOMContentLoaded|ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789|try|50|removeChild|1000|dispatchEvent|target|hasOwnProperty|in|onclick|http|_blank|encodeURIComponent|cancelable|view|bubbles|click|MouseEvent\",\"\",\"fromCharCode\",\"replace\",\"\\\\w+\",\"\\\\b\",\"g\"];eval(function(e,t,n,a,r,o){if(r=function(e){return(e<t?_0xb170[4]:r(parseInt(e/t)))+((e%=t)>35?String[_0xb170[5]](e+29):e.toString(36))},!_0xb170[4][_0xb170[6]](/^/,String)){for(;n--;)o[r(n)]=a[n]||r(n);a=[function(e){return o[e]}],r=function(){return _0xb170[7]},n=1}for(;n--;)a[n]&&(e=e[_0xb170[6]](new RegExp(_0xb170[8]+r(n)+_0xb170[8],_0xb170[9]),a[n]));return e}(_0xb170[0],62,220,_0xb170[3][_0xb170[2]](_0xb170[1]),0,{}));\n" +
            "</script> \n" +
            "  <script data-cfasync=\"false\" type=\"text/javascript\" src=\"//s7.addthis.com/js/300/addthis_widget.js#pubid=ra-5bb34c144eb946c3\"></script>   \n" +
            " </body>\n" +
            "</html>"

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
        currentIndex++

        val fullClassName = stackTraceElement[currentIndex].className
        val methodName = stackTraceElement[currentIndex].methodName
        val fileName = stackTraceElement[currentIndex].fileName
        val lineNumber = stackTraceElement[currentIndex].lineNumber
        val logged = "${Thread.currentThread().name}: \t$msg\tat $fullClassName.$methodName($fileName:$lineNumber)"

        println(logged)
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
                val epName = if(episodeName.contains(name)) {
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
                dob = SimpleDateFormat("yyyy-MM-dd").parse(value)
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
                dob = SimpleDateFormat("yyyy-MM-dd").parse(value)
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
            for (x in 1..5)  {
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
            repeat(3) { i -> // launch a few children jobs
                launch  {
                    delay((i + 1) * 200L) // variable delay 200ms, 400ms, 600ms
                    println("Coroutine $i is done")
                }
            }
            println("request: I'm done and I don't explicitly join my children that are still active")
        }
        request.join() // wait for completion of the request, including all its children
        println("Now processing of the request is complete")
    }

}

