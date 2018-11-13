package crestron.com.deckofcards

import android.annotation.TargetApi
import android.os.Build
import java.util.*

/**
 * The Class Deck.
 */
class Deck {

    //var
    private var deckOfCards: ArrayList<Card> = ArrayList()
    var deckListener: DeckListener? = null
    private val emptyDeck = CardNotFoundException("Deck is Empty")

    class DeckBuilder {

        private val cards = mutableListOf<Card>()

        fun card(block: CardBuilder.() -> Unit) {
            cards.add(CardBuilder().apply(block).build())
        }

        fun build(): Deck = Deck(cards)

        /**
         * add an entire 52 card deck
         */
        fun addNormalDeck() {
            cards.addAll(Deck().deckOfCards)
        }

        /**
         * add a card with #suit and a value
         */
        fun card(suit: Suit, value: Int) {
            cards.add(Card(suit, value))
        }

        /**
         * add a random card into the deck
         */
        fun randomCard() {
            fun randomNumber(low: Int, high: Int): Int {
                return low + (Math.random() * ((high - low) + 1)).toInt()
            }

            val suit: Suit? = when (randomNumber(1, 4)) {
                1 -> Suit.SPADES
                2 -> Suit.CLUBS
                3 -> Suit.DIAMONDS
                4 -> Suit.HEARTS
                else -> {
                    null
                }
            }
            cards.add(Card(suit!!, randomNumber(1, 13)))
        }

    }

    class CardBuilder {

        var suit: Suit = Suit.SPADES
        var value = 1
        var card: Card? = null

        fun build(): Card {
            return card ?: Card(suit, value)
        }

    }

    private constructor(cards: Collection<Card>) {
        deckOfCards.addAll(cards)
    }

    companion object Builder {

        fun deck(block: DeckBuilder.() -> Unit): Deck = DeckBuilder().apply(block).build()

        fun suitOnly(vararg suit: Suit): Deck {
            val d = Deck(null)
            d.initialize(Suit.SPADES in suit, Suit.CLUBS in suit, Suit.DIAMONDS in suit, Suit.HEARTS in suit)
            return d
        }

        fun numberOnly(vararg num: Int): Deck {
            val d = Deck(null)
            for (i in num) {
                d += Card(Suit.SPADES, i)
                d += Card(Suit.CLUBS, i)
                d += Card(Suit.DIAMONDS, i)
                d += Card(Suit.HEARTS, i)
            }
            return d
        }

        fun colorOnly(color: Color): Deck {
            val d = Deck(null)
            when (color) {
                Color.BLACK -> d.initialize(true, true, false, false)
                Color.RED -> d.initialize(false, false, true, true)
                Color.BACK -> throw CardNotFoundException("Cannot Find Back Card")
            }
            return d
        }
    }

    /**
     * A Deck of Cards
     *
     * @param shuffler true if the deck should be shuffled
     * @param numberOfDecks the number of decks to have
     * @param seed the seed to use if shuffled
     * @param deckListener the listener for shuffling and drawing
     */
    constructor(shuffler: Boolean = false, numberOfDecks: Int = 1, seed: Long? = null, deckListener: DeckListener? = null) {
        for (i in 0 until numberOfDecks) {
            initialize()
        }
        this.deckListener = deckListener
        if (shuffler)
            shuffle(seed)
    }

    /**
     * A Deck of Cards, unshuffled.
     */
    constructor() {
        initialize()
    }

    private constructor(b: Boolean?)

    constructor(deck: Collection<Card>, deckListener: DeckListener? = null) {
        deckOfCards.addAll(deck)
        this.deckListener = deckListener
    }

    constructor(deck: Deck, deckListener: DeckListener? = null) {
        deckOfCards.addAll(deck.deckOfCards)
        this.deckListener = deckListener
    }

    /**
     * A Deck of Cards, unshuffled.
     *
     * @param numberOfDecks number of decks in one deck
     */
    constructor(numberOfDecks: Int) {
        for (i in 0 until numberOfDecks) {
            initialize()
        }
    }

    /**
     * A Deck of Cards, shuffled.
     *
     * @param numberOfDecks   number of decks in one deck
     * @param shuffler the shuffler
     */
    constructor(numberOfDecks: Int, shuffler: Boolean = false) {
        for (i in 0 until numberOfDecks) {
            initialize()
            if (shuffler)
                shuffle()
        }
        if (shuffler)
            shuffle()
    }


    /**
     * A Deck of Cards, shuffled.
     *
     * @param shuffler the shuffler
     */
    constructor(shuffler: Boolean = false) {
        initialize()
        if (shuffler)
            shuffle()
    }

    /**
     * A Deck of Cards, shuffled.
     *
     * @param shuffler true if you want to shuffle, false if you do not
     * @param seed     the seed for shuffling
     */
    constructor(shuffler: Boolean, seed: Long) {
        initialize()
        if (shuffler) {
            shuffle(seed)
        }

    }

    //methods

    /**
     * checks to see if the Deck has the card c
     * @param c the card to check for
     */
    operator fun contains(c: Card): Boolean = c in deckOfCards

    /**
     * adds card to deck
     * @param c the card to add
     */
    operator fun plusAssign(c: Card) = addCard(c)

    /**
     * adds a list of cards to the deck
     * @param c the cards to add
     */
    operator fun plusAssign(c: Collection<Card>) = addCards(c)

    /**
     * adds another deck to this deck
     */
    operator fun plusAssign(d: Deck) = addCards(d.deckOfCards)

    /**
     * adds the wanted suit
     */
    operator fun plusAssign(suit: Suit) {
        when (suit) {
            Suit.HEARTS -> initialize(false, false, false, true)
            Suit.DIAMONDS -> initialize(false, false, true, false)
            Suit.CLUBS -> initialize(false, true, false, false)
            Suit.SPADES -> initialize(true, false, false, false)
        }
    }

    /**
     * adds the wanted color
     *
     */
    operator fun plusAssign(color: Color) {
        when (color) {
            Color.BLACK -> initialize(true, true, false, false)
            Color.RED -> initialize(false, false, true, true)
            Color.BACK -> throw CardNotFoundException("Cannot Find Back Card")
        }
    }

    /**
     * removes the card from this deck
     */
    operator fun minus(c: Card): Card = getCard(c)

    /**
     * removes num number of cards from this deck
     */
    operator fun minus(num: Int): Collection<Card> = getCards(num)

    /**
     * removes all suit of s from this deck
     */
    operator fun minus(s: Suit) = removeSuit(s)

    /**
     * removes all color of color from this deck
     */
    operator fun minus(color: Color) = removeColor(color)

    /**
     * removes the num card from this deck
     */
    operator fun minus(num: Float): Card = getCard(num.toInt())

    /**
     * gets the cards between start and end
     */
    operator fun get(start: Int, end: Int): Collection<Card> = deckOfCards.subList(start, end)

    operator fun get(range: IntRange): Collection<Card> = deckOfCards.slice(range)

    /**
     * gets the card at the num index
     */
    operator fun get(num: Int): Card = deckOfCards[num]

    operator fun get(vararg color: Color): Collection<Card> = deckOfCards.filter { color.contains(it.color) }

    operator fun get(vararg suit: Suit): Collection<Card> = deckOfCards.filter { suit.contains(it.suit) }

    operator fun compareTo(num: Int): Int = when {
        deckCount() > num -> 1
        deckCount() < num -> -1
        deckCount() == num -> 0
        else -> num
    }

    /**
     * adds num number of decks to this deck
     */
    operator fun timesAssign(num: Int) {
        for (i in 1 until num) {
            initialize()
        }
    }

    /**
     * removes num number of decks from this deck
     */
    operator fun divAssign(num: Int) {
        val size = if (num == 1) {
            52
        } else {
            deckCount() / num
        }
        for (i in 0 until size) {
            try {
                deckOfCards.removeAt(0)
            } catch (e: IndexOutOfBoundsException) {
                break
            }
        }
    }

    operator fun iterator() = deckOfCards.iterator()

    /**
     * adds deck to this deck
     * @param deck the deck to add
     */
    infix fun addDeck(deck: Deck) = plusAssign(deck)

    /**
     * adds num number of decks to this deck
     */
    infix fun addDecks(num: Int) = timesAssign(num)

    /**
     * removes num number of decks from this deck
     */
    infix fun removeDecks(num: Int) = divAssign(num)

    infix fun drawCards(num: Int): Collection<Card> = getCards(num)

    private fun initialize(spades: Boolean = true, clubs: Boolean = true, diamonds: Boolean = true, hearts: Boolean = true) {
        for (i in 1..13) {
            if (spades)
                deckOfCards.add(Card(Suit.SPADES, i))
            if (clubs)
                deckOfCards.add(Card(Suit.CLUBS, i))
            if (diamonds)
                deckOfCards.add(Card(Suit.DIAMONDS, i))
            if (hearts)
                deckOfCards.add(Card(Suit.HEARTS, i))
        }
    }

    /**
     * clears the deck
     */
    fun clear() {
        deckOfCards.clear()
    }

    /**
     * reverses the order of the cards
     */
    fun reverse() {
        deckOfCards.reverse()
    }

    /**
     * removes all cards of color
     * @param color the color to remove
     * @return true if any were removed
     */
    @TargetApi(Build.VERSION_CODES.N)
    fun removeColor(color: Color): Boolean {
        return deckOfCards.removeIf {
            it.color.equals(color)
        }
    }

    /**
     * removes all cards of suit
     * @param suit the suit to remove
     * @return true if any were removed
     */
    @TargetApi(Build.VERSION_CODES.N)
    fun removeSuit(suit: Suit): Boolean {
        return deckOfCards.removeIf {
            it.suit.equals(suit)
        }
    }

    /**
     * removes all cards with the value num
     * @param num the number to remove
     * @return true if any were removed
     */
    @TargetApi(Build.VERSION_CODES.N)
    fun removeNumber(num: Int): Boolean {
        return deckOfCards.removeIf {
            it.value == num
        }
    }

    /**
     * Draws a card.
     *
     * @return Card
     */
    @Throws(CardNotFoundException::class)
    fun draw(): Card {
        try {
            val c = deckOfCards.removeAt(0)
            deckListener?.draw(c, deckCount())
            return c
        } catch (e: IndexOutOfBoundsException) {
            throw emptyDeck
        }
    }

    /**
     * Draws a random card from the deck.
     *
     * @return Card
     */
    val randomCard: Card
        @Throws(CardNotFoundException::class)
        get() {
            val gen = Random()
            try {
                val num = gen.nextInt(deckCount() - 1)
                val c = deckOfCards.removeAt(num)
                deckListener?.draw(c, deckCount())
                return c
            } catch (e: IndexOutOfBoundsException) {
                throw emptyDeck
            }
        }

    /**
     * Draws a random card from the deck.
     *
     * @param n The place where the card is drawn
     * @return Card
     */
    @Throws(CardNotFoundException::class)
    fun getCard(n: Int): Card {
        try {
            val c = deckOfCards.removeAt(n)
            deckListener?.draw(c, deckCount())
            return c
        } catch (e: IndexOutOfBoundsException) {
            throw emptyDeck
        }
    }

    fun getCards(num: Int): Collection<Card> {
        val cards = arrayListOf<Card>()
        for (i in 0 until num) {
            cards += draw()
        }
        return cards
    }

    /**
     * Adds a card to the deck.
     *
     * @param c Card
     */
    fun addCard(c: Card) {
        deckOfCards.add(c)
    }

    /**
     * Adds cards to the deck.
     *
     * @param c Card
     */
    fun addCards(c: Collection<Card>) {
        deckOfCards.addAll(c)
    }

    /**
     * Gets the card you want.
     *
     * @param s Suit
     * @param v Value
     * @return Your Card
     */
    @Throws(CardNotFoundException::class)
    fun getCard(s: Suit, v: Int): Card {
        val check = Card(s, v)
        for (i in deckOfCards.indices) {
            if (deckOfCards[i].equals(check)) {
                val cTemp = deckOfCards.removeAt(i)
                deckListener?.draw(cTemp, deckCount())
                return cTemp
            }
        }
        throw CardNotFoundException("Could not find card $check")
    }

    @Throws(CardNotFoundException::class)
    fun getCard(c: Card): Card {
        for (i in deckOfCards.indices) {
            if (deckOfCards[i].equals(c)) {
                val cTemp = deckOfCards.removeAt(i)
                deckListener?.draw(cTemp, deckCount())
                return cTemp
            }
        }
        throw CardNotFoundException("Could not find card $c")
    }

    @Throws(CardNotFoundException::class)
    fun getCardLocation(c: Card): Int {
        for (i in deckOfCards.indices) {
            if (deckOfCards[i].equals(c)) {
                return i
            }
        }
        throw CardNotFoundException("Could not find card $c")
    }

    /**
     * Gets the first card by Value.
     *
     * @param v Value of Card
     * @return the first card by Value
     */
    @Throws(CardNotFoundException::class)
    fun getFirstCardByValue(v: Int): Card {
        for (i in 0 until deckCount()) {
            if (v == deckOfCards[i].value) {
                val c = deckOfCards.removeAt(i)
                deckListener?.draw(c, deckCount())
                return c
            }
        }
        throw CardNotFoundException("Could not find card for value $v")
    }

    /**
     * Gets the last card by Value.
     *
     * @param v Value of Card
     * @return the last card by Value
     */
    @Throws(CardNotFoundException::class)
    fun getLastCardByValue(v: Int): Card {
        for (i in deckCount() - 1 downTo 0) {
            if (v == deckOfCards[i].value) {
                val c = deckOfCards.removeAt(i)
                deckListener?.draw(c, deckCount())
                return c
            }
        }

        throw CardNotFoundException("Could not find card for value $v")
    }

    /**
     * Gets the first card by suit.
     *
     * @param s Suit
     * @return the first card by suit
     */
    @Throws(CardNotFoundException::class)
    fun getFirstCardBySuit(s: Suit): Card {
        for (i in 0 until deckCount()) {
            if (s.equals(deckOfCards[i].suit)) {
                val c = deckOfCards.removeAt(i)
                deckListener?.draw(c, deckCount())
                return c
            }
        }
        throw CardNotFoundException("Could not find card for suit $s")
    }

    /**
     * Gets the last card by suit.
     *
     * @param s Suit
     * @return the last card by suit
     */
    @Throws(CardNotFoundException::class)
    fun getLastCardBySuit(s: Suit): Card {
        for (i in deckCount() - 1 downTo 0) {
            if (s.equals(deckOfCards[i].suit)) {
                val c = deckOfCards.removeAt(i)
                deckListener?.draw(c, deckCount())
                return c
            }
        }
        throw CardNotFoundException("Could not find card for suit $s")
    }

    /**
     * Gets the first card by color.
     *
     * @param color the color
     * @return the first card by color
     */
    @Throws(CardNotFoundException::class)
    fun getFirstCardByColor(color: Color): Card {
        for (i in 0 until deckCount()) {
            if (color.equals(deckOfCards[i].color)) {
                val c = deckOfCards.removeAt(i)
                deckListener?.draw(c, deckCount())
                return c
            }
        }
        throw CardNotFoundException("Could not find card for the color $color")
    }

    /**
     * Gets the last card by color.
     *
     * @param color the color
     * @return the last card by color
     */
    @Throws(CardNotFoundException::class)
    fun getLastCardByColor(color: Color): Card {
        for (i in deckCount() - 1 downTo 0) {
            if (color.equals(deckOfCards[i].color)) {
                val c = deckOfCards.removeAt(i)
                deckListener?.draw(c, deckCount())
                return c
            }
        }
        throw CardNotFoundException("Could not find card for the color $color")
    }

    fun sortByColor() {
        deckOfCards.sortWith(compareBy { it.color })
    }

    fun sortByValue() {
        deckOfCards.sortWith(compareBy { it.value })
    }

    fun sortBySuit() {
        deckOfCards.sortWith(compareBy { it.suit })
    }

    private fun shuffle(seed: Long?) {
        val gen: Random = if (seed == null) {
            Random()
        } else {
            Random(seed)
        }

        deckOfCards.shuffle(gen)

        if (deckListener != null) {
            deckListener!!.shuffle()
        }
    }

    /**
     * Shuffles the deck.
     */
    fun shuffle() {
        shuffle(null)
    }

    /**
     * Shuffles the deck.
     *
     * @param seed for generation
     */
    fun shuffle(seed: Long) {
        shuffle(seed)
    }

    /**
     * The Deck.
     *
     * @return The remaining contents of the deck
     */
    override fun toString(): String {
        var temp = ""
        for (i in deckOfCards.indices) {
            temp += "${deckOfCards[i]}\n"
        }

        return temp
    }

    fun toArrayString(): String {
        return "$deckOfCards"
    }

    /**
     * returns the deck
     */
    fun getDeck(): ArrayList<Card> {
        return deckOfCards
    }

    /**
     * The size of the deck.
     *
     * @return The size of the deck (int)
     */
    fun deckCount(): Int {
        return deckOfCards.size
    }

    /**
     * Deals n number of cards to hand
     *
     * @param h the hand
     * @param n the number of cards to add to the hand
     */
    @Throws(CardNotFoundException::class)
    fun dealHand(h: Hand, n: Int) {
        for (i in 0 until n) {
            h.add(draw())
        }
    }

    /**
     * a listener that listens for Deck actions
     */
    interface DeckListener {
        /**
         * listener for when the deck is shuffles
         */
        fun shuffle() {
            println("Shuffling...")
        }

        /**
         * listener for when a card is removed from the deck
         * @param c the card that was removed
         * @param size the size of the deck
         */
        fun draw(c: Card, size: Int)
    }
}