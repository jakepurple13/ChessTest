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
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.appendHTML
import kotlinx.html.stream.createHTML
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
