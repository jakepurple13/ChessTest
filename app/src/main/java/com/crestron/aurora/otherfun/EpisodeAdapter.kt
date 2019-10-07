package com.crestron.aurora.otherfun

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.Episode
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.showapi.EpisodeInfo
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.episode_info.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.runOnUiThread
import java.util.*

//class EpisodeAdapter(private val items: ArrayList<String>, private val links: ArrayList<String>, private val name: String, val reverse: Boolean = false, val context: Context, val slideOrButton: Boolean, private val action: EpisodeActivity.EpisodeAction = object : EpisodeActivity.EpisodeAction {}) : RecyclerView.Adapter<ViewHolderEpisode>() {
class EpisodeAdapter(val items: ArrayList<EpisodeInfo>, private val name: String, val reverse: Boolean = false, val context: Context, val slideOrButton: Boolean, val downloadOrStream: Boolean, private val action: EpisodeActivity.EpisodeAction = object : EpisodeActivity.EpisodeAction {}) : RecyclerView.Adapter<ViewHolderEpisode>() {

    var casting = false

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return items.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderEpisode {
        return if (slideOrButton)
            ViewHolderEpisode(LayoutInflater.from(context).inflate(R.layout.episode_info, parent, false))
        else
            ViewHolderEpisode(LayoutInflater.from(context).inflate(R.layout.episode_info_button, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolderEpisode, position: Int) {
        //holder.cardType.text = "${items[position]}"
        //holder.episodeName.text = items[position]
        //holder.episodeName.text = ""

        holder.watched.text = items[position].name
        holder.episodeDownload.setOnClickListener {
            //action.hit(items[position].name, items[position].url)
            action.hit(items[position])
        }

        /*holder.slideToDownload.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
            override fun onSwipe_Forward(p0: Swipe_Button_View?) {
                //action.hit(items[position], links[position])
                //action.hit(items[position].name, items[position].url)
                action.hit(items[position])
            }

            override fun onSwipe_Reverse(p0: Swipe_Button_View?) {

            }
        })*/

        holder.slideToDownload.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
            override fun onSlideComplete(view: SlideToActView) {
                action.hit(items[position])

                GlobalScope.launch {
                    delay(500)
                    context.runOnUiThread {
                        view.resetSlider()
                    }
                }
            }
        }

        holder.episodeDownload.text = if(downloadOrStream) "Download" else "Stream"

        //holder.slideToDownload.setText(if(downloadOrStream) "Download" else "Stream")
        holder.slideToDownload.text = if(downloadOrStream) "Download" else "Stream"

        if(casting)
            holder.slideToDownload.text = "Cast"

        val show = ShowDatabase.getDatabase(this@EpisodeAdapter.context).showDao()

        GlobalScope.launch {
            val episodes = show.getEpisodes(name)//show.getEpisodeFromShow(name)
            //Loged.wtf("$episodes")

            /*holder.watched.isChecked = episodes.any {
                "${name
                        .replace("(", "\\(")
                        .replace(")", "\\)")
                        .replace("\"", "\\\"")
                        .replace(".", "\\.")} (.*) ${it.episodeNumber + 1}".toRegex().matches(items[position].name) || "$name (.*) ${it.episodeNumber + 1} (.*)".toRegex().matches(items[position].name)
            }*/

            holder.watched.setOnCheckedChangeListener(null)

            holder.watched.isChecked = items[position].url in episodes.map { it.showUrl }

            holder.watched.setOnCheckedChangeListener { _, b ->
                GlobalScope.launch {
                    try {
                        if (b) {
                            Loged.e("Inserted ${items[position]}")
                            show.insertEpisode(Episode(position, name, items[position].url))
                        } else {
                            Loged.e("Deleted")
                            show.deleteEpisode(position)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        this@EpisodeAdapter.context.runOnUiThread {
                            Toast.makeText(this@EpisodeAdapter.context, "Please Favorite Show if you plan on Checking the Episodes", Toast.LENGTH_LONG).show()
                        }
                        holder.watched.isChecked = false
                    }
                }
            }

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
    }
}

class ViewHolderEpisode(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val episodeDownload = view.download_episode!!
    val watched = view.watched_button!!
    val slideToDownload = view.okay_to_download!!

    init {
        setIsRecyclable(false)
    }
}