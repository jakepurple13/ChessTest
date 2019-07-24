package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.widget.ArrayAdapter
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.views.StickHeaderItemDecoration
import com.kaopiz.kprogresshud.KProgressHUD
import com.prof.rssparser.Article
import com.prof.rssparser.Parser
import kotlinx.android.synthetic.main.activity_rss.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.jsoup.Jsoup
import java.text.SimpleDateFormat


class RssActivity : AppCompatActivity() {

    open class MainInfo(val info: String) {
        override fun toString(): String {
            return info
        }
    }

    open class MALInfo(val title: String, val description: String, val time: String, val imageLink: String, val episodeNumber: String) : MainInfo(title)

    open class HeaderInfo(val title: String) : MainInfo(title)

    var list = arrayListOf<MainInfo>()

    interface RssAdapterListener {
        fun isHeader(boolean: Boolean, position: Int, name: String)
    }

    val listMap = mutableMapOf<String, List<MainInfo>>()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rss)

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        val hud = KProgressHUD.create(this@RssActivity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Loading")
                .setDetailsLabel("Loading Shows")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .setCancellable(false)

        textView5.append("\nCurrent Date is ${SimpleDateFormat("MM/dd/yyyy E hh:mm a").format(System.currentTimeMillis())}")
        spinner.isEnabled = false

        fun getStuff() = GlobalScope.async {
            runOnUiThread {
                hud.show()
            }
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

                listMap["All"] = list.apply {
                    add(HeaderInfo("All"))
                }

                for (h in header) {
                    //val showLists = arrayListOf<MALInfo>()
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
                        val episodeNumber = i.select("div.eps").select("span").text()

                        val m = MALInfo(title, description, time, imageLink, episodeNumber)
                        //showLists.add(m)
                        list.add(m)
                    }
                    listMap[headed.title] = arrayListOf()//showLists
                }

                Loged.i("Done getting information with ${list.size}")

                runOnUiThread {

                    val spinnerAdapter = ArrayAdapter<String>(this@RssActivity,
                            android.R.layout.simple_spinner_item, listMap.keys.toList())

                    spinner.setAdapter(spinnerAdapter)

                    spinner.isEnabled = true

                    spinner.addOnItemClickListener { _, _, position, _ ->
                        runOnUiThread {
                            val smoothScroller = object : LinearSmoothScroller(this@RssActivity) {
                                override fun getVerticalSnapPreference(): Int {
                                    return LinearSmoothScroller.SNAP_TO_START
                                }
                            }

                            smoothScroller.targetPosition = list.indexOfFirst { it.info == listMap.keys.toList()[position] }

                            feed_list.layoutManager!!.startSmoothScroll(smoothScroller)
                        }
                    }
                }

                Loged.i("${listMap.entries}")

            }

            malGet()

            runOnUiThread {
                val adapter = RssAdapter(list, this@RssActivity, object : RssAdapterListener {
                    override fun isHeader(boolean: Boolean, position: Int, name: String) {
                        if (boolean) {
                            spinner.selectedIndex = listMap.keys.asSequence().indexOf(name)
                        }
                    }
                })
                feed_list.adapter = adapter
                feed_list.addItemDecoration(StickHeaderItemDecoration(adapter))
                feed_list.addItemDecoration(ItemOffsetDecoration(20, list))
                //refresh_feed.isRefreshing = false
                hud.dismiss()
            }
        }

        feed_list.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(feed_list.context, (feed_list.layoutManager as LinearLayoutManager).orientation)
        feed_list.addItemDecoration(dividerItemDecoration)
        //feed_list.addItemDecoration(ItemOffsetDecoration(20))
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

    class ItemOffsetDecoration(private val mItemOffset: Int, val list: List<MainInfo>) : RecyclerView.ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                    state: RecyclerView.State) {
            super.getItemOffsets(outRect, view, parent, state)
            if (list[parent.getChildAdapterPosition(view)] !is HeaderInfo)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
        }
    }

}
