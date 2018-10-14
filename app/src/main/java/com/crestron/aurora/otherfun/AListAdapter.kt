package com.crestron.aurora.otherfun

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
import com.like.LikeButton
import com.like.OnLikeListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.text_layout.view.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jsoup.Jsoup

class AListAdapter : RecyclerView.Adapter<ViewHolder>, SectionIndexer {

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
    lateinit var links: ArrayList<String>
    var context: Context
    var action: AniDownloadActivity.LinkAction
    lateinit var stuff: List<ShowListActivity.NameAndLink>

    constructor(stuff: List<ShowListActivity.NameAndLink>, context: Context, action: AniDownloadActivity.LinkAction = object : AniDownloadActivity.LinkAction {}) {
        this.stuff = stuff
        this.context = context
        this.action = action
    }

    constructor(items: ArrayList<String>, links: ArrayList<String>, context: Context, action: AniDownloadActivity.LinkAction = object : AniDownloadActivity.LinkAction {}) {
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
        //holder.cardType.text = "${items[position]}"
        if (items != null) {
            holder.linkType.text = items!![position]
            holder.linkType.setOnClickListener {
                Loged.wtf("I was pressed")
                action.hit(items!![position], links[position])
            }
        } else {
            //holder.linkType.visibility = View.GONE
            holder.linkType.text = stuff[position].name
            holder.linkType.setOnClickListener {
                Loged.wtf("I was pressed")
                action.hit(stuff[position].name, stuff[position].url)
            }

            holder.linkType.setOnLongClickListener {
                //holder.favorite.isChecked != holder.favorite.isChecked
                action.longhit(stuff[position], holder.linkType)
                true
            }

            action.longhit(stuff[position], holder.layout)

            //holder.favorite.text = ""//stuff[position].name
            /*holder.favorite.setOnClickListener {
                Loged.wtf("I was pressed")
                action.hit(stuff[position].name, stuff[position].url)
            }

            holder.favorite.setOnLongClickListener {
                holder.favorite.isChecked != holder.favorite.isChecked
                true
            }*/

            Picasso.get().setIndicatorsEnabled(true)
            holder.imageView.visibility = View.GONE

            /*async(UI) {
                Picasso.get().load(getShowIMG(stuff[position].url).await()).resize((600*.6).toInt(), (800*.6).toInt()).into(holder.imageView)
            }*/

            if(stuff[position].imgURL=="") {
                holder.imageView.visibility = View.GONE
                /*Picasso.get().setIndicatorsEnabled(true)
                async(UI) {
                    Picasso.get().load(getShowIMG(stuff[position].url).await()).resize((600*.6).toInt(), (800*.6).toInt()).into(holder.imageView)
                }*/
            } else {
                /*async(UI) {
                    Picasso.get().load(stuff[position].imgURL).resize((600*.6).toInt(), (800*.6).toInt()).into(holder.imageView)
                }*/
            }

            holder.layout.setOnClickListener {
                holder.linkType.performClick()
            }

            /*holder.layout.setOnLongClickListener {
                holder.layout.performClick()
                true
            }*/

            val show = ShowDatabase.getDatabase(context)

            launch {
                if (show.showDao().isUrlInDatabase(stuff[position].url) > 0) {
                    holder.favorite.isLiked = true
                }
            }

            /*holder.favorite.setOnCheckedChangeListener { _, b ->
                async {
                    if (b) {
                        show.showDao().insert(Show(stuff[position].url, stuff[position].name))

                        async {
                            val s = show.showDao().getShow(stuff[position].name)
                            val showList = getEpisodeList(stuff[position].url).await()
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
            }*/

            holder.favorite.setOnLikeListener(object : OnLikeListener {
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
                                val showList = getEpisodeList(stuff[position].url).await()
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

            })

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