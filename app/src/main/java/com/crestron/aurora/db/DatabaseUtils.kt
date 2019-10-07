package com.crestron.aurora.db

import android.content.Context
import com.crestron.aurora.server.toJson
import com.crestron.aurora.showapi.ShowApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

fun Context.getAllShowsAndEpisodesAsync() = GlobalScope.async {
    val showAndEpisode = mutableMapOf<String, MutableList<Episode>>()
    val shows = ShowDatabase.getDatabase(this@getAllShowsAndEpisodesAsync).showDao().allShows
    for (i in shows) {
        showAndEpisode[i.link] = ShowDatabase.getDatabase(this@getAllShowsAndEpisodesAsync).showDao().getEpisodesByUrl(i.link)
    }
    val s = showAndEpisode.toJson()
    s
}

fun Context.importAllShowsAndEpisodes(jsonString: String) = GlobalScope.launch {
    val database = ShowDatabase.getDatabase(this@importAllShowsAndEpisodes).showDao()
    try {
        val g = Gson().fromJson<MutableMap<String,
                MutableList<Episode>>>(jsonString, object : TypeToken<MutableMap<String, MutableList<Episode>>>() {}.type)
        val shows = ShowApi.getAll()
        for (i in g) {
            val s = Show(i.key, shows.find { it.url == i.key }?.name ?: "N/A")
            database.insert(s)
            for (e in i.value) {
                database.insertEpisode(e)
            }
        }
    } catch(e: Exception) {
        val g = Gson().fromJson(jsonString, Array<Show>::class.java)
        for (i in g) {
            if (database.isInDatabase(i.name) <= 0) {
                database.insert(i)
            }
        }
    }
}