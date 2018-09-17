package com.crestron.aurora

import android.content.Intent
import android.graphics.Rect
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.otherfun.EpisodeActivity
import com.crestron.aurora.otherfun.ShowListActivity
import kotlinx.android.synthetic.main.activity_settings_show.*
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.defaultSharedPreferences

class SettingsShowActivity : AppCompatActivity() {

    interface ShowHit {
        fun longClick(name: String, url: String, checked: Boolean) {

        }

        fun click(name: String, url: String) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_show)

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

        async {
            val s = ShowDatabase.getDatabase(this@SettingsShowActivity).showDao()
            val showList = s.allShows
            showList.sortBy { it.name }
            for (s1 in showList)
                stuff.add(ShowListActivity.NameAndLink(s1.name, s1.link))
            runOnUiThread {
                list_to_choose.adapter = SettingsShowAdapter(stuff, this@SettingsShowActivity, object : ShowHit {
                    override fun longClick(name: String, url: String, checked: Boolean) {
                        super.longClick(name, url, checked)
                    }

                    override fun click(name: String, url: String) {
                        super.click(name, url)
                        val intented = Intent(this@SettingsShowActivity, EpisodeActivity::class.java)
                        intented.putExtra(ConstantValues.URL_INTENT, url)
                        intented.putExtra(ConstantValues.NAME_INTENT, name)
                        startActivity(intented)
                    }
                })
            }
        }

    }
}
