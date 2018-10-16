package com.crestron.aurora

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.otherfun.EpisodeActivity
import com.crestron.aurora.otherfun.ShowListActivity
import com.google.gson.Gson
import com.peekandpop.shalskar.peekandpop.PeekAndPop
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_settings_show.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.textColor
import org.jsoup.Jsoup
import uk.co.deanwild.flowtextview.FlowTextView
import java.util.*

class SettingsShowActivity : AppCompatActivity() {

    interface ShowHit {
        fun longClick(name: String, url: String, checked: Boolean) {

        }

        fun click(name: String, url: String) {

        }

        fun isChecked(name: String): Boolean {
            return false
        }

        fun longhit(info: ShowListActivity.NameAndLink, vararg view: View) {

        }
    }

    var homeScreen = false
    var shouldReset = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_show)

        homeScreen = intent.getBooleanExtra("homeScreen", false)

        favorite_text.text = intent.getStringExtra("displayText")

        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
            }
        }

        list_to_choose.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(list_to_choose.context, (list_to_choose.layoutManager as LinearLayoutManager).orientation)
        list_to_choose.addItemDecoration(dividerItemDecoration)
        list_to_choose.addItemDecoration(ItemOffsetDecoration(20))

        val stuff = arrayListOf<ShowListActivity.NameAndLink>()

        launch {
            val s = ShowDatabase.getDatabase(this@SettingsShowActivity).showDao()
            val showList = s.allShows

            showList.sortBy { it.name }

            for (s1 in showList)
                stuff.add(ShowListActivity.NameAndLink(s1.name, s1.link))

            runOnUiThread {
                favorite_text.append("\nFavorite Count: ${showList.size}")
                list_to_choose.adapter = SettingsShowAdapter(stuff, this@SettingsShowActivity, object : ShowHit {
                    override fun longClick(name: String, url: String, checked: Boolean) {
                        super.longClick(name, url, checked)
                        shouldReset = true
                        addOrRemoveToHomescreen(name, url, checked)
                    }

                    override fun isChecked(name: String): Boolean {
                        val list = defaultSharedPreferences.getString("homeScreenAdding", "{\"list\" : []}")
                        val showLists = Gson().fromJson<NameList>(list, NameList::class.java)
                        return showLists.list.any { it.name == name }
                    }

                    override fun click(name: String, url: String) {
                        super.click(name, url)
                        val intented = Intent(this@SettingsShowActivity, EpisodeActivity::class.java)
                        intented.putExtra(ConstantValues.URL_INTENT, url)
                        intented.putExtra(ConstantValues.NAME_INTENT, name)
                        startActivity(intented)
                    }

                    override fun longhit(info: ShowListActivity.NameAndLink, vararg view: View) {
                        val peekAndPop = PeekAndPop.Builder(this@SettingsShowActivity)
                                .peekLayout(R.layout.image_dialog_layout)
                                .apply {
                                    for (v in view) {
                                        longClickViews(v)
                                    }
                                }
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
                                            //title.text = info.name
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

                })
            }
        }
    }

    override fun onBackPressed() {
        if (!homeScreen) {
            //val intent = Intent(this@SettingsShowActivity, ChoiceActivity::class.java)
            //startActivity(intent)
            Loged.i("$shouldReset")
            val returnIntent = Intent()
            returnIntent.putExtra("restart", shouldReset)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        } else {
            val returnIntent = Intent()
            returnIntent.putExtra("restart", shouldReset)
            setResult(3, returnIntent)
            finish()
            //super.onBackPressed()
        }
    }

    open class NameList(var list: ArrayList<NameUrl>)

    open class NameUrl(val name: String, val url: String)

    fun addOrRemoveToHomescreen(name: String, url: String, addOrRemove: Boolean) {

        val list = defaultSharedPreferences.getString("homeScreenAdding", "{\"list\" : []}")

        Loged.i(list!!)

        val showList = Gson().fromJson<NameList>(list, NameList::class.java)

        if (addOrRemove)
            showList.list.add(NameUrl(name, url))
        else {
            showList.list.removeIf { it.name == name }
        }

        defaultSharedPreferences.edit().putString("homeScreenAdding", Gson().toJson(showList)).apply()

        Loged.d(Gson().toJson(showList))

    }

}
