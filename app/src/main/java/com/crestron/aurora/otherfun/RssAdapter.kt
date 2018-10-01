package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.crestron.aurora.R
import com.crestron.aurora.views.StickHeaderItemDecoration
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.rss_layout_item.view.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.runOnUiThread


class RssAdapter(var stuff: List<RssActivity.MainInfo>, var context: Context) : RecyclerView.Adapter<RssAdapter.BaseHolder>(), StickHeaderItemDecoration.StickyHeaderInterface {
    override fun getHeaderPositionForItem(itemPosition: Int): Int {
        var itemP = itemPosition
        var headerPosition = 0
        do {
            if (this.isHeader(itemP)) {
                headerPosition = itemP
                break
            }
            itemP -= 1
        } while (itemP >= 0)
        return headerPosition

    }

    override fun getHeaderLayout(headerPosition: Int): Int {
        return if (stuff[headerPosition] is RssActivity.HeaderInfo)
            R.layout.header_layout
        else {
            R.layout.rss_layout_item
        }
    }

    override fun bindHeaderData(header: View?, headerPosition: Int) {
        //Loged.i("$headerPosition")
        (header!! as TextView).text = stuff[headerPosition].info
    }

    override fun isHeader(itemPosition: Int): Boolean {
        return stuff[itemPosition] is RssActivity.HeaderInfo
    }

    // Gets the number of animals in the list
    override fun getItemCount(): Int {
        return stuff.size
    }

    // Inflates the item views
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        return when (viewType) {
            1 -> HeaderHolder(LayoutInflater.from(context).inflate(R.layout.header_layout, parent, false))
            2 -> ViewHolderRss(LayoutInflater.from(context).inflate(R.layout.rss_layout_item, parent, false))
            else -> ViewHolderRss(LayoutInflater.from(context).inflate(R.layout.rss_layout_item, parent, false))
        }
        //return ViewHolderRss(LayoutInflater.from(context).inflate(R.layout.rss_layout_item, parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        return if (stuff[position] is RssActivity.HeaderInfo) {
            1
        } else 2
    }

    // Binds each animal in the ArrayList to a view
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        //holder.cardType.text = "${items[position]}"
        //holder.info.text = "${stuff[position].title}\n${SimpleDateFormat("MM/dd/yyyy hh:mm a").format(stuff[position].pubDate)}"

        //val readMoreOption = ReadMoreOption.Builder(this.context).build()

        if (stuff[position] is RssActivity.MALInfo) {
            holder as ViewHolderRss
            val information = stuff[position] as RssActivity.MALInfo

            holder.info.text = information.title
            holder.des.text = information.description
            holder.timeInfo.text = information.time
            launch(UI) {
                //try {
                //Picasso.get().load(information.imageLink).resize((600 * .6).toInt(), (800 * .6).toInt()).into(holder.image)
                //} catch (e: IllegalArgumentException) {
                //}
            }

            context.runOnUiThread {
                try {
                    Picasso.get().load(information.imageLink).resize((600 * .6).toInt(), (800 * .6).toInt()).into(holder.image)
                } catch (ignored: IllegalArgumentException) {
                }
            }

        } else if (stuff[position] is RssActivity.HeaderInfo) {
            holder as HeaderHolder
            val information = stuff[position] as RssActivity.HeaderInfo
            holder.header.text = information.title
        }

        /*else if(stuff[position] is RssActivity.HeaderInfo) {
            val information = stuff[position] as RssActivity.HeaderInfo
            holder.info.text = information.title
            holder.info.textSize *= 2
            holder.info.gravity += Gravity.CENTER_HORIZONTAL
            holder.des.visibility = View.GONE
            holder.timeInfo.visibility = View.GONE
            holder.image.visibility = View.GONE
            holder.progressBar1.visibility = View.GONE
            holder.progressBar2.visibility = View.GONE
        }*/

        /*val readMoreOption = ReadMoreOption.Builder(this.context)
                .textLength(3, ReadMoreOption.TYPE_LINE) // OR
                //.textLength(300, ReadMoreOption.TYPE_CHARACTER)
                .moreLabel("MORE")
                .lessLabel("LESS")
                .labelUnderLine(true)
                .build()

        readMoreOption.addReadMoreTo(holder.des, "${holder.des.text}")*/

    }

    open class BaseHolder(view: View) : RecyclerView.ViewHolder(view)

    class ViewHolderRss(view: View) : BaseHolder(view) {
        // Holds the TextView that will add each animal to
        val info = view.item_feed!!
        val image = view.show_image!!
        val des = view.description!!
        val timeInfo = view.time_info!!
        val progressBar1 = view.progressBar3!!
        val progressBar2 = view.progressBar4!!

        init {
            setIsRecyclable(false)
        }
    }

    class HeaderHolder(itemView: View) : BaseHolder(itemView) {
        var header: TextView = itemView as TextView

        init {
            setIsRecyclable(false)
        }
    }

}
