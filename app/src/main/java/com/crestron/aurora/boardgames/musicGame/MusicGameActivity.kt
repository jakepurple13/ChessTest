package com.crestron.aurora.boardgames.musicGame

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Switch
import androidx.annotation.IntRange
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.isBlankOrEmpty
import com.crestron.aurora.views.BubbleEmitter
import com.crestron.aurora.views.createBubbles
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_music_game.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import kotlin.random.Random

class MusicGameActivity : AppCompatActivity() {

    enum class TrackChoice {
        NAME, ALBUM, ARTIST
    }

    private lateinit var trackList: MutableList<Track>

    private var choice: TrackChoice = TrackChoice.values().random()

    private var choices: Array<String> = arrayOf("", "", "", "")

    private lateinit var current: Track

    private var correctTrack: Int = 0

    private var currentRight = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            score.text = "$currentRight/$total\n${trackList.size} left"
        }
    private var total = 0
        @SuppressLint("SetTextI18n")
        set(value) {
            field = value
            score.text = "$currentRight/$total\n${trackList.size} left"
        }

    @SuppressLint("SetTextI18n")
    private fun <T> MutableList<T>.randomRemoveAndUpdate(): T {
        val s = randomRemove()
        runOnUiThread {
            score.text = "$currentRight/$total\n${trackList.size} left"
        }
        return s
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_music_game)

        getInfo()

        /*GlobalScope.launch {
            //trackList = TrackApi.getTopTracks(ChartName.HOT, 100).toMutableList()
            trackList = TrackApi.getTrackByInfo(artistName = "Ninja Sex Party").toMutableList()
            nextQuestion()
        }*/

        buttonA.setOnClickListener {
            onClick(0)
        }

        buttonB.setOnClickListener {
            onClick(1)
        }

        buttonC.setOnClickListener {
            onClick(2)
        }

        buttonD.setOnClickListener {
            onClick(3)
        }

        nextQuestionButton.setOnClickListener {
            GlobalScope.launch {
                nextQuestion()
            }
        }

        whatToGuess.text = "Q: ${when (choice) {
            TrackChoice.NAME -> "Song Snippet"
            else -> "Track Name"
        }
        }\nA: ${when (choice) {
            TrackChoice.NAME -> "Song Name"
            TrackChoice.ALBUM -> "Album Name"
            TrackChoice.ARTIST -> "Artist Name"
        }
        }"

    }

    private fun getInfo() {
        val linearLayout = LinearLayout(this@MusicGameActivity)
        linearLayout.orientation = LinearLayout.VERTICAL
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)

        val artistInput = EditText(this@MusicGameActivity)
        artistInput.layoutParams = lp
        artistInput.hint = "Artist Name"

        val lyricInput = EditText(this@MusicGameActivity)
        lyricInput.layoutParams = lp
        lyricInput.hint = "Lyrics"

        val chooseOrTop = Switch(this@MusicGameActivity)
        chooseOrTop.layoutParams = lp
        chooseOrTop.textOn = "Custom"
        chooseOrTop.textOff = "Songs From The Chart"
        chooseOrTop.showText = true
        chooseOrTop.splitTrack = true
        chooseOrTop.isChecked = true
        chooseOrTop.setOnCheckedChangeListener { _, b ->
            lyricInput.isEnabled = b
            artistInput.isEnabled = b
        }

        linearLayout.addView(chooseOrTop)
        linearLayout.addView(artistInput)
        linearLayout.addView(lyricInput)

        val builder = AlertDialog.Builder(this@MusicGameActivity)
        builder.setView(linearLayout)
        builder.setTitle("Customize the game")
        builder.setMessage("Choose What Songs Can Be Shown")
        builder.setCancelable(false)
        // Add the buttons
        builder.setPositiveButton("Okay!") { _, _ ->
            GlobalScope.launch {
                //trackList = TrackApi.getTopTracks(ChartName.HOT, 100).toMutableList()
                trackList = if (chooseOrTop.isChecked)
                    TrackApi.getTrackByInfo(artistName = artistInput.text.toString(), anyLyrics = lyricInput.text.toString()).toMutableList()
                else
                    TrackApi.getTopTracks(ChartName.values().random(), 100).toMutableList()
                nextQuestion()
            }
        }
        val dialog = builder.create()
        dialog.show()
    }

    private fun getButton(value: Int): Button? {
        return when (value) {
            0 -> buttonA
            1 -> buttonB
            2 -> buttonC
            3 -> buttonD
            else -> null
        }
    }

    @SuppressLint("SetTextI18n")
    private fun onClick(value: Int) {
        //runOnUiThread {
        if (getChoice(current) == choices[value]) {
            Loged.d("You did it!")
            getButton(value)!!.text = "${getButton(correctTrack)!!.text} ✅"
            currentRight++
            musicGameLayout.createBubbles {
                setColors(fill = Color.GREEN)
                touchEvent = BubbleEmitter.BUBBLE_POP
                oneBubble(10)
            }
        } else {
            Loged.e("You got it wrong")
            // else "❌"
            getButton(correctTrack)!!.text = "${getButton(correctTrack)!!.text} ✅"
        }
        nextQuestionButton.isEnabled = true
        //}

    }

    private fun getChoice(t: Track): String {
        return when (choice) {
            TrackChoice.NAME -> t.trackName
            TrackChoice.ALBUM -> t.albumName
            TrackChoice.ARTIST -> t.artistName
        }
    }

    @SuppressLint("SetTextI18n")
    fun nextQuestion() {
        if(trackList.size<=4) {
            runOnUiThread {
                getInfo()
            }
        } else {
            current = trackList.randomRemoveAndUpdate()

            val listOfTracks = listOf(current, trackList.randomRemoveAndUpdate(), trackList.randomRemoveAndUpdate(), trackList.randomRemoveAndUpdate()).shuffled()

            correctTrack = listOfTracks.indexOf(current)

            choices[0] = getChoice(listOfTracks[0])
            choices[1] = getChoice(listOfTracks[1])
            choices[2] = getChoice(listOfTracks[2])
            choices[3] = getChoice(listOfTracks[3])

            if (choice != TrackChoice.NAME) {
                runOnUiThread {
                    questionText.text = current.trackName
                }
            } else {
                GlobalScope.launch {
                    val snippet = LyricApi.getLyricSnippet(current)
                    runOnUiThread {
                        questionText.text = snippet.snippet_body
                    }
                }
            }

            setButtons()

            println(current.trackName)

            runOnUiThread {
                nextQuestionButton.isEnabled = false
                total++
            }
        }
    }

    @SuppressLint("SetTextI18n")
    fun setButtons() = runOnUiThread {
        buttonA.text = "(A) ${choices[0]}".trimStart()
        buttonB.text = "(B) ${choices[1]}".trimStart()
        buttonC.text = "(C) ${choices[2]}".trimStart()
        buttonD.text = "(D) ${choices[3]}".trimStart()
    }

}

data class Track(
        @SerializedName("updated_time")
        @Expose
        val updatedTime: String,
        @SerializedName("track_share_url")
        @Expose
        val trackShareUrl: String,
        @SerializedName("primary_genres")
        @Expose
        val primaryGenres: PrimaryGenres,
        @SerializedName("track_name_translation_list")
        @Expose
        val trackNameTranslationList: List<Any?>,
        @SerializedName("artist_name")
        @Expose
        val artistName: String,
        @SerializedName("commontrack_id")
        @Expose
        val commontrackId: Int,
        @SerializedName("artist_id")
        @Expose
        val artistId: Int,
        @SerializedName("explicit")
        @Expose
        val explicit: Int,
        @SerializedName("num_favourite")
        @Expose
        val numFavourite: Int,
        @SerializedName("track_rating")
        @Expose
        val trackRating: Int,
        @SerializedName("has_richsync")
        @Expose
        val hasRichsync: Int,
        @SerializedName("track_id")
        @Expose
        val trackId: Int,
        @SerializedName("instrumental")
        @Expose
        val instrumental: Int,
        @SerializedName("album_name")
        @Expose
        val albumName: String,
        @SerializedName("restricted")
        @Expose
        val restricted: Int,
        @SerializedName("has_subtitles")
        @Expose
        val hasSubtitles: Int,
        @SerializedName("album_id")
        @Expose
        val albumId: Int,
        @SerializedName("has_lyrics")
        @Expose
        val hasLyrics: Int,
        @SerializedName("track_edit_url")
        @Expose
        val trackEditUrl: String,
        @SerializedName("track_name")
        @Expose
        val trackName: String
)

data class PrimaryGenres(
        @SerializedName("music_genre_list") val musicGenreList: List<Any?>
)

data class SecondaryGenres(
        @SerializedName("music_genre_list")
        @Expose
        var musicGenreList: List<Any?>
)


data class Lyrics(
        @SerializedName("lyrics_id") val lyricsID: Long,
        //@SerializedName("restricted") val restricted: Long,
        //@SerializedName("instrumental") val instrumental: Long,
        @SerializedName("lyrics_body") val lyricsBody: String,
        @SerializedName("lyrics_language") val lyricsLanguage: String,
        @SerializedName("script_tracking_url") val scriptTrackingURL: String,
        @SerializedName("pixel_tracking_url") val pixelTrackingURL: String,
        @SerializedName("lyrics_copyright") val lyricsCopyright: String
        //@SerializedName("backlink_url") val backlinkURL: String,
        //@SerializedName("updated_time") val updatedTime: String
)

data class Album(

        @SerializedName("album_id")
        @Expose
        var albumId: Int,
        @SerializedName("album_mbid")
        @Expose
        var albumMbid: Any,
        @SerializedName("album_name")
        @Expose
        var albumName: String,
        @SerializedName("album_rating")
        @Expose
        var albumRating: Int,
        @SerializedName("album_track_count")
        @Expose
        var albumTrackCount: Int,
        @SerializedName("album_release_date")
        @Expose
        var albumReleaseDate: String,
        @SerializedName("album_release_type")
        @Expose
        var albumReleaseType: String,
        @SerializedName("artist_id")
        @Expose
        var artistId: Int,
        @SerializedName("artist_name")
        @Expose
        var artistName: String,
        @SerializedName("primary_genres")
        @Expose
        var primaryGenres: PrimaryGenres,
        @SerializedName("secondary_genres")
        @Expose
        var secondaryGenres: SecondaryGenres,
        @SerializedName("album_pline")
        @Expose
        var albumPline: String,
        @SerializedName("album_copyright")
        @Expose
        var albumCopyright: String,
        @SerializedName("album_label")
        @Expose
        var albumLabel: String,
        @SerializedName("updated_time")
        @Expose
        var updatedTime: String,
        @SerializedName("album_coverart_100x100")
        @Expose
        var albumCoverart100x100: String
)

data class Snippet(val snippet_language: String, val restricted: Number, val instrumental: Number, val snippet_body: String, val script_tracking_url: String, val pixel_tracking_url: String, val html_tracking_url: String, val updated_time: String)

data class TrackList2(
        @SerializedName("track")
        @Expose
        var track: Track
)

data class SnippetMessage(
        @SerializedName("track_list")
        @Expose
        var trackList: List<TrackList2>
)

data class AlbumList(
        @SerializedName("album")
        @Expose
        var album: Album
)

data class AlbumMessage(
        @SerializedName("album_list")
        @Expose
        var albumList: List<AlbumList>
)


fun getApiCalls(url: String, onError: (Response) -> Unit = {}, retrieve: (String) -> Unit) {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .build()
    val response = client.newCall(request).execute()
    if (response.code() == 200) {
        val resString = response.body()!!.string()
        retrieve(resString)
    } else {
        onError(response)
    }
}

inline fun <reified T> getApiCalls(url: String, onError: (Response) -> Unit = {}): T {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .build()
    val response = client.newCall(request).execute()
    if (response.code() == 200) {
        val resString = response.body()!!.string()
        val l = JSONObject(resString).getJSONObject("message").getJSONObject("body")
        val key = l.keys().next()
        val l2 = try {
            l.getJSONObject(key).toString()
        } catch (e: JSONException) {
            l.toString()//.getJSONArray(key).toString()
        }
        return Gson().fromJson(l2, T::class.java)
    } else {
        onError(response)
    }
    throw Exception("Nope")
}

inline fun <reified T> getApiListCalls(url: String, onError: (Response) -> Unit = {}): T {
    val client = OkHttpClient()
    val request = okhttp3.Request.Builder()
            .url(url)
            .get()
            .build()
    val response = client.newCall(request).execute()
    if (response.code() == 200) {
        val resString = response.body()!!.string()
        val l = JSONObject(resString).getJSONObject("message").getJSONObject("body")
        //val l2 = l.getJSONArray(l.keys().next()).toString()
        val l2 = l.toString()

        //val groupListType = object : TypeToken<LinkedTreeMap<String, T>>() {}.type
        //val d = Gson().fromJson<LinkedTreeMap<String, T>>(l2, groupListType)
        //println(d.values.toString())
        //return Gson().fromJson<HashMap<String, T>>(l2, groupListType).values
        return Gson().fromJson(l2, T::class.java)
    } else {
        onError(response)
    }
    throw Exception("Nope")
}

internal class GetAPI {
    companion object {
        inline fun <reified T> getInfo(url: String): T {
            return getApiCalls("https://api.musixmatch.com/ws/1.1/$url&apikey=67053f507ef88fc99c544f4d7052dfa8")
        }

        inline fun <reified T> getListInfo(url: String): T {
            return getApiListCalls("https://api.musixmatch.com/ws/1.1/$url&apikey=67053f507ef88fc99c544f4d7052dfa8")
        }
    }
}

enum class ChartName(val value: String) {
    TOP("top"), HOT("hot"), MXMWEEKLY("mxmweekly"), MXMWEEKLY_NEW("mxmweekly_new")
}

class TrackApi {

    companion object {
        fun getTrack(name: String, artist: String? = null): Track = GetAPI.getInfo("matcher.track.get?q_track=$name${if (artist != null) "&q_artist=$artist" else ""}")

        fun getTopTracks(chartName: ChartName = ChartName.TOP, @IntRange(from = 1, to = 100) amount: Int = 5): List<Track> {
            val s = GetAPI.getInfo<SnippetMessage>("chart.tracks.get?chart_name=${chartName.value}&page=1&page_size=$amount&f_has_lyrics=1").trackList
            val list = arrayListOf<Track>()
            s.forEach {
                list += it.track
            }
            return list
        }

        fun getTrackByInfo(trackName: String? = null, artistName: String? = null, anyLyrics: String? = null): List<Track> {
            val tName = if (trackName != null && !trackName.isBlankOrEmpty()) "q_track=$trackName" else ""
            val aName = if (artistName != null&& !artistName.isBlankOrEmpty()) "${if (tName.isBlankOrEmpty()) "" else "&"}q_artist=$artistName" else ""
            val lyric = if (anyLyrics != null && !anyLyrics.isBlankOrEmpty()) "${if (tName.isBlankOrEmpty() && aName.isBlankOrEmpty()) "" else "&"}q_lyrics=$anyLyrics" else ""
            val s = GetAPI.getInfo<SnippetMessage>("track.search?$tName$aName$lyric&f_lyrics_language=en&page_size=100&page=1&f_has_lyrics=1").trackList
            val list = arrayListOf<Track>()
            s.forEach {
                list += it.track
            }
            return list
        }
    }

}

class ArtistApi {
    companion object {
        fun getArtistAlbums(track: Track): List<Album> {
            val s = GetAPI.getInfo<AlbumMessage>("artist.albums.get?artist_id=${track.artistId}&s_release_date=desc&g_album_name=1").albumList
            val list = arrayListOf<Album>()
            s.forEach {
                list += it.album
            }
            return list
        }

        fun getArtistAlbums(id: Int): List<Album> {
            val s = GetAPI.getInfo<AlbumMessage>("artist.albums.get?artist_id=$id&s_release_date=desc&g_album_name=1").albumList
            val list = arrayListOf<Album>()
            s.forEach {
                list += it.album
            }
            return list
        }
    }
}

class AlbumApi {
    companion object {
        fun getAlbum(album: Album): List<Track> {
            val s = GetAPI.getInfo<SnippetMessage>("album.tracks.get?album_id=${album.albumId}&f_has_lyrics=1").trackList
            val list = arrayListOf<Track>()
            s.forEach {
                list += it.track
            }
            return list
        }

        fun getAlbum(id: Int): List<Track> {
            val s = GetAPI.getInfo<SnippetMessage>("album.tracks.get?album_id=$id&f_has_lyrics=1").trackList
            val list = arrayListOf<Track>()
            s.forEach {
                list += it.track
            }
            return list
        }

        fun getAlbum(track: Track): List<Track> {
            val s = GetAPI.getInfo<SnippetMessage>("album.tracks.get?album_id=${track.albumId}&f_has_lyrics=1").trackList
            val list = arrayListOf<Track>()
            s.forEach {
                list += it.track
            }
            return list
        }
    }
}

class LyricApi {

    companion object {
        fun getLyrics(track: Track): Lyrics = GetAPI.getInfo("track.lyrics.get?track_id=${track.trackId}")
        fun getLyricSnippet(track: Track): Snippet = GetAPI.getInfo("track.snippet.get?track_id=${track.trackId}")
    }

}

private fun <T> Collection<T>.prettyPrint(transform: (T) -> CharSequence) {
    Loged.i(joinToString(separator = "\n") { transform(it) })
}

private fun <T> MutableList<T>.randomRemove(): T {
    return removeAt(Random.nextInt(0, size))
}