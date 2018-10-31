package com.crestron.aurora

import com.crestron.aurora.boardgames.yahtzee.Dice
import com.crestron.aurora.boardgames.yahtzee.YahtzeeScores
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.Source
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

}
