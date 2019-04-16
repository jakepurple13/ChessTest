package com.crestron.aurora.cardgames.videopoker

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.AnimationUtility
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.views.VideoPokerDialog
import com.wx.wheelview.widget.WheelView
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Hand
import hari.floatingtoast.FloatingToast
import io.kimo.konamicode.KonamiCode
import kotlinx.android.synthetic.main.activity_video_poker.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import nl.dionsegijn.steppertouch.OnStepCallback

class VideoPokerActivity : AppCompatActivity() {

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
        fun wheelChange(t: Card?)
        fun getHand(): Hand
    }

    var betAmount = 3

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

        bet_amount.enableSideTap(true)
        bet_amount.stepper.setMin(1)
        bet_amount.stepper.setMax(5)
        bet_amount.stepper.setValue(3)
        bet_amount.stepper.addStepCallback(object : OnStepCallback {
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
                back_button_videopoker.text = "Back\n${deckOfCards.deckCount()} Cards Left"
            }

            override fun shuffle() {
                super.shuffle()
                GlobalScope.launch(Dispatchers.Main) {

                    for (i in hand.hand.indices) {
                        cardsAndButtons[i]!!.holdButton.isEnabled = false
                        cardsAndButtons[i]!!.cardView.isEnabled = false
                    }

                    refreshLayout()
                    discard_button.isEnabled = false
                    play_again.isEnabled = false
                    val delayTime = 50
                    val seq = sequence {
                        while (true)
                            yield(".")
                    }
                    for (i in 0..3) {
                        for (j in 0..3) {
                            back_button_videopoker.text = "Shuffling${seq.take(j).joinToString("")}"
                            delay(delayTime.toLong())
                        }
                        for (j in 3 downTo 0) {
                            back_button_videopoker.text = "Shuffling${seq.take(j).joinToString("")}"
                            delay(delayTime.toLong())
                        }
                    }
                    back_button_videopoker.text = "Back\n${deckOfCards.deckCount()} Cards Left"
                    play_again.isEnabled = true
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

        cardsAndButtons[0] = ViewAndButton(card_one, hold_one, debugListener(0))
        cardsAndButtons[1] = ViewAndButton(card_two, hold_two, debugListener(1))
        cardsAndButtons[2] = ViewAndButton(card_three, hold_three, debugListener(2))
        cardsAndButtons[3] = ViewAndButton(card_four, hold_four, debugListener(3))
        cardsAndButtons[4] = ViewAndButton(card_five, hold_five, debugListener(4))

        score_view.text = Html.fromHtml(scores.htmlValues(betAmount), Html.FROM_HTML_MODE_COMPACT)

        discard_button.setOnClickListener {
            for (i in cardsAndButtons.indices) {
                if (!cardsAndButtons[i]!!.hold) {
                    hand.replaceCard(i, deckOfCards.draw())
                }
            }
            //hand.sortHandByValue()
            for (i in hand.hand.indices) {
                cardsAndButtons[i]!!.card = hand.getCard(i)
                cardsAndButtons[i]!!.holdButton.isEnabled = false
                cardsAndButtons[i]!!.cardView.isEnabled = false
                //cardsAndButtons[i]!!.longClick = false
            }

            refreshLayout()
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

                for ((i, c) in hand.hand.withIndex()) {
                    cardsAndButtons[i]!!.card = c
                    cardsAndButtons[i]!!.hold = false
                    cardsAndButtons[i]!!.holdButton.isEnabled = true
                    cardsAndButtons[i]!!.cardView.isEnabled = true
                }

                refreshLayout()

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

    override fun onBackPressed() {
        //super.onBackPressed()
        back_button_videopoker.performClick()
    }

}
