package com.crestron.aurora.views

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import androidx.annotation.ColorInt
import androidx.annotation.LayoutRes
import androidx.annotation.StyleRes
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class SettingsBuilder(var context: Context) {

    /**
     * Extend this if you want to make a custom item
     */
    abstract class SettingListItem : SettingItem {
        var tag: Any? = null
        /**
         * The view that will be added
         */
        abstract val layout: View

        /**
         * The view and any additions that need to be changed. Use [apply] here for setup before the view gets added
         */
        override fun build(): View = layout.apply { tag = this@SettingListItem.tag }

        /**
         * Show or hide this Row
         */
        fun show(show: Boolean = true) {
            layout.visibility = if (show) View.VISIBLE else View.GONE
        }
    }

    interface SettingItem {
        fun build(): View
    }

    inner class ButtonItem : SettingListItem() {
        override val layout = MaterialButton(context).apply {
            gravity = Gravity.CENTER
        }
        var text: String = ""
            get() = layout.text.toString()
            set(value) {
                field = value
                layout.text = value
            }
        private var action: (View) -> Unit = {}
            set(value) {
                field = value
                layout.setOnClickListener(action)
            }

        fun action(block: (View) -> Unit) {
            action = block
        }

        @ColorInt
        var backgroundColor = "#03CFFF".toColorInt()
            set(value) {
                field = value
                layout.setBackgroundColor(value)
            }
    }

    inner class CheckBoxItem : SettingListItem() {
        override val layout = CheckBox(context).apply { layoutDirection = CheckBox.LAYOUT_DIRECTION_RTL }
        var text: String = ""
            get() = layout.text.toString()
            set(value) {
                field = value
                layout.text = value
            }
        var checked = false
            get() = layout.isChecked
            set(value) {
                field = value
                layout.isChecked = value
            }
        private var action: (View, Boolean) -> Unit = { _, _ -> }
            set(value) {
                field = value
                layout.setOnCheckedChangeListener(value)
            }

        fun style(@StyleRes styleId: Int) = layout.setTextAppearance(styleId)
        fun action(block: (View, Boolean) -> Unit) {
            action = block
        }
    }

    inner class Divider(var dividerType: DividerTypes = DividerTypes.NONE, var dividerDrawable: Drawable? = null)

    enum class DividerTypes(internal var num: Int) {
        NONE(LinearLayout.SHOW_DIVIDER_NONE),
        MIDDLE(LinearLayout.SHOW_DIVIDER_MIDDLE),
        END(LinearLayout.SHOW_DIVIDER_END),
        BEGINNING(LinearLayout.SHOW_DIVIDER_BEGINNING)
    }

    companion object {
        fun settingsBuilder(context: Context, block: SettingsBuilder.() -> Unit) = SettingsBuilder(context).apply(block).build()
    }

    private var dividerInfo: Divider = Divider()
    fun divider(block: Divider.() -> Unit) = dividerInfo.apply(block)

    private val viewList = mutableListOf<View>()
    private val layoutInflater = LayoutInflater.from(context)

    fun checkboxItem(block: CheckBoxItem.() -> Unit = {}) = CheckBoxItem().apply(block).buildItem()
    fun buttonItem(block: ButtonItem.() -> Unit = {}) = ButtonItem().apply(block).buildItem()
    fun <T : SettingListItem> T.buildItem() = Pair(this, build()).apply { +second }.first

    operator fun View.unaryPlus() = viewList.add(this)
    fun View.addRow() = viewList.add(this)
    fun addView(item: View) = viewList.add(item)
    fun addView(newView: () -> View) = addView(newView())

    private var viewSetup: (View) -> Unit = {}
    fun viewSetup(block: (View) -> Unit) {
        viewSetup = block
    }

    fun build(): LinearLayout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        weightSum = viewList.size.toFloat()
        showDividers = dividerInfo.dividerType.num
        dividerDrawable = dividerInfo.dividerDrawable
        viewList.forEach {
            addView(it, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f))
            if (it is MaterialButton) buttonSetup(it)
            viewSetup(it)
        }
    }

    private fun buttonSetup(button: MaterialButton) {
        val ll = button.layoutParams as LinearLayout.LayoutParams
        ll.width = LinearLayout.LayoutParams.WRAP_CONTENT
        ll.gravity = Gravity.CENTER
        button.layoutParams = ll
    }

}

fun ViewGroup.addSettings(params: ViewGroup.LayoutParams, block: SettingsBuilder.() -> Unit) {
    addView(SettingsBuilder.settingsBuilder(context, block), params)
    post { scrollTo(0, 0) }
}

//An example of adding a new item
class TestItem(context: Context) : SettingsBuilder.SettingListItem() {
    override val layout: Button = Button(context)
    var text: String = ""
        set(value) {
            field = value
            layout.text = value
        }
    private var action: (View) -> Unit = {}
        set(value) {
            field = value
            layout.setOnClickListener(value)
        }

    fun action(block: (View) -> Unit) {
        action = block
    }
}

//Then create this and you are good to go
fun SettingsBuilder.testItem(block: TestItem.() -> Unit = {}) = TestItem(context).apply(block).buildItem()

/**
 * example
 *
 * ```
 *   recyclerViewGenericItem {
 *       addItem(R.layout.text_row_item) {
 *           textText.text = "6 is here right now"
 *       }
 *       addItems<FanMode>(R.layout.edittext_row, FanMode.values().toList()) {
 *           textLayout.editText?.setText(it.toString())
 *       }
 *   }
 * ```
 */
class RecyclerViewGenericItem(var context: Context) : SettingsBuilder.SettingListItem() {
    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)
    private var recyclerSetup: RecyclerView.() -> Unit = {
        layoutManager = LinearLayoutManager(context).apply { orientation = LinearLayoutManager.VERTICAL }
    }
    private val adapter = RVIAdapter()
    val rvAdapter: RecyclerView.Adapter<ViewHolder> = adapter
    override val layout: RecyclerView = RecyclerView(context).apply { this.adapter = this@RecyclerViewGenericItem.adapter }

    constructor(context: Context, block: RecyclerView.() -> Unit) : this(context) {
        recyclerSetup = block
    }

    override fun build(): View = layout.apply(recyclerSetup)
    fun removeItem(position: Int) = adapter.removeItem(position)
    fun <T> removeItem(item: T) = adapter.removeItem(item)
    fun <T> removeItem(predicate: (T) -> Boolean) = adapter.removeItem(predicate)
    fun addItem(@LayoutRes layoutRes: Int, block: View.(Unit) -> Unit) = adapter.addItem(layoutRes to RVDynamicItem(Unit, block))
    fun <T> T.addItem(@LayoutRes layoutRes: Int, block: View.(T) -> Unit) = adapter.addItem(layoutRes to RVDynamicItem(this, block))
    fun <T> Collection<T>.addItems(@LayoutRes layoutRes: Int, block: View.(T) -> Unit) = addItems(layoutRes, this, block)
    fun <T> Array<T>.addItems(@LayoutRes layoutRes: Int, block: View.(T) -> Unit) = addItems(layoutRes, this, block)

    inline fun <reified T : Enum<T>> addEnums(@LayoutRes layoutRes: Int, noinline block: View.(T) -> Unit) =
            T::class.java.enumConstants?.map { it }?.addItems(layoutRes, block)

    fun <T> addItems(@LayoutRes layoutRes: Int, list: Collection<T>, block: View.(T) -> Unit) =
            adapter.addItems(list.map { layoutRes to RVDynamicItem(it, block) })

    fun <T> addItems(@LayoutRes layoutRes: Int, list: Array<T>, block: View.(T) -> Unit) =
            adapter.addItems(list.map { layoutRes to RVDynamicItem(it, block) })

    inner class RVDynamicItem<T>(var data: T, private var itemRenderer: View.(T) -> Unit = {}) {
        internal fun renderItem(view: View) = view.itemRenderer(data!!)
        override fun toString(): String = data.toString()
    }

    inner class RVIAdapter : RecyclerView.Adapter<ViewHolder>() {
        private var list: MutableList<Pair<Int, RVDynamicItem<*>>> = mutableListOf()
        override fun getItemCount(): Int = list.size
        override fun onBindViewHolder(holder: ViewHolder, position: Int) = list[position].second.renderItem(holder.itemView)
        override fun getItemViewType(position: Int): Int = list[position].first
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder = ViewHolder(layoutInflater.inflate(viewType, parent, false))
        fun <T> addItem(item: Pair<Int, RVDynamicItem<T>>): T {
            val position = list.size
            list.add(item)
            notifyItemInserted(position)
            return item.second.data
        }

        fun <T> addItems(items: Collection<Pair<Int, RVDynamicItem<T>>>): List<T> {
            list.addAll(items)
            notifyDataSetChanged()
            return items.map { it.second.data }
        }

        fun removeItem(position: Int) {
            list.removeAt(position)
            notifyItemRemoved(position)
        }

        fun <T> removeItem(item: T) {
            list.removeAll { item == it.second.data }
            notifyDataSetChanged()
        }

        @Suppress("UNCHECKED_CAST")
        fun <T> removeItem(predicate: (T) -> Boolean) {
            list.removeAll { (it.second.data as? T)?.let(predicate) ?: false }
            notifyDataSetChanged()
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}

fun SettingsBuilder.recyclerViewGenericItem(block: RecyclerViewGenericItem.() -> Unit = {}) =
        RecyclerViewGenericItem(context).apply(block).buildItem()

fun SettingsBuilder.recyclerViewGenericItem(recycleSetup: RecyclerView.() -> Unit, block: RecyclerViewGenericItem.() -> Unit = {}) =
        RecyclerViewGenericItem(context, recycleSetup).apply(block).buildItem()

