package com.crestron.aurora.cardgames.canfield

import android.support.v7.app.AppCompatActivity
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
        val c1 = deck - Card(Suit.CLUBS, c.value)
        val c2 = deck - Card(Suit.DIAMONDS, c.value)
        val c3 = deck - Card(Suit.HEARTS, c.value)



    }

}
