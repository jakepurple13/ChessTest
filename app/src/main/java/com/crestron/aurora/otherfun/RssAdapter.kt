package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.crestron.aurora.R
import com.prof.rssparser.Article
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rss_layout_item.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.text.DateFormat
import java.text.SimpleDateFormat

class RssAdapter(var stuff: List<Article>, var context: Context) : RecyclerView.Adapter<ViewHolderRss>() {

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return stuff.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderRss {
        return ViewHolderRss(LayoutInflater.from(context).inflate(R.layout.rss_layout_item, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolderRss, position: Int) {
        //holder.cardType.text = "${items[position]}"
        holder.info.text = "${stuff[position].title}\n${SimpleDateFormat("MM/dd/yyyy hh:mm a").format(stuff[position].pubDate)}"
        launch(UI) {
            Picasso.get().load(stuff[position].image).resize((600 * .6).toInt(), (800 * .6).toInt()).into(holder.image)
        }
    }

}

class ViewHolderRss(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val info = view.item_feed!!
    val image = view.show_image!!

    init {
        setIsRecyclable(false)
    }
}