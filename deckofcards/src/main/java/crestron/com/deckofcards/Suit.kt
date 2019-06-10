package crestron.com.deckofcards


// TODO: Auto-generated Javadoc


/**
 * The Enum Suit.
 */
enum class Suit
/**
 * Instantiates a new suit.
 *
 * @param printableName the printableName
 * @param symbol        the symbol
 * @param unicodeSymbol the symbol in unicode
 */
(private val printableName: String, val symbol: String, val unicodeSymbol: String) {

    /**
     * Hearts.
     */
    HEARTS("Hearts", "H", "♥"),

    /**
     * Diamonds.
     */
    DIAMONDS("Diamonds", "D", "♦"),

    /**
     * Clubs.
     */
    CLUBS("Clubs", "C", "♣"),

    /**
     * Spades.
     */
    SPADES("Spades", "S", "♠");

    /**
     * get the [color] of the card
     * [Color.BLACK] is [SPADES] and [CLUBS]
     * [Color.RED] is [DIAMONDS] and [HEARTS]
     */
    fun getColor(): Color {
        return when (this) {
            SPADES, CLUBS -> Color.BLACK
            DIAMONDS, HEARTS -> Color.RED
        }
    }

    /**
     * @return [printableName]
     */
    override fun toString(): String {
        return printableName
    }

    /**
     * checks to see if the suits are the same
     * @return true if the two are equal, false if they are not
     */
    fun equals(other: Suit): Boolean {
        return symbol == other.symbol &&
                printableName == other.printableName &&
                unicodeSymbol == other.unicodeSymbol
    }

    companion object {
        /**
         * @return a random suit
         */
        fun randomSuit(): Suit {
            return when (CardUtil.randomNumber(1, 4)) {
                1 -> SPADES
                2 -> CLUBS
                3 -> DIAMONDS
                4 -> HEARTS
                else -> {
                    SPADES
                }
            }
        }
    }

}

/**
 * The Color
 */
enum class Color(private val colorName: String) {
    BLACK("Black"),
    RED("Red"),
    BACK("Back");

    fun equals(c: Color): Boolean {
        return colorName == c.colorName
    }

    override fun toString(): String {
        return colorName
    }

    companion object {
        fun randomColor(color: Color): Suit {
            return when (color) {
                BLACK -> {
                    when (CardUtil.randomNumber(1, 2)) {
                        1 -> Suit.SPADES
                        2 -> Suit.CLUBS
                        else -> Suit.SPADES
                    }
                }
                RED -> {
                    when (CardUtil.randomNumber(1, 2)) {
                        1 -> Suit.DIAMONDS
                        2 -> Suit.HEARTS
                        else -> Suit.DIAMONDS
                    }
                }
                else -> {
                    Suit.SPADES
                }
            }
        }
    }

}

enum class CardDescriptor {
    WRITTEN_OUT,
    SYMBOL,
    UNICODE_SYMBOL;

    companion object {
        fun randomDescriptor(): CardDescriptor {
            return when (CardUtil.randomNumber(1, 3)) {
                1 -> WRITTEN_OUT
                2 -> SYMBOL
                3 -> UNICODE_SYMBOL
                else -> WRITTEN_OUT
            }
        }

        fun setRandomDescriptor() {
            Card.cardDescriptor = randomDescriptor()
        }
    }
}

internal class CardUtil {
    companion object {
        fun randomNumber(low: Int, high: Int): Int {
            return low + (Math.random() * ((high - low) + 1)).toInt()
        }
    }
}
