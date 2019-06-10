package crestron.com.deckofcards

import junit.framework.TestCase.assertEquals
import org.junit.Test
import kotlin.properties.Delegates

class TestTwo {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, (2 + 2).toLong())
    }

    private fun log(s: String) {
        println("$s\n")
    }

    @Test
    fun test2345() {
        //val f = 1..9
        //System.out.println(5.5 in f)
    }

    @Test
    fun dTest() {
        val d = Deck()

        //d-=Card(Suit.SPADES, 5)

        val c = d.getCardLocation(Card(Suit.SPADES, 5))
        log("${Card(Suit.SPADES, 5)} is in the $c place of the deck")

        val dq = Deck.deck {
            addNormalDeck()
            card(Card.RandomCard)
            build()
        }
    }

    @Test
    fun kotlinFunTest() {
        val numbers = listOf("one", "two", "three", "four")
        val numbersSequence = numbers.asSequence()
        log("$numbersSequence")
    }

    @Test
    @Throws(CardNotFoundException::class)
    fun randomTest() {

        log("${Card.RandomCard}")

        var d = Deck()

        log(d.toString())
        log(d.toNormalString())
        log(d.toSymbolString())
        log(d.toPrettyString())
        log(d.toArrayString())
        log(d.toArrayNormalString())
        log(d.toArraySymbolString())
        log(d.toArrayPrettyString())

        Card.cardDescriptor = CardDescriptor.UNICODE_SYMBOL

        d.deckListener = object : Deck.DeckListener {
            override fun draw(c: Card, size: Int) {
                log("$c and $size")
            }
        }

        log("Random card is ${d.randomCard}")
        log("Random card is ${d.randomCard}")
        log("Random card is ${d.randomCard}")
        log("Random card is ${d.randomCard}")
        log("Random card is ${d.getCard(6)}")
        d += Card.RandomCard
        d += arrayListOf<Card>().apply {
            add(Card.RandomCard)
            add(Card.RandomCard)
            add(Card.RandomCard)
            add(Card.randomCardByColor(Color.BLACK))
            add(Card.randomCardBySuit(Suit.SPADES))
            add(Card.randomCardByValue(5))
        }
        d += Deck()
        Card.ClearCard
        d += Deck.deck {
            card {
                suit = Suit.randomSuit()
                value = CardUtil.randomNumber(1, 13)
            }
            addNormalDeck()
            card(Card.RandomCard)
        }

        d.randomCard.compareSuit(Card.RandomCard)

        d.clear()

        d = Deck()

        d.deckListener = object : Deck.DeckListener {
            override fun draw(c: Card, size: Int) {
                log("$c and $size")
            }
        }

        CardDescriptor.setRandomDescriptor()

        log(d.toArrayString())
        d.trueRandomShuffle()
        log(d.toArrayString())
        d.sortToReset()
        log(d.toArrayString())
        d.trueRandomShuffle(3)
        log(d.toArrayString())
        d.sortToReset()
        d.trueRandomShuffle(3)
        log(d.toArrayString())

        /*
        var total = 0
        for(c in d) {
            total+=c.value
        }
        log("$total") //total == 364
        */

        val x = Card.RandomCard + Card.RandomCard
        log("$x")

    }

    @Test
    @Throws(CardNotFoundException::class)
    fun deckTest() {
        var d = Deck()

        println(d.removeColor(Color.BLACK))

        println(d.removeSuit(Suit.HEARTS))

        println(d.removeNumber(6))

        println(d)

        log("Deck without black, hearts, and 6: $d")
        val deck = Deck()
        d += deck.getDeck()
        log("Deck plus another deck: $d")
        d += Card(Suit.SPADES, 1)
        log("Deck plus Ace of Spades: $d")
        d - Suit.SPADES
        log("Deck minus spades: $d")
        d - Color.RED
        log("Deck minus red: $d")
        log("5 cards from deck: ${(d - 5)}")
        log("One card from deck: ${(d - 5f)}")
        d *= 2
        log("Deck size is ${d.deckCount()} and : $d")
        d /= 2
        log("Deck size is ${d.deckCount()} and : $d")

        d = Deck()

        log("Random card is ${d.randomCard}")
        log("Random card is ${d.randomCard}")
        log("Random card is ${d.randomCard}")
        log("Random card is ${d.randomCard}")
        log("Random card is ${d.getCard(6)}")

        d = Deck()

        log("${d.draw() + d.draw()}")
        var c = d.draw()
        log("$c")
        log("${d.draw() > d.draw()}")
        log("${d.draw() < d.draw()}")
        log("${d.draw() >= d.draw()}")
        log("${d.draw() <= d.draw()}")
        log("${d.draw() == d.draw()}")
        log("${d.draw() + d.draw()}")
        log("${d.draw() - d.draw()}")

        d = Deck()

        c = Card(Suit.SPADES, 1)

        log("${c in d}")
        log("${c !in d}")
        log("${d.contains(c)}")
        log("${!d.contains(c)}")

        d = Deck()
        d *= 2
        d - Suit.DIAMONDS
        d - Suit.CLUBS
        d - Suit.HEARTS

        log("New deck is: $d and the size is ${d.deckCount()}")
        d.shuffle()
        log("New deck is: $d and the size is ${d.deckCount()}")

        val hand = Hand()

        infix fun Hand.deal(num: Int) {
            deck.dealHand(this, num)
        }

        hand deal 5

        log("$hand")

        val cd = d.getCard(5)
        log("$cd")
        d addDeck deck
        log("1 New deck is: $d and the size is ${d.deckCount()}")
        d addDecks 2
        log("2 New deck is: $d and the size is ${d.deckCount()}")
        d removeDecks 1
        log("3 New deck is: $d and the size is ${d.deckCount()}")
        for (i in d[2, 6]) {
            log("Range [2,6] $i")
        }

        log("Ranged[2..6] only ${d[2..6]}")

        log("Suits only ${d[Suit.SPADES, Suit.DIAMONDS]}")

        log("Suits only ${d[Suit.SPADES]}")

        log("Color only ${d[Color.BLACK]}")

        log("Color only ${d[Color.BACK]}")

        d.sortByColor()
        log("Sort color ${d.toArrayString()}")
        d.shuffle()
        d.sortByValue()
        log("Sort value ${d.toArrayString()}")
        d.shuffle()
        d.sortBySuit()
        log("Sort suit ${d.toArrayString()}")
        d.shuffle()
        d.sortByValue()
        d.sortBySuit()
        log("Sort value and suit ${d.toArrayString()}")
        log("Cards ${d drawCards 4}")
        d /= 1
        d /= 1
        d /= 1
        log("deck $d")
        d += Suit.SPADES
        log("deck ${d.toArrayString()}")
        d += Color.RED
        log("deck ${d.toArrayString()}")

        log("First Color ${d.getFirstCardByColor(Color.RED)}")
        log("First Suit ${d.getFirstCardBySuit(Suit.SPADES)}")
        log("First Value ${d.getFirstCardByValue(2)}")
        log("Last Color ${d.getLastCardByColor(Color.BLACK)}")
        log("Last Suit ${d.getLastCardBySuit(Suit.SPADES)}")
        log("Last Value ${d.getLastCardByValue(2)}")

        d.getCards(4)
        d.getCard(Suit.SPADES, 7)
        d.addCards(Deck().getDeck())
        d.getCardLocation(Card(Suit.DIAMONDS, 6))
        d.reverse()

        d.draw().compareColor(Card(Suit.DIAMONDS, 4))

    }

    @Test
    fun zxcv() {

        loopi@ for (i in 1..3) {
            loopj@ for (j in 5..7) {
                if (i == 2 /*&& j == 6*/) break@loopj
                print((i * 100) + j)
                print(" ")
            }
            println("$i loop ends")
        }

        println("We are done")

        loopi@ for (i in 1..3) {
            for (j in 5..7) {
                if (i == 2 && j == 6) continue@loopi
                print((i * 100) + j)
                print(" ")
            }
            println("$i loop ends")
        }

        println("We are done")

        fun foo() {
            listOf(1, 2, 3, 4, 5).forEach lit@{
                if (it == 3) return@lit // non-local return directly to the caller of foo()
                print(it)
            }
            println("this point is unreachable")
        }

        foo()

        val bytes = 0b11010010_01101001_10010100_10010010

        log("$bytes")

        val secondProperty = "Second property: $bytes".also(::println)

        log(secondProperty)

        val answer by lazy {
            println("Calculating the answer...")
            42
        }

        println("The answer is $answer.")

        class Suits {

            inline fun <reified T : Enum<T>> printAllValues() {
                print(enumValues<T>().joinToString { it.name })
            }

        }

        Suits().printAllValues<Suit>()

        val lazyValue: String by lazy {
            println("computed!")
            "Hello"
        }
        println(lazyValue)
        println(lazyValue)

        class User(val map: Map<String, Any?>? = null) {
            var name: String by Delegates.observable("<no name>") { _, old, new ->
                println("$old -> $new")
            }

            val name1: String by map
            val age: Int by map
        }

        val user1 = User(mapOf("name" to "John Doe", "age" to 25))
        log(user1.map!!.toString())

        val user = User()
        user.name = "first"
        user.name = "second"
        log(user.name)
        log(user.name1)
        log("${user.age}")

        val items = listOf(1, 2, 3, 4, 5)
        // Lambdas are code blocks enclosed in curly braces.
        items.fold(0) { acc: Int, i: Int ->
            print("acc = $acc, i = $i, ")
            val result = acc + i
            println("result = $result")
            result
        }
        // Parameter types in a lambda are optional if they can be inferred:
        val joinedToString = items.fold("Elements:") { acc, i -> "$acc $i" }
        // Function references can also be used for higher-order function calls:
        val product = items.fold(1, Int::times)
        //sampleEnd
        println("joinedToString = $joinedToString")
        println("product = $product")

        var decked = Deck.suitOnly(Suit.SPADES, Suit.HEARTS)

        println("${decked.getDeck()}")

        decked = Deck.numberOnly(2, 3, 4)

        log(decked.toArrayString())

        decked = Deck.colorOnly(Color.BLACK)

        log(decked.toArrayString())

        decked = Deck(Deck())

        log(decked.toArrayString())

        decked = Deck(arrayListOf<Card>().apply {
            add(Card.RandomCard)
        })

        log(decked.toArrayString())

        decked = Deck(Deck())

        log(decked.toArrayString())

        decked = Deck(2)

        log(decked.toArrayString())

        decked = Deck(2, true)

        log(decked.toArrayString())

    }

}