package com.crestron.aurora.firebaseserver

import android.content.Context
import com.crestron.aurora.Loged
import com.crestron.aurora.db.Episode
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.server.toJson
import com.crestron.aurora.showapi.ShowApi
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
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

    fun loadAllSettings(afterLoad: () -> Unit = {}) {
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
                                else -> null
                            }?.apply()
                        }
                        edit.apply()
                    } catch (e: Exception) {
                    }
                    afterLoad()
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    companion object {
        fun firebaseSetup() {
            val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        }
    }

    fun storeDb() {
        GlobalScope.launch {
            val showAndEpisode = mutableMapOf<Show, MutableList<Episode>>()
            val db = ShowDatabase.getDatabase(context).showDao()
            val shows = db.allShows
            for (i in shows) {
                showAndEpisode[i] = ShowDatabase.getDatabase(context).showDao().getEpisodesByUrl(i.link)
            }
            for (i in showAndEpisode) {
                storeShow(Pair(i.key, i.value))
            }
        }
    }

    data class FirebaseEpisode(
            val name: String? = null,
            val url: String? = null
    )

    data class FirebaseShow(
            val name: String? = null,
            val url: String? = null,
            var showNum: Int = 0,
            val episodeInfo: List<FirebaseEpisode>? = null
    )

    fun addShow(show: Show) {
        val data2 = FirebaseShow(show.name, show.link)

        val user = FirebaseAuth.getInstance()
        val store = FirebaseFirestore.getInstance()
                .collection(user.uid!!)
                .document(show.link.replace("/", "<"))
                .set(data2)

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }.addOnCompleteListener {
            Loged.d("All done!")
        }
    }

    fun removeShow(show: Show) {
        val user = FirebaseAuth.getInstance()
        val store = FirebaseFirestore.getInstance()
                .collection(user.uid!!)
                .document(show.link.replace("/", "<"))
                .delete()

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }.addOnCompleteListener {
            Loged.d("All done!")
        }
    }

    fun addEpisode(url: String, episode: Episode) {
        val user = FirebaseAuth.getInstance()
        val store = FirebaseFirestore.getInstance()
                .collection(user.uid!!)
                .document(url.replace("/", "<"))
                .update("episodeInfo", FieldValue.arrayUnion(FirebaseEpisode(episode.showName, episode.showUrl)))


        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }.addOnCompleteListener {
            Loged.d("All done!")
        }
    }

    fun removeEpisode(url: String, episode: Episode) {
        val user = FirebaseAuth.getInstance()
        val store = FirebaseFirestore.getInstance()
                .collection(user.uid!!)
                .document(url.replace("/", "<"))
                .update("episodeInfo", FieldValue.arrayRemove(FirebaseEpisode(episode.showName, episode.showUrl)))

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }.addOnCompleteListener {
            Loged.d("All done!")
        }
    }

    fun getShow(url: String, updateUI: (FirebaseShow?) -> Unit = {}) {
        val user = FirebaseAuth.getInstance()

        FirebaseFirestore.getInstance()
                .collection(user.uid!!)
                .document(url.replace("/", "<"))
                .get()
                .addOnSuccessListener { document ->
                    Loged.d("Success!")
                    updateUI(try {
                        if (document.exists()) {
                            document.toObject(FirebaseShow::class.java)
                        } else {
                            null
                        }
                    } catch (e: Exception) {
                        null
                    })
                }
                .addOnFailureListener { exception ->
                    Loged.d("get failed with $exception")
                }
    }

    fun getShowSync(url: String): FirebaseShow? {
        val user = FirebaseAuth.getInstance()
        return try {
            Tasks.await(FirebaseFirestore.getInstance()
                    .collection(user.uid!!)
                    .document(url.replace("/", "<"))
                    .get()).toObject(FirebaseShow::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getAllShowsSync(): List<FirebaseShow> {
        return try {
            Tasks.await(FirebaseFirestore.getInstance()
                    .collection(FirebaseAuth.getInstance().uid!!)
                    .get()).toObjects(FirebaseShow::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun storeShow(showInfo: Pair<Show, MutableList<Episode>>) {
        val data2 = FirebaseShow(showInfo.first.name, showInfo.first.link, showInfo.first.showNum, showInfo.second.map { FirebaseEpisode(it.showName, it.showUrl) })

        val user = FirebaseAuth.getInstance()
        val store = FirebaseFirestore.getInstance()
                .collection(user.uid!!)
                .document(showInfo.first.link.replace("/", "<"))
                .set(data2)

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }
    }

    fun updateShowNum(showInfo: FirebaseShow) {
        val user = FirebaseAuth.getInstance()
        val store = FirebaseFirestore.getInstance()
                .collection(user.uid!!)
                .document(showInfo.url!!.replace("/", "<"))
                .update("showNum", showInfo.showNum)

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }
    }

    fun getAllShows(updateUI: (List<FirebaseShow>) -> Unit = {}) {
        val user = FirebaseAuth.getInstance()

        FirebaseFirestore.getInstance()
                .collection(user.uid!!)
                .get()
                .addOnSuccessListener { document ->
                    Loged.d("Success!")
                    updateUI(try {
                        if (!document.isEmpty) document.toObjects(FirebaseShow::class.java) else emptyList()
                    } catch (e: Exception) {
                        emptyList<FirebaseShow>()
                    })
                }
                .addOnFailureListener { exception ->
                    Loged.d("get failed with $exception")
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