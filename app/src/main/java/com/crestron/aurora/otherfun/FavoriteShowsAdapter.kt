package com.crestron.aurora.otherfun

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import androidx.recyclerview.widget.RecyclerView
import com.crestron.aurora.R
import kotlinx.android.synthetic.main.favorites_show_layout.view.*

class FavoriteShowsAdapter(private var stuff: List<ShowListActivity.NameAndLink>,
                           var context: Context,
                           var toBeChecked: List<FavoriteShowsActivity.NameUrl>,
                           var action: FavoriteShowsActivity.ShowHit = object : FavoriteShowsActivity.ShowHit {}) : RecyclerView.Adapter<ViewHolder>(), SectionIndexer {

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
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.favorites_show_layout, parent, false))
    }

    // Binds each animal in the ArrayList to a view
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.linkType.text = stuff[position].name

        holder.linkType.setOnClickListener {
            action.click(stuff[position].name, stuff[position].url, it)
        }

        action.longhit(stuff[position], holder.layout, holder.linkType)

        //Picasso.get().setIndicatorsEnabled(true)
        holder.imageView.visibility = View.GONE

        holder.layout.setOnClickListener {
            holder.linkType.performClick()
        }

        holder.favorite.setOnCheckedChangeListener(null)
        holder.favorite.isChecked = toBeChecked.any { it.url == stuff[position].url }
        holder.favorite.setOnCheckedChangeListener { _, isChecked ->
            action.longClick(stuff[position].name, stuff[position].url, isChecked)
        }

        if(stuff[position].url.contains("animeplus.tv", true)) {
            holder.itemView.alpha = .5f
        } else {
            holder.itemView.alpha = 1f
        }

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