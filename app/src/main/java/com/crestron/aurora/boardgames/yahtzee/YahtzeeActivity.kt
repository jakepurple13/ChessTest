package com.crestron.aurora.boardgames.yahtzee

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.AnimationUtility
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.utilities.nextColor
import com.crestron.aurora.views.BubbleEmitter
import com.crestron.aurora.views.createBubbles
import com.crestron.aurora.views.stopAllBubbles
import com.plattysoft.leonids.ParticleSystem
import hari.floatingtoast.FloatingToast
import kotlinx.android.synthetic.main.activity_yahtzee.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import kotlin.random.Random

class YahtzeeActivity : AppCompatActivity() {

    class DiceButton(val imageView: ImageView, dices: Dice) {
        var dice = dices
            set(value) {
                field = value
                //imageView.setImageResource(field.getImage())
                imageView.setImageLevel(field.num)
            }
        var hold = false
            set(value) {
                field = value
                imageView.background = if (value)
                    imageView.context.resources.getDrawable(R.drawable.image_border, null)
                else
                    null
            }

        init {
            imageView.setOnClickListener {
                hold = !hold
                it.background = if (hold)
                    it.context.resources.getDrawable(R.drawable.image_border, null)
                else
                    null
            }
        }

        fun setImage(num: Int) {
            imageView.setImageLevel(num)
        }

    }

    private fun getRandomNum(): Int {
        return Random.nextInt(6) + 1
    }

    private var roll = 0
    private var totalScore = 0
    private var smallScore = 0
    private var largeScore = 0
    private val scores = YahtzeeScores()
    private var yatScore = 0

    private var rollAmount = 3

    private fun getAllDice(): ArrayList<Dice> {
        return arrayListOf(firstDice.dice, secondDice.dice, thirdDice.dice, fourthDice.dice, fifthDice.dice)
    }

    private lateinit var firstDice: DiceButton
    private lateinit var secondDice: DiceButton
    private lateinit var thirdDice: DiceButton
    private lateinit var fourthDice: DiceButton
    private lateinit var fifthDice: DiceButton

    private lateinit var defaultButton: Drawable

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yahtzee)

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        firstDice = DiceButton(first_dice, Dice(1))
        secondDice = DiceButton(second_dice, Dice(1))
        thirdDice = DiceButton(third_dice, Dice(1))
        fourthDice = DiceButton(fourth_dice, Dice(1))
        fifthDice = DiceButton(fifth_dice, Dice(1))

        defaultButton = default_button_yahtzee.background
        //resources.getDrawable(R.drawable.default_button_please, theme)//roll_button.background.current

        fun checkWhatToDo(reset: Boolean = false) {

            fun buttonSet(view: Button, boolean: Boolean) {
                try {
                    //view.background.colorFilter = null
                } catch (e: Exception) {
                    Loged.e("${e.message}")
                }
                if (boolean && view.isEnabled) {
                    //view.backgroundDrawable = resources.getDrawable(R.drawable.rounded_corner, null)
                    view.background.setTint(resources.getColor(R.color.niceBlue, null))
                } else {
                    //view.background = defaultButton
                    view.background.setTint(resources.getColor(android.R.color.tertiary_text_dark, null))
                }
                view.clearFocus()
            }

            val allDice = getAllDice()

            buttonSet(three_kind_score, if (reset) false else scores.canGetThreeKind(allDice))
            buttonSet(four_kind_score, if (reset) false else scores.canGetFourKind(allDice))
            buttonSet(full_house_score, if (reset) false else scores.canGetFullHouse(allDice))
            buttonSet(small_straight_score, if (reset) false else scores.canGetSmallStraight(allDice))
            buttonSet(large_straight_score, if (reset) false else scores.canGetLargeStraight(allDice))
            buttonSet(yahtzee_score, if (reset) false else scores.canGetYahtzee(allDice))

            buttonSet(one_score, false)
            buttonSet(two_score, false)
            buttonSet(three_score, false)
            buttonSet(four_score, false)
            buttonSet(five_score, false)
            buttonSet(six_score, false)

            if (!reset) {

                val groupedCheck = allDice.groupingBy { it.num }.eachCount().toList().sortedWith(compareBy({ it.second }, { it.first })).reversed().toMap()

                var count = 0

                for (i in groupedCheck.keys.withIndex()) {
                    if (i.index < 5) {
                        val view = when (i.value) {
                            1 -> one_score
                            2 -> two_score
                            3 -> three_score
                            4 -> four_score
                            5 -> five_score
                            6 -> six_score
                            else -> roll_button
                        }
                        val level = when (count) {
                            0 -> resources.getColor(R.color.emeraldGreen, null)
                            1 -> resources.getColor(R.color.sunflower, null)
                            2 -> resources.getColor(R.color.alizarin, null)
                            else -> null
                        }.apply {
                            if (view.isEnabled)
                                count++
                        }
                        if (view.isEnabled && level != null) {
                            buttonSet(view, true)
                            view.background.setTint(level)
                        }
                    }
                }
            }
        }

        fun resetStuff() {
            firstDice.hold = false
            secondDice.hold = false
            thirdDice.hold = false
            fourthDice.hold = false
            fifthDice.hold = false

            firstDice.imageView.isEnabled = false
            secondDice.imageView.isEnabled = false
            thirdDice.imageView.isEnabled = false
            fourthDice.imageView.isEnabled = false
            fifthDice.imageView.isEnabled = false

            roll_button.isEnabled = true
            roll = 0
            //small_score.text = "Small Score: ${scores.smallTotal}"
            AnimationUtility.startCountAnimation(smallScore, scores.smallTotal, interpolator = OvershootInterpolator(), view = small_score, countListener = object : AnimationUtility.CountListener {
                override fun numberUpdate(change: Number) {
                    small_score.text = "Small Score: $change"
                }
            })
            smallScore = scores.smallTotal
            //large_score.text = "Large Score: ${scores.largeTotal}"
            AnimationUtility.startCountAnimation(largeScore, scores.largeTotal, interpolator = OvershootInterpolator(), view = large_score, countListener = object : AnimationUtility.CountListener {
                override fun numberUpdate(change: Number) {
                    large_score.text = "Large Score: $change"
                }
            })
            largeScore = scores.largeTotal
            FloatingToast.makeToast(this@YahtzeeActivity, "+${scores.total - totalScore}", FloatingToast.LENGTH_SHORT).show()
            AnimationUtility.startCountAnimation(totalScore, scores.total, interpolator = OvershootInterpolator(), view = total_score, countListener = object : AnimationUtility.CountListener {
                override fun numberUpdate(change: Number) {
                    total_score.text = "Total Score: $change"
                }

                override fun endAnimation() {
                    super.endAnimation()
                    //total_score.text = "Large Score: ${scores.total}"
                    val smallGood = !one_score.isEnabled && !two_score.isEnabled && !three_score.isEnabled &&
                            !four_score.isEnabled && !five_score.isEnabled && !six_score.isEnabled
                    val largeGood = !three_kind_score.isEnabled && !four_kind_score.isEnabled && !full_house_score.isEnabled &&
                            !small_straight_score.isEnabled && !large_straight_score.isEnabled && !chance_score.isEnabled &&
                            (!yahtzee_score.isEnabled || yatScore > 0)

                    if (smallGood && largeGood) {
                        Loged.wtf("All finished! $smallGood and $largeGood")
                        winDialog()
                        findViewById<ViewGroup>(android.R.id.content).setOnClickListener {
                            winDialog()
                        }
                    }
                }
            })
            totalScore = scores.total
            checkWhatToDo(true)
            roll_button.requestFocus()
        }

        total_score.setOnLongClickListener {
            roll = -100
            roll_button.isEnabled = true
            true
        }

        fun rollDice(diceButton: DiceButton) {
            if (!diceButton.hold) {
                GlobalScope.launch(Dispatchers.Main) {
                    for (i in 0..10) {
                        delay(50)
                        //diceButton.dice = Dice(getRandomNum())
                        diceButton.setImage(getRandomNum())
                    }
                    diceButton.dice = Dice(getRandomNum())
                    checkWhatToDo()
                }
            }
        }

        fun canAdd(): Boolean {
            return firstDice.imageView.isEnabled && secondDice.imageView.isEnabled &&
                    thirdDice.imageView.isEnabled && fourthDice.imageView.isEnabled &&
                    fifthDice.imageView.isEnabled
        }

        roll_button.setOnClickListener {

            firstDice.imageView.isEnabled = true
            secondDice.imageView.isEnabled = true
            thirdDice.imageView.isEnabled = true
            fourthDice.imageView.isEnabled = true
            fifthDice.imageView.isEnabled = true

            if (roll < rollAmount) {
                rollDice(firstDice)
                rollDice(secondDice)
                rollDice(thirdDice)
                rollDice(fourthDice)
                rollDice(fifthDice)
                roll++
            }
            if (roll >= rollAmount)
                roll_button.isEnabled = false

            roll_button.requestFocus()
        }

        roll_button.performClick()

        one_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getOnes(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        one_score.text = "Ones: $change"
                    }
                })
                one_score.isEnabled = false
                resetStuff()
            }
        }

        two_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getTwos(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        two_score.text = "Twos: $change"
                    }
                })
                two_score.isEnabled = false
                resetStuff()
            }
        }

        three_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getThrees(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        three_score.text = "Threes: $change"
                    }
                })
                three_score.isEnabled = false
                resetStuff()
            }
        }

        four_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getFours(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        four_score.text = "Fours: $change"
                    }
                })
                four_score.isEnabled = false
                resetStuff()
            }
        }

        five_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getFives(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        five_score.text = "Fives: $change"
                    }
                })
                five_score.isEnabled = false
                resetStuff()
            }
        }

        six_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getSixes(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        six_score.text = "Sixes: $change"
                    }
                })
                six_score.isEnabled = false
                resetStuff()
            }
        }

        three_kind_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getThreeOfAKind(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        three_kind_score.text = "Three of a Kind: $change"
                    }
                })
                it.isEnabled = false
                resetStuff()
            }
        }

        four_kind_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getFourOfAKind(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        four_kind_score.text = "Four of a Kind: $change"
                    }
                })
                it.isEnabled = false
                resetStuff()
            }
        }

        full_house_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getFullHouse(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        full_house_score.text = "Full House: $change"
                    }
                })
                it.isEnabled = false
                resetStuff()
            }
        }

        small_straight_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getSmallStraight(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        small_straight_score.text = "Small Straight: $change"
                    }
                })
                it.isEnabled = false
                resetStuff()
            }
        }

        large_straight_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getLargeStraight(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        large_straight_score.text = "Large Straight: $change"
                    }
                })
                it.isEnabled = false
                resetStuff()
            }
        }

        yahtzee_score.setOnClickListener {
            if (canAdd() && (scores.canGetYahtzee(getAllDice()) || yatScore == 0)) {
                val yat = scores.getYahtzee(getAllDice())
                AnimationUtility.startCountAnimation(yatScore, yatScore + yat, view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        yahtzee_score.text = "Yahtzee: $change"
                    }
                })
                yatScore += yat
                if (yat < 50) {
                    it.isEnabled = false
                } else {
                    val ps = ParticleSystem(this, 100, R.drawable.star_pink, 800)
                    ps.setScaleRange(0.7f, 1.3f)
                    ps.setSpeedRange(0.1f, 0.25f)
                    ps.setRotationSpeedRange(90f, 180f)
                    ps.setFadeOut(200, AccelerateInterpolator())
                    ps.oneShot(yahtzee_layout, 70)

                    val ps2 = ParticleSystem(this, 100, R.drawable.star_white, 800)
                    ps2.setScaleRange(0.7f, 1.3f)
                    ps2.setSpeedRange(0.1f, 0.25f)
                    ps.setRotationSpeedRange(90f, 180f)
                    ps2.setFadeOut(200, AccelerateInterpolator())
                    ps2.oneShot(yahtzee_layout, 70)

                    ps.stopEmitting()
                    ps2.stopEmitting()
                }
                resetStuff()
            }
        }

        chance_score.setOnClickListener {
            if (canAdd()) {
                AnimationUtility.startCountAnimation(0, scores.getChance(getAllDice()), view = it, countListener = object : AnimationUtility.CountListener {
                    override fun numberUpdate(change: Number) {
                        chance_score.text = "Chance: $change"
                    }
                })
                it.isEnabled = false
                resetStuff()
            }
        }

    }

    private fun winDialog() {

        startBubbles()

        val pref = defaultSharedPreferences
        val oldScore = pref.getInt("yahtzee_high_score", 0)
        var beat = false
        if (oldScore < totalScore) {
            val prefEdit = pref.edit()
            prefEdit.putInt("yahtzee_high_score", totalScore)
            prefEdit.apply()
            beat = true
        }

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Game Over!")
        builder.setMessage("Game Over!" +
                "\nWith a score of $totalScore!" +
                (if (beat) "\nYou beat your old high score which was $oldScore!" else "\nYour high score is $oldScore.") +
                "\nWant to play again?")
        // Add the buttons
        builder.setPositiveButton("Play Again") { _, _ ->
            yahtzee_layout.stopAllBubbles(false)
            finish()
            startActivity(intent)
        }
        builder.setNegativeButton("Nope") { _, _ ->
            yahtzee_layout.stopAllBubbles(false)
            finish()
        }
        val dialog = builder.create()
        dialog.show()
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure you want to quit?")
        builder.setMessage("You currently have a score of $totalScore")
        // Add the buttons
        builder.setPositiveButton("Quit") { _, _ ->
            super.onBackPressed()
        }
        builder.setNegativeButton("Nope") { _, _ ->
            yahtzee_layout.stopAllBubbles(true)
        }
        val dialog = builder.create()
        dialog.show()
        startBubbles()
    }

    private fun startBubbles() {
        yahtzee_layout.createBubbles {
            for(i in 0..10)
                fillColorsToUse+=Random.nextColor()
            touchEvent = BubbleEmitter.BUBBLE_POP
        }.startEmitting()
    }

    fun playAgain(v: View) {
        val pref = defaultSharedPreferences
        val oldScore = pref.getInt("yahtzee_high_score", 0)
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Are you sure you want to restart?")
        builder.setMessage("You currently have a score of $totalScore" +
                "\nYour current high score is $oldScore")
        // Add the buttons
        builder.setPositiveButton("Play Again") { _, _ ->
            finish()
            startActivity(intent)
        }
        builder.setNegativeButton("Nope") { _, _ ->

        }
        val dialog = builder.create()
        dialog.show()
    }

}
