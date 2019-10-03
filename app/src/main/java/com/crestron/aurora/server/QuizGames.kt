package com.crestron.aurora.server

import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowInfo

class ShowQuizActivity : QuizActivity() {

    override val dialogHintText: String = "Choose a Source"
    override val dialogMessage: String = "Choose from Gogoanime, Putlocker, or Animetoon"

    override val postHighScoreLink: String? = null
    override val highScoreLink: String? = null

    override var dialogTitle: String = "Pick a Source"

    override fun onCreated() {
        titleText = "Show Quiz"
        type = QuizChoiceType.CHOICES
        setChoices("Gogoanime", "Putlocker", "Animetoon")
    }

    override fun getInfoLink(type: String): String = "/show/quiz/show_type=$type.json"

}

class MusicQuizActivity : QuizActivity() {

    override val dialogHintText: String = "Artist Name"
    override val dialogMessage: String = "Choose What Songs Can Be Shown"

    override val postHighScoreLink: String? = "/music"
    override val highScoreLink: String? = "/music/mobileHighScores.json"

    override var dialogTitle: String = "Choose an Artist/Band"

    override fun onCreated() {
        titleText = "Music Quiz"
    }

    override fun getInfoLink(type: String): String = "/music/music_get_quiz_from=$type.json"
}

class TestQuizActivity : QuizActivity() {

    override val dialogHintText: String = "Test Name"
    override val dialogMessage: String = "Test"
    override var dialogTitle: String = "Test"

    override fun onCreated() {
        titleText = "Test Quiz"
        type = QuizChoiceType.NONE
    }

    override fun getInfoLink(type: String): String = ""

    override suspend fun getQuestions(): Array<QuizQuestions> {

        var showList = ShowDatabase.getDatabase(this).showDao().allShows.shuffled()
        showList = if (showList.size >= 100) {
            showList.take(100)
        } else {
            showList
        }

        return quizMaker(showList.toMutableList(), {
            it.name
        }, {
            it.name
        })
    }
}

class QuizShowActivity : QuizActivity() {

    override val dialogHintText: String = "Test Name"
    override val dialogMessage: String = "Test"
    override var dialogTitle: String = "Test"

    override fun onCreated() {
        titleText = "Test Quiz"
        type = QuizChoiceType.NONE
    }

    override fun getInfoLink(type: String): String = ""

    override suspend fun getQuestions(): Array<QuizQuestions> {

        var showList = ShowDatabase.getDatabase(this).showDao().allShows.shuffled()
        showList = if (showList.size >= 100) {
            showList.take(100)
        } else {
            showList
        }

        return quizMaker(showList.toMutableList(), question = {
            EpisodeApi(ShowInfo(it.name, it.link)).description
        }, answers = {
            it.name
        })
    }
}