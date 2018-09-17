package com.crestron.aurora

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import com.crestron.aurora.otherfun.ShowListActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.text_layout.view.*
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup

class SettingsShowAdapter(private var stuff: List<ShowListActivity.NameAndLink>,
                          var context: Context,
                          var action: SettingsShowActivity.ShowHit = object : SettingsShowActivity.ShowHit {}) : RecyclerView.Adapter<ViewHolder>(), SectionIndexer {

    private var mSectionPositions: ArrayList<Int>? = null

    override fun getSections(): Array<String> {
        val sections = ArrayList<String>(26)
        mSectionPositions = ArrayList(26)
        var i = 0
        val size = stuff.size
        while (i < size) {
            val section = stuff[i].name[0].toString().toUpperCase()
            if (!sections.contains(section)) {
                sections.add(section)
                mSectionPositions!!.add(i)
            }
            i++
        }
        return sections.toTypedArray()
    }

    override fun getSectionForPosition(position: Int): Int {
        return 0
    }

    override fun getPositionForSection(sectionIndex: Int): Int {
        return mSectionPositions!![sectionIndex]
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return stuff.size
    }

    /*fun getView(position: Int, convertView: View, parent: ViewGroup) {
        var view: ImageView? = convertView as ImageView
        if (view == null) {
            view = ImageView(context)
        }
        val url = items[position]

        Picasso.get().load(url).into(view)
    }*/

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.text_layout, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.linkType.text = stuff[position].name

        holder.linkType.setOnClickListener {
            action.click(stuff[position].name, stuff[position].url)
        }

        holder.linkType.setOnLongClickListener {
            holder.favorite.isChecked != holder.favorite.isChecked
            true
        }

        holder.favorite.text = ""//stuff[position].name

        Picasso.get().setIndicatorsEnabled(true)
        holder.imageView.visibility = View.GONE

        /*holder.layout.setOnClickListener {
            holder.favorite.performClick()
        }*/

        holder.favorite.isChecked = action.isChecked(stuff[position].name)

        holder.favorite.setOnCheckedChangeListener { _, b ->
            action.longClick(stuff[position].name, stuff[position].url, b)
        }
    }

    private fun getEpisodeList(url: String) = async {
        val doc1 = Jsoup.connect(url).get()
        val stuffList = doc1.allElements.select("div#videos").select("a[href^=http]")
        stuffList.size
    }

    private fun getShowIMG(url: String) = async {
        val doc1 = Jsoup.connect(url).get()
        doc1.select("div.left_col").select("img[src^=http]#series_image").attr("abs:src")
    }

}

class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val linkType = view.link_list!!
    val favorite = view.checkBox!!
    val layout = view.show_layout!!
    val imageView = view.show_img!!

    init {
        setIsRecyclable(false)
    }
}