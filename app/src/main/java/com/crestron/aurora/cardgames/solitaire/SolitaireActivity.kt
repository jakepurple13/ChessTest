package com.crestron.aurora.cardgames.solitaire

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.ImageView
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.*
import com.crestron.aurora.views.BubbleEmitter
import com.crestron.aurora.views.createBubbles
import com.crestron.aurora.views.stopAllBubbles
import com.github.jinatonic.confetti.CommonConfetti
import com.plattysoft.leonids.ParticleSystem
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.CardNotFoundException
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Suit
import kotlinx.android.synthetic.main.activity_solitaire.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import spencerstudios.com.bungeelib.Bungee
import java.util.*
import java.util.concurrent.TimeUnit

class SolitaireActivity : AppCompatActivity() {

    private var deckOfCards: Deck = Deck(true, 1, null, object : Deck.DeckListener {
        @SuppressLint("SetTextI18n")
        override fun draw(c: Card, size: Int) {
            Loged.i("$c")
            deck_info.text = "$size Cards Left"
        }
    })

    private val fieldSlots = arrayOfNulls<FieldSlot>(7)

    /**
     * place = foundation
     * location = up and down
     */
    open class CardLocation(val location: Int, val card: Card, val place: Int)

    private var cardLoc: CardLocation? = null

    private val drawList: ArrayList<Card> = arrayListOf()

    @SuppressLint("SetTextI18n")
    private fun ArrayList<Card>.addCard(card: Card) {
        this.add(card)
        cards_in_pile.text = "${this.size} Cards"
    }

    @SuppressLint("SetTextI18n")
    private fun ArrayList<Card>.addCards(card: Collection<Card>) {
        this.addAll(card)
        cards_in_pile.text = "${this.size} Cards"
    }

    @SuppressLint("SetTextI18n")
    private fun ArrayList<Card>.removeCardAt(num: Int): Card {
        val c = this.removeAt(num)
        cards_in_pile.text = "${this.size} Cards"
        return c
    }

    private val foundation1: ArrayList<Card> = arrayListOf()
    private val foundation2: ArrayList<Card> = arrayListOf()
    private val foundation3: ArrayList<Card> = arrayListOf()
    private val foundation4: ArrayList<Card> = arrayListOf()

    private var drawAmount = 1

    private var moveCount = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            move_count.text = "$value moves"
        }

    private var score = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            AnimationUtility.startCountAnimation(field, value, interpolator = OvershootInterpolator(), countListener = object : AnimationUtility.CountListener {
                override fun numberUpdate(change: Number) {
                    score_solitaire.text = "Score: $change points"
                }
            })
            field = value
            //score_solitaire.text = "Score: $value points"
        }

    private val time = TimerUtil(delay = 0, period = 1)

    private var win = false
    private val gen = Random()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_solitaire)

        ViewUtil.revealing(solitaire_layout, intent)

        drawAmount = intent.getIntExtra(ConstantValues.DRAW_AMOUNT, 1)

        for (i in 0 until 7) {
            fieldSlots[i] = FieldSlot(this, i, deckOfCards)
        }

        for (i in 0 until 7) {
            Loged.d("${fieldSlots[i]} and face down number is ${fieldSlots[i]!!.faceDownSize()}")
        }

        deck_info.text = "${deckOfCards.deckCount()} Cards Left"

        fieldSetUp(0, slot1_cards, slot1)
        fieldSetUp(1, slot2_cards, slot2)
        fieldSetUp(2, slot3_cards, slot3)
        fieldSetUp(3, slot4_cards, slot4)
        fieldSetUp(4, slot5_cards, slot5)
        fieldSetUp(5, slot6_cards, slot6)
        fieldSetUp(6, slot7_cards, slot7)

        deck_of_cards.setOnLongClickListener {
            cardLoc = null
            true
        }

        deck_of_cards.setOnClickListener {
            if (deckOfCards.deckCount() > 0) {
                try {
                    /*for (i in 0 until drawAmount) {
                        drawList.addCard(deckOfCards.draw())
                        deck_of_cards.setImageResource(drawList.last().getImage(this))
                    }*/

                    if (drawList.isEmpty()) {
                        for (i in 0 until drawAmount) {
                            drawList.addCard(deckOfCards.draw())
                            //drawList.addCards(deckOfCards-drawAmount)
                            AnimationUtility.animateCard(deck_of_cards, drawList.last(), this@SolitaireActivity, 50)
                        }
                    } else {
                        //drawList.addCards(deckOfCards-drawAmount)
                        //deck_of_cards.setImageResource(drawList.last().getImage(this))
                        for (i in 0 until drawAmount) {
                            drawList.addCard(deckOfCards.draw())
                            deck_of_cards.setImageResource(drawList.last().getImage(this))
                        }
                    }

                } catch (e: CardNotFoundException) {
                    //e.printStackTrace()
                    Loged.e("${e.message}", showPretty = false)
                }
            } else {
                //deckOfCards.addCards(drawList)

                deckOfCards += drawList
                drawList.clear()
                TypingAnimation(cards_in_pile, 10).animateTextSides("${drawList.size} Cards")
                TypingAnimation(deck_info, 10).animateTextSides("${deckOfCards.deckCount()} Cards Left")
                //cards_in_pile.text = "${drawList.size} Cards"

                AnimationUtility.animateCard(deck_of_cards, Card.BackCard, this@SolitaireActivity, 50, true)
                if (!win)
                    score -= 10
            }
            deck_info.text = "${deckOfCards.deckCount()} Cards Left"
            //deck_info.text = "${deckOfCards.deckCount()} Cards Left"
            cardLoc = null
            if (!win)
                moveCount++
        }

        fun startingFound(c: Card, list: ArrayList<Card>) {
            try {
                //list.add(deckOfCards.getCard(c))
                list.add(deckOfCards.getCard(c))
                /* when {
                     foundation1.isEmpty() -> foundation1
                     foundation2.isEmpty() -> foundation2
                     foundation3.isEmpty() -> foundation3
                     foundation4.isEmpty() -> foundation4
                     else -> null
                 }?.add(deckOfCards - c)*/

                score += 10
                moveCount++
            } catch (e: CardNotFoundException) {
                Loged.d("Already out there")
            }
        }

        val aceS = Card(Suit.SPADES, 1)
        val aceC = Card(Suit.CLUBS, 1)
        val aceD = Card(Suit.DIAMONDS, 1)
        val aceH = Card(Suit.HEARTS, 1)

        startingFound(aceS, foundation1)
        startingFound(aceC, foundation2)
        startingFound(aceD, foundation3)
        startingFound(aceH, foundation4)

        fun foundationClick(view: ImageView, foundation: ArrayList<Card>) = View.OnClickListener {
            if (cardLoc != null) {
                try {
                    if (cardLoc!!.card.value == foundation.last().value + 1 && cardLoc!!.card.suit.equals(foundation.last().suit)) {
                        foundation.add(fieldSlots[cardLoc!!.place]!!.removeCard())
                        moveCount++
                        score += 10
                    }
                } catch (e: NoSuchElementException) {
                    //Loged.w("${e.message}")
                    Loged.w("${e.message}", showPretty = false)
                    if (cardLoc!!.card.value == 1) {
                        foundation.add(fieldSlots[cardLoc!!.place]!!.removeCard())
                        moveCount++
                        score += 10
                    }
                }
            } else {
                try {
                    if ((drawList.last().value == foundation.last().value + 1 && drawList.last().suit.equals(foundation.last().suit))
                            || drawList.last().value == 1) {
                        foundation.add(drawList.removeCardAt(drawList.size - 1))
                        moveCount++
                        score += 10
                    }
                } catch (e: NoSuchElementException) {
                    //Loged.w("${e.message}")
                    Loged.w("${e.message}", showPretty = false)
                    try {
                        if (drawList.last().value == 1) {
                            foundation.add(drawList.removeCardAt(drawList.size - 1))
                            moveCount++
                            score += 10
                        }
                    } catch (e1: NoSuchElementException) {
                        //Loged.w("${e1.message}")
                        Loged.w("${e1.message}", showPretty = false)
                    }
                }
            }
            try {
                view.setImageResource(foundation.last().getImage(this@SolitaireActivity))
            } catch (e: NoSuchElementException) {
                view.setImageResource(Card.BackCard.getImage(this@SolitaireActivity))
            }
            refreshLayout()
            cardLoc = null

            val checker = try {
                val num = 13
                foundation1.last().value == num &&
                        foundation2.last().value == num &&
                        foundation3.last().value == num &&
                        foundation4.last().value == num
            } catch (e: NoSuchElementException) {
                false
            }

            if (checker) {

                val lastScore = this@SolitaireActivity.defaultSharedPreferences.getInt("solitaire_score", 0)

                if (lastScore < score)
                    this@SolitaireActivity.defaultSharedPreferences.edit().putInt("solitaire_score", score).apply()

                val lastMove = this@SolitaireActivity.defaultSharedPreferences.getInt("solitaire_moves", Int.MAX_VALUE)

                if (lastMove > moveCount && moveCount != 0)
                    this@SolitaireActivity.defaultSharedPreferences.edit().putInt("solitaire_moves", moveCount).apply()

                val lastTime = this@SolitaireActivity.defaultSharedPreferences.getLong("solitaire_time", Long.MAX_VALUE)

                if (lastTime > time.time && time.time != 0L)
                    this@SolitaireActivity.defaultSharedPreferences.edit().putLong("solitaire_time", time.time).apply()

                win = true

                time.cancel()

                GlobalScope.launch(Dispatchers.Main) {
                    winAnimation(foundation_1, foundation1)
                    delay(500)
                    winAnimation(foundation_2, foundation2)
                    delay(500)
                    winAnimation(foundation_3, foundation3)
                    delay(500)
                    winAnimation(foundation_4, foundation4)
                }

                val colors = IntArray(11)
                colors[0] = Color.BLUE
                colors[1] = Color.YELLOW
                colors[2] = Color.RED
                colors[3] = Color.BLACK
                colors[4] = Color.CYAN
                colors[5] = Color.DKGRAY
                colors[6] = Color.GRAY
                colors[7] = Color.GREEN
                colors[8] = Color.LTGRAY
                colors[9] = Color.MAGENTA
                colors[10] = Color.WHITE
                CommonConfetti.rainingConfetti(solitaire_layout, colors)
                        .stream(1000)
                        .setNumInitialCount(50)
                        .setTouchEnabled(true).animate()

                val ps = ParticleSystem(this, 100, R.drawable.star_pink, 800)
                ps.setScaleRange(0.7f, 1.3f)
                ps.setSpeedRange(0.1f, 0.25f)
                ps.setRotationSpeedRange(90f, 180f)
                ps.setFadeOut(200, AccelerateInterpolator())
                ps.oneShot(solitaire_layout, 70)

                val ps2 = ParticleSystem(this, 100, R.drawable.star_white, 800)
                ps2.setScaleRange(0.7f, 1.3f)
                ps2.setSpeedRange(0.1f, 0.25f)
                ps.setRotationSpeedRange(90f, 180f)
                ps2.setFadeOut(200, AccelerateInterpolator())
                ps2.oneShot(solitaire_layout, 70)

                ps.stopEmitting()
                ps2.stopEmitting()

            }
        }

        foundation_1.setOnClickListener(foundationClick(foundation_1, foundation1))
        foundation_2.setOnClickListener(foundationClick(foundation_2, foundation2))
        foundation_3.setOnClickListener(foundationClick(foundation_3, foundation3))
        foundation_4.setOnClickListener(foundationClick(foundation_4, foundation4))

        fun longClicker(foundation: ArrayList<Card>) = View.OnLongClickListener {
            Loged.wtf("$foundation")
            true
        }

        foundation_1.setOnLongClickListener(longClicker(foundation1))
        foundation_2.setOnLongClickListener(longClicker(foundation2))
        foundation_3.setOnLongClickListener(longClicker(foundation3))
        foundation_4.setOnLongClickListener(longClicker(foundation4))

        foundation_1.performClick()
        foundation_2.performClick()
        foundation_3.performClick()
        foundation_4.performClick()

        new_game_solitaire.setOnClickListener {
            time.cancel()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Start a New Game?")
            builder.setMessage("Are you sure you want to end your current game?")
            // Add the buttons
            builder.setPositiveButton("Yes!") { _, _ ->
                intent.putExtra(ConstantValues.DRAW_AMOUNT, drawAmount)
                finish()
                startActivity(intent)
                Bungee.swipeLeft(this@SolitaireActivity)
            }
            builder.setNegativeButton("Nope") { _, _ ->
                time.startTimer(solitaire_timer)
            }
            builder.setOnDismissListener {
                solitaire_layout.stopAllBubbles(true)
                time.startTimer(solitaire_timer)
            }
            val dialog = builder.create()
            dialog.show()
            startBubbles()
        }

        back_button_solitaire.setOnClickListener {
            time.cancel()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Done Playing?")
            builder.setMessage("Are you sure you want to stop?")
            // Add the buttons
            builder.setPositiveButton("Yes, I want to stop") { _, _ ->
                finish()
                Bungee.shrink(this@SolitaireActivity)
            }
            builder.setNegativeButton("No, I do not want to stop") { _, _ ->
                if (!win)
                    time.startTimer(solitaire_timer)
            }
            builder.setOnDismissListener {
                solitaire_layout.stopAllBubbles(true)
                if (!win)
                    time.startTimer(solitaire_timer)
            }
            val dialog = builder.create()
            dialog.show()
            startBubbles()
        }

        time.startTimer(solitaire_timer)

        cards_in_pile.text = "${drawList.size} Cards"

        auto_move.setOnClickListener {
            autoMove()
        }

        solitaire_layout.setOnClickListener {
            if (win) {
                winDialog()
            }
        }

        findViewById<ViewGroup>(android.R.id.content).setOnClickListener {
            if (win) {
                winDialog()
            }
        }

        high_score_view.setOnClickListener {
            val lastScore = this@SolitaireActivity.defaultSharedPreferences.getInt("solitaire_score", 0)
            val lastMove = this@SolitaireActivity.defaultSharedPreferences.getInt("solitaire_moves", 0)
            val lastTime = this@SolitaireActivity.defaultSharedPreferences.getLong("solitaire_time", 0)
            time.cancel()
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Your Current High Score")
            builder.setMessage("Highest Score: ${if (lastScore == 0) "N/A" else lastScore.toString()}\n" +
                    "Lowest Move Count: ${if (lastMove == 0) "N/A" else lastMove.toString()}\n" +
                    "Fastest Time: ${if (lastTime == 0L) "N/A" else getTime(lastTime)}")
            // Add the buttons
            builder.setPositiveButton("Cool!") { _, _ ->
                solitaire_layout.stopAllBubbles(true)
                if (!win)
                    time.startTimer(solitaire_timer)
            }
            builder.setOnDismissListener {
                solitaire_layout.stopAllBubbles(true)
                if (!win)
                    time.startTimer(solitaire_timer)
            }
            val dialog = builder.create()
            dialog.show()
            startBubbles()
        }
    }

    private fun getTime(length: Long): String {
        val m = TimeUnit.MILLISECONDS.toMinutes(length)
        val s = TimeUnit.MILLISECONDS.toSeconds(length - m * 60 * 1000)
        return String.format("%02d:%02d", m, s)
    }

    private fun startBubbles() {
        solitaire_layout.createBubbles {
            for(i in 0..10)
                fillColorsToUse+= kotlin.random.Random.nextColor()
            touchEvent = BubbleEmitter.BUBBLE_POP
        }.startEmitting()
    }

    private fun winDialog() {

        startBubbles()

        val lastScore = this@SolitaireActivity.defaultSharedPreferences.getInt("solitaire_score", 0)
        val lastMove = this@SolitaireActivity.defaultSharedPreferences.getInt("solitaire_moves", 0)
        val lastTime = this@SolitaireActivity.defaultSharedPreferences.getLong("solitaire_time", 0)

        deck_of_cards.setOnClickListener(null)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("YOU WIN!")
        builder.setMessage("You won!" +
                "\nIt took ${time.getTime()} seconds!" +
                "\nWith a score of $score!" +
                "\nA move count of $moveCount!" +
                "\nYour high score is $lastScore." +
                "\nYour lowest move count is $lastMove." +
                "\nYour fastest time is ${getTime(lastTime)}" +
                "\nWant to play again?")
        // Add the buttons
        builder.setPositiveButton("Play Again") { _, _ ->
            intent.putExtra(ConstantValues.DRAW_AMOUNT, drawAmount)
            finish()
            startActivity(intent)
            Bungee.swipeLeft(this@SolitaireActivity)
        }
        builder.setNegativeButton("Nope") { _, _ ->
            finish()
            Bungee.swipeLeft(this@SolitaireActivity)
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun winAnimation(cardView: ImageView, listOfCard: ArrayList<Card>) {
        var i = listOfCard.lastIndex
        var upOrDown = true
        AnimationUtility.animateCardWin(cardView, listOfCard[i], this@SolitaireActivity, reverse = gen.nextBoolean(), end = object : AnimationUtility.AnimationEnd {
            override fun onAnimationEnd() {
                val an = this
                GlobalScope.launch(Dispatchers.Main) {
                    delay(500)
                    if (i >= 0 && upOrDown) {
                        i--
                        upOrDown = i != 0
                    } else if (i <= listOfCard.lastIndex && !upOrDown) {
                        i++
                        upOrDown = i == listOfCard.lastIndex
                    }
                    if (i >= 0) {
                        AnimationUtility.animateCardWin(cardView, listOfCard[i], this@SolitaireActivity, reverse = gen.nextBoolean(), end = an)
                    }
                }
            }
        })
    }

    private fun autoMove() {
        for (i in fieldSlots.indices) {
            cardLoc = null
            try {
                val card = fieldSlots[i]!!.lastCard()
                cardLoc = CardLocation(fieldSlots[i]!!.list.lastIndex, card, i)
                if (card.value == 1) {
                    when {
                        foundation1.isEmpty() -> foundation_1
                        foundation2.isEmpty() -> foundation_2
                        foundation3.isEmpty() -> foundation_3
                        foundation4.isEmpty() -> foundation_4
                        else -> null
                    }?.performClick()
                } else {
                    when {
                        foundation1.isNotEmpty() && foundation1.last().suit == card.suit -> foundation_1
                        foundation2.isNotEmpty() && foundation2.last().suit == card.suit -> foundation_2
                        foundation3.isNotEmpty() && foundation3.last().suit == card.suit -> foundation_3
                        foundation4.isNotEmpty() && foundation4.last().suit == card.suit -> foundation_4
                        else -> null
                    }?.performClick()
                }
            } catch (e: NullPointerException) {
                Loged.e("${e.message}", showPretty = false)
                //e.printStackTrace()
                continue
            } catch (e: NoSuchElementException) {
                Loged.e("${e.message}", showPretty = false)
                //e.printStackTrace()
                continue
            }
        }
        cardLoc = null
        try {
            if (drawList[drawList.size - 1].value == 1) {
                when {
                    foundation1.isEmpty() -> foundation_1
                    foundation2.isEmpty() -> foundation_2
                    foundation3.isEmpty() -> foundation_3
                    foundation4.isEmpty() -> foundation_4
                    else -> null
                }?.performClick()
            } else {
                val suit = drawList[drawList.size - 1].suit
                when {
                    foundation1.isNotEmpty() && foundation1.last().suit == suit -> foundation_1
                    foundation2.isNotEmpty() && foundation2.last().suit == suit -> foundation_2
                    foundation3.isNotEmpty() && foundation3.last().suit == suit -> foundation_3
                    foundation4.isNotEmpty() && foundation4.last().suit == suit -> foundation_4
                    else -> null
                }?.performClick()
            }
        } catch (e1: IndexOutOfBoundsException) {
            Loged.e("${e1.message}", showPretty = false)
        } catch (e: NoSuchElementException) {
            Loged.e("${e.message}", showPretty = false)
        }
    }

    private fun cardAdaptSetUp(num: Int) = CardAdapter(fieldSlots[num]!!.list, this, num, object : AdapterPress {
        override fun action(location: Int, c: Card, place: Int) {
            if (cardLoc != null) {
                try {
                    if (fieldSlots[num]!!.checkToAdd(fieldSlots[cardLoc!!.place]!!.getCards(cardLoc!!.location))) {
                        fieldSlots[num]!!.addCards(fieldSlots[cardLoc!!.place]!!.removeCards(cardLoc!!.location))
                        score += 3
                        moveCount++
                    }
                } catch (e: Exception) {
                    //e.printStackTrace()
                    Loged.w("${e.message}", showPretty = false)
                }
                cardLoc = null
            } else {
                try {
                    if (fieldSlots[num]!!.checkToAdd(drawList.last())) {
                        fieldSlots[num]!!.addCard(drawList.removeCardAt(drawList.size - 1))
                        try {
                            deck_of_cards.setImageResource(drawList.last().getImage(this@SolitaireActivity))
                        } catch (e2: NoSuchElementException) {
                            //deck_of_cards.setImageResource(Card(Suit.SPADES, 16).getImage(this@SolitaireActivity))
                            deck_of_cards.setImageResource(Card.BackCard.getImage(this@SolitaireActivity))
                        }
                        score += 5
                        moveCount++
                    }
                } catch (e1: Exception) {
                    //e1.printStackTrace()
                    Loged.e("${e1.message}", showPretty = false)
                }
                cardLoc = CardLocation(location, c, place)
            }

            refreshLayout()

        }
    })

    inner class OverlapDecoration(private var verticalOverlap: Int = -200) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
            val itemPosition = parent.getChildAdapterPosition(view)
            if (itemPosition == 0) {
                return
            }
            outRect.set(0, verticalOverlap, 0, 0)
        }
    }

    private fun fieldSetUp(num: Int, recycle: RecyclerView, cardView: ImageView) {
        val layoutManager = LinearLayoutManager(this)
        recycle.setHasFixedSize(true)
        val bitmap = BitmapFactory.decodeResource(resources, fieldSlots[num]!!.getImage())
        recycle.addItemDecoration(OverlapDecoration((-bitmap.height / 1.5).toInt()))
        recycle.layoutManager = layoutManager
        recycle.adapter = cardAdaptSetUp(num)
        cardView.setImageResource(fieldSlots[num]!!.getImage())
        cardView.setOnClickListener {
            try {
                if (fieldSlots[num]!!.checkToAdd(fieldSlots[cardLoc!!.place]!!.getCards(cardLoc!!.location))) {
                    fieldSlots[num]!!.addCards(fieldSlots[cardLoc!!.place]!!.removeCards(cardLoc!!.location))
                    score += 3
                    moveCount++
                }
                cardLoc = null
            } catch (e: Exception) {
                Loged.w("${e.message}", showPretty = false)
                try {
                    if (fieldSlots[num]!!.checkToAdd(drawList.last())) {
                        fieldSlots[num]!!.addCard(drawList.removeCardAt(drawList.size - 1))
                        try {
                            deck_of_cards.setImageResource(drawList.last().getImage(this))
                        } catch (e2: NoSuchElementException) {
                            deck_of_cards.setImageResource(Card(Suit.SPADES, 16).getImage(this))
                        }
                        score += 5
                        moveCount++
                    }
                } catch (e1: Exception) {
                    Log.e(Loged.TAG, e1.message!!)
                }
            }
            refreshLayout()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun refreshLayout() {
        for (i in fieldSlots.indices) {
            if (fieldSlots[i]!!.canFlipFaceDownCard())
                score += fieldSlots[i]!!.flipFaceDownCard()
            val view: ImageView = when (i) {
                0 -> slot1
                1 -> slot2
                2 -> slot3
                3 -> slot4
                4 -> slot5
                5 -> slot6
                6 -> slot7
                else -> deck_of_cards
            }

            view.setImageResource(fieldSlots[i]!!.getImage())

            val adapter: CardAdapter = cardAdaptSetUp(i)

            val recycle: RecyclerView = when (i) {
                0 -> slot1_cards
                1 -> slot2_cards
                2 -> slot3_cards
                3 -> slot4_cards
                4 -> slot5_cards
                5 -> slot6_cards
                6 -> slot7_cards
                else -> slot1_cards
            }
            recycle.adapter = adapter
        }

        try {
            deck_of_cards.setImageResource(drawList.last().getImage(this))
        } catch (e2: NoSuchElementException) {
            deck_of_cards.setImageResource(Card(Suit.SPADES, 16).getImage(this))
        }

        deck_info.text = "${deckOfCards.deckCount()} Cards Left"

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && !win)
            time.startTimer(solitaire_timer)
        else
            time.cancel()
    }

    override fun onBackPressed() {
        time.cancel()
        back_button_solitaire.performClick()
        startBubbles()
    }

    override fun onDestroy() {
        solitaire_layout.stopAllBubbles(false)
        time.cancel()
        super.onDestroy()
    }

    override fun onRestart() {
        solitaire_layout.stopAllBubbles(false)
        time.cancel()
        super.onRestart()
    }

    override fun onPause() {
        solitaire_layout.stopAllBubbles(false)
        time.cancel()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        time.startTimer(solitaire_timer)
    }

    interface AdapterPress {
        fun action(location: Int, c: Card, place: Int)
    }

}
