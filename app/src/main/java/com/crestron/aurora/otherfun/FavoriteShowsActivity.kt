package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.firebaseserver.FirebaseDB
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowInfo
import com.google.gson.Gson
import com.peekandpop.shalskar.peekandpop.PeekAndPop
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_favorites_show.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import uk.co.deanwild.flowtextview.FlowTextView

class FavoriteShowsActivity : AppCompatActivity() {

    interface ShowHit {
        fun longClick(name: String, url: String, checked: Boolean) {

        }

        fun click(name: String, url: String, view: View) {

        }

        fun isChecked(url: String): Boolean {
            return false
        }

        fun longhit(info: ShowListActivity.NameAndLink, vararg view: View) {

        }
    }

    private var homeScreen = false
    var shouldReset = false

    private var allData: ArrayList<ShowListActivity.NameAndLink> = arrayListOf()

    private var showPutlocker = true
    private var showGogoanime = true
    private var showAnimetoon = true

    lateinit var adapter: FavoriteShowsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites_show)

        homeScreen = intent.getBooleanExtra("homeScreen", false)

        //favorite_text.text = intent.getStringExtra("displayText")
        search_info.hint = intent.getStringExtra("displayText")
        //search_layout.setPrefixText(intent.getStringExtra("displayText"))

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

        val chipCheck: (ShowListActivity.NameAndLink) -> Boolean = {
            it.name.contains(search_info.text.toString(), ignoreCase = true) &&
                    ((if (showPutlocker) it.url.contains("putlocker", true) else false)
                    || (if (showGogoanime) it.url.contains("gogoanime", true) else false)
                    || (if (showAnimetoon) it.url.contains("animetoon", true) else false))
        }

        fun checkFilter() = runOnUiThread {
            adapter.setListNotify(allData.filter(chipCheck) as ArrayList<ShowListActivity.NameAndLink>)
        }

        search_info.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                checkFilter()
            }
        })

        putlocker.setOnCheckedChangeListener { _, isChecked ->
            showPutlocker = isChecked
            checkFilter()
        }

        gogoanime.setOnCheckedChangeListener { _, isChecked ->
            showGogoanime = isChecked
            checkFilter()
        }

        animetoon.setOnCheckedChangeListener { _, isChecked ->
            showAnimetoon = isChecked
            checkFilter()
        }

        GlobalScope.launch {
            val s2 = FirebaseDB(this@FavoriteShowsActivity).getAllShowsSync()
            val s = ShowDatabase.getDatabase(this@FavoriteShowsActivity).showDao()
            val showList = s.allShows

            s2.toMutableList().removeAll { it.name.isNullOrBlank() }

            allData.addAll((showList.map { ShowListActivity.NameAndLink(it.name, it.link) } + s2.map {
                ShowListActivity.NameAndLink(it.name ?: "N/A", it.url ?: "N/A")
            }.filter { it.name != "N/A" }).sortedBy { it.name }.distinctBy { it.url })

            runOnUiThread {
                val listScreen = defaultSharedPreferences.getString("homeScreenAdding", "{\"list\" : []}")
                val showListsForScreen = Gson().fromJson(listScreen, NameList::class.java)
                //favorite_text.append("\nFavorite Count: ${showList.size}")
                search_layout.helperText = "Favorite Count: ${allData.size}"
                adapter = FavoriteShowsAdapter(allData, this@FavoriteShowsActivity, showListsForScreen.list, hitShow)
                list_to_choose.adapter = adapter
            }
        }
    }

    private val hitShow = object : ShowHit {
        override fun longClick(name: String, url: String, checked: Boolean) {
            super.longClick(name, url, checked)
            shouldReset = true
            addOrRemoveToHomeScreen(name, url, checked)
        }

        override fun isChecked(url: String): Boolean {
            val list = defaultSharedPreferences.getString("homeScreenAdding", "{\"list\" : []}")
            val showLists = Gson().fromJson(list, NameList::class.java)
            return showLists.list.any { it.url == url }
        }

        override fun click(name: String, url: String, view: View) {
            super.click(name, url, view)
            val intented = Intent(this@FavoriteShowsActivity, EpisodeActivity::class.java)
            intented.putExtra(ConstantValues.URL_INTENT, url)
            intented.putExtra(ConstantValues.NAME_INTENT, name)
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@FavoriteShowsActivity, view, "show_name_trans")
            startActivity(intented, options.toBundle())
        }

        override fun longhit(info: ShowListActivity.NameAndLink, vararg view: View) {
            val peekAndPop = PeekAndPop.Builder(this@FavoriteShowsActivity)
                    .peekLayout(R.layout.image_dialog_layout)
                    .flingTypes(true, true)
                    .longClickViews(*view)
                    .build()

            peekAndPop.setOnGeneralActionListener(object : PeekAndPop.OnGeneralActionListener {
                override fun onPop(p0: View?, p1: Int) {

                }

                @SuppressLint("SetTextI18n")
                override fun onPeek(p0: View?, p1: Int) {
                    val peekView = peekAndPop.peekView
                    val title = peekView.findViewById(R.id.title_dialog) as TextView
                    val description = peekView.findViewById(R.id.ftv) as FlowTextView
                    val episodeNumber = peekView.findViewById(R.id.episode_number_dialog) as TextView
                    val image = peekView.findViewById(R.id.image_dialog) as ImageView
                    val button = peekView.findViewById(R.id.button_dialog) as Button

                    button.visibility = View.GONE
                    title.text = Html.fromHtml("<b>${info.name}<b>", Html.FROM_HTML_MODE_COMPACT)
                    title.setTextColor(Color.WHITE)
                    episodeNumber.text = ""
                    description.textColor = Color.WHITE
                    description.setTextSize(episodeNumber.textSize)

                    GlobalScope.launch {
                        try {
                            val epiApi = EpisodeApi(ShowInfo(info.name, info.url))
                            runOnUiThread {
                                try {
                                    Picasso.get().load(epiApi.image)
                                            .error(R.drawable.apk).resize((600 * .6).toInt(), (800 * .6).toInt()).into(image)
                                } catch (e: java.lang.IllegalArgumentException) {
                                    Picasso.get().load(android.R.drawable.stat_notify_error).resize((600 * .6).toInt(), (800 * .6).toInt()).into(image)
                                }
                                title.text = info.name
                                description.text = epiApi.description
                            }
                        } catch (e: IllegalArgumentException) {
                            Loged.e(e.toString())
                        }
                    }
                }

            })
            peekAndPop.isEnabled = true
        }

    }

    override fun onBackPressed() {
        Loged.i("$shouldReset")
        val returnIntent = Intent()
        returnIntent.putExtra("restart", shouldReset)
        setResult(if (!homeScreen) Activity.RESULT_OK else 3, returnIntent)
        finish()
    }

    open class NameList(var list: ArrayList<NameUrl>)

    open class NameUrl(val name: String, val url: String)

    fun addOrRemoveToHomeScreen(name: String, url: String, addOrRemove: Boolean) {

        val list = defaultSharedPreferences.getString("homeScreenAdding", "{\"list\" : []}")

        Loged.i(list!!)

        val showList = Gson().fromJson(list, NameList::class.java)

        if (addOrRemove)
            showList.list.add(NameUrl(name, url))
        else {
            showList.list.removeIf { it.url == url }
        }
        showList.list = showList.list.distinctBy { it.url } as ArrayList<NameUrl>
        defaultSharedPreferences.edit().putString("homeScreenAdding", Gson().toJson(showList)).apply()

        Loged.d(Gson().toJson(showList))

    }

}
