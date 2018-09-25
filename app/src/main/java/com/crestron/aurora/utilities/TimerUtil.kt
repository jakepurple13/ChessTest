package com.crestron.aurora.utilities

import android.widget.TextView
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.launch
import java.util.*
import java.util.concurrent.TimeUnit

class TimerUtil(var time: Long = 0L, var timeAction: TimerAction? = null, private val delay: Long = 1, private val period: Long = 1) {

    private var timer: Timer = Timer()

    fun startTimer(view: TextView? = null, upOrDown: Boolean = true) {
        cancel()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                launch(UI) {
                    time = setInterval(upOrDown)
                    view?.text = getTime()
                }
            }
        }, delay, period)
    }

    fun cancel() {
        timer.cancel()
        timer = Timer()
    }

    private fun setInterval(upOrDown: Boolean = true): Long {
        if(upOrDown) {
            ++time
        } else {
            --time
        }
        if(timeAction!=null) {
            timeAction!!.timeChange(time, this)
        }
        return time
    }

    fun getTime(): String {
        val m = TimeUnit.MILLISECONDS.toMinutes(time)
        val s = TimeUnit.MILLISECONDS.toSeconds(time - m * 60 * 1000)
        return String.format("%02d:%02d", m, s)
    }

    interface TimerAction {
        fun timeChange(time: Long, util: TimerUtil)
    }

}