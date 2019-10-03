package com.crestron.aurora.server


import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.crestron.aurora.R
import com.crestron.aurora.utilities.randomRemove
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.HttpMethod
import kotlinx.android.synthetic.main.activity_quiz.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

enum class QuizChoiceType {
    TEXT, CHOICES, NONE
}

data class QuizQuestions(
        val question: String,
        val choices: List<String>,
        val correctAnswer: String
)

data class UserInfo(val name: String, val artist: String, val score: String)

@Suppress("unused")
fun <T : QuizActivity, V> T.quizMaker(questionList: MutableList<V>, question: (V) -> String, answers: (V) -> String): Array<QuizQuestions> {
    val qList = mutableListOf<QuizQuestions>()
    while (questionList.size >= 4) {
        val answer = questionList.randomRemove()
        qList += QuizQuestions(question(answer), listOf(
                answers(answer),
                answers(questionList.randomRemove()),
                answers(questionList.randomRemove()),
                answers(questionList.randomRemove())).shuffled(),
                answers(answer))
    }
    return qList.toTypedArray()
}

abstract class QuizActivity : AppCompatActivity() {

    abstract val dialogTitle: String
    abstract val dialogHintText: String
    abstract val dialogMessage: String
    open val postHighScoreLink: String? = null
    open val highScoreLink: String? = null
    var titleText: String = "Quiz"
        set(value) {
            field = value
            runOnUiThread {
                title_text.text = titleText
            }
        }
    var type = QuizChoiceType.TEXT
    private var choices = mutableListOf<String>()

    abstract fun getInfoLink(type: String): String
    open fun onCreated() {}
    open fun nextQuestionAction() {}
    open fun previousQuestionAction() {}
    open fun answerChecking() {}

    private val client = HttpClient()
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
                doneButton.visibility = if (field) View.VISIBLE else View.GONE
            }
        }
    private var quizChoice = ""

    private lateinit var hud: KProgressHUD

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        onCreated()

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Loading")
                .setDetailsLabel("Loading Questions")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .setCancellable(false)

        getInfo()

        getHighScores()

        nextButton.setOnClickListener { nextQuestion() }
        prevButton.setOnClickListener { prevQuestion() }
        doneButton.setOnClickListener { answerCheck() }
    }

    private fun getHighScores() {
        if (!highScoreLink.isNullOrBlank()) {
            GlobalScope.launch {
                val s = getHighScore()
                runOnUiThread {
                    highScoreTable.text = s
                }
            }
        }
    }

    open suspend fun getHighScore(): String {
        return client.get(highScoreLink!!) {
            method = HttpMethod.Get
            host = ClientHandler.host
            port = 8080
        }
    }

    open suspend fun postHighScore(userInfo: UserInfo) {
        client.post<String>(postHighScoreLink!!) {
            method = HttpMethod.Post
            host = ClientHandler.host
            port = 8080
            header("Content-type", "application/json")
            body = userInfo.toJson()
        }
    }

    open suspend fun getQuestions(): Array<QuizQuestions> {
        val choice = getInfoLink(quizChoice)
        val s = client.get<String>(choice) {
            method = HttpMethod.Get
            host = ClientHandler.host
            port = 8080
        }
        return Gson().fromJson(s, Array<QuizQuestions>::class.java)
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
            builder.setPositiveButton("Okay!") { _, _ ->
                quizSetup(choiceInput)
            }
            builder.setNegativeButton("Never Mind") { _, _ ->
                finish()
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun quizSetup(choiceInput: View? = null) {
        hud.setLabel("Loading")
        hud.setDetailsLabel("Loading Questions")
        hud.show()
        GlobalScope.launch {
            val chosen = when (type) {
                QuizChoiceType.TEXT -> (choiceInput as EditText).text.toString()
                QuizChoiceType.CHOICES -> (choiceInput as Spinner).adapter.getItem(choiceInput.selectedItemPosition)!!.toString()
                QuizChoiceType.NONE -> ""
            }
            quizChoice = chosen
            quizQuestions = getQuestions()
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

    fun setChoices(vararg s: String) {
        choices.addAll(s)
    }

    private fun answerCheck() {
        answerChecking()
        val infoList = arrayListOf<String>()
        var count = 0
        for (q in quizQuestions.withIndex()) {
            if (q.value.correctAnswer == answerList[q.index]?.second) {
                count++
            }
            infoList += "${q.index}) Your Pick: ${answerList[q.index]?.second} | Correct Answer: ${q.value.correctAnswer}"
        }

        val linearLayout = LinearLayout(this)
        linearLayout.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        )

        val scoreView = ListView(this)
        scoreView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                300
        )
        scoreView.adapter = ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                infoList
        )

        linearLayout.addView(scoreView)

        val userInput = EditText(this)

        if (!postHighScoreLink.isNullOrBlank()) {
            userInput.layoutParams = lp
            userInput.hint = "Your Name (for the high score list)"
            userInput.imeOptions = EditorInfo.IME_ACTION_NEXT
            linearLayout.addView(userInput)
        }

        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(linearLayout)
        builder.setTitle("Score")
        builder.setMessage("You got $count/${quizQuestions.size}")
        builder.setCancelable(false)
        // Add the buttons
        if (!postHighScoreLink.isNullOrBlank()) {
            builder.setPositiveButton("Submit Score") { _, _ ->
                hud.setLabel("Posting")
                hud.setDetailsLabel("Posting Score")
                hud.show()
                GlobalScope.launch {
                    postHighScore(UserInfo(
                            userInput.text.toString(),
                            quizChoice,
                            "$count/${quizQuestions.size}"
                    ))
                    runOnUiThread {
                        hud.dismiss()
                    }
                    getHighScores()
                }
                getInfo()
            }
        }
        builder.setNeutralButton("Stop Playing") { _, _ ->
            finish()
        }
        builder.setNegativeButton("Play Again!") { _, _ ->
            getInfo()
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun answerSet() {
        try {
            answerList[counter] = Pair(
                    answerSection.checkedRadioButtonId,
                    findViewById<RadioButton>(answerSection.checkedRadioButtonId).text.toString()
                            .removePrefix("A) ")
                            .removePrefix("B) ")
                            .removePrefix("C) ")
                            .removePrefix("D) ")
            )
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
