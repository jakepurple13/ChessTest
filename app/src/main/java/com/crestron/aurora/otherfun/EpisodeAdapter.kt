package com.crestron.aurora.otherfun

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.Episode
import com.crestron.aurora.db.ShowDatabase
import kotlinx.android.synthetic.main.episode_info.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.runOnUiThread
import java.util.*

class EpisodeAdapter(private val items: ArrayList<String>, private val links: ArrayList<String>, private val name: String, val reverse: Boolean = false, val context: Context, private val action: EpisodeActivity.EpisodeAction = object : EpisodeActivity.EpisodeAction {}) : RecyclerView.Adapter<ViewHolderEpisode>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderEpisode {
        return ViewHolderEpisode(LayoutInflater.from(context).inflate(R.layout.episode_info, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolderEpisode, position: Int) {
        //holder.cardType.text = "${items[position]}"
        //holder.episodeName.text = items[position]
        //holder.episodeName.text = ""

        holder.watched.text = items[position]
        holder.episodeDownload.setOnClickListener {
            action.hit(items[position], links[position])
        }

        val show = ShowDatabase.getDatabase(this@EpisodeAdapter.context).showDao()

        launch {
            val episodes = show.getEpisodeFromShow(name)
            Loged.wtf("$episodes")

            holder.watched.isChecked = episodes.any { "${name
                    .replace("(", "\\(")
                    .replace(")", "\\)")
                    .replace("\"", "\\\"")
                    .replace(".", "\\.")} (.*) ${it.episodeNumber+1}".toRegex().matches(items[position]) || "$name (.*) ${it.episodeNumber+1} (.*)".toRegex().matches(items[position]) }

            /*for (i in episodes) {

                val check = if (reverse)
                    position
                else
                    items.size - position - 1

                if (check == i.episodeNumber) {
                    holder.watched.isChecked = true
                }

                Loged.i("$name is the name and ${items[position]} and the matching is ${"$name (.*) ${i.episodeNumber}".toRegex().matches(items[position])}")

                //this@EpisodeAdapter.context.runOnUiThread {
                //holder.watched.isChecked = "$name (.*) ${i.episodeNumber}".toRegex().matches(items[position])
                //}
            }*/
        }

        holder.watched.setOnCheckedChangeListener { _, b ->
            async {
                if (b) {
                    Loged.e("Inserted")
                    show.insertEpisode(Episode(position, name))
                } else {
                    Loged.e("Deleted")
                    show.deleteEpisode(position)
                }
            }
        }
    }
}

class ViewHolderEpisode(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val episodeDownload = view.download_episode!!
    //val episodeName = view.episode_name!!
    val watched = view.watched_button!!

    init {
        setIsRecyclable(false)
    }
}