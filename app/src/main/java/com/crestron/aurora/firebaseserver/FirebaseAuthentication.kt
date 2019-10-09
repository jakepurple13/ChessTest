package com.crestron.aurora.firebaseserver

import android.content.Context
import com.crestron.aurora.Loged
import com.crestron.aurora.db.Episode
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.db.getAllShowsAndEpisodesAsync
import com.crestron.aurora.server.toJson
import com.crestron.aurora.showapi.ShowApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class FirebaseDB(val context: Context) {
    fun storeData() {
        GlobalScope.launch {
            FirebaseAuth.getInstance().currentUser?.let {
                val database = FirebaseDatabase.getInstance()
                val ref = database.getReference(it.uid)
                ref.setValue(context.getAllShowsAndEpisodesAsync().await())
                ref.addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        val value = p0.getValue(String::class.java)
                        Loged.d("Value is: $value")
                    }

                    override fun onCancelled(p0: DatabaseError) {
                        Loged.w("Failed to read value. ${p0.toException()}");
                    }
                })
            }
        }
    }

    fun getAndStore() {
        FirebaseAuth.getInstance().currentUser?.let {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference(it.uid)
            //ref.setValue()
            ref.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    GlobalScope.launch {
                        val value = p0.getValue(String::class.java)
                        Loged.d("Value is: $value")
                        val data = Gson().fromJson<MutableMap<String, MutableList<Episode>>>(value, object : TypeToken<MutableMap<String, MutableList<Episode>>>() {}.type)

                        val showAndEpisode = mutableMapOf<String, MutableList<Episode>>()
                        val db = ShowDatabase.getDatabase(context).showDao()
                        val shows = db.allShows
                        for (i in shows) {
                            showAndEpisode[i.link] = ShowDatabase.getDatabase(context).showDao().getEpisodesByUrl(i.link)
                        }

                        val both = mutableMapOf<String, MutableList<Episode>>()
                        both.putAll(data)
                        both.putAll(showAndEpisode)

                        val allShows = ShowApi.getAll()

                        for (i in both) {
                            val s = Show(i.key, allShows.find { it.url == i.key }?.name ?: "N/A")
                            db.insert(s)
                            for (e in i.value) {
                                db.insertEpisode(e)
                            }
                        }
                        ref.setValue(both.toJson())
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    Loged.w("Failed to read value. ${p0.toException()}");
                }
            })
        }
    }
}