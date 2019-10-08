package com.crestron.aurora.server

import android.os.Bundle
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.db.ShowSource
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowInfo
import com.programmerbox.quizlibrary.*

class ShowQuizActivity : QuizActivity() {
    override val dialogHintText: String = "Choose a Source"
    override val dialogMessage: String = "Choose from Gogoanime, Putlocker, or Animetoon"
    override var dialogTitle: String = "Pick a Source"
    override fun getInfoLink(type: String): String = "/show/quiz/show_type=$type.json"
    override fun onCreated(savedInstanceState: Bundle?) {
        titleText = "Show Quiz"
        type = QuizChoiceType.CHOICES
        setChoices("Gogoanime", "Putlocker", "Animetoon")
    }
}

class MusicQuizActivity : QuizActivity() {
    override val dialogHintText: String = "Artist Name"
    override val dialogMessage: String = "Choose What Songs Can Be Shown"
    override val postHighScoreLink: String? = "/music"
    override val highScoreLink: String? = "/music/mobileHighScores.json"
    override var dialogTitle: String = "Choose an Artist/Band"
    override fun getInfoLink(type: String): String = "/music/music_get_quiz_from=$type.json"
    override fun onCreated(savedInstanceState: Bundle?) {
        titleText = "Music Quiz"
        type = QuizChoiceType.TEXT
    }

    override suspend fun postHighScore(userInfo: UserInfo) {
        try {
            super.postHighScore(userInfo)
        } catch (e: Exception) {

        }
    }
}

class TestQuizActivity : QuizActivity() {
    override val dialogHintText: String = "Test Name"
    override val dialogMessage: String = "Test"
    override var dialogTitle: String = "Test"
    override fun getInfoLink(type: String): String = ""
    override fun onCreated(savedInstanceState: Bundle?) {
        titleText = "Test Quiz"
    }

    override suspend fun getQuestions(chosen: String): Array<QuizQuestions> {
        return quizMaker(mutableListOf("asdf", "zxcv", "qwer", "jkl;"))
    }
}

class QuizShowActivity : QuizActivity() {
    override val dialogHintText: String = "Shows"
    override val dialogMessage: String = "Pick a Show Source"
    override var dialogTitle: String = "Show Source"
    override fun getInfoLink(type: String): String = ""
    override fun onCreated(savedInstanceState: Bundle?) {
        titleText = "Show Quiz"
        type = QuizChoiceType.CHOICES
        setChoices("All", "Putlocker", "Gogoanime", "Animetoon")
    }

    override suspend fun getQuestions(chosen: String): Array<QuizQuestions> {
        val source = when (chosen) {
            "Putlocker" -> ShowSource.PUTLOCKER
            "Gogoanime" -> ShowSource.GOGOANIME
            "Animetoon" -> ShowSource.ANIMETOON
            "All" -> null
            else -> null
        }

        val showList = if (source != null) {
            ShowDatabase.getDatabase(this).showDao().getShowsFromSource(source).shuffled()
        } else {
            ShowDatabase.getDatabase(this).showDao().allShows.shuffled()
        }
        val shows = if (showList.size >= 100) {
            showList.take(100)
        } else {
            showList
        }.map { ShowInfo(it.name, it.link) }

        return quizMaker(shows.toMutableList(), question = {
            EpisodeApi(it).description
        }, answers = {
            it.name
        })
    }
}
