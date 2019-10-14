package com.crestron.aurora.utilities

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.GradientDrawable.RECTANGLE
import android.view.View
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.core.view.doOnPreDraw
import androidx.recyclerview.widget.*
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Finds similarities between two lists based on a predicate
 */
fun <T, U> List<T>.intersect(uList: List<U>, filterPredicate: (T, U) -> Boolean) =
        filter { m -> uList.any { filterPredicate(m, it) } }

fun <T> MutableList<T>.randomRemove(): T {
    return removeAt(Random.nextInt(0, size))
}

fun joinWith(vararg args: String, separator: String = " "): String = String.format("%s$separator".repeat(args.size).removeSuffix(separator).trimEnd(), *args)

fun <T> joinWith(vararg args: T, separator: String = " ", transform: (T) -> String = { it.toString() }): String =
        String.format("%s$separator".repeat(args.size).removeSuffix(separator).trimEnd(), *args.map(transform).toTypedArray())

fun <T> Collection<T>.joinWith(separator: String = " ", transform: (T) -> String = { it.toString() }): String =
        String.format("%s$separator".repeat(size).removeSuffix(separator).trimEnd(), *map(transform).toTypedArray())

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

inline fun <reified T> SharedPreferences.getObject(key: String): T? = try {
    Gson().fromJson(getString(key, null), T::class.java)
} catch (e: Exception) {
    null
}

inline fun <reified T> SharedPreferences.getObject(key: String, defaultValue: T): T = try {
    Gson().fromJson(getString(key, null), T::class.java)
} catch (e: Exception) {
    defaultValue
}
