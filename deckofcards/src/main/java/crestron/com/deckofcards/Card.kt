package crestron.com.deckofcards

import android.content.Context

/**
 * The Class Card.
 */
open class Card(val suit: Suit, val value: Int) : Comparable<Card> {

    protected var maxValue = 16
    protected var minValue = 1

    operator fun plus(c: Card) = value + c.value
    operator fun minus(c: Card) = value - c.value
    override fun equals(other: Any?) = suit.equals((other as Card).suit) && value == other.value

    companion object DefaultCard {
        val ClearCard = Card(Suit.SPADES, 15)
        val BackCard = Card(Suit.SPADES, 16)
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
        return when (value) {
            1 -> "Ace of $suit"
            11 -> "Jack of $suit"
            12 -> "Queen of $suit"
            13 -> "King of $suit"
            else -> "$value of $suit"
        }
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

    fun ace(): Int {
        return if (value == 1) {
            14
        } else {
            value
        }
    }

    /**
     * Gives the card image.
     */
    fun getImage(context: Context): Int {

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
