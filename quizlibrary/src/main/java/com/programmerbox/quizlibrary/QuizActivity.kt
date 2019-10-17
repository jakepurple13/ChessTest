package com.programmerbox.quizlibrary

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kaopiz.kprogresshud.KProgressHUD
import kotlinx.android.synthetic.main.activity_quiz.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.random.Random

internal fun <T> MutableList<T>.randomRemove(): T = removeAt(Random.nextInt(0, size))

enum class QuizChoiceType {
    TEXT, CHOICES, NONE
}

@SuppressLint("StaticFieldLeak")
object Quiz {
    internal lateinit var context: Context

    /**
     * Use this if you want to change what the [QuizQuestions.freebie] Question shows
     */
    fun initializeQuiz(context: Context) {
        this.context = context
    }
}

/**
 * QuizQuestions
 * A nice an easy to understand format
 * @param question The question to ask
 * @param choices The 4 possible choices to choose from
 * @param correctAnswer the correct answer
 */
data class QuizQuestions(
        val question: String,
        val choices: List<String>,
        val correctAnswer: String
) {
    companion object {
        /**
         * A Freebie question that will show up if you don't have enough questions or something goes wrong in the question maker process
         */
        val freebie = try {
            QuizQuestions(Quiz.context.getString(R.string.freebie), listOf(Quiz.context.getString(R.string.thisOne), Quiz.context.getString(R.string.thisOne), Quiz.context.getString(R.string.thisOne), Quiz.context.getString(R.string.thisOne)), Quiz.context.getString(R.string.thisOne))
        } catch (e: Exception) {
            e.printStackTrace()
            QuizQuestions("Freebie", listOf("This One", "This One", "This One", "This One"), "This One")
        }
    }
}

/**
 * User info for posting the score
 */
data class UserInfo(val name: String, val quizChoice: String, val score: String)

/**
 * quizMaker
 * This creates a bunch of questions based off of a list of items
 * @param questionList the list you are creating a quiz off of
 * @param question Modifying the way a question is displayed
 * @param answers modifying the way the answers are displayed
 * @return an array of [QuizQuestions]
 */
fun <V> quizMaker(questionList: MutableList<V>, question: (V) -> String = { it.toString() }, answers: (V) -> String = { it.toString() }): Array<QuizQuestions> {
    val qList = mutableListOf<QuizQuestions>()
    if (questionList.size < 4) {
        qList += QuizQuestions.freebie
    }
    while (questionList.size >= 4) {
        qList += try {
            val answer = questionList.randomRemove()
            QuizQuestions(question(answer), listOf(
                    answers(answer),
                    answers(questionList.randomRemove()),
                    answers(questionList.randomRemove()),
                    answers(questionList.randomRemove())).shuffled(),
                    answers(answer))
        } catch (e: Exception) {
            QuizQuestions.freebie
        }
    }
    return qList.toTypedArray()
}

abstract class QuizActivity : AppCompatActivity() {

    companion object {
        var hostAddress = ""
        internal val resource = QuizActivity::getBaseContext
    }

    /**
     * The title of the dialog
     */
    abstract val dialogTitle: String
    /**
     * The hint text of the dialog if needed
     */
    abstract val dialogHintText: String
    /**
     * The dialog message to describe what the user is choosing
     */
    abstract val dialogMessage: String
    /**
     * override this if you have a link to post their score to
     */
    open val postHighScoreLink: String? = null
    /**
     * override this if there is a link to get high scores
     */
    open val highScoreLink: String? = null
    /**
     * if you want high scores to be entered
     */
    var showHighScore: Boolean = true
    /**
     * This describes what kind of quiz this is
     */
    var titleText: String = "Quiz"
        set(value) {
            field = value
            runOnUiThread {
                title_text.text = titleText
            }
        }
    /**
     * This allows you to let the user:
     * [QuizChoiceType.TEXT] - Type in their choice
     * [QuizChoiceType.CHOICES] - Give the user choices to choose from
     * [QuizChoiceType.NONE] - Do not give them any choices
     */
    var type = QuizChoiceType.NONE
    private var choices = mutableListOf<String>()

    /**
     * If you want to add anything into the [onCreate]
     */
    open fun onCreated(savedInstanceState: Bundle?) {}

    /**
     * When the user moves to the next question
     */
    open fun nextQuestionAction() {}

    /**
     * When the user moves to the previous question
     */
    open fun previousQuestionAction() {}

    /**
     * When we are doing our answer checking
     */
    open fun answerChecking() {}

    /**
     * override this if you want to customize where you are getting high scores from
     */
    open suspend fun getHighScore(): String = ""

    /**
     * override this if you want to customize where you are posting the score to
     */
    open suspend fun postHighScore(userInfo: UserInfo, questionList: Array<QuizQuestions>) {}

    /**
     * override this if you want to customize where/how you are getting questions
     */
    abstract suspend fun getQuestions(chosen: String): Array<QuizQuestions>

    /**
     * If you are doing choices, this allows you to set the choices
     */
    fun setChoices(vararg s: String) {
        choices.addAll(s)
    }

    private lateinit var quizQuestions: Array<QuizQuestions>
    private var counter = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            runOnUiThread {
                counterText.text = "${counter + 1}/${quizQuestions.size}"
            }
            if (counter + 1 == quizQuestions.size) {
                finished = true
            }
        }
    private val answerList = mutableMapOf<Int, Pair<Int, String>>()
    private var finished = false
        set(value) {
            field = value
            runOnUiThread {
                doneButton.visibility = if (field) View.VISIBLE else View.INVISIBLE
            }
        }
    private var quizChoice = ""

    private lateinit var hud: KProgressHUD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        onCreated(savedInstanceState)

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(getString(R.string.loading))
                .setDetailsLabel(getString(R.string.loadingQuestions))
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .setCancellable(false)

        answerSection.setOnCheckedChangeListener { _, _ ->
            answerSet(false)
        }

        getInfo()

        getHighScores()

        nextButton.setOnClickListener { nextQuestion() }
        prevButton.setOnClickListener { prevQuestion() }
        doneButton.setOnClickListener { answerCheck() }
    }

    private fun getHighScores() {
        GlobalScope.launch {
            val s = getHighScore()
            runOnUiThread {
                highScoreTable.text = s
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun getInfo() {
        resetQuestions()
        if (type == QuizChoiceType.NONE) {
            quizSetup()
        } else {
            val linearLayout = LinearLayout(this)
            linearLayout.orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val choiceInput: View

            when (type) {
                QuizChoiceType.TEXT -> {
                    choiceInput = EditText(this)
                    choiceInput.hint = dialogHintText
                    choiceInput.imeOptions = EditorInfo.IME_ACTION_NEXT
                }
                QuizChoiceType.CHOICES -> {
                    if (choices.isEmpty())
                        throw Exception("You don't have any choices!")
                    choiceInput = Spinner(this)
                    choiceInput.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, choices)
                }
                QuizChoiceType.NONE -> {
                    choiceInput = View(this)
                }
            }

            choiceInput.layoutParams = lp

            linearLayout.addView(choiceInput)

            val builder = MaterialAlertDialogBuilder(this)
            builder.setView(linearLayout)
            builder.setTitle(dialogTitle)
            builder.setMessage(dialogMessage)
            builder.setCancelable(false)
            // Add the buttons
            builder.setPositiveButton(getString(R.string.startQuiz)) { _, _ ->
                quizSetup(choiceInput)
            }
            builder.setNegativeButton(getString(R.string.stopQuiz)) { _, _ ->
                finish()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun quizSetup(choiceInput: View? = null) {
        hud.setLabel(getString(R.string.loading))
                .setDetailsLabel(getString(R.string.loadingQuestions))
                .show()
        GlobalScope.launch {
            val chosen = when (type) {
                QuizChoiceType.TEXT -> (choiceInput as EditText).text.toString()
                QuizChoiceType.CHOICES -> (choiceInput as Spinner).adapter.getItem(choiceInput.selectedItemPosition)!!.toString()
                QuizChoiceType.NONE -> ""
            }
            quizChoice = chosen
            quizQuestions = getQuestions(chosen)
            answerList.clear()
            runOnUiThread {
                finished = false
                counter = 0
                counterText.text = "${counter + 1}/${quizQuestions.size}"
                setQuestionUp()
                hud.dismiss()
                answerA.isEnabled = true
                answerB.isEnabled = true
                answerC.isEnabled = true
                answerD.isEnabled = true
            }
        }
    }

    private fun answerCheck() {
        answerChecking()
        val infoList = arrayListOf<String>()
        var count = 0
        for (q in quizQuestions.withIndex()) {
            if (q.value.correctAnswer == answerList[q.index]?.second) {
                count++
            }
            //"${q.index}) Your Pick: ${answerList[q.index]?.second} | Correct Answer: ${q.value.correctAnswer}"
            infoList += getString(R.string.answerString, q.index, answerList[q.index]?.second, q.value.correctAnswer)
        }

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val scoreView = ListView(this)
        scoreView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500)
        scoreView.adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                infoList
        )

        linearLayout.addView(scoreView)

        val userInput = EditText(this)

        if (showHighScore) {
            userInput.layoutParams = lp
            userInput.hint = getString(R.string.nameHighScore)
            userInput.imeOptions = EditorInfo.IME_ACTION_NEXT
            linearLayout.addView(userInput)
        }

        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(linearLayout)
        builder.setTitle(getString(R.string.highScoreTitle, "$count/${quizQuestions.size}"))
        builder.setCancelable(false)
        // Add the buttons
        if (showHighScore) {
            builder.setPositiveButton(getString(R.string.submit)) { _, _ ->
                hud.setLabel(getString(R.string.posting))
                hud.setDetailsLabel(getString(R.string.postingScore))
                hud.show()
                GlobalScope.launch {
                    postHighScore(UserInfo(
                            userInput.text.toString(),
                            quizChoice,
                            "$count/${quizQuestions.size}"
                    ), quizQuestions)
                    runOnUiThread {
                        hud.dismiss()
                        getInfo()
                    }
                    getHighScores()
                }
            }
        }
        builder.setNeutralButton(getString(R.string.stopQuiz)) { _, _ ->
            finish()
        }
        builder.setNegativeButton(getString(R.string.playAgain)) { _, _ ->
            getInfo()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun answerSet(clearCheck: Boolean = true) {
        try {
            answerList[counter] = Pair(
                    answerSection.checkedRadioButtonId,
                    findViewById<RadioButton>(answerSection.checkedRadioButtonId).text.toString()
                            .removePrefix("A) ")
                            .removePrefix("B) ")
                            .removePrefix("C) ")
                            .removePrefix("D) ")
            )
            if (clearCheck)
                answerSection.clearCheck()
        } catch (e: IllegalStateException) {

        }
    }

    private fun nextQuestion() {
        answerSet()
        if (counter + 1 <= quizQuestions.size - 1)
            counter += 1
        setQuestionUp()
        nextQuestionAction()
    }

    private fun prevQuestion() {
        answerSet()
        if (counter - 1 >= 0)
            counter -= 1
        setQuestionUp()
        previousQuestionAction()
    }

    @SuppressLint("SetTextI18n")
    private fun setQuestionUp() {
        answerList[counter]?.let {
            answerSection.check(it.first)
        }
        val question = quizQuestions[counter]
        questionText.text = question.question
        answerA.text = "A) ${question.choices[0]}"
        answerB.text = "B) ${question.choices[1]}"
        answerC.text = "C) ${question.choices[2]}"
        answerD.text = "D) ${question.choices[3]}"
    }

    @SuppressLint("SetTextI18n")
    private fun resetQuestions() {
        answerSection.clearCheck()
        questionText.text = "Hello"
        answerA.text = "A)"
        answerB.text = "B)"
        answerC.text = "C)"
        answerD.text = "D)"
        answerA.isEnabled = false
        answerB.isEnabled = false
        answerC.isEnabled = false
        answerD.isEnabled = false
    }

}
