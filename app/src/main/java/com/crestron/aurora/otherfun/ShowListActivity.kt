package com.crestron.aurora.otherfun

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.utilities.ViewUtil
import com.peekandpop.shalskar.peekandpop.PeekAndPop
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show_list.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.textColor
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import uk.co.deanwild.flowtextview.FlowTextView
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

        override fun longhit(info: NameAndLink, views: View) {
            val peekAndPop = PeekAndPop.Builder(this@ShowListActivity)
                    .peekLayout(R.layout.image_dialog_layout)
                    .longClickViews(views)
                    .build()

            peekAndPop.setOnGeneralActionListener(object : PeekAndPop.OnGeneralActionListener {
                override fun onPop(p0: View?, p1: Int) {

                }

                override fun onPeek(p0: View?, p1: Int) {
                    val peekView = peekAndPop.peekView
                    val title = peekView.findViewById(R.id.title_dialog) as TextView
                    val description = peekView.findViewById(R.id.ftv) as FlowTextView
                    val episodeNumber = peekView.findViewById(R.id.episode_number_dialog) as TextView
                    val image = peekView.findViewById(R.id.image_dialog) as ImageView
                    val button = peekView.findViewById(R.id.button_dialog) as Button

                    button.visibility = View.GONE
                    title.text = Html.fromHtml("<b>${info.name}<b>", Html.FROM_HTML_MODE_COMPACT)
                    title.textColor = Color.WHITE
                    //description_dialog.text = description
                    episodeNumber.text = ""
                    description.textColor = Color.WHITE
                    description.setTextSize(episodeNumber.textSize)

                    async {
                        try {
                            val doc1 = Jsoup.connect(info.url).get()
                            runOnUiThread {
                                Picasso.get().load(doc1.select("div.left_col").select("img[src^=http]#series_image").attr("abs:src"))
                                        .error(R.drawable.apk).resize((600 * .6).toInt(), (800 * .6).toInt()).into(image)
                                //Picasso.get().load(info.imgURL).resize(360, 480).into(image)
                                title.text = info.name
                                val des = if (doc1.allElements.select("div#series_details").select("span#full_notes").hasText())
                                    doc1.allElements.select("div#series_details").select("span#full_notes").text().removeSuffix("less")
                                else {
                                    val d = doc1.allElements.select("div#series_details").select("div:contains(Description:)").select("div").text()
                                    try {
                                        d.substring(d.indexOf("Description: ") + 13, d.indexOf("Category: "))
                                    } catch (e: StringIndexOutOfBoundsException) {
                                        Loged.e(e.message!!)
                                        d
                                    }
                                }
                                description.text = des
                            }
                        } catch (e: IllegalArgumentException) {

                        }
                    }
                }

            })

            //val dialog = ImageDialog(this@RssAdapter.context, information.title, information.description, information.episodeNumber, information.imageLink)
            //dialog.show()

            peekAndPop.isEnabled = true

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_list)

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        val showDatabase = ShowDatabase.getDatabase(this@ShowListActivity)

        val recentChoice = intent.getBooleanExtra(ConstantValues.RECENT_OR_NOT, false)
        val url = intent.getStringExtra(ConstantValues.SHOW_LINK)

        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {

            //constructor(@NonNull context: Context, itemOffsetId: Int) : this(context.resources.getDimensionPixelSize(itemOffsetId)) {}

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

            try {

                if (recentChoice) {

                    Loged.i("We are beginning")

                    val doc = Jsoup.connect(urlToUse).get()

                    val lists = doc.allElements

                    var listOfStuff = lists.select("div.left_col").select("table#updates").select("a[href^=http]")
                    Loged.wtf(listOfStuff.size.toString())
                    if (listOfStuff.size == 0) {
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

                    for (element in listOfStuff) {
                        //Loged.wtf("$i: ${element.html()}")
                        listOfNames.add(element.text())
                        listOfLinks.add(element.attr("abs:href"))
                        listOfNameAndLink.add(NameAndLink(element.text(), element.attr("abs:href")))
                    }

                }

            } catch (e: HttpStatusException) {
                Loged.wtf("${e.message}")
            }

            if(!recentChoice)
                listOfNameAndLink.sortBy { it.name }

            runOnUiThread {
                show_info.adapter = AListAdapter(listOfNameAndLink, this@ShowListActivity, actionHit)
                favorite_show.isEnabled = true//!recentChoice
                search_info.isEnabled = true
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

        search_info.isEnabled = false
        favorite_show.isEnabled = false

        favorite_show.setOnCheckedChangeListener { _, b ->
            launch {
                val listToShow = if (b) {
                    val showList = showDatabase.showDao().allShows
                    val nnList = arrayListOf<NameAndLink>()
                    for (i in showList) {
                        nnList.add(NameAndLink(i.name, i.link))
                    }

                    fun checkItems(nn: NameAndLink): Boolean {
                        for (s in showList) {
                            if (nn.url == s.link) {
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
