package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.ArrayAdapter
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.views.StickHeaderItemDecoration
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import kotlinx.android.synthetic.main.activity_rss.*
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup
import java.text.SimpleDateFormat


class RssActivity : AppCompatActivity() {

    open class MainInfo(val info: String) {
        override fun toString(): String {
            return info
        }
    }

    open class MALInfo(val title: String, val description: String, val time: String, val imageLink: String) : MainInfo(title)

    open class HeaderInfo(val title: String) : MainInfo(title)

    var list = arrayListOf<MainInfo>()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss)

        textView5.append("\nCurrent Date is ${SimpleDateFormat("MM/dd/yyyy E hh:mm a").format(System.currentTimeMillis())}")

        fun getStuff() = async {

            fun livechartRss() = async {

                val urlString = "https://www.livechart.me/feeds/episodes"
                val parser = Parser()
                parser.execute(urlString)
                parser.onFinish(object : Parser.OnTaskCompleted {

                    override fun onTaskCompleted(list: ArrayList<Article>) {
                        //what to do when the parsing is done
                        //the Array List contains all article's data. For example you can use it for your adapter.
                        //this@RssActivity.list = list

                        //feed_list.adapter = RssAdapter(list, this@RssActivity)
                        //refresh_feed.isRefreshing = false
                    }

                    override fun onError() {
                        //what to do in case of error
                        //refresh_feed.isRefreshing = false
                    }
                })

            }

            fun malGet() {

                val doc = Jsoup.connect("https://myanimelist.net/anime/season/schedule").get()

                val el = doc.select("div.js-categories-seasonal")
                val header = el.select("div.seasonal-anime-list")

                val listMap = mutableMapOf<String, List<MainInfo>>()

                listMap["All"] = list.apply {
                    add(HeaderInfo("All"))
                }

                for (h in header) {
                    val showLists = arrayListOf<MALInfo>()
                    val j = h.select("div.seasonal-anime")
                    val headed = HeaderInfo(h.select("div.anime-header").text())
                    list.add(headed)
                    for (i in j) {
                        val time = i.select("div.information").select("span.remain-time").text()
                        //Loged.d(SimpleDateFormat("MM/dd/yyyy hh:mm a").format(time))
                        //Loged.d(time)
                        //TimeZone.getDefault().getDisplayName()
                        val title = i.select("p.title-text").text()
                        val description = i.select("div.synopsis").text()
                        var imageLink = i.select("div.image").select("img").attr("data-src")
                        if (imageLink.isBlank())
                            imageLink = i.select("div.image").select("img").attr("src")

                        val m = MALInfo(title, description, time, imageLink)
                        //showLists.add(m)
                        list.add(m)
                    }
                    listMap[headed.title] = showLists
                }

                //runOnUiThread {

                val spinnerAdapter = ArrayAdapter<String>(this@RssActivity,
                        android.R.layout.simple_spinner_item, listMap.keys.toList())

                spinner.setAdapter(spinnerAdapter)

                spinner.addOnItemClickListener { _, _, position, _ ->

                    runOnUiThread {

                        //feed_list.smoothScrollToPosition(list.indexOfFirst { it.info == listMap.keys.toList()[position] })

                        val smoothScroller = object : LinearSmoothScroller(this@RssActivity) {
                            override fun getVerticalSnapPreference(): Int {
                                return LinearSmoothScroller.SNAP_TO_START
                            }
                        }

                        smoothScroller.targetPosition = list.indexOfFirst { it.info == listMap.keys.toList()[position] }

                        feed_list.layoutManager!!.startSmoothScroll(smoothScroller)
                        /*val listing = arrayListOf<MainInfo>().apply {
                            add(HeaderInfo(listMap.keys.toList()[position]))
                            addAll(listMap[listMap.keys.toList()[position]]!!)
                        }

                        feed_list.removeItemDecoration(StickHeaderItemDecoration(feed_list.adapter as StickHeaderItemDecoration.StickyHeaderInterface))
                        val adapter = RssAdapter(listing, this@RssActivity)
                        feed_list.adapter = adapter
                        feed_list.addItemDecoration(StickHeaderItemDecoration(adapter))
                        */
                    }
                }

                //}

                Loged.i("${listMap.entries}")

            }

            malGet()

            runOnUiThread {

                val adapter = RssAdapter(list, this@RssActivity)
                feed_list.adapter = adapter
                feed_list.addItemDecoration(StickHeaderItemDecoration(adapter))
                //refresh_feed.isRefreshing = false
            }

        }

        feed_list.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(feed_list.context, (feed_list.layoutManager as LinearLayoutManager).orientation)
        feed_list.addItemDecoration(dividerItemDecoration)
        feed_list.addItemDecoration(ItemOffsetDecoration(20))
        feed_list.isNestedScrollingEnabled = true


        /*refresh_feed.setOnRefreshListener {
            spinner.addOnItemClickListener(null)
            list.clear()
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            getStuff()
        }*/

        getStuff()

    }

    class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

        constructor(@NonNull context: Context, itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId)) {}

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                    state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
        }
    }

}
