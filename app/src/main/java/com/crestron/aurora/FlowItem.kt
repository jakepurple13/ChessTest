package com.crestron.aurora

import android.view.View
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class FlowItem<T>(startingValue: T, capacity: Int = 1) {
    private val itemBroadcast = BroadcastChannel<T>(capacity)
    private val itemFlow = itemBroadcast.asFlow().onStart { emit(startingValue) }
    private var flowItem: T = startingValue
        set(value) {
            field = value
            itemBroadcast.sendLaunch(value)
        }

    fun collect(action: suspend (value: T) -> Unit) = itemFlow.flowQuery(action)
    fun collectOnUI(action: (value: T) -> Unit) = itemFlow.flowQuery { GlobalScope.launch(Dispatchers.Main) { action(it) } }
    fun <R : View> bindToUI(view: R, action: R.(T) -> Unit) = itemFlow.flowQuery { view.post { view.action(it) } }
    fun getFlow() = itemFlow
    operator fun invoke() = getValue()
    operator fun invoke(value: T) = setValue(value)
    fun getValue() = flowItem
    fun setValue(value: T) = run { flowItem = value }
    private fun <T> Flow<T>.flowQuery(block: suspend (T) -> Unit) = GlobalScope.launch { collect(action = block) }
}

fun <T> T.asFlowItem() = FlowItem(this)
fun <T> SendChannel<T>.sendLaunch(value: T) = GlobalScope.launch { send(value) }
