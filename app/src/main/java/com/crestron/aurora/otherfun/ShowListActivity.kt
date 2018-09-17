package com.crestron.aurora.otherfun

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.annotation.NonNull
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.ShowDatabase
import kotlinx.android.synthetic.main.activity_show_list.*
import kotlinx.coroutines.experimental.async
import org.jsoup.Jsoup
import java.util.*


class ShowListActivity : AppCompatActivity() {

    private val listOfLinks = arrayListOf<String>()
    private val listOfNames = arrayListOf<String>()
    private val listOfNameAndLink = arrayListOf<NameAndLink>()
    private val actionHit = object : AniDownloadActivity.LinkAction {
        override fun hit(name: String, url: String) {
            super.hit(name, url)

            val intented = Intent(this@ShowListActivity, EpisodeActivity::class.java)
            intented.putExtra(ConstantValues.URL_INTENT, url)
            intented.putExtra(ConstantValues.NAME_INTENT, name)
            startActivity(intented)

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_list)

        val showDatabase = ShowDatabase.getDatabase(this@ShowListActivity)

        val recentChoice = intent.getBooleanExtra(ConstantValues.RECENT_OR_NOT, false)
        val url = intent.getStringExtra(ConstantValues.SHOW_LINK)


        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

            constructor(@NonNull context: Context, itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId)) {}

            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
            }
        }

        show_info.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(show_info.context, (show_info.layoutManager as LinearLayoutManager).orientation)
        show_info.addItemDecoration(dividerItemDecoration)
        show_info.addItemDecoration(ItemOffsetDecoration(20))
        show_info.setIndexBarVisibility(!recentChoice)

        fun getListOfAnime(urlToUse: String) {

            if (recentChoice) {

                Loged.i("We are beginning")

                val doc = Jsoup.connect(urlToUse).get()

                val lists = doc.allElements

                var listOfStuff = lists.select("div.left_col").select("table#updates").select("a[href^=http]")
                Loged.wtf(listOfStuff.size.toString())
                if(listOfStuff.size==0) {
                    listOfStuff = lists.select("div.s_left_col").select("table#updates").select("a[href^=http]")
                }

                for (element in listOfStuff) {

                    val nameAndLink = NameAndLink(element.text(), element.attr("abs:href"))

                    if (!element.text().contains("Episode"))
                        listOfNameAndLink.add(nameAndLink)
                }

            } else {

                Loged.i("We are beginning")

                val doc = Jsoup.connect(urlToUse).get()

                val lists = doc.allElements

                val listOfStuff = lists.select("td").select("a[href^=http]")

                for ((i, element) in listOfStuff.withIndex()) {
                    //Loged.wtf("$i: ${element.html()}")
                    listOfNames.add(element.text())
                    listOfLinks.add(element.attr("abs:href"))
                    listOfNameAndLink.add(NameAndLink(element.text(), element.attr("abs:href")))
                }

            }

            if(!recentChoice)
                listOfNameAndLink.sortBy { it.name }

            runOnUiThread {
                show_info.adapter = AListAdapter(listOfNameAndLink, this@ShowListActivity, actionHit)
                favorite_show.isEnabled = true//!recentChoice
            }

            Loged.d("${(show_info.adapter!! as AListAdapter).itemCount}")
            refresh_list.isRefreshing = false
        }
        fun getStuff() = async {
            Loged.i(url)
            getListOfAnime(url)
        }

        getStuff()

        refresh_list.setOnRefreshListener {
            listOfNameAndLink.clear()
            listOfNames.clear()
            listOfLinks.clear()
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            favorite_show.isEnabled = false//!recentChoice
            getStuff()
        }

        search_info.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                runOnUiThread {

                    val filtered = listOfNameAndLink.filter { it.name.contains(search_info.text.toString(), ignoreCase = true) }
                    show_info.adapter = AListAdapter(filtered, this@ShowListActivity, actionHit)

                }
            }

        })

        val gen = Random()

        random_button.setOnClickListener {
            val nameAndLink = listOfNameAndLink[gen.nextInt(listOfNameAndLink.size)]
            actionHit.hit(nameAndLink.name, nameAndLink.url)
        }

        favorite_show.isEnabled = false

        favorite_show.setOnCheckedChangeListener { _, b ->
            async {
                val listToShow = if (b) {
                    val showList = showDatabase.showDao().allShows
                    val nnList = arrayListOf<NameAndLink>()
                    for (i in showList) {
                        nnList.add(NameAndLink(i.name, i.link))
                    }

                    fun checkItems(nn: NameAndLink): Boolean {
                        for (s in showList) {
                            if (nn.name == s.name) {
                                return true
                            }
                        }
                        return false
                    }

                    listOfNameAndLink.filter { checkItems(it) }
                } else {
                    listOfNameAndLink
                }.distinctBy { it.name }
                runOnUiThread {
                    show_info.adapter = AListAdapter(listToShow, this@ShowListActivity, actionHit)
                }
            }
        }

    }

    class NameAndLink(val name: String, val url: String, var imgURL: String = "")
}
