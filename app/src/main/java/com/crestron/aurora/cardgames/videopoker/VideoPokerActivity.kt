package com.crestron.aurora.cardgames.videopoker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.AnimationUtility
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.views.BubbleEmitter
import com.crestron.aurora.views.VideoPokerDialog
import com.crestron.aurora.views.createBubbles
import com.crestron.aurora.views.stopAllBubbles
/*import com.programmerbox.dragswipe.Direction
import com.programmerbox.dragswipe.DragSwipeActions
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.programmerbox.dragswipe.DragSwipeUtils
import com.programmerbox.dragswipeex.shuffleItems*/
import com.wx.wheelview.widget.WheelView
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Hand
import hari.floatingtoast.FloatingToast
import io.kimo.konamicode.KonamiCode
import jp.wasabeef.recyclerview.animators.SlideInDownAnimator
import kotlinx.android.synthetic.main.activity_video_poker.*
import kotlinx.android.synthetic.main.video_poker_card_button.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.steppertouch.OnStepCallback
import kotlin.math.abs

class VideoPokerActivity : AppCompatActivity() {

    lateinit var adapter: VideoPokerAdapter

    private val scores = Scores()
    val hand: Hand = Hand("")
    var deckOfCards = Deck(true)
    private var winning = 20
        set(value) {
            AnimationUtility.startCountAnimation(field, value, interpolator = OvershootInterpolator(),
                    countListener = object : AnimationUtility.CountListener {
                        @SuppressLint("SetTextI18n")
                        override fun numberUpdate(change: Number) {
                            winnings.text = "\$$change"
                        }

                    }, view = winnings)
            field = value
            //winnings.text = "\$$value"
        }

    private fun animateFun() {
        /*val randomNumberAnimation = RandomNumberAnimation(winnings)
        launch(UI) {
            randomNumberAnimation.start()
            delay(25)
            randomNumberAnimation.stop(keepChange = false)
        }*/
    }

    companion object VideoDebug {
        var debugged = false
    }

    interface DebugListener {
        fun wheelChange(t: Card?) {

        }

        fun wheelChange(t: Card?, num: Int) {

        }

        fun getHand(): Hand
    }

    var betAmount = 3

    open class CardHold(var c: Card, var hold: Boolean = false)

    open class ViewAndButton(val cardView: ImageView, val holdButton: Button, val listener: DebugListener) {
        var hold = false
            set(value) {
                holdButton.text = if (value) {
                    "Holding"
                } else {
                    "Hold"
                }
                field = value
            }

        lateinit var card: Card

        init {
            val clickListener = View.OnClickListener {
                hold = !hold
            }
            cardView.setOnClickListener(clickListener)
            holdButton.setOnClickListener(clickListener)
            cardView.isEnabled = false
            holdButton.isEnabled = false
            cardView.setOnLongClickListener {
                if (cardView.isEnabled && debugged)
                    VideoPokerDialog(it.context, card, listener.getHand(), object : WheelView.OnWheelItemSelectedListener<Card> {
                        override fun onItemSelected(position: Int, t: Card?) {
                            card = t!!
                            cardView.setImageResource(t.getImage(it.context))
                            listener.wheelChange(t)
                        }
                    }).show()
                true
            }
        }
    }

    private val cardsAndButtons = arrayOfNulls<ViewAndButton>(5)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_poker)

        debugged = false

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        back_button_videopoker.setOnClickListener {
            finish()
        }

        bet_amount.sideTapEnabled = true
        bet_amount.minValue = 1
        bet_amount.maxValue = 5
        bet_amount.count = 3
        bet_amount.addStepCallback(object : OnStepCallback {
            override fun onStep(value: Int, positive: Boolean) {
                betAmount = value
                try {
                    score_view.text = Html.fromHtml(scores.htmlValues(scores.getWinningHand(hand), betAmount), Html.FROM_HTML_MODE_COMPACT)
                } catch (e: IndexOutOfBoundsException) {
                    score_view.text = Html.fromHtml(scores.htmlValues(betAmount), Html.FROM_HTML_MODE_COMPACT)
                }
            }

        })

        jack_or_better.setOnCheckedChangeListener { _, isChecked ->
            scores.jacksOrBetter = isChecked
        }

        deckOfCards.deckListener = object : Deck.DeckListener {

            @SuppressLint("SetTextI18n")
            override fun draw(c: Card, size: Int) {
                if (size < 10) {
                    deckOfCards = Deck(true, deckListener = this)
                }
                cards_left.text = "${deckOfCards.deckCount()} Cards Left"
            }

            override fun shuffle() {
                super.shuffle()
                GlobalScope.launch(Dispatchers.Main) {

                    for (i in hand.hand.indices) {
                        cardsAndButtons[i]!!.holdButton.isEnabled = false
                        cardsAndButtons[i]!!.cardView.isEnabled = false
                    }

                    refreshLayouts()
                    discard_button.isEnabled = false
                    play_again.isEnabled = false
                    val delayTime = 50
                    val seq = sequence {
                        while (true)
                            yield(".")
                    }
                    for (i in 0..3) {
                        for (j in 0..3) {
                            cards_left.text = "Shuffling${seq.take(j).joinToString("")}"
                            delay(delayTime.toLong())
                        }
                        for (j in 3 downTo 0) {
                            cards_left.text = "Shuffling${seq.take(j).joinToString("")}"
                            delay(delayTime.toLong())
                        }
                    }
                    cards_left.text = "${deckOfCards.deckCount()} Cards Left"
                    play_again.isEnabled = true
                    winning += betAmount
                }
            }
        }

        play_cards.isEnabled = false
        discard_button.isEnabled = false

        fun debugListener(num: Int) = object : DebugListener {
            override fun wheelChange(t: Card?) {
                hand.replaceCard(num, t!!)
                current_hand.text = scores.getWinningHand(hand)
                score_view.text = Html.fromHtml(scores.htmlValues(scores.getWinningHand(hand), betAmount), Html.FROM_HTML_MODE_COMPACT)
            }

            override fun getHand(): Hand {
                return hand
            }
        }

        val debugListener2 = object : DebugListener {
            override fun wheelChange(t: Card?, num: Int) {
                hand.replaceCard(num, t!!)
                current_hand.text = scores.getWinningHand(hand)
                score_view.text = Html.fromHtml(scores.htmlValues(scores.getWinningHand(hand), betAmount), Html.FROM_HTML_MODE_COMPACT)
            }

            override fun getHand(): Hand {
                return hand
            }
        }

        class OverlapDecoration(private var horizontalOverlap: Int = -200) : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
                val itemPosition = parent.getChildAdapterPosition(view)
                if (itemPosition == 0) {
                    return
                }
                outRect.set(horizontalOverlap, 0, 0, 0)
            }
        }

        val cardList = arrayListOf<CardHold>()

        for (i in 0 until 5) {
            cardList += CardHold(Card.BackCard)
        }

        adapter = VideoPokerAdapter(cardList, this@VideoPokerActivity, object : VideoPokerListeners {
            override fun isDebugged(): Boolean {
                return debugged
            }
        }, debugListener2)

        val bitmap = BitmapFactory.decodeResource(resources, Card.BackCard.getImage(this@VideoPokerActivity))
        video_poker_rv.addItemDecoration(OverlapDecoration((-bitmap.width / 2.5).toInt()))
        video_poker_rv.setHasFixedSize(true)
        video_poker_rv.itemAnimator = SlideInDownAnimator(LinearInterpolator())
        video_poker_rv.adapter = adapter
        /*DragSwipeUtils.setDragSwipeUp(adapter, video_poker_rv, dragDirs = Direction.START + Direction.END, dragSwipeActions = object : DragSwipeActions<CardHold, ViewHolder> {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder, dragSwipeAdapter: DragSwipeAdapter<CardHold, ViewHolder>) {
                super.onMove(recyclerView, viewHolder, target, dragSwipeAdapter)
                hand.clearHand()
                for (i in adapter.list)
                    hand.add(i.c)
            }
        })*/

        //, swipeDirs = Direction.UP.value)

        cardsAndButtons[0] = ViewAndButton(card_one, hold_one, debugListener(0))
        cardsAndButtons[1] = ViewAndButton(card_two, hold_two, debugListener(1))
        cardsAndButtons[2] = ViewAndButton(card_three, hold_three, debugListener(2))
        cardsAndButtons[3] = ViewAndButton(card_four, hold_four, debugListener(3))
        cardsAndButtons[4] = ViewAndButton(card_five, hold_five, debugListener(4))

        score_view.text = Html.fromHtml(scores.htmlValues(betAmount), Html.FROM_HTML_MODE_COMPACT)

        shuffle_hand.setOnClickListener {
            //adapter.shuffleItems()
            hand.clearHand()
            for (i in adapter.list)
                hand.add(i.c)
        }

        discard_button.setOnClickListener {
            for (i in adapter.list.indices) {
                if (!adapter.list[i].hold) {
                    hand.replaceCard(i, deckOfCards.draw())
                }
            }
            //hand.sortHandByValue()
            for (i in hand.hand.indices) {
                /*cardsAndButtons[i]!!.card = hand.getCard(i)
                cardsAndButtons[i]!!.holdButton.isEnabled = false
                cardsAndButtons[i]!!.cardView.isEnabled = false*/
                //cardsAndButtons[i]!!.longClick = false
                if (!adapter.list[i].hold) {
                    val ch = CardHold(hand.getCard(i))
                    //adapter.removeItem(i)
                    //adapter.addItem(ch, i)
                }
            }

            refreshLayouts()
            discard_button.isEnabled = false
            play_again.isEnabled = true
            Loged.wtf("${hand.hand}")
        }

        play_cards.setOnClickListener {
            winning += scores.winCheck(hand, 5)
            winnings.text = "\$$winning"
            play_cards.isEnabled = false
        }

        play_cards.visibility = View.GONE

        winnings.text = "\$$winning"
        current_hand.text = "No Hand"

        var newGame = true

        play_again.setOnClickListener {

            if (!discard_button.isEnabled && !newGame) {
                try {
                    val money = scores.winCheck(hand, betAmount)
                    winning += money
                    FloatingToast.makeToast(this@VideoPokerActivity, "$$money ${if (money < 0) "lost" else "won"}", FloatingToast.LENGTH_SHORT).show()

                    GlobalScope.launch {
                        runOnUiThread {
                            video_poker_layout.createBubbles {
                                if (money >= 0) {
                                    setColors(fill = Color.GREEN)
                                } else {
                                    setColors(fill = Color.RED)
                                    rotation = 180f
                                }
                                touchEvent = BubbleEmitter.BUBBLE_POP
                                //startEmitting()
                                oneBubble(abs(money))
                            }
                        }
                        delay(2500)
                        runOnUiThread {
                            video_poker_layout.stopAllBubbles(true)
                        }
                    }
                } catch (e: IndexOutOfBoundsException) {
                }
                // winnings.text = "\$$winning"
                animateFun()
                play_again.isEnabled = true
                play_again.text = "Play Again"
                newGame = true
                bet_amount.visibility = View.VISIBLE
                if (winning < 0) {
                    val builder = AlertDialog.Builder(this)
                    builder.setCancelable(false)
                    builder.setTitle("You're out of Money!")
                    builder.setMessage("Sorry")
                    // Add the buttons
                    builder.setPositiveButton("Play Again") { _, _ ->
                        finish()
                        startActivity(intent)
                    }
                    builder.setNegativeButton("Nope") { _, _ ->
                        finish()
                    }
                    val dialog = builder.create()
                    dialog.show()
                }

            } else {

                bet_amount.visibility = View.GONE

                hand.clearHand()

                deckOfCards.dealHand(hand, 5)

                //hand.sortHandByValue()

                val cardLists = arrayListOf<CardHold>()

                for (c in hand.hand) {
                    cardLists += CardHold(c)
                    //adapter.removeItem(i)
                    //adapter.addItem(CardHold(c), i)
                    //cardsAndButtons[i]!!.card = c
                    //cardsAndButtons[i]!!.hold = false
                    //cardsAndButtons[i]!!.holdButton.isEnabled = true
                    //cardsAndButtons[i]!!.cardView.isEnabled = true
                }

                //adapter.setListNotify(cardLists)
                adapter.setNewList(cardLists)
                //adapter.notifyDataSetChanged()

                refreshLayouts()

                discard_button.isEnabled = true
                play_again.isEnabled = false
                play_again.text = "Play Cards"
                newGame = false
            }
        }

        KonamiCode.Installer(this)
                .on(this)
                .callback {
                    Toast.makeText(this@VideoPokerActivity, "Debug Mode Activated!", Toast.LENGTH_SHORT).show()
                    debugged = true
                }.install()

    }

    @Suppress("unused")
    private fun refreshLayout() {
        for (i in cardsAndButtons.indices) {
            if (!cardsAndButtons[i]!!.hold) {
                AnimationUtility.animateCard(cardsAndButtons[i]!!.cardView, Card.BackCard, this@VideoPokerActivity, end = object : AnimationUtility.AnimationEnd {
                    override fun onAnimationEnd() {
                        super.onAnimationEnd()
                        GlobalScope.launch(Dispatchers.Main) {
                            delay(200)
                            AnimationUtility.animateCard(cardsAndButtons[i]!!.cardView, cardsAndButtons[i]!!.card, this@VideoPokerActivity, end = object : AnimationUtility.AnimationEnd {
                                override fun onAnimationEnd() {
                                    super.onAnimationEnd()
                                    current_hand.text = scores.getWinningHand(hand)
                                    //score_view.text = Html.fromHtml(scores.htmlValuesWithMark(scores.getWinningHand(hand)), Html.FROM_HTML_MODE_COMPACT)
                                    score_view.text = Html.fromHtml(scores.htmlValues(scores.getWinningHand(hand), betAmount), Html.FROM_HTML_MODE_COMPACT)
                                }
                            })
                        }

                    }
                })
            }
        }

    }

    private fun refreshLayouts() {
        /*for(i in adapter.list.indices) {
            if (!adapter.list[i].hold) {
                val vpView = video_poker_rv.findViewHolderForAdapterPosition(i) as? ViewHolder
                AnimationUtility.animateCard(vpView!!.cardView, Card.BackCard, this@VideoPokerActivity, end = object : AnimationUtility.AnimationEnd {
                    override fun onAnimationEnd() {
                        super.onAnimationEnd()
                        GlobalScope.launch(Dispatchers.Main) {
                            delay(200)
                            AnimationUtility.animateCard(vpView.cardView, adapter.list[i].c, this@VideoPokerActivity, end = object : AnimationUtility.AnimationEnd {
                                override fun onAnimationEnd() {
                                    super.onAnimationEnd()
                                    current_hand.text = scores.getWinningHand(hand)
                                    //score_view.text = Html.fromHtml(scores.htmlValuesWithMark(scores.getWinningHand(hand)), Html.FROM_HTML_MODE_COMPACT)
                                    score_view.text = Html.fromHtml(scores.htmlValues(scores.getWinningHand(hand), betAmount), Html.FROM_HTML_MODE_COMPACT)
                                }
                            })
                        }

                    }
                })
            }
        }*/
        current_hand.text = scores.getWinningHand(hand)
        //score_view.text = Html.fromHtml(scores.htmlValuesWithMark(scores.getWinningHand(hand)), Html.FROM_HTML_MODE_COMPACT)
        score_view.text = Html.fromHtml(scores.htmlValues(scores.getWinningHand(hand), betAmount), Html.FROM_HTML_MODE_COMPACT)

    }

    override fun onBackPressed() {
        //super.onBackPressed()
        back_button_videopoker.performClick()
    }

    interface VideoPokerListeners {
        fun isDebugged(): Boolean
    }

    class VideoPokerAdapter(val list: ArrayList<CardHold>, val context: Context, private val listeners: VideoPokerListeners, val debugListener: DebugListener) : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, index: Int) {
            val listener = View.OnClickListener {
                list[index].hold = !list[index].hold
                //holder.holdButton.text = if (list[index].hold) "Holding" else "Hold"
                holder.holdButton.text = if (list[index].hold) "✅" else "❌"
            }
            holder.holdButton.text = if (list[index].hold) "✅" else "❌"
            //list[index].hold = false
            holder.cardView.setImageResource(list[index].c.getImage(context))
            holder.cardView.setOnClickListener(listener)
            holder.holdButton.setOnClickListener(listener)
            holder.cardView.setOnLongClickListener {
                if (listeners.isDebugged()) {
                    VideoPokerDialog(it.context, list[index].c, debugListener.getHand(), object : WheelView.OnWheelItemSelectedListener<Card> {
                        override fun onItemSelected(position: Int, t: Card?) {
                            list[index].c = t!!
                            holder.cardView.setImageResource(t.getImage(it.context))
                            debugListener.wheelChange(t, index)
                        }
                    }).show()
                    true
                } else {
                    false
                }
            }
        }

        fun setNewList(newList: List<CardHold>) {
            /*for (i in newList.withIndex()) {
                removeItem(i.index)
                addItem(i.value, i.index)
            }*/
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.video_poker_card_button, parent, false))
        }

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: ImageView = itemView.vp_card!!
        val holdButton: Button = itemView.vp_button!!
    }

}
