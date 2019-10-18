package com.crestron.aurora.server

import android.os.Bundle
import com.crestron.aurora.boardgames.musicGame.LyricApi
import com.crestron.aurora.boardgames.musicGame.TrackApi
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.db.ShowSource
import com.crestron.aurora.firebaseserver.getFirebase
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.utilities.getCollection
import com.crestron.aurora.utilities.putObject
import com.google.gson.Gson
import com.programmerbox.quizlibrary.*
import io.ktor.client.HttpClient
import io.ktor.client.request.*
import io.ktor.http.HttpMethod
import org.jetbrains.anko.defaultSharedPreferences

class ShowQuizActivity : QuizActivity() {
    val client = HttpClient()
    override val dialogHintText: String = "Choose a Source"
    override val dialogMessage: String = "Choose from Gogoanime, Putlocker, or Animetoon"
    override var dialogTitle: String = "Pick a Source"
    override val type: QuizChoiceType = QuizChoiceType.CHOICES
    override val titleText: String = "Show Quiz"
    override fun onCreated(savedInstanceState: Bundle?) {
        setChoices("Gogoanime", "Putlocker", "Animetoon")
    }

    override suspend fun getQuestions(chosen: String): Array<QuizQuestions> {
        val choice = "/show/quiz/show_type=$chosen.json"
        val s = client.get<String>(choice) {
            method = HttpMethod.Get
            host = hostAddress
            port = 8080
        }
        return Gson().fromJson(s, Array<QuizQuestions>::class.java)
    }
}

class MusicQuizActivity : QuizActivity() {
    val client = HttpClient()
    override val dialogHintText: String = "Artist Name"
    override val dialogMessage: String = "Choose What Songs Can Be Shown"
    override val postHighScoreLink: String? = "/music"
    override val highScoreLink: String? = "/music/mobileHighScores.json"
    override var dialogTitle: String = "Choose an Artist/Band"
    override val type: QuizChoiceType = QuizChoiceType.TEXT
    override val titleText: String = "Music Quiz"

    override fun onCreated(savedInstanceState: Bundle?) {
        showHighScore = true
    }

    override suspend fun getHighScore(): String {
        return client.get(highScoreLink!!) {
            method = HttpMethod.Get
            host = hostAddress
            port = 8080
        }
    }

    override suspend fun postHighScore(userInfo: UserInfo, questionList: Array<QuizQuestions>) {
        client.post<String>(postHighScoreLink!!) {
            method = HttpMethod.Post
            host = hostAddress
            port = 8080
            header("Content-type", "application/json")
            body = userInfo.toJson()
        }
    }

    override suspend fun getQuestions(chosen: String): Array<QuizQuestions> {
        val choice = "/music/music_get_quiz_from=$chosen.json"
        val s = client.get<String>(choice) {
            method = HttpMethod.Get
            host = hostAddress
            port = 8080
        }
        return Gson().fromJson(s, Array<QuizQuestions>::class.java)
    }
}

class NewMusicGameActivity : QuizActivity() {
    override val dialogTitle: String = "Music Game"
    override val dialogHintText: String = "Artist Name"
    override val dialogMessage: String = "Pick an Artist"
    override val titleText: String = "Music Quiz"
    override val type: QuizChoiceType = QuizChoiceType.TEXT

    override suspend fun getQuestions(chosen: String): Array<QuizQuestions> = quizMaker(TrackApi.getTrackByInfo(artistName = chosen), {
        LyricApi.getLyricSnippet(it).snippet_body
    }) {
        it.trackName
    }
}

class TestQuizActivity : QuizActivity() {
    override val dialogHintText: String = "Test Name"
    override val dialogMessage: String = "Test"
    override var dialogTitle: String = "Test"
    override val type: QuizChoiceType = QuizChoiceType.NONE
    override var titleText: String = "Test Quiz"

    override suspend fun getQuestions(chosen: String): Array<QuizQuestions> {
        return quizMaker(mutableListOf("asdf", "zxcv", "qwer", "jkl;"))
    }

    override suspend fun postHighScore(userInfo: UserInfo, questionList: Array<QuizQuestions>) {
        val currentInfo = defaultSharedPreferences.getCollection<List<UserInfo>>("testScore", emptyList())!!.toMutableList()
        currentInfo += userInfo
        defaultSharedPreferences.edit().putObject("testScore", currentInfo).apply()
    }

    override suspend fun getHighScore(): String = defaultSharedPreferences.getCollection<List<UserInfo>>("testScore", emptyList())!!.toString()

}

class QuizShowActivity : QuizActivity() {
    override val dialogHintText: String = "Shows"
    override val dialogMessage: String = "Pick a Show Source"
    override var dialogTitle: String = "Show Source"
    override val type: QuizChoiceType = QuizChoiceType.CHOICES
    override var titleText: String = "Show Quiz"
    override fun onCreated(savedInstanceState: Bundle?) {
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

        var fire = getFirebase().getAllShowsSync().filter { !it.name.isNullOrBlank() && !it.url.isNullOrBlank() }.map { ShowInfo(it.name!!, it.url!!) }
        fire = if (source != null) {
            fire.filter { it.url.contains(source.sourceName, true) }
        } else {
            fire
        }

        val showList = (if (source != null) {
            ShowDatabase.getDatabase(this).showDao().getShowsFromSource(source).shuffled()
        } else {
            ShowDatabase.getDatabase(this).showDao().allShows.shuffled()
        }.map { ShowInfo(it.name, it.link) } + fire).distinctBy { it.url }

        return quizMaker(showList, question = {
            EpisodeApi(it).description
        }, answers = {
            it.name
        })
    }
}
