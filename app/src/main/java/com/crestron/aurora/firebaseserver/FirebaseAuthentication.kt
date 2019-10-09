package com.crestron.aurora.firebaseserver

import android.content.Context
import com.crestron.aurora.Loged
import com.crestron.aurora.db.Episode
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
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
import org.jetbrains.anko.defaultSharedPreferences


class FirebaseDB(val context: Context) {

    fun storeAllSettings() {
        FirebaseAuth.getInstance().currentUser?.let {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference(it.uid).child("/settings")
            Loged.r(context.defaultSharedPreferences.all)
            ref.setValue(context.defaultSharedPreferences.all.toJson())
        }
    }

    fun loadAllSettings() {
        FirebaseAuth.getInstance().currentUser?.let {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference(it.uid).child("/settings")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        val value = p0.getValue(String::class.java)
                        Loged.d("Value is: $value")
                        val loadedSettings = Gson().fromJson<Map<String, *>>(value, object : TypeToken<Map<String, *>>() {}.type)
                        val edit = context.defaultSharedPreferences.edit()
                        for (i in loadedSettings) {
                            when (i.value) {
                                is String -> edit.putString(i.key, i.value as String)
                                is Int -> edit.putInt(i.key, i.value as Int)
                                is Float -> edit.putFloat(i.key, i.value as Float)
                                is Long -> edit.putLong(i.key, i.value as Long)
                                is Boolean -> edit.putBoolean(i.key, i.value as Boolean)
                            }
                        }
                        edit.apply()
                    } catch(e: Exception) {
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    fun getAndStore() {
        FirebaseAuth.getInstance().currentUser?.let {
            val database = FirebaseDatabase.getInstance()
            val ref = database.getReference(it.uid).child("/shows")
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(p0: DataSnapshot) {
                    GlobalScope.launch {
                        val both = mutableMapOf<String, MutableList<Episode>>()
                        try {
                            if (p0.exists()) {
                                val value = p0.getValue(String::class.java)
                                Loged.d("Value is: $value")
                                val data = Gson().fromJson<MutableMap<String, MutableList<Episode>>>(value, object : TypeToken<MutableMap<String, MutableList<Episode>>>() {}.type)
                                both.putAll(data ?: emptyMap())
                            }
                        } catch (ignored: Exception) {
                        }
                        val showAndEpisode = mutableMapOf<String, MutableList<Episode>>()
                        val db = ShowDatabase.getDatabase(context).showDao()
                        val shows = db.allShows
                        for (i in shows) {
                            showAndEpisode[i.link] = ShowDatabase.getDatabase(context).showDao().getEpisodesByUrl(i.link)
                        }

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