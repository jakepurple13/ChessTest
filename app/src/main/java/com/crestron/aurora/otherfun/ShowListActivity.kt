@file:Suppress("DeferredResultUnused")

package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.FormActivity
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.showapi.Source
import com.crestron.aurora.utilities.*
import com.google.android.material.snackbar.Snackbar
import com.kaopiz.kprogresshud.KProgressHUD
import com.like.LikeButton
import com.peekandpop.shalskar.peekandpop.PeekAndPop
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_show_list.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.anko.*
import uk.co.deanwild.flowtextview.FlowTextView
import java.net.SocketTimeoutException
import java.util.*


class ShowListActivity : AppCompatActivity() {
    private val listOfNameAndLink = arrayListOf<ShowInfo>()
    private val actionHit = object : LinkAction {
        override fun hit(name: String, url: String, vararg view: View) {
            super.hit(name, url, *view)
            val intented = Intent(this@ShowListActivity, EpisodeActivity::class.java)
            intented.putExtra(ConstantValues.URL_INTENT, url)
            intented.putExtra(ConstantValues.NAME_INTENT, name)
            val titleView = Pair(view[0], "show_name_trans")
            val likeView = if (view.size > 1) Pair(view[1], "like_trans") else null
            val viewsToUse = arrayListOf<Pair<View, String>>()
            viewsToUse.add(titleView)
            if (likeView != null) {
                viewsToUse.add(likeView)
                intented.putExtra("is_favorited", (view[1] as LikeButton).isLiked)
            }
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(this@ShowListActivity, *viewsToUse.toTypedArray())
            startActivityForResult(intented, 111, options.toBundle())
        }

        override fun longhit(info: ShowInfo, vararg views: View) {
            val peekAndPop = PeekAndPop.Builder(this@ShowListActivity)
                    .peekLayout(R.layout.image_dialog_layout)
                    /*.apply {
                        for (v in views) {
                            longClickViews(v)
                        }
                    }*/
                    .flingTypes(true, true)
                    .longClickViews(*views)
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
                    val button2 = peekView.findViewById(R.id.button_dialog_two) as Button

                    button.visibility = View.GONE
                    button2.visibility = View.GONE
                    title.text = Html.fromHtml("<b>${info.name}<b>", Html.FROM_HTML_MODE_COMPACT)
                    title.setTextColor(Color.WHITE)
                    //description_dialog.text = description
                    episodeNumber.text = ""
                    description.textColor = Color.WHITE
                    description.setTextSize(episodeNumber.textSize)

                    GlobalScope.launch {
                        try {
                            val episode = EpisodeApi(info, 5000)
                            runOnUiThread {
                                try {
                                    Picasso.get().load(episode.image)
                                            .error(R.drawable.apk).resize((600 * .6).toInt(), (800 * .6).toInt()).into(image)
                                } catch (e: java.lang.IllegalArgumentException) {
                                    Picasso.get().load(android.R.drawable.stat_notify_error).resize((600 * .6).toInt(), (800 * .6).toInt()).into(image)
                                }
                                title.text = info.name
                                description.text = episode.description
                            }
                        } catch (e: IllegalArgumentException) {

                        } catch (e: SocketTimeoutException) {

                        }
                    }
                }

            })

            peekAndPop.isEnabled = true

        }
    }

    @SuppressLint("SetTextI18n")
    private fun showAlert(info: ShowInfo) {

        val alertDialog = AlertDialog.Builder(this@ShowListActivity)

        val inflater = layoutInflater

        val peekView = inflater.inflate(R.layout.image_dialog_layout, show_list_layout, false)
        val title = peekView.findViewById(R.id.title_dialog) as TextView
        val description = peekView.findViewById(R.id.ftv) as FlowTextView
        val episodeNumber = peekView.findViewById(R.id.episode_number_dialog) as TextView
        val image = peekView.findViewById(R.id.image_dialog) as ImageView
        val button = peekView.findViewById(R.id.button_dialog) as Button
        val button2 = peekView.findViewById(R.id.button_dialog_two) as Button

        //button.visibility = View.GONE
        //button2.visibility = View.GONE
        button.text = "Cancel"
        button2.text = "Go To Show"
        title.text = Html.fromHtml("<b>${info.name}<b>", Html.FROM_HTML_MODE_COMPACT)
        title.setTextColor(Color.WHITE)
        //description_dialog.text = description
        episodeNumber.text = ""
        description.textColor = Color.WHITE
        description.setTextSize(episodeNumber.textSize)

        GlobalScope.launch {
            try {
                val episode = EpisodeApi(info, 5000)
                runOnUiThread {
                    try {
                        Picasso.get().load(episode.image)
                                .error(R.drawable.apk).resize((600 * .6).toInt(), (800 * .6).toInt()).into(image)
                    } catch (e: java.lang.IllegalArgumentException) {
                        Picasso.get().load(android.R.drawable.stat_notify_error).resize((600 * .6).toInt(), (800 * .6).toInt()).into(image)
                    }
                    title.text = info.name
                    description.text = episode.description
                }
            } catch (e: IllegalArgumentException) {

            } catch (e: SocketTimeoutException) {

            }
        }

        val alert = alertDialog
                .setView(peekView)
                .create()

        button.setOnClickListener {
            alert.dismiss()
        }

        button2.setOnClickListener {
            alert.dismiss()
            actionHit.hit(info.name, info.url, random_button)
        }

        alert.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == 111 && data != null) {
            val urlString = data.getStringExtra("url_string")
            val isFavorite = data.getBooleanExtra("is_favorited", false)
            Loged.wtf("$urlString is now $isFavorite")
            (show_info.adapter as AListAdapter).notifyItemChanged((show_info.adapter as AListAdapter).stuff.indexOfFirst { it.url == urlString }, isFavorite)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_list)

        ViewUtil.revealing(findViewById(android.R.id.content), intent)

        val hud = KProgressHUD.create(this@ShowListActivity)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Loading")
                .setDetailsLabel("Loading Shows")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .setCancellable(false)

        val showDatabase = ShowDatabase.getDatabase(this@ShowListActivity)

        val recentChoice = intent.getBooleanExtra(ConstantValues.RECENT_OR_NOT, false)
        val url = intent.getStringExtra(ConstantValues.SHOW_LINK)!!
        val movie = intent.getBooleanExtra(ConstantValues.SHOW_MOVIE, false)

        show_info.layoutManager = LinearLayoutManager(this).apply { this@apply.isSmoothScrollbarEnabled = true }
        show_info.setHasFixedSize(true)
        val dividerItemDecoration = DividerItemDecoration(show_info.context, (show_info.layoutManager as LinearLayoutManager).orientation)
        show_info.addItemDecoration(dividerItemDecoration)
        show_info.setIndexBarVisibility(!recentChoice)

        fun getListOfAnime(urlToUse: String) {

            try {

                val source = Source.getSourceFromUrl(urlToUse)
                source.movie = movie
                val showApi = ShowApi(source)

                listOfNameAndLink.addAll(showApi.showInfoList)

                if (!recentChoice)
                    listOfNameAndLink.sortBy { it.name }
                else
                    defaultSharedPreferences.edit().putInt(ConstantValues.UPDATE_COUNT, 0).apply()

                runOnUiThread {
                    show_info.adapter = AListAdapter(listOfNameAndLink, this@ShowListActivity, showDatabase, actionHit, recentChoice)
                    favorite_show.isEnabled = true//!recentChoice
                    search_info.isEnabled = true
                    Loged.d("${(show_info.adapter!! as AListAdapter).itemCount}")
                    hud.dismiss()
                    refresh_list.isRefreshing = false
                }

            } catch (e: SocketTimeoutException) {
                errorHasOccurred(e.message!!)
            }
        }

        fun getStuffAsync() = GlobalScope.async {
            Loged.i(url)
            runOnUiThread {
                hud.show()
            }
            getListOfAnime(url)
        }

        if (Utility.isNetwork(this))
            getStuffAsync()
        else {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("No Internet")
            builder.setMessage("Please get connected to internet to use this section")
            // Add the buttons
            builder.setPositiveButton("Okay") { _, _ ->
                finish()
            }
            builder.setCancelable(false)
            val dialog = builder.create()
            dialog.show()
        }

        refresh_list.setOnRefreshListener {
            listOfNameAndLink.clear()
            // Your code to refresh the list here.
            // Make sure you call swipeContainer.setRefreshing(false)
            // once the network request has completed successfully.
            favorite_show.isEnabled = false//!recentChoice
            getStuffAsync()
        }

        search_info.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                runOnUiThread {
                    val filtered = listOfNameAndLink.filter { it.name.contains(search_info.text.toString(), ignoreCase = true) }
                    show_info.adapter = AListAdapter(filtered, this@ShowListActivity, showDatabase, actionHit)
                }
            }

        })

        toTop.setOnClickListener {
            show_info.smoothScrollAction(0) {
                Snackbar.make(show_info, "At Top", Snackbar.LENGTH_SHORT).show()
            }
        }

        val gen = Random()

        random_button.setOnClickListener {
            val num = gen.nextInt(listOfNameAndLink.size)
            show_info.smoothScrollAction(num) {
                GlobalScope.launch {
                    val l = show_info.findViewHolderForAdapterPosition(num) as? ViewHolderShow
                    val lay = l?.layout
                    lay?.flashScreen(duration = 250, paramSetup = {
                        it.alignParentStart()
                        it.alignParentEnd()
                        it.sameTop(l.favorite)
                        it.sameBottom(l.favorite)
                    }, doOnEnd = {
                        GlobalScope.launch {
                            delay(250)
                            runOnUiThread {
                                val nameAndLink = listOfNameAndLink[num]
                                showAlert(nameAndLink)
                            }
                        }
                    })
                }
            }
        }

        search_info.isEnabled = false
        favorite_show.isEnabled = false

        favorite_show.setOnCheckedChangeListener { _, b ->
            GlobalScope.launch {
                val listToShow = if (b) {
                    val showList = showDatabase.showDao().allShows
                    val nnList = arrayListOf<NameAndLink>()
                    for (i in showList) {
                        nnList.add(NameAndLink(i.name, i.link))
                    }

                    fun checkItems(nn: ShowInfo): Boolean {
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
                    show_info.adapter = AListAdapter(listToShow, this@ShowListActivity, showDatabase, actionHit)
                }
            }
        }

    }

    private fun errorHasOccurred(message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("An error has occurred")
        builder.setMessage("Please send feedback and explain what you were doing when you found this error. I am sorry for your inconvenience.")
        // Add the buttons
        builder.setPositiveButton("OK") { _, _ ->
            startActivity(Intent(this@ShowListActivity, FormActivity::class.java).apply {
                putExtra("error_feedback", message)
            })
            finish()
        }
        builder.setCancelable(false)
        val dialog = builder.create()
        runOnUiThread {
            dialog.show()
        }
    }

    class NameAndLink(val name: String, val url: String)

    interface LinkAction {
        fun hit(name: String, url: String, vararg view: View) {
            Loged.wtf("$name: $url")
        }

        fun longhit(info: ShowInfo, vararg views: View) {

        }
    }
}
