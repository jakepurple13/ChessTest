package com.crestron.aurora

import android.util.Log
import androidx.annotation.IntRange

/**
 * Created by Jacob on 10/3/17.
 */
object Loged {

    var SHOW_PRETTY: Boolean = true
    var WITH_THREAD_NAME: Boolean = true
    var FILTER_BY_CLASS_NAME = ""
    var TAG = "Loged"
    private const val HELPER_NAME = "Loged"

    private fun prettyLog(tag: String, msg: Any?, level: Int, threadName: Boolean) {
        //the stack trace
        val wanted = Thread.currentThread().stackTrace
                .filter { it.className.contains(FILTER_BY_CLASS_NAME) && !it.className.contains(HELPER_NAME) }
                .reversed()
                .map { "${it.className}.${it.methodName}(${it.fileName}:${it.lineNumber})" }
        var loc = "\n╚═▷\t"
        for (i in wanted.indices.reversed()) {
            loc += wanted[i]
            //doing a future check
            if (wanted.size > 1 && i - 1 >= 0) {
                loc += "\n\t${if (i - 1 > 0) '╠' else '╚'}═▷\t"
            }
        }
        Log.println(level, tag + if (threadName) "/${Thread.currentThread().name}" else "", "${msg.toString()}$loc")
    }

    //---------------------------------EVERYTHING BELOW WORKS FINE----------------------------------

    private fun log(tag: String, msg: Any?, level: Int, threadName: Boolean) {
        val stackTraceElement = Thread.currentThread().stackTrace
        val currentIndex = stackTraceElement.indexOfFirst { it.methodName.compareTo("log") == 0 } + 3
        val logged = with(stackTraceElement[currentIndex]) { "$msg\n╚═▷\t$className.$methodName($fileName:$lineNumber)" }
        Log.println(level, tag + if (threadName) "/${Thread.currentThread().name}" else "", logged)
    }

    //Console Log-------------------------------------------------

    /**
     * Error log
     *
     * @param msg the message to log
     */
    fun e(msg: Any? = null, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
        when (showPretty) {
            true -> prettyLog(tag, msg, Log.ERROR, threadName)
            false -> log(tag, msg, Log.ERROR, threadName)
        }
    }

    /**
     * Info log
     *
     * @param msg the message to log
     */
    fun i(msg: Any? = null, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
        when (showPretty) {
            true -> prettyLog(tag, msg, Log.INFO, threadName)
            false -> log(tag, msg, Log.INFO, threadName)
        }
    }

    /**
     * Assert log
     *
     * @param msg the message to log
     */
    fun a(msg: Any? = null, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
        when (showPretty) {
            true -> prettyLog(tag, msg, Log.ASSERT, threadName)
            false -> log(tag, msg, Log.ASSERT, threadName)
        }
    }

    /**
     * What a Terrible Failure log
     *
     * @param msg the message to log
     */
    fun wtf(msg: Any? = null, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
        when (showPretty) {
            true -> prettyLog(tag, msg, Log.ASSERT, threadName)
            false -> log(tag, msg, Log.ASSERT, threadName)
        }
    }

    /**
     * Warning log
     *
     * @param msg the message to log
     */
    fun w(msg: Any? = null, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
        when (showPretty) {
            true -> prettyLog(tag, msg, Log.WARN, threadName)
            false -> log(tag, msg, Log.WARN, threadName)
        }
    }

    /**
     * Debug log
     *
     * @param msg the message to log
     */
    fun d(msg: Any? = null, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
        when (showPretty) {
            true -> prettyLog(tag, msg, Log.DEBUG, threadName)
            false -> log(tag, msg, Log.DEBUG, threadName)
        }
    }

    /**
     * Verbose log
     *
     * @param msg the message to log
     */
    fun v(msg: Any? = null, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
        when (showPretty) {
            true -> prettyLog(tag, msg, Log.VERBOSE, threadName)
            false -> log(tag, msg, Log.VERBOSE, threadName)
        }
    }

    /**
     * Random log
     *
     * @param msg the message to log
     * @param choices the different priority levels. MUST be between 2-7
     *  ### [Log.VERBOSE] = 2
     *  ### [Log.DEBUG] = 3
     *  ### [Log.INFO] = 4
     *  ### [Log.WARN] = 5
     *  ### [Log.ERROR] = 6
     *  ### [Log.ASSERT] = 7
     */
    fun r(
            msg: Any? = null,
            tag: String = TAG,
            showPretty: Boolean = SHOW_PRETTY,
            threadName: Boolean = WITH_THREAD_NAME,
            @IntRange(from = 2, to = 7) vararg choices: Int = intArrayOf(2, 3, 4, 5, 6, 7)
    ) {
        when (showPretty) {
            true -> prettyLog(tag, msg, choices.random(), threadName)
            false -> log(tag, msg, choices.random(), threadName)
        }
    }
}
