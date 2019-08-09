package com.crestron.aurora.otherfun

import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.utilities.KUtility
import com.google.gson.Gson
import com.ncorti.slidetoact.SlideToActView
import com.tonyodev.fetch2.NetworkType
import kotlinx.android.synthetic.main.activity_notification_bubble.*
import kotlinx.android.synthetic.main.bubble_info_layout.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotificationBubbleActivity : AppCompatActivity() {

    companion object {
        const val BUBBLE_LINKS = "bubble_show_links"
        const val BUBBLE_NAMES = "bubble_show_names"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification_bubble)

        KUtility.clearNotiJsonList()
        //val url = intent.getStringExtra(ConstantValues.URL_INTENT)!!
        //val name = intent.getStringExtra(ConstantValues.NAME_INTENT)!!

        //val urls = intent.getStringArrayListExtra(BUBBLE_LINKS)!!
        //val names = intent.getStringArrayListExtra(BUBBLE_NAMES)!!

        val listString = Gson().fromJson(intent.getStringExtra(BUBBLE_LINKS)!!, ShowInfosList::class.java)

        Loged.wtf(listString.list.joinToString { "${it.name} with ${it.url}" })

        val shows = arrayListOf<ShowInfo>()
        /*for(i in urls.indices) {
            shows+=ShowInfo(names[i], urls[i])
        }*/
        for (i in listString.list)
            shows += ShowInfo(i.name, i.url)

        bubble_shows.layoutManager = LinearLayoutManager(this)
        class ItemOffsetDecoration(private val mItemOffset: Int) : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView,
                                        state: RecyclerView.State) {
                super.getItemOffsets(outRect, view, parent, state)
                outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset)
            }
        }

        bubble_shows.layoutManager = LinearLayoutManager(this)
        val dividerItemDecoration = DividerItemDecoration(bubble_shows.context, (bubble_shows.layoutManager as LinearLayoutManager).orientation)
        bubble_shows.addItemDecoration(dividerItemDecoration)
        bubble_shows.addItemDecoration(ItemOffsetDecoration(20))

        bubble_shows.adapter = BubbleAdapter(shows, this@NotificationBubbleActivity)

    }

    class BubbleAdapter(
            val list: ArrayList<ShowInfo>,
            private val context: Context
    ) : RecyclerView.Adapter<ViewHolder>() {

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.title.text = list[position].name
            /*holder.button.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
                override fun onSwipe_Forward(swipe_button_view: Swipe_Button_View?) {
                    downloadShow(list[position].name, list[position].url)
                }

                override fun onSwipe_Reverse(swipe_button_view: Swipe_Button_View?) {

                }
            })*/
            holder.button.onSlideCompleteListener = object : SlideToActView.OnSlideCompleteListener {
                override fun onSlideComplete(view: SlideToActView) {
                    downloadShow(list[position].name, list[position].url)
                }
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                    LayoutInflater.from(context).inflate(
                            R.layout.bubble_info_layout,
                            parent,
                            false
                    )
            )
        }

        fun downloadShow(name: String, url: String) {
            try {
                GlobalScope.launch {
                    val epApi = EpisodeApi(ShowInfo(name, url))

                    val fetchingUtils = FetchingUtils(context)
                    fetchingUtils.getVideo(epApi.episodeList.first(), NetworkType.ALL,
                            EpisodeActivity.KeyAndValue(ConstantValues.URL_INTENT, url),
                            EpisodeActivity.KeyAndValue(ConstantValues.NAME_INTENT, name))
                }
            } catch (e: Exception) {
            }
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.bubble_title_name!!
        val button: SlideToActView = view.bubble_show_download!!
    }

}
