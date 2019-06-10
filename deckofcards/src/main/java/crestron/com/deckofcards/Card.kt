package crestron.com.deckofcards

import android.content.Context

/**
 * The Class Card.
 */
open class Card(val suit: Suit, val value: Int) : Comparable<Card> {

    private var maxValue = 16
    private var minValue = 1

    operator fun plus(c: Card) = value + c.value
    operator fun minus(c: Card) = value - c.value
    override fun equals(other: Any?) = suit.equals((other as Card).suit) && value == other.value

    companion object DefaultCard {
        /**
         * A Clear Card, [value] = 15, [suit] = SPADES
         * Used as a placeholder for ImageViews when wanting to have a spot for a card but do not want anything to show
         */
        val ClearCard = Card(Suit.SPADES, 15)

        /**
         * The Back of a Card, [value] = 16, [suit] = SPADES
         * Used as a placeholder for ImageViews when wanting to have a spot for a card
         * but want to show the back of a card
         */
        val BackCard = Card(Suit.SPADES, 16)

        /**
         * A card of both random suit and number
         */
        val RandomCard: Card
            get() {
                return Card(Suit.randomSuit(), CardUtil.randomNumber(1, 13))
            }

        /**
         * A card of random value but chosen suit
         */
        fun randomCardBySuit(suit: Suit) = Card(suit, CardUtil.randomNumber(1, 13))

        /**
         * A card of random suit but chosen value
         */
        fun randomCardByValue(value: Int) = Card(Suit.randomSuit(), value)

        /**
         * A card of random color and value
         */
        fun randomCardByColor(color: Color) = Card(Color.randomColor(color), CardUtil.randomNumber(1, 13))

        /**
         * Sets how the printout of the cards are.
         * e.g.
         *
         * [CardDescriptor.WRITTEN_OUT] = Ace of Spades
         *
         * [CardDescriptor.SYMBOL] = AS
         *
         * [CardDescriptor.UNICODE_SYMBOL] = A♠
         */
        var cardDescriptor = CardDescriptor.randomDescriptor()
    }

    init {
        if (value > maxValue || value < minValue) {
            throw CardNotFoundException("The value isn't a card")
        }
    }

    /**
     * Compares the values of the two cards.
     *
     * @param other the other
     * @return 1 if this is greater than other
     *
     *-1 if other is greater than this
     *
     *0 if this is equal to other
     */
    override operator fun compareTo(other: Card) = when {
        value > other.value -> 1
        value < other.value -> -1
        value == other.value -> 0
        else -> value
    }

    //add >,< and make others return something different

    /**
     * Gets the value ten.
     *
     * @return the value ten
     */
    val valueTen: Int
        get() = if (value > 10) {
            10
        } else {
            value
        }

    /**
     * The color of the suit
     *
     * @return The color of the suit of this card. Black is true, red is false.
     */
    val color = suit.getColor()

    override fun toString(): String {
        return when (cardDescriptor) {
            CardDescriptor.UNICODE_SYMBOL -> toPrettyString()
            CardDescriptor.SYMBOL -> toSymbolString()
            CardDescriptor.WRITTEN_OUT -> toNormalString()
        }
    }

    internal fun toNormalString(): String {
        return when (value) {
            1 -> "Ace"
            11 -> "Jack"
            12 -> "Queen"
            13 -> "King"
            else -> "$value"
        } + " of $suit"
    }

    internal fun toSymbolString(): String {
        return when (value) {
            1 -> "A"
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            else -> "$value"
        } + suit.symbol
    }

    internal fun toPrettyString(): String {
        return when (value) {
            1 -> "A"
            11 -> "J"
            12 -> "Q"
            13 -> "K"
            else -> "$value"
        } + suit.unicodeSymbol
    }

    /**
     * Compares the Suits of this and c.
     *
     * @param c the c
     * @return true if the two cards have the same suit
     *
     *false if the two cards don't have the same suit
     */
    fun equals(c: Card): Boolean {
        return suit.equals(c.suit) && value == c.value
    }

    /**
     * Gives the card image.
     */
    open fun getImage(context: Context): Int {

        val num = when (suit) {
            Suit.CLUBS -> 1
            Suit.SPADES -> 2
            Suit.HEARTS -> 3
            Suit.DIAMONDS -> 4
        }

        val s = if (cardName(value) == "clear" || cardName(value) == "b1fv")
            cardName(value)
        else {
            cardName(value) + num
        }

        return context.resources.getIdentifier(s, "drawable", context.packageName)
    }

    private fun cardName(num: Int): String {
        return when (num) {
            1 -> "ace"
            2 -> "two"
            3 -> "three"
            4 -> "four"
            5 -> "five"
            6 -> "six"
            7 -> "seven"
            8 -> "eight"
            9 -> "nine"
            10 -> "ten"
            11 -> "jack"
            12 -> "queen"
            13 -> "king"
            15 -> "clear"
            else -> "b1fv"
        }
    }

    fun compareSuit(c: Card): Boolean {
        return suit == c.suit
    }

    fun compareColor(c: Card): Boolean {
        return color == c.color
    }

    override fun hashCode(): Int {
        var result = suit.hashCode()
        result = 31 * result + value
        result = 31 * result + color.hashCode()
        return result
    }

}

