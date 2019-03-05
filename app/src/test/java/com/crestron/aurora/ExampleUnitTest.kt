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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */

typealias cad = Card

fun cadTest() {
    val d = cad(Suit.DIAMONDS, 3)
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
        val logged = "$msg\t.$methodName\t($fileName:$lineNumber)"

        System.out.println(logged)
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
            randomCard()
        }
        d - Suit.CLUBS
        d - Suit.DIAMONDS

        System.out.println(d.toArrayString())

    }

}
