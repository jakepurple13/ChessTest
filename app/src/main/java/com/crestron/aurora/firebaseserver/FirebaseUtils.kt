package com.crestron.aurora.firebaseserver

import android.content.Context
import com.crestron.aurora.Loged
import com.crestron.aurora.db.Episode
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.utilities.KUtility
import com.crestron.aurora.utilities.fromJson
import com.crestron.aurora.utilities.toJson
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Source
import kotlinx.coroutines.*
import org.jetbrains.anko.defaultSharedPreferences


fun Context.getFirebase(persistence: Boolean = true) = FirebaseDB(this, persistence)

class FirebaseDB(private val context: Context, persistence: Boolean = true) {

    private val firebaseInstance: FirebaseFirestore = FirebaseFirestore.getInstance()

    init {
        firebaseInstance.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(persistence)
                .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                .build()
    }

    private fun <TResult> Task<TResult>.await(): TResult = Tasks.await(this)

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
                        val loadedSettings = value?.fromJson<Map<String, *>>()
                        val edit = context.defaultSharedPreferences.edit()
                        if (loadedSettings != null) {
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
                        }
                        edit.apply()
                    } catch (e: Exception) {
                    }
                    val newValue = context.defaultSharedPreferences.getLong("pref_duration", 3_600_000)
                    KUtility.currentDurationTime = newValue
                    KUtility.cancelAlarm(context)
                    KUtility.scheduleAlarm(context, newValue)
                    afterLoad()
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })
        }
    }

    companion object {
        fun firebaseSetup(persistence: Boolean = true) {
            //FirebaseFirestore.setLoggingEnabled(true)
            val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(persistence)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            FirebaseFirestore.getInstance().firestoreSettings = settings
        }

        suspend fun getAllShows(context: Context): List<ShowInfo> = GlobalScope.async {
            val shows = ShowDatabase.getDatabase(context).showDao().allShows
            val fireShow = try {
                Tasks.await(FirebaseFirestore.getInstance()
                        .collection(FirebaseAuth.getInstance().uid!!)
                        .get()).toObjects(FirebaseShow::class.java)
            } catch (e: Exception) {
                emptyList<FirebaseShow>()
            }
            return@async (shows.map { ShowInfo(it.name, it.link) } + fireShow.map {
                ShowInfo(it.name ?: "N/A", it.url ?: "N/A")
            }.toMutableList().filter { it.name != "N/A" }).distinctBy { it.url }
        }.await()

        suspend fun getAllFireShows(context: Context, source: Source = Source.DEFAULT): List<FirebaseShow> = GlobalScope.async {
            val shows = ShowDatabase.getDatabase(context).showDao().allShows
            val fireShow = try {
                Tasks.await(FirebaseFirestore.getInstance()
                        .collection(FirebaseAuth.getInstance().uid!!)
                        .get(source)).toObjects(FirebaseShow::class.java)
            } catch (e: Exception) {
                emptyList<FirebaseShow>()
            }
            return@async (shows.map { FirebaseShow(it.name, it.link, it.showNum) } + fireShow.toMutableList().filter { it.name != "N/A" }).distinctBy { it.url }
        }.await()

        suspend fun getShowSync(url: String, context: Context): FirebaseShow? = withContext(Dispatchers.Default) {
            val fire = try {
                Tasks.await(FirebaseFirestore.getInstance()
                        .collection(FirebaseAuth.getInstance().uid!!)
                        .document(url.replace("/", "<"))
                        .get()).toObject(FirebaseShow::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            val show = try {
                ShowDatabase.getDatabase(context).showDao().getEpisodes(url)
            } catch (e: Exception) {
                emptyList<Episode>()
            }
            if (fire != null) {
                FirebaseShow(fire.name,
                        fire.url,
                        fire.showNum,
                        fire.episodeInfo?.plus(show.map { FirebaseEpisode(it.showName, it.showUrl) })?.distinctBy { it.url } ?: emptyList())
            } else {
                null
            }
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
        val store = firebaseInstance
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
        val store = firebaseInstance
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
        val store = firebaseInstance
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
        val store = firebaseInstance
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

    fun getShowSync(url: String): FirebaseShow? = try {
        firebaseInstance
                .collection(FirebaseAuth.getInstance().uid!!)
                .document(url.replace("/", "<"))
                .get().await().toObject(FirebaseShow::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    fun getAllShowsSync(): List<FirebaseShow> = try {
        firebaseInstance
                .collection(FirebaseAuth.getInstance().uid!!)
                .get().await().toObjects(FirebaseShow::class.java)
    } catch (e: Exception) {
        emptyList()
    }

    private fun storeShow(showInfo: Pair<Show, MutableList<Episode>>) {
        val data2 = FirebaseShow(showInfo.first.name, showInfo.first.link, showInfo.first.showNum, showInfo.second.map { FirebaseEpisode(it.showName, it.showUrl) })

        val user = FirebaseAuth.getInstance()
        val store = firebaseInstance
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
        val store = firebaseInstance
                .collection(user.uid!!)
                .document(showInfo.url!!.replace("/", "<"))
                .update("showNum", showInfo.showNum)

        store.addOnSuccessListener {
            Loged.d("Success!")
        }.addOnFailureListener {
            Loged.wtf("Failure!")
        }
    }

}