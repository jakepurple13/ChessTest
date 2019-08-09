package com.crestron.aurora

import androidx.annotation.IntRange
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Test


class TestUnitTwo {

    @Before
    fun setUp() {
        Loged.FILTER_BY_CLASS_NAME = "crestron"
    }

    /*data class Track(
            @SerializedName("track_id") val trackID: Long,
            @SerializedName("track_name") val trackName: String,
            @SerializedName("track_name_translation_list") val trackNameTranslationList: List<Any?>,
            @SerializedName("track_rating") val trackRating: Long,
            @SerializedName("common_track_id") val commontrackID: Long,
            @SerializedName("instrumental") val instrumental: Long,
            @SerializedName("explicit") val explicit: Long,
            @SerializedName("has_lyrics") val hasLyrics: Long,
            @SerializedName("has_subtitles") val hasSubtitles: Long,
            @SerializedName("has_rich_sync") val hasRichsync: Long,
            @SerializedName("num_favourite") val numFavourite: Long,
            @SerializedName("album_id") val albumID: Long,
            @SerializedName("album_name") val albumName: String,
            @SerializedName("artist_id") val artistID: Long,
            @SerializedName("artist_name") val artistName: String,
            @SerializedName("track_share_url") val trackShareURL: String,
            @SerializedName("track_edit_url") val trackEditURL: String,
            @SerializedName("restricted") val restricted: Long,
            @SerializedName("updated_time") val updatedTime: String,
            @SerializedName("primary_genres") val primaryGenres: PrimaryGenres
    )*/

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

    companion object API {

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

    @Test
    fun newApi() {
        //val t = TrackApi()
        //val track = t("Starlight Brigade", "TWRP")
        val track = TrackApi.getTrack("Starlight brigade", "twrp")
        prettyLog(track.albumName)
        val lyrics = LyricApi.getLyrics(track)
        prettyLog(lyrics.lyricsBody)
        val snippet = LyricApi.getLyricSnippet(track)
        prettyLog(snippet.snippet_body)
        val trackList = TrackApi.getTopTracks()
        //prettyLog(trackList.tracks.joinToString { "\n$it" })
        prettyLog(trackList.joinToString { "\n${it.trackName}" })
        //prettyLog(trackList[0].trackName)
        /*trackList.forEach {
            val newSnippet = LyricApi.getLyricSnippet(it)
            prettyLog("${it.trackName}: ${newSnippet.snippet_body}")
        }*/
        val trackLists = TrackApi.getTopTracks(ChartName.MXMWEEKLY, 10)
        prettyLog(trackLists.joinToString { "\n${it.trackName}" })
        val tracking = AlbumApi.getAlbum(track)
        prettyLog(tracking.joinToString { "\n${it.trackName}" })
        val trackings = ArtistApi.getArtistAlbums(trackLists.random())
        prettyLog(trackings.joinToString { "\n${it.albumName}" })
    }

    @Test
    fun apiTeststuff() {

    }

    private fun prettyLog(msg: Any) {
        //the main message to be logged
        var logged = msg.toString()
        //the arrow for the stack trace
        val arrow = "${9552.toChar()}${9655.toChar()}\t"
        //the stack trace
        val stackTraceElement = Thread.currentThread().stackTrace

        val elements = listOf(*stackTraceElement)
        val wanted = elements.filter { it.className.contains(Loged.FILTER_BY_CLASS_NAME) && !it.methodName.contains("prettyLog") }

        var loc = "\n"

        for (i in wanted.indices.reversed()) {
            val fullClassName = wanted[i].className
            //get the method name
            val methodName = wanted[i].methodName
            //get the file name
            val fileName = wanted[i].fileName
            //get the line number
            val lineNumber = wanted[i].lineNumber
            //add this to location in a format where we can click on the number in the console
            loc += "$fullClassName.$methodName($fileName:$lineNumber)"

            if (wanted.size > 1 && i - 1 >= 0) {
                val typeOfArrow: Char =
                        if (i - 1 > 0)
                            9568.toChar() //middle arrow
                        else
                            9562.toChar() //ending arrow
                loc += "\n\t$typeOfArrow$arrow"
            }
        }

        logged += loc

        println(logged + "\n")
    }

}