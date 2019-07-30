package com.crestron.aurora.cardgames.matching


import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crestron.aurora.R
import com.crestron.aurora.utilities.TimerUtil
import com.crestron.aurora.utilities.ViewUtil
import com.programmerbox.dragswipe.Direction
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.programmerbox.dragswipe.DragSwipeUtils
import com.programmerbox.dragswipe.plus
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.CardDescriptor
import crestron.com.deckofcards.Deck
import kotlinx.android.synthetic.main.activity_matching.matching_layouts
import kotlinx.android.synthetic.main.activity_matching.timer_info
import kotlinx.android.synthetic.main.match_item.view.*
import kotlinx.android.synthetic.main.matching_activity_two.*


class MatchingActivityTwo : AppCompatActivity() {

    private var deck = Deck()
    var cardFlipped: Card? = null
    var buttonFlipped: CardButton? = null
    lateinit var adapter: MatchAdapter
    private val time = TimerUtil(90000L, object : TimerUtil.TimerAction {
        override fun timeChange(time: Long, util: TimerUtil) {
            val isWinning = winCheck == 12

            if (time == 0L || isWinning) {
                util.cancel()
                val builder = AlertDialog.Builder(this@MatchingActivityTwo)
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
                    for(v in adapter.list) {
                        if(v.isEnabled) {
                            v.flipped = true
                            v.animateImage()
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
        setContentView(R.layout.matching_activity_two)

        Card.cardDescriptor = CardDescriptor.UNICODE_SYMBOL

        ViewUtil.revealing(matching_layouts, intent)

        reset()
        setUp()
    }

    override fun onDestroy() {
        time.cancel()
        super.onDestroy()
    }

    private fun getNewCard(): CardButton {
        val cardButton = CardButton(this, deck.draw(), object : MatchingActivity.CardButtonFlip {
            override fun flip(c: Card, imageView: CardButton) {
                if (cardFlipped == null && buttonFlipped == null) {
                    cardFlipped = c
                    buttonFlipped = imageView
                    imageView.animateImage()
                } else if (cardFlipped != null && buttonFlipped != imageView) {
                    winCheck += imageView.checkMatch(cardFlipped!!, buttonFlipped!!)
                    buttonFlipped = null
                    cardFlipped = null
                }
            }
        })

        cardButton.setImageNow()

        return cardButton
    }

    private fun setUp() {
        matching_rv.setHasFixedSize(true)
        matching_rv.layoutManager = GridLayoutManager(this, 4)

        val list = arrayListOf<CardButton>()
        while (deck.deckCount() > 0) {
            list += getNewCard()
        }
        list.shuffle()
        adapter = MatchAdapter(list, this)

        matching_rv.adapter = adapter

        DragSwipeUtils.setDragSwipeUp(adapter, matching_rv, Direction.START + Direction.END + Direction.UP + Direction.DOWN)

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
        deck.shuffle()
        val c = deck.getDeck().slice(0..11)
        deck.clear()
        deck.addCards(c)
        for(i in 0..11) {
            deck.addCard(deck[i])
        }
        deck.shuffle()
    }

    class MatchAdapter(list: ArrayList<CardButton>, val context: Context) : DragSwipeAdapter<CardButton, ViewHolder>(list) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.linearLayout.addView(list[position], LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT))
        }

        // Inflates the item views
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.match_item, parent, false))
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.match_card_item_layout!!
    }

}