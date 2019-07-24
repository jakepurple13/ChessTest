package com.crestron.aurora.cardgames

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.views.HintedImageView
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.CardNotFoundException
import crestron.com.deckofcards.Deck
import hari.floatingtoast.FloatingToast
import id.co.ionsoft.randomnumberanimationlibrary.RandomNumberAnimation
import kotlinx.android.synthetic.main.activity_black_jack.*
import kotlinx.android.synthetic.main.card_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import spencerstudios.com.bungeelib.Bungee
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
        GlobalScope.launch(Dispatchers.Main) {
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

        //blackjack_layout.setBackgroundResource(R.drawable.drkgreen)
        //ViewUtil.revealing(blackjack_layout, intent)

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

        fun dealerMoved() = GlobalScope.launch(Dispatchers.Main) {
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
                FloatingToast.makeToast(this@BlackJackActivity, "${winner.text}", FloatingToast.LENGTH_SHORT).show()
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

            blackjack_player_cards.adapter = BlackJackAdapter(playerCardList, this)
            blackjack_dealer_cards.adapter = BlackJackAdapter(dealerCardList, this)

            start().start()

        }

        backButtonJack.setOnClickListener {
            finish()
            Bungee.swipeRight(this@BlackJackActivity)
        }

        val layoutManagerPlayer = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        blackjack_player_cards.setHasFixedSize(true)
        val bitmap = BitmapFactory.decodeResource(resources, Card.BackCard.getImage(this))
        blackjack_player_cards.addItemDecoration(OverlapDecoration((-bitmap.width / 1.5).toInt()))
        blackjack_player_cards.layoutManager = layoutManagerPlayer

        val layoutManagerDealer = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        blackjack_dealer_cards.setHasFixedSize(true)
        blackjack_dealer_cards.addItemDecoration(OverlapDecoration((-bitmap.width / 1.5).toInt()))
        blackjack_dealer_cards.layoutManager = layoutManagerDealer

    }

    inner class OverlapDecoration(private var horizontalOverlap: Int = -200) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val itemPosition = parent.getChildAdapterPosition(view)
            if (itemPosition == 0) {
                return
            }
            outRect.set(horizontalOverlap, 0, 0, 0)
        }
    }

    @SuppressLint("SetTextI18n")
    fun start() = GlobalScope.launch(Dispatchers.Main) {

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
        /*AnimationUtility.animateCard(cardView, c, this@BlackJackActivity, end = object : AnimationUtility.AnimationEnd {
            override fun onAnimationEnd() {
                listCardsView.append("\n$c")
            }
        })*/
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
        blackjack_player_cards.adapter = BlackJackAdapter(playerCardList, this)
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
        blackjack_dealer_cards.adapter = BlackJackAdapter(dealerCardList, this)
        dealerTotal.text = "$dealerVal"
        if (dealerVal > 21) {
            playing = false
            win++
            win_count.text = "W:$win L:$lose T:$tie"
            animateFun()
            winner.text = "Dealer Busted!\nYou Win!"
        }
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        backButtonJack.performClick()
    }

    class BlackJackAdapter(private var stuff: ArrayList<Card>,
                           var context: Context) : RecyclerView.Adapter<ViewHolder>() {

        // Gets the number of animals in the list
        override fun getItemCount(): Int {
            return stuff.size
        }

        // Inflates the item views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_item, parent, false))
        }

        // Binds each animal in the ArrayList to a view
        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.cardInfo.setImageResource(stuff[position].getImage(context))
            holder.cardInfo.contentDescription = stuff[position].toString()
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Holds the TextView that will add each animal to
        val cardInfo: HintedImageView = view.card_info_cards!!

        init {
            setIsRecyclable(true)
        }
    }

}
