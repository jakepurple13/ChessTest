package com.crestron.aurora.cardgames.calculation

import android.annotation.SuppressLint
import android.content.ClipData
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.DragEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.crestron.aurora.utilities.AnimationUtility
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.views.TypeWriter
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.CardNotFoundException
import crestron.com.deckofcards.Deck
import kotlinx.android.synthetic.main.activity_calculation.*


class CalculationActivity : AppCompatActivity() {

    private val deck = Deck(shuffler = true, numberOfDecks = 1, seed = System.currentTimeMillis(), deckListener = object : Deck.DeckListener {
        override fun draw(c: Card, size: Int) {
            Loged.i(c.toString())
        }

        override fun shuffle() {
            //Toast.makeText(this@CalculationActivity, "Shuffling...", Toast.LENGTH_SHORT).show()
        }
    })
    private val firstList: ArrayList<Card> = arrayListOf()
    private val secondList: ArrayList<Card> = arrayListOf()
    private val thirdList: ArrayList<Card> = arrayListOf()
    private val fourthList: ArrayList<Card> = arrayListOf()
    private val firstFoundation: ArrayList<Card> = arrayListOf()
    private val secondFoundation: ArrayList<Card> = arrayListOf()
    private val thirdFoundation: ArrayList<Card> = arrayListOf()
    private val fourthFoundation: ArrayList<Card> = arrayListOf()
    private var pickUpCard: Card? = null
    private var locPick: View? = null
    private var lastList: ArrayList<Card>? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculation)

        try {
            firstList.add(deck.getFirstCardByValue(1))
            secondList.add(deck.getFirstCardByValue(2))
            thirdList.add(deck.getFirstCardByValue(3))
            fourthList.add(deck.getFirstCardByValue(4))
        } catch (e: CardNotFoundException) {
            e.printStackTrace()
        }

        //aceList.setImageResource(firstList[0].getImage(this))
        //twoList.setImageResource(secondList[0].getImage(this))
        //threeList.setImageResource(thirdList[0].getImage(this))
        //fourList.setImageResource(fourthList[0].getImage(this))

        next_one.text = "${nextVal(firstList.last().value, 1)}"
        next_two.text = "${nextVal(secondList.last().value, 2)}"
        next_three.text = "${nextVal(thirdList.last().value, 3)}"
        next_four.text = "${nextVal(fourthList.last().value, 4)}"

        deck_info.text = "${deck.deckCount()} cards left"

        deck_list.setOnClickListener {
            if (pickUpCard == null && deck.deckCount() > 0) {
                pickUpCard = deck.draw()
                deck_info.text = "${deck.deckCount()} cards left"
                AnimationUtility.animateCard(deck_list, pickUpCard!!, this@CalculationActivity, speed = 100)
                //deck_list.setImageResource(pickUpCard!!.getImage(this@CalculationActivity))
                locPick = it
            }
        }

        data class DataHold(val s1: String, val s2: String)

        fun onLongClick(dataHold: DataHold) = View.OnLongClickListener {
            val (label, text) = dataHold
            val data = ClipData.newPlainText(label, text)
            val shadowBuilder = View.DragShadowBuilder(it)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                it.startDragAndDrop(data, shadowBuilder, it, 0)
            }
            false
        }

        fun dragListener(dataHold: DataHold) = View.OnDragListener { _: View?, p1: DragEvent? ->
            when (p1!!.action) {
                DragEvent.ACTION_DRAG_STARTED -> {
                    Loged.d("Started")
                }
                DragEvent.ACTION_DRAG_ENTERED -> {
                    Loged.v("Entered")
                }
                DragEvent.ACTION_DROP -> {
                    val s = p1.clipData.description.label
                    Loged.w("Dropped at $s")
                    when (s) {
                        "one" -> {
                            foundation1.performClick()
                        }
                        "two" -> {
                            foundation2.performClick()
                        }
                        "three" -> {
                            foundation3.performClick()
                        }
                        "four" -> {
                            foundation4.performClick()
                        }
                        "deck" -> {
                            deck_list.performClick()
                        }
                    }
                    when (dataHold.s1) {
                        "once" -> aceList.performClick()
                        "second" -> twoList.performClick()
                        "third" -> threeList.performClick()
                        "fourth" -> fourList.performClick()
                    }
                }
                DragEvent.ACTION_DRAG_ENDED -> {
                    Loged.e("Ended")
                }
                DragEvent.ACTION_DRAG_LOCATION -> {
                    Loged.i("Drag Location")
                }
                DragEvent.ACTION_DRAG_EXITED -> {
                    Loged.wtf("Exited")
                }
            }
            true
        }

        foundation1.setOnLongClickListener(onLongClick(DataHold("one", "two")))
        foundation2.setOnLongClickListener(onLongClick(DataHold("two", "two")))
        foundation3.setOnLongClickListener(onLongClick(DataHold("three", "two")))
        foundation4.setOnLongClickListener(onLongClick(DataHold("four", "two")))
        deck_list.setOnLongClickListener(onLongClick(DataHold("deck", "two")))

        aceList.setOnDragListener(dragListener(DataHold("first", "one")))
        twoList.setOnDragListener(dragListener(DataHold("second", "one")))
        threeList.setOnDragListener(dragListener(DataHold("third", "one")))
        fourList.setOnDragListener(dragListener(DataHold("fourth", "one")))

        fun place(cardList: ArrayList<Card>, count: Int, nextValue: TextView, cardView: ImageView) {
            if (pickUpCard != null) {
                val valueCheck = nextVal(cardList.last().value, count)
                if (valueCheck == pickUpCard!!.value) {
                    updateViews(cardList, cardView, pickUpCard!!, null)
                    nextValue.text = "${nextVal(cardList.last().value, count)}"
                    lastList!!.removeAt(lastList!!.size - 1)
                    pickUpCard = null
                } else {
                    pickUpCard = null
                }
            }
            updateAllViews()
        }

        aceList.setOnClickListener {
            place(firstList, 1, next_one, aceList)
        }

        twoList.setOnClickListener {
            place(secondList, 2, next_two, twoList)
        }

        threeList.setOnClickListener {
            place(thirdList, 3, next_three, threeList)
        }

        fourList.setOnClickListener {
            place(fourthList, 4, next_four, fourList)
        }

        fun foundPlace(cardList: ArrayList<Card>, cardListed: TypeWriter, imageView: ImageView) {
            pickUpCard = if (pickUpCard != null && locPick != null) {
                updateViews(cardList, imageView, pickUpCard!!, cardListed)
                locPick = null
                null
            } else {
                val cards: Card? = if (locPick == null && cardList.isNotEmpty()) {
                    //cardList.removeAt(cardList.size - 1)
                    lastList = cardList
                    cardList.last()
                } else {
                    null
                }
                locPick = null
                cards
            }
        }

        foundation1.setOnClickListener {
            foundPlace(firstFoundation, one_cards, foundation1)
        }

        foundation2.setOnClickListener {
            foundPlace(secondFoundation, two_cards, foundation2)
        }

        foundation3.setOnClickListener {
            foundPlace(thirdFoundation, three_cards, foundation3)
        }

        foundation4.setOnClickListener {
            foundPlace(fourthFoundation, four_cards, foundation4)
        }

    }

    private fun nextVal(currentVal: Int, upBy: Int): Int {
        val added = currentVal + upBy
        return if (added > 13) {
            added - 13
        } else {
            added
        }
    }

    override fun onResume() {
        super.onResume()
        AnimationUtility.animateCard(aceList, firstList[0], this@CalculationActivity)
        AnimationUtility.animateCard(twoList, secondList[0], this@CalculationActivity)
        AnimationUtility.animateCard(threeList, thirdList[0], this@CalculationActivity)
        AnimationUtility.animateCard(fourList, fourthList[0], this@CalculationActivity)
    }

    private fun updateViews(list: ArrayList<Card>, imageView: ImageView, c: Card, textView: TypeWriter?) {
        list.add(c)
        imageView.setImageResource(c.getImage(this))
        textView?.animateTextSides(list.toString())
        deck_list.setImageResource(R.drawable.b1fv)
        updateAllViews()
    }

    private fun updateAllViews() {
        fun setUp(found: ImageView, texted: TypeWriter, cardList: ArrayList<Card>) {
            if (cardList.isNotEmpty()) {
                found.setImageResource(cardList.last().getImage(this))
                texted.animateTextSides(cardList.toString())
            } else {
                found.setImageResource(R.drawable.b1fv)
                //texted.removeText()
                texted.removeTextSides()
            }
        }

        setUp(foundation1, one_cards, firstFoundation)
        setUp(foundation2, two_cards, secondFoundation)
        setUp(foundation3, three_cards, thirdFoundation)
        setUp(foundation4, four_cards, fourthFoundation)
    }

}
