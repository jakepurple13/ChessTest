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
 */
(private val printableName: String, val symbol: String) {

    /**
     * Hearts.
     */
    HEARTS("Hearts", "H"),

    /**
     * Diamonds.
     */
    DIAMONDS("Diamonds", "D"),

    /**
     * Clubs.
     */
    CLUBS("Clubs", "C"),

    /**
     * Spades.
     */
    SPADES("Spades", "S");


    fun getColor(): Color {
        return when (this) {
            SPADES, CLUBS -> Color.BLACK
            DIAMONDS, HEARTS -> Color.RED
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    override fun toString(): String {
        return printableName
    }

    fun equals(other: Suit): Boolean {
        return symbol == other.symbol && printableName == other.printableName
    }

}

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
}
