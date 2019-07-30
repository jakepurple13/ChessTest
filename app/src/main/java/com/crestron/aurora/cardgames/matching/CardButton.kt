package com.crestron.aurora.cardgames.matching

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import com.crestron.aurora.Loged
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.Suit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("ViewConstructor")
class CardButton(context: Context?, var card: Card = Card.BackCard, var action: MatchingActivity.CardButtonFlip) : ImageView(context), View.OnClickListener {
    var flipped = false
    override fun onClick(p0: View?) {
        Loged.wtf("$card")
        action.flip(card, this)
        flipped = true
    }
    fun setImageNow() {
        setImageResource(Card(Suit.SPADES, 16).getImage(context))
    }
    fun animateImage() {
        rotationY = 0f
        animate().setDuration(100).rotationY(90f).setListener(object : Animator.AnimatorListener {
            override fun onAnimationRepeat(p0: Animator?) {
            }

            override fun onAnimationEnd(p0: Animator?) {
                if(flipped)
                    setImageResource(card.getImage(context))
                else
                    setImageResource(Card(Suit.SPADES, 16).getImage(context))
                rotationY = 270f
                animate().rotationY(360f).setListener(null)
            }

            override fun onAnimationStart(p0: Animator?) {
            }

            override fun onAnimationCancel(p0: Animator?) {
            }

        })
    }

    fun setCardImage() {
        setImageResource(card.getImage(context))
    }

    private fun checkCo(cardFlipped: Card, buttonFlipped: CardButton) = GlobalScope.launch(Dispatchers.Main) {
        delay(500)
        if (cardFlipped.equals(card)) {
            isEnabled = false
            buttonFlipped.isEnabled = false
        } else {
            //flip face down
            flipped = false
            animateImage()
            buttonFlipped.flipped = false
            buttonFlipped.animateImage()
        }
    }

    fun checkMatch(cardFlipped: Card, buttonFlipped: CardButton): Int {
        animateImage()
        checkCo(cardFlipped, buttonFlipped).cancel()
        checkCo(cardFlipped, buttonFlipped).start()
        return if (cardFlipped.equals(card)) {
            isEnabled = false
            buttonFlipped.isEnabled = false
            1
        } else {
            0
        }
    }
    init {
        setOnClickListener(this)
    }
}