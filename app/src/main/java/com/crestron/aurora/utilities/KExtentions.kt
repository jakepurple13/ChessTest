package com.crestron.aurora.utilities

import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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