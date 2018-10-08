package com.crestron.aurora

import android.app.Activity
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.otherfun.EpisodeActivity
import com.crestron.aurora.otherfun.ShowListActivity
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_settings_show.*
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.defaultSharedPreferences
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
            super.onBackPressed()
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
