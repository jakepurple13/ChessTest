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
import com.crestron.aurora.R
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import kotlinx.android.synthetic.main.activity_rss.*

class RssActivity : AppCompatActivity() {

    var list = arrayListOf<Article>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss)

        fun getStuff() {

            /*async {

                val htmlParse = Jsoup.connect("https://www.livechart.me/schedule/tv").get()
                Loged.e(htmlParse.text())
                val cards = htmlParse.select("div.chart compact")
                for(i in cards) {
                    Loged.v(i.attr("data-title"))
                }

            }*/

            val urlString = "https://www.livechart.me/feeds/episodes"
            val parser = Parser()
            parser.execute(urlString)
            parser.onFinish(object : Parser.OnTaskCompleted {

                override fun onTaskCompleted(list: ArrayList<Article>) {
                    //what to do when the parsing is done
                    //the Array List contains all article's data. For example you can use it for your adapter.
                    this@RssActivity.list = list

                    feed_list.adapter = RssAdapter(list, this@RssActivity)
                    refresh_feed.isRefreshing = false
                }

                override fun onError() {
                    //what to do in case of error
                    refresh_feed.isRefreshing = false
                }
            })

        }

        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

            constructor(@NonNull context: Context, itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId)) {}

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
            }
        }

        feed_list.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(feed_list.context, (feed_list.layoutManager as LinearLayoutManager).orientation)
        feed_list.addItemDecoration(dividerItemDecoration)
        feed_list.addItemDecoration(ItemOffsetDecoration(20))

        refresh_feed.setOnRefreshListener {
            list.clear()
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            getStuff()
        }

        getStuff()

    }
}
