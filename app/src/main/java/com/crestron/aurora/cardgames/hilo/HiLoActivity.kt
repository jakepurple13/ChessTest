package com.crestron.aurora.cardgames.hilo

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.crestron.aurora.utilities.AnimationUtility
import com.crestron.aurora.R
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import kotlinx.android.synthetic.main.activity_hi_lo.*

class HiLoActivity : AppCompatActivity() {

    private var deck: Deck? = null
    private var mysteryCard: Card? = null
    private var playerCard: Card? = null
    private var wins = 0
    private var loses = 0
    private var ties = 0
    private var deckCount = 1
    private val deckListener = object : Deck.DeckListener {

        override fun draw(c: Card, size: Int) {
            if (size == 0) {
                deck = Deck(true, numberOfDecks = ++deckCount, deckListener = this)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hi_lo)

        deck = Deck(true, deckListener = deckListener)

        playerCard = deck!!.draw()
        player_card_view.setImageResource(playerCard!!.getImage(this))
        next.isEnabled = false

        fun clicking(condition: Boolean) = View.OnClickListener {
            if (mysteryCard == null) {
                mysteryCard = deck!!.draw()
                AnimationUtility.animateCard(chosen_cards, mysteryCard!!,
                        this@HiLoActivity,
                        reverse = !condition,
                        end = object : AnimationUtility.AnimationEnd {

                    override fun onAnimationEnd() {
                        super.onAnimationEnd()

                        //chosen_cards.setImageResource(mysteryCard!!.getImage(this@HiLoActivity))
                        val winned = when {
                            playerCard!!.ace() == mysteryCard!!.ace() -> null
                            condition -> playerCard!!.ace() < mysteryCard!!.ace()
                            else -> playerCard!!.ace() > mysteryCard!!.ace()
                        }

                        win_choose.text = when {
                            winned == null -> "You Tied! W: $wins L: $loses T:${++ties}"
                            winned -> "You Win! W: ${++wins} L: $loses T:$ties"
                            else -> "You Lost! W: $wins L: ${++loses} T:$ties"
                        }
                        win_choose.append("\n${deck!!.deckCount()} Cards Left")
                        next.isEnabled = true
                        high.isEnabled = false
                        low.isEnabled = false

                    }

                })
            } else {
                chosen_cards.setImageResource(Card.BackCard.getImage(this@HiLoActivity))
                playerCard = mysteryCard
                player_card_view.setImageResource(playerCard!!.getImage(this@HiLoActivity))
                mysteryCard = null
            }
        }

        high.setOnClickListener(clicking(true))
        low.setOnClickListener(clicking(false))
        next.setOnClickListener {
            if (mysteryCard != null) {
                chosen_cards.setImageResource(Card.BackCard.getImage(this@HiLoActivity))
                playerCard = mysteryCard
                player_card_view.setImageResource(playerCard!!.getImage(this@HiLoActivity))
                mysteryCard = null
                next.isEnabled = false
                high.isEnabled = true
                low.isEnabled = true
            }
        }

        back_button_hilo.setOnClickListener {
            finish()
        }

    }
}
