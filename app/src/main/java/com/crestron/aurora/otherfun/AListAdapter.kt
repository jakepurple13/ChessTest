package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SectionIndexer
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowInfo
import com.like.LikeButton
import com.like.OnLikeListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.text_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class AListAdapter : RecyclerView.Adapter<ViewHolderShow>, SectionIndexer {

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

    private var items: ArrayList<String>? = null
    private lateinit var links: ArrayList<String>
    var context: Context
    var action: ShowListActivity.LinkAction
    lateinit var stuff: List<ShowInfo>
    lateinit var show: ShowDatabase
    var isRecent = false

    constructor(stuff: List<ShowInfo>, context: Context, showDatabase: ShowDatabase, action: ShowListActivity.LinkAction = object : ShowListActivity.LinkAction {}) {
        this.stuff = stuff
        this.context = context
        this.action = action
        this.show = showDatabase
    }

    constructor(stuff: List<ShowInfo>, context: Context, showDatabase: ShowDatabase, action: ShowListActivity.LinkAction = object : ShowListActivity.LinkAction {}, isRecent: Boolean) {
        this.stuff = stuff
        this.context = context
        this.action = action
        this.show = showDatabase
        this.isRecent = isRecent
    }

    constructor(items: ArrayList<String>, links: ArrayList<String>, context: Context, action: ShowListActivity.LinkAction = object : ShowListActivity.LinkAction {}) {
        this.items = items
        this.links = links
        this.context = context
        this.action = action
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return if (items != null) {
            items!!.size
        } else {
            stuff.size
        }
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderShow {
        return ViewHolderShow(LayoutInflater.from(context).inflate(R.layout.text_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolderShow, position: Int, payloads: MutableList<Any>) {
        //super.onBindViewHolder(holder, position, payloads)
        try {
            holder.favorite.isLiked = payloads[0] as Boolean
        } catch (e: IndexOutOfBoundsException) {

        }
        onBindViewHolder(holder, position)
    }

    // Binds each animal in the ArrayList to a view
    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolderShow, @SuppressLint("RecyclerView") position: Int) {
        if (items != null) {
            holder.linkType.text = items!![position]
            holder.linkType.setOnClickListener {
                Loged.wtf("I was pressed")
                action.hit(items!![position], links[position], it, holder.favorite)
            }
        } else {
            holder.linkType.text = stuff[position].name
            holder.linkType.setOnClickListener {
                Loged.wtf("I was pressed")
                action.hit(stuff[position].name, stuff[position].url, it, holder.favorite)
            }

            holder.layout.setOnClickListener {
                holder.linkType.performClick()
            }

            action.longhit(stuff[position], holder.layout, holder.linkType)

            Picasso.get().setIndicatorsEnabled(true)
            holder.imageView.visibility = View.GONE
            /*if(!isRecent) {
                //http://www.animeplus.tv/images/series/big/1.jpg
                holder.imageView.visibility = View.VISIBLE
                Picasso.get()
                        .load("http://www.animeplus.tv/images/series/big/${position+1}.jpg")
                        .resize((600 * .6).toInt(), (800 * .6).toInt())
                        .into(holder.imageView)
            }*/
            GlobalScope.launch {

                holder.favorite.apply {

                    setOnLikeListener(null)
                    isLiked = show.showDao().isUrlInDatabase(stuff[position].url) > 0
                    setOnLikeListener(object : OnLikeListener {
                        override fun liked(p0: LikeButton?) {
                            liked(p0!!.isLiked)
                        }

                        override fun unLiked(p0: LikeButton?) {
                            liked(p0!!.isLiked)
                        }

                        fun liked(like: Boolean) {
                            launch {
                                if (like) {
                                    show.showDao().insert(Show(stuff[position].url, stuff[position].name))
                                    /*launch {
                                        val s = show.showDao().getShow(stuff[position].name)
                                        val showList = getEpisodeList(stuff[position]).await()
                                        if (s.showNum < showList) {
                                            s.showNum = showList
                                            show.showDao().updateShow(s)
                                        }
                                        Loged.wtf("${s.name} and size is $showList")
                                    }*/

                                } else {
                                    show.showDao().deleteShow(stuff[position].name)
                                }
                            }
                        }

                    })
                }

                //holder.favorite.isLiked = show.showDao().isUrlInDatabase(stuff[position].url) > 0

                /*holder.favorite.setOnLikeListener(object : OnLikeListener {
                    override fun liked(p0: LikeButton?) {
                        liked(p0!!.isLiked)
                    }

                    override fun unLiked(p0: LikeButton?) {
                        liked(p0!!.isLiked)
                    }

                    fun liked(like: Boolean) {
                        launch {
                            if (like) {

                                show.showDao().insert(Show(stuff[position].url, stuff[position].name))

                                launch {
                                    val s = show.showDao().getShow(stuff[position].name)
                                    val showList = getEpisodeList(stuff[position]).await()
                                    if (s.showNum < showList) {
                                        s.showNum = showList
                                        show.showDao().updateShow(s)
                                    }
                                    Loged.wtf("${s.name} and size is $showList")
                                }

                            } else {
                                show.showDao().deleteShow(stuff[position].name)
                            }
                        }
                    }

                })*/
            }

        }
    }

    private fun getEpisodeList(show: ShowInfo) = GlobalScope.async {
        EpisodeApi(show).episodeList.size
    }

}

class ViewHolderShow(view: View) : RecyclerView.ViewHolder(view) {
    // Holds the TextView that will add each animal to
    val linkType = view.link_list!!
    val favorite = view.checkBox!!
    val layout = view.show_layout!!
    val imageView = view.show_img!!

    init {
        setIsRecyclable(false)
    }
}