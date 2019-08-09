package com.crestron.aurora.utilities

import android.graphics.Color
import androidx.annotation.IntRange
import androidx.recyclerview.widget.RecyclerView
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
    return if(s.find()) {
        s.group(1)
    } else {
        null
    }
}

fun String.findRegex(string: String): String? {
    val s = toRegex().toPattern().matcher(string)
    return if(s.find()) {
        s.group(1)
    } else {
        null
    }
}


