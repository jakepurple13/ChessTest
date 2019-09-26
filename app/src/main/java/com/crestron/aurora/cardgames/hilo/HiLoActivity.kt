package com.crestron.aurora.cardgames.hilo

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crestron.aurora.R
import com.crestron.aurora.utilities.AnimationUtility
import com.crestron.aurora.utilities.ViewUtil/*
import com.programmerbox.dragswipe.Direction
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.programmerbox.dragswipe.DragSwipeUtils*/
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.android.synthetic.main.activity_hi_lo.*
import kotlinx.android.synthetic.main.match_item.view.*

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

    lateinit var adapter: HiLoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hi_lo)

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        deck = Deck(true, deckListener = deckListener)

        playerCard = deck!!.draw()
        player_card_view.setImageResource(playerCard!!.getImage(this))
        next.isEnabled = false

        val list = arrayListOf<Card>()

        adapter = HiLoAdapter(list, this)

        hilo_past_cards.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }
        hilo_past_cards.setHasFixedSize(true)
        hilo_past_cards.adapter = adapter
        hilo_past_cards.itemAnimator = SlideInUpAnimator(OvershootInterpolator())

        //DragSwipeUtils.setDragSwipeUp(adapter, hilo_past_cards, dragDirs = Direction.START + Direction.END)

        hilo_show_past_cards.isChecked = true

        hilo_show_past_cards.setOnCheckedChangeListener { _, b ->
            hilo_past_cards.animate().alpha(if(b) 1f else 0f)
        }

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
                            playerCard!!.value == mysteryCard!!.value -> null
                            condition -> playerCard!!.value < mysteryCard!!.value
                            else -> playerCard!!.value > mysteryCard!!.value
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
                //adapter.addItem(playerCard!!, 0)
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

    class HiLoAdapter(val list: ArrayList<Card>, val context: Context) : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val iv = ImageView(context)
            iv.setImageResource(list[position].getImage(context))
            holder.linearLayout.addView(iv)
            //holder.cardView.setImageResource(list[position].getImage(context))
            //holder.cardView.contentDescription = list[position].toString()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.match_item, parent, false))
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //val cardView: ImageView = itemView.card_info_cards!!
        val linearLayout: LinearLayout = itemView.match_card_item_layout!!
    }

}
