package crestron.com.deckofcards

//builders
class DeckBuilder {

    companion object {
        /**
         * Create a deck via DSL
         */
        fun deck(block: DeckBuilder.() -> Unit): Deck = DeckBuilder().apply(block).build()
    }

    private val cards = mutableListOf<Card>()

    fun card(block: CardBuilder.() -> Unit) {
        cards.add(CardBuilder().apply(block).build())
    }

    fun build(): Deck = Deck(cards)

    /**
     * add an entire 52 card deck
     */
    fun addNormalDeck() = cards.addAll(Deck().getDeck())

    /**
     * add a card with #suit and a #value
     */
    fun card(suit: Suit, value: Int) = cards.add(Card(suit, value))

    /**
     * add a card
     */
    fun card(c: Card) = cards.add(c)

}

class CardBuilder {

    var suit: Suit = Suit.SPADES
    var value = 1
    var card: Card? = null

    fun build(): Card = card ?: Card(suit, value)

}