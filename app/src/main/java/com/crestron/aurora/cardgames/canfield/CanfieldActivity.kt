package com.crestron.aurora.cardgames.canfield

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.crestron.aurora.R
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Suit
import java.util.*

class CanfieldActivity : AppCompatActivity() {

    var deck = Deck()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_canfield)





    }

    private fun setUp() {
        val c = deck.getFirstCardByValue(Random().nextInt(13) + 1)
        deck -= Card(Suit.CLUBS, c.value)
        deck -= Card(Suit.DIAMONDS, c.value)
        deck -= Card(Suit.HEARTS, c.value)



    }

}
