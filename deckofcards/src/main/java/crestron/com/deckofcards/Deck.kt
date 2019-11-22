package crestron.com.deckofcards

import android.annotation.TargetApi
import android.os.Build
import java.util.*

/**
 * The Class Deck.
 */
class Deck {

    //var
    private var deckOfCards: ArrayList<Card> = arrayListOf()
    var deckListener: DeckListener? = null
    private val emptyDeck = CardNotFoundException("Deck is Empty")

    companion object {
        /**
         * Builds a deck of only [suit]
         * @return a Deck of [suit]
         */
        fun suitOnly(vararg suit: Suit): Deck = Deck().apply { initialize(Suit.SPADES in suit, Suit.CLUBS in suit, Suit.DIAMONDS in suit, Suit.HEARTS in suit) }

        /**
         * Builds a deck of only [num]s
         * @return a Deck of [num]s
         */
        fun numberOnly(vararg num: Int): Deck = Deck().apply {
            for (i in num) {
                this += Card(Suit.SPADES, i)
                this += Card(Suit.CLUBS, i)
                this += Card(Suit.DIAMONDS, i)
                this += Card(Suit.HEARTS, i)
            }
        }

        /**
         * Builds a deck of only [color]
         * @return a Deck of [color]
         */
        fun colorOnly(color: Color): Deck = Deck().apply {
            when (color) {
                Color.BLACK -> initialize(spades = true, clubs = true, diamonds = false, hearts = false)
                Color.RED -> initialize(spades = false, clubs = false, diamonds = true, hearts = true)
                Color.BACK -> throw CardNotFoundException("Cannot Find Back Card")
            }
        }
    }

    //Constructors
    /**
     * A Deck of Cards
     *
     * @param shuffler true if the deck should be shuffled
     * @param numberOfDecks the number of decks to have
     * @param seed the seed to use if shuffled
     * @param deckListener the listener for shuffling and drawing
     */
    constructor(
            shuffler: Boolean = false,
            numberOfDecks: Int = 1,
            seed: Long? = null,
            deckListener: DeckListener? = null
    ) {
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
    private constructor()

    private constructor(cards: Collection<Card>) {
        deckOfCards.addAll(cards)
    }

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
            if (shuffler) shuffle()
        }
        if (shuffler) shuffle()
    }


    /**
     * A Deck of Cards, shuffled.
     *
     * @param shuffler the shuffler
     */
    constructor(shuffler: Boolean = false) {
        initialize()
        if (shuffler) shuffle()
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
    operator fun plusAssign(suit: Suit) = when (suit) {
        Suit.HEARTS -> initialize(spades = false, clubs = false, diamonds = false, hearts = true)
        Suit.DIAMONDS -> initialize(spades = false, clubs = false, diamonds = true, hearts = false)
        Suit.CLUBS -> initialize(spades = false, clubs = true, diamonds = false, hearts = false)
        Suit.SPADES -> initialize(spades = true, clubs = false, diamonds = false, hearts = false)
    }

    /**
     * adds the wanted color
     *
     */
    operator fun plusAssign(color: Color) = when (color) {
        Color.BLACK -> initialize(spades = true, clubs = true, diamonds = false, hearts = false)
        Color.RED -> initialize(spades = false, clubs = false, diamonds = true, hearts = true)
        Color.BACK -> throw CardNotFoundException("Cannot Find Back Card")
    }

    /**
     * removes a card from the deck
     */
    operator fun minusAssign(c: Card) {
        getCard(c)
    }

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

    /**
     * gets all of the cards in [range]
     * @return a [Collection] of cards that are in the [range]
     */
    operator fun get(range: IntRange): Collection<Card> = deckOfCards.slice(range)

    /**
     * gets the card at the num index
     */
    operator fun get(num: Int): Card = deckOfCards[num]

    /**
     * gets all of the colors of [color]
     * @return a [Collection] of cards that are [color]s
     */
    operator fun get(vararg color: Color): Collection<Card> = deckOfCards.filter { color.contains(it.color) }

    /**
     * gets all of the suits of [suit]
     * @return a [Collection] of cards that are [suit]s
     */
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

    /**
     * allows iteration
     */
    operator fun iterator() = deckOfCards.iterator()

    /**
     * Shorthand to draw from the deck
     */
    operator fun unaryMinus() = draw()

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

    /**
     * draws [num] cards from the deck
     * @return a [Collection] of cards
     */
    infix fun drawCards(num: Int): Collection<Card> = getCards(num)

    private fun initialize(
            spades: Boolean = true,
            clubs: Boolean = true,
            diamonds: Boolean = true,
            hearts: Boolean = true
    ) {
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
    fun clear() = deckOfCards.clear()

    /**
     * reverses the order of the cards
     */
    fun reverse() = deckOfCards.reverse()

    /**
     * removes all cards of color
     * @param color the color to remove
     * @return true if any were removed
     */
    @TargetApi(Build.VERSION_CODES.N)
    fun removeColor(color: Color): Boolean = deckOfCards.removeIf {
        it.color.equals(color)
    }

    /**
     * removes all cards of suit
     * @param suit the suit to remove
     * @return true if any were removed
     */
    @TargetApi(Build.VERSION_CODES.N)
    fun removeSuit(suit: Suit): Boolean = deckOfCards.removeIf {
        it.suit.equals(suit)
    }

    /**
     * removes all cards with the value num
     * @param num the number to remove
     * @return true if any were removed
     */
    @TargetApi(Build.VERSION_CODES.N)
    fun removeNumber(num: Int): Boolean = deckOfCards.removeIf {
        it.value == num
    }

    /**
     * Draws a card.
     *
     * @return Card
     */
    @Throws(CardNotFoundException::class)
    fun draw(): Card = try {
        val c = deckOfCards.removeAt(0)
        deckListener?.draw(c, deckCount())
        c
    } catch (e: IndexOutOfBoundsException) {
        throw emptyDeck
    }

    /**
     * Draws a random card from the deck.
     *
     * @return Card
     */
    val randomCard: Card
        @Throws(CardNotFoundException::class)
        get() = try {
            val num = CardUtil.randomNumber(1, deckCount() - 1)
            val c = deckOfCards.removeAt(num)
            deckListener?.draw(c, deckCount())
            c
        } catch (e: IndexOutOfBoundsException) {
            throw emptyDeck
        }

    /**
     * Adds a card to the deck.
     *
     * @param c Card
     */
    fun addCard(c: Card) {
        deckOfCards.add(c)
        deckListener?.cardAdded(c)
    }

    /**
     * Adds cards to the deck.
     *
     * @param c Card
     */
    fun addCards(c: Collection<Card>) {
        deckOfCards.addAll(c)
        if (deckListener != null)
            for (i in c)
                deckListener?.cardAdded(i)
    }

    /**
     * Draws a random card from the deck.
     *
     * @param n The place where the card is drawn
     * @return Card
     */
    @Throws(CardNotFoundException::class)
    fun getCard(n: Int): Card = try {
        val c = deckOfCards.removeAt(n)
        deckListener?.draw(c, deckCount())
        c
    } catch (e: IndexOutOfBoundsException) {
        throw emptyDeck
    }

    /**
     * Gets a [Collection] of cards from the top of the deck
     * @param num the number of cards to [draw]
     * @return a [Collection] of cards
     */
    fun getCards(num: Int): Collection<Card> {
        val cards = arrayListOf<Card>()
        for (i in 0 until num) {
            cards += draw()
        }
        return cards
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

    /**
     * Gets a card out of the deck
     * @param c the wanted card
     * @return the card
     */
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

    /**
     * Get the location of a card
     * @param c the wanted card
     * @return the location of [c]
     */
    @Throws(CardNotFoundException::class)
    fun getCardLocation(c: Card): Int {
        val loc = deckOfCards.indexOf(c)
        if (loc != -1)
            return loc
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

    /**
     * Sorts the deck by color
     * (Black, Red)
     */
    fun sortByColor() = deckOfCards.sortWith(compareBy { it.color })

    /**
     * Sorts the deck by card value
     */
    fun sortByValue() = deckOfCards.sortWith(compareBy { it.value })

    /**
     * Sorts the deck by suit
     * (Spades, Clubs, Diamonds, Hearts)
     */
    fun sortBySuit() = deckOfCards.sortWith(compareBy { it.suit })

    /**
     * Sorts the deck to a brand new deck. Values are Ascending and Suit order (Spades, Clubs, Diamonds, Hearts)
     */
    fun sortToReset() {
        val spadesList = this[Suit.SPADES]
        val clubsList = this[Suit.CLUBS]
        val diamondsList = get(Suit.DIAMONDS)
        val heartsList = get(Suit.HEARTS)
        clear()
        deckOfCards.apply {
            addAll(spadesList.sortedWith(compareBy { it.value }))
            addAll(clubsList.sortedWith(compareBy { it.value }))
            addAll(diamondsList.sortedWith(compareBy { it.value }))
            addAll(heartsList.sortedWith(compareBy { it.value }))
        }
    }

    /**
     * Shuffles the deck.
     *
     * @param seed for generation
     */
    fun shuffle(seed: Long? = null) {
        val gen: Random = if (seed == null) Random() else Random(seed)

        deckOfCards.shuffle(gen)

        if (deckListener != null) {
            deckListener!!.shuffle()
        }
    }

    /**
     * People say that you need to shuffle a deck 7 times before it is truly shuffled and randomized.
     * That is what this does.
     *
     * @param seed for generation (Yes, you can seed a true random shuffle if you wish)
     */
    fun trueRandomShuffle(seed: Long? = null) {
        for (i in 1..7) {
            shuffle(seed)
        }
    }

    /**
     * The Deck.
     *
     * @return The remaining contents of the deck
     */
    override fun toString(): String = deckOfCards.joinToString("\n", transform = Card::toString)

    /**
     * The Deck.
     *
     * @return The remaining contents of the deck
     */
    fun toNormalString(): String = deckOfCards.joinToString("\n", transform = Card::toNormalString)

    /**
     * The Deck.
     *
     * @return The remaining contents of the deck
     */
    fun toSymbolString(): String = deckOfCards.joinToString("\n", transform = Card::toSymbolString)

    /**
     * The Deck.
     *
     * @return The remaining contents of the deck
     */
    fun toPrettyString(): String = deckOfCards.joinToString("\n", transform = Card::toPrettyString)

    /**
     * The Deck in Array String Format
     *
     * @return The remaining contents of the deck
     */
    fun toArrayString(): String = "$deckOfCards"

    /**
     * The Deck in Array String Format
     *
     * @return The remaining contents of the deck
     */
    fun toArrayNormalString(): String = "[${deckOfCards.joinToString(separator = ", ", transform = Card::toNormalString)}]"

    /**
     * The Deck in Array String Format
     *
     * @return The remaining contents of the deck
     */
    fun toArraySymbolString(): String = "[${deckOfCards.joinToString(separator = ", ", transform = Card::toSymbolString)}]"

    /**
     * The Deck in Array String Format
     *
     * @return The remaining contents of the deck
     */
    fun toArrayPrettyString(): String = "[${deckOfCards.joinToString(separator = ", ", transform = Card::toPrettyString)}]"

    /**
     * returns the deck
     */
    fun getDeck(): ArrayList<Card> = deckOfCards

    /**
     * The size of the deck.
     *
     * @return The size of the deck (int)
     */
    fun deckCount(): Int = deckOfCards.size

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

        /**
         * when a card is added to the deck
         * @param c the card that was added
         */
        fun cardAdded(vararg c: Card) {
            println("$c")
        }
    }
}