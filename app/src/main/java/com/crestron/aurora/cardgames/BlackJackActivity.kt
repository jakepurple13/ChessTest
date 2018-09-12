package com.crestron.aurora.cardgames

import android.annotation.SuppressLint
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.crestron.aurora.*
import com.crestron.aurora.utilities.AnimationUtility
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.CardNotFoundException
import crestron.com.deckofcards.Deck
import id.co.ionsoft.randomnumberanimationlibrary.RandomNumberAnimation
import kotlinx.android.synthetic.main.activity_black_jack.*
import kotlinx.coroutines.experimental.*
import kotlinx.coroutines.experimental.android.UI
import java.util.*

class BlackJackActivity : AppCompatActivity() {

    private var deckOfCards = Deck(true)
    private var win = 0
    private var lose = 0
    private var tie = 0
    private var playerVal = 0
    private var dealerVal = 0
    private var playing = false
    private var playerDone = false
    private var dealerDone = false
    private lateinit var playerCardList: ArrayList<Card>
    private lateinit var dealerCardList: ArrayList<Card>
    private var playerAce = false
    private var numberOfDecks = 1
    private fun animateFun() {
        val randomNumberAnimation = RandomNumberAnimation(win_count)
        launch(UI) {
            randomNumberAnimation.start()
            delay(1500)
            randomNumberAnimation.stop(keepChange = false)
        }
    }

    private var shuffleListener = object : Deck.DeckListener {
        @SuppressLint("SetTextI18n")
        override fun draw(c: Card, size: Int) {
            Loged.i(c.toString())
            backButtonJack.text = "Back\nCards Left: $size"
            if (size == 0) {
                deckOfCards = Deck(true, numberOfDecks = ++numberOfDecks)
                deckOfCards.deckListener = this
            }
        }

        override fun shuffle() {
            Toast.makeText(this@BlackJackActivity, "Shuffling...", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_black_jack)

        deckOfCards.deckListener = shuffleListener

        win_count.text = "W:0 L:0 T:0"

        resetGame.text = "Play"

        controls.text = """
Hit = Play Button
Stand = Stop Button
Reset = Last Button
Control Screen = Info
Back = Rewind Button
"""

        controls.visibility = View.GONE

        newCard.isEnabled = false
        stay.isEnabled = false
        playerCards.isEnabled = false

        newCard.setOnClickListener {
            if (playing && !playerDone) {
                playerMove()
            }
        }

        playerCards.setOnClickListener {
            newCard.performClick()
        }

        playerCards.setOnLongClickListener {
            stay.performClick()
        }

        fun dealerMoved() = launch(UI) {
            newCard.isEnabled = false
            stay.isEnabled = false
            playerCards.isEnabled = false
            while (dealerVal <= 16) {
                dealerMove()
                delay(1000)
            }
            if (playing) {
                winner.text = when {
                    dealerVal in (playerVal + 1)..21 -> {
                        lose++
                        "Dealer Wins!"
                    }
                    playerVal in (dealerVal + 1)..21 -> {
                        win++
                        "Player Wins!"
                    }
                    dealerVal == playerVal -> {
                        tie++
                        "Its a tie!"
                    }
                    else -> ""
                }
            }
            resetGame.isEnabled = true
            playing = false
            resetGame.text = "Play"
            win_count.text = "W:$win L:$lose T:$tie"
            animateFun()
        }

        stay.setOnClickListener {
            if (playing && !dealerDone) {
                dealerDone = true
                playerDone = true
                dealerMoved().start()
            }
        }

        resetGame.setOnClickListener {

            resetGame.isEnabled = false
            start().cancel()
            dealerMoved().cancel()

            dealerDone = false
            playerDone = false
            cardList.text = ""
            dealerCards.text = ""
            total.text = "Total"
            dealerTotal.text = "Total"
            dealerVal = 0
            playerVal = 0
            winner.text = ""
            playerCards.setImageResource(R.drawable.b1fv)
            dealer.setImageResource(R.drawable.b1fv)
            resetGame.text = "Play Again"

            playerAce = false

            playerCardList = arrayListOf()
            dealerCardList = arrayListOf()

            start().start()

        }

        backButtonJack.setOnClickListener { finish() }

    }

    @SuppressLint("SetTextI18n")
    fun start() = launch(UI) {

        playerMove()
        delay(1000)
        dealerMove()
        delay(1000)
        playerMove()
        delay(1000)
        newCard.isEnabled = true
        stay.isEnabled = true
        playerCards.isEnabled = true
        playing = true

        if (playerVal == 21) {
            newCard.isEnabled = false
            stay.isEnabled = false
            playerCards.isEnabled = false
            winner.text = "You win!"
            win++
            win_count.text = "W:$win L:$lose T:$tie"
            animateFun()
            resetGame.isEnabled = true
            playing = false
            dealerDone = true
            playerDone = true
            resetGame.text = "Play"
        }

    }

    private fun hit(listCardsView: TextView, listCard: ArrayList<Card>, cardView: ImageView): Int {
        val c: Card = try {
            deckOfCards.draw()
        } catch (e: CardNotFoundException) {
            e.printStackTrace()
            deckOfCards = Deck(true)
            deckOfCards.draw()
        }
        AnimationUtility.animateCard(cardView, c, this@BlackJackActivity, end = object : AnimationUtility.AnimationEnd {
            override fun onAnimationEnd() {
                listCardsView.append("\n$c")
            }
        })
        //backButtonJack.text = "Back\nCards Left: ${deckOfCards.deckCount()}"
        listCard.add(c)
        val sortedCards = arrayListOf<Card>()
        for (foo in listCard) {
            sortedCards.add(foo)
        }
        Collections.sort(sortedCards, compareByDescending
        { it.valueTen })
        Loged.i(sortedCards.toString())
        var num = 0
        for (card in sortedCards) {
            val amount = if (card.value == 1 && num + 11 < 22) {
                11
            } else if (card.value == 1) {
                1
            } else {
                card.valueTen
            }
            num += amount
        }
        Loged.d(c.toString())

        return num
    }

    @SuppressLint("SetTextI18n")
    private fun playerMove() {
        playerVal = hit(cardList, playerCardList, playerCards)
        total.text = "$playerVal"
        if (playerVal > 21) {
            playing = false
            winner.text = "You Busted!"
            lose++
            win_count.text = "W:$win L:$lose T:$tie"
            animateFun()
            newCard.isEnabled = false
            stay.isEnabled = false
            playerCards.isEnabled = false
            resetGame.isEnabled = true
        }
    }

    @SuppressLint("SetTextI18n")
    private fun dealerMove() {
        dealerVal = hit(dealerCards, dealerCardList, dealer)
        dealerTotal.text = "$dealerVal"
        if (dealerVal > 21) {
            playing = false
            win++
            win_count.text = "W:$win L:$lose T:$tie"
            animateFun()
            winner.text = "Dealer Busted!\nYou Win!"
        }
    }

}
