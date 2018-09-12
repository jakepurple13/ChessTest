package com.crestron.aurora.cardgames.matching

import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.widget.TableRow
import com.crestron.aurora.R
import com.crestron.aurora.utilities.TimerUtil
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Suit
import kotlinx.android.synthetic.main.activity_matching.*


class MatchingActivity : AppCompatActivity() {

    private var deck = Deck()
    var cardFlipped: Card? = null
    var buttonFlipped: CardButton? = null
    private val time = TimerUtil(90000L, object : TimerUtil.TimerAction {
        override fun timeChange(time: Long, util: TimerUtil) {
            val isWinning = winCheck == 12

            if (time == 0L || isWinning) {
                util.cancel()
                val builder = AlertDialog.Builder(this@MatchingActivity)
                builder.setTitle("Time's Up!")
                val msg = if (isWinning) {
                    "You won! You had ${util.getTime()} seconds left to finish!"
                } else {
                    "Time is up!"
                }
                builder.setMessage(msg)
                // Add the buttons
                builder.setPositiveButton("Play Again") { _, _ ->
                    finish()
                    startActivity(intent)
                }
                builder.setNeutralButton("Let me see the board.") { _: DialogInterface, _: Int ->
                    for (i in 0..matching_layout.childCount) {
                        val view = matching_layout.getChildAt(i)
                        if (view is TableRow) {
                            // then, you can remove the the row you want...
                            // for instance...
                            for (q in 0..view.childCount) {
                                try {
                                    if ((view.getChildAt(q) as CardButton).isEnabled) {
                                        (view.getChildAt(q) as CardButton).flipped = true
                                        (view.getChildAt(q) as CardButton).animateImage()
                                    }
                                } catch (e: NullPointerException) {
                                    continue
                                } catch (e: IllegalStateException) {
                                    continue
                                } catch (e: TypeCastException) {
                                    continue
                                }
                            }
                        }
                    }
                }
                builder.setNegativeButton("Nope") { _, _ ->
                    finish()
                }
                val dialog = builder.create()
                dialog.show()
            }
        }

    })
    var winCheck = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matching)

        reset()
        setUp()

    }

    override fun onDestroy() {
        time.cancel()
        super.onDestroy()
    }

    private fun getNewCard(): CardButton {
        val cardButton = CardButton(this, deck.draw(), object : CardButtonFlip {
            override fun flip(c: Card, imageView: CardButton) {
                if (cardFlipped == null && buttonFlipped == null) {
                    cardFlipped = c
                    buttonFlipped = imageView
                    imageView.animateImage()
                } else if (cardFlipped != null && buttonFlipped != imageView) {
                    winCheck+=imageView.checkMatch(cardFlipped!!, buttonFlipped!!)
                    buttonFlipped = null
                    cardFlipped = null
                }
            }
        })

        cardButton.setImageNow()

        return cardButton
    }

    private fun setUp() {
        var tr: TableRow?
        while (deck.deckCount() > 0) {
            tr = TableRow(this)
            var i = 0
            while (i < 4 && deck.deckCount() > 0) {
                tr.isClickable = true
                tr.addView(getNewCard())
                i++
            }
            tr.gravity = Gravity.CENTER
            matching_layout.addView(tr)
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Matching!")
        builder.setMessage("Are you ready to play?")
        // Add the buttons
        builder.setPositiveButton("Play!") { _, _ ->
            time.startTimer(timer_info, false)
        }
        builder.setNegativeButton("Nope!") { _, _ ->
            finish()
        }
        val dialog = builder.create()
        dialog.show()

    }

    private fun reset() {
        deck = Deck()
        deck *= 2
        deck - Suit.DIAMONDS
        deck - Suit.CLUBS
        deck - Suit.HEARTS
        deck.removeNumber(13)
        deck.shuffle()
    }


    interface CardButtonFlip {
        fun flip(c: Card, imageView: CardButton)
    }

}
