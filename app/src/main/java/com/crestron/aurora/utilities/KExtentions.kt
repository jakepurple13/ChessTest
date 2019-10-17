package com.crestron.aurora.utilities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.RECTANGLE
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Finds similarities between two lists based on a predicate
 */
fun <T, U> List<T>.intersect(uList: List<U>, filterPredicate: (T, U) -> Boolean) = filter { m -> uList.any { filterPredicate(m, it) } }

fun <T> MutableList<T>.randomRemove(): T = removeAt(Random.nextInt(size))

fun joinWith(vararg args: String, separator: String = " "): String = String.format("%s$separator".repeat(args.size).removeSuffix(separator), *args)

fun <T> joinWith(vararg args: T, separator: String = " ", transform: (T) -> String = { it.toString() }): String =
        String.format("%s$separator".repeat(args.size).removeSuffix(separator), *args.map(transform).toTypedArray())

fun <T> Collection<T>.joinWith(separator: String = " ", transform: (T) -> String = { it.toString() }): String =
        String.format("%s$separator".repeat(size).removeSuffix(separator), *map(transform).toTypedArray())

fun RecyclerView.smoothScrollAction(
        position: Int,
        delay: Long = if (adapter!!.itemCount / 10 < 250) 250 else adapter!!.itemCount / 10L,
        action: () -> Unit
) = GlobalScope.launch {
    smoothScrollToPosition(position)
    delay(delay)
    stopScroll()
    GlobalScope.launch(Dispatchers.Main) {
        scrollToPosition(position)
    }
    delay(100)
    action()
}

fun RecyclerView.scrollAction(position: Int, action: () -> Unit) = GlobalScope.launch {
    GlobalScope.launch(Dispatchers.Main) {
        scrollToPosition(position)
    }
    delay(100)
    action()
}

/**
 * returns a random color
 */
fun Random.nextColor(
        @IntRange(from = 0, to = 255) alpha: Int = nextInt(0, 255),
        @IntRange(from = 0, to = 255) red: Int = nextInt(0, 255),
        @IntRange(from = 0, to = 255) green: Int = nextInt(0, 255),
        @IntRange(from = 0, to = 255) blue: Int = nextInt(0, 255)
): Int = Color.argb(alpha, red, green, blue)

fun String.regex(regex: String): String? {
    val s = regex.toRegex().toPattern().matcher(this)
    return if (s.find()) {
        s.group(1)
    } else {
        null
    }
}

fun String.findRegex(string: String): String? {
    val s = toRegex().toPattern().matcher(string)
    return if (s.find()) {
        s.group(1)
    } else {
        null
    }
}

fun String.isBlankOrEmpty(): Boolean = isBlank() || isEmpty()

fun TextView.setMaxLinesToEllipsize() = doOnPreDraw {
    val numberOfCompletelyVisibleLines = (measuredHeight - paddingTop - paddingBottom) / lineHeight
    maxLines = numberOfCompletelyVisibleLines
}

fun <T> recyclerDeleteItemWithUndo(adapterList: MutableList<T>, adapter: RecyclerView.Adapter<*>, positionForDeletion: Int, rootLayout: View) {
    val deleted = adapterList.removeAt(positionForDeletion)
    adapter.notifyItemRemoved(positionForDeletion)
    adapter.notifyItemRangeChanged(positionForDeletion, adapter.itemCount)
    Snackbar.make(rootLayout, "Item removed!", Snackbar.LENGTH_LONG)
            .setAction("Undo") {
                adapterList.add(positionForDeletion, deleted)
                adapter.notifyItemInserted(positionForDeletion)
            }.show()
}

fun Context.dp2px(dpValue: Float): Int {
    return (dpValue * resources.displayMetrics.density + 0.5f).toInt()
}

fun Context.dp2px(dpValue: Int): Int {
    return (dpValue * resources.displayMetrics.density + 0.5f).toInt()
}

fun View.dp2px(dpValue: Float): Int? {
    return context?.dp2px(dpValue)
}

fun View.dp2px(dpValue: Int): Int? {
    return context?.dp2px(dpValue)
}

fun RecyclerView.divider(color: Int = Color.parseColor("#CCCCCC"), size: Int = 1): RecyclerView {
    val decoration = DividerItemDecoration(context, orientation)
    decoration.setDrawable(GradientDrawable().apply {
        setColor(color)
        shape = RECTANGLE
        val dpSize = dp2px(size)
        dpSize?.let {
            setSize(dpSize, dpSize)
        }
    })
    addItemDecoration(decoration)
    return this
}

inline val RecyclerView.orientation
    get() = if (layoutManager == null) -1 else layoutManager.run {
        when (this) {
            is LinearLayoutManager -> orientation
            is GridLayoutManager -> orientation
            is StaggeredGridLayoutManager -> orientation
            else -> -1
        }
    }

fun <T> SharedPreferences.Editor.putObject(key: String, value: T): SharedPreferences.Editor = putString(key, Gson().toJson(value))

inline fun <reified T> SharedPreferences.getObject(key: String, defaultValue: T? = null): T? = try {
    Gson().fromJson(getString(key, null), T::class.java)
} catch (e: Exception) {
    defaultValue
}

inline fun <reified T> SharedPreferences.getCollection(key: String, defaultValue: T? = null): T? = try {
    Gson().fromJson(getString(key, null), object : TypeToken<T>() {}.type)
} catch (e: Exception) {
    defaultValue
}

/**
 * Close keyboard when ENTER ic clicked, clears focus from view and calls [InputMethodManager.hideSoftInputFromWindow]
 */
fun EditText.closeKeyboardOnEnter() {
    setOnKeyListener { _, keyCode, event ->
        if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN) {
            hideKeyboard()
            return@setOnKeyListener true
        }
        false
    }
}

/**
 * Clears focus from view and calls [InputMethodManager.hideSoftInputFromWindow]
 */
fun View.hideKeyboard() {
    clearFocus()
    (context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    requestFocus()
    (context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(this, InputMethodManager.SHOW_FORCED)
}

internal val romanValues = listOf(
        Pair("M", 1000),
        Pair("CM", 900),
        Pair("D", 500),
        Pair("CD", 400),
        Pair("C", 100),
        Pair("XC", 90),
        Pair("L", 50),
        Pair("XL", 40),
        Pair("X", 10),
        Pair("IX", 9),
        Pair("V", 5),
        Pair("IV", 4),
        Pair("I", 1)
)

/** Returns the Roman Numeral representation of this integer. Returns null for non-positive numbers */
val Int.romanNumeral: String?
    get() {
        var romanValue: String? = null
        // this calculation is only valid for positive numbers
        if (this > 0) {
            romanValue = ""
            var startingValue = this
            romanValues.forEach {
                val (romanChar, arabicValue) = it
                val div = startingValue / arabicValue
                if (div > 0) {
                    for (i in 1..div) {
                        romanValue += romanChar
                    }
                    startingValue -= arabicValue * div
                }
            }
        }
        return romanValue
    }

inline fun <T> T?.otherWise(nullBlock: () -> T) = this ?: nullBlock()


