package com.crestron.aurora.otherfun

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.views.StickHeaderItemDecoration
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import kotlinx.android.synthetic.main.activity_rss.*
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup


class RssActivity : AppCompatActivity() {

    open class MainInfo(val info: String)

    open class MALInfo(val title: String, val description: String, val time: String, val imageLink: String) : MainInfo(title)

    open class HeaderInfo(val title: String) : MainInfo(title)

    var list = arrayListOf<MainInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss)

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
                        refresh_feed.isRefreshing = false
                    }

                    override fun onError() {
                        //what to do in case of error
                        refresh_feed.isRefreshing = false
                    }
                })

            }

            fun aniChartGet() {
                val doc = Jsoup.connect("http://anichart.net/airing").get()

                Loged.wtf(doc.html())

                val el = doc.select("div.section")

                for (i in el) {
                    val head = i.select("h2").text()
                    Loged.w(head)
                    val items = i.select("div.item")
                    for (j in items) {
                        val title = j.select("div.title").text()
                        val airing = j.select("div.airing").text()
                        Loged.i("$title at $airing")
                        val imageUrl = j.select("div.image").attr("style")

                        //background-image:url(https://cdn.anilist.co/img/dir/anime/reg/101115-8W9RCORvvMuc.jpg)

                    }
                }

            }

            fun malGet() {

                val doc = Jsoup.connect("https://myanimelist.net/anime/season/schedule").get()

                val el = doc.select("div.js-categories-seasonal")
                val header = el.select("div.seasonal-anime-list")

                for (h in header) {
                    //Loged.w(h.select("div.anime-header").text())
                    val j = h.select("div.seasonal-anime")
                    list.add(HeaderInfo(h.select("div.anime-header").text()))
                    for (i in j) {
                        //Loged.i(h.select("div.anime-header").text())
                        val time = i.select("div.information").select("span.remain-time").text()
                        //Loged.d(SimpleDateFormat("MM/dd/yyyy hh:mm a").format(time))
                        //Loged.d(time)
                        //TimeZone.getDefault().getDisplayName()
                        val title = i.select("p.title-text").text()
                        val description = i.select("div.synopsis").text()
                        val imageLink = i.select("div.image").select("img[src^=https]").attr("abs:src")
                        //val info = "$title\n$description\n$time\n$imageLink"
                        //Loged.i(info)

                        list.add(MALInfo(title, description, time, imageLink))
                    }
                }
            }

            malGet()

            runOnUiThread {

                val adapter = RssAdapter(list, this@RssActivity)
                feed_list.adapter = adapter
                feed_list.addItemDecoration(StickHeaderItemDecoration(adapter))
                refresh_feed.isRefreshing = false
            }

        }

        feed_list.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(feed_list.context, (feed_list.layoutManager as LinearLayoutManager).orientation)
        feed_list.addItemDecoration(dividerItemDecoration)
        feed_list.addItemDecoration(ItemOffsetDecoration(20))
        feed_list.isNestedScrollingEnabled = true


        refresh_feed.setOnRefreshListener {
            list.clear()
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            getStuff()
        }

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
