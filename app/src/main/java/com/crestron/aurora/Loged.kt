package com.crestron.aurora

import android.util.Log
import java.util.*

/**
 * Created by Jacob on 10/3/17.
 */

object Loged {

    var SHOW_PRETTY: Boolean = true
    var WITH_THREAD_NAME: Boolean = true
    var FILTER_BY_CLASS_NAME = ""
    var TAG = "Loged"
    private const val HELPER_NAME = "Loged"

    private fun prettyLog(tag: String, msg: Any, level: Int, threadName: Boolean) {
        //the main message to be logged
        var logged = msg.toString()
        //the arrow for the stack trace
        val arrow = "${9552.toChar()}${9655.toChar()}\t"
        //the stack trace
        val stackTraceElement = Thread.currentThread().stackTrace

        val elements = listOf(*stackTraceElement)
        val wanted = ArrayList<StackTraceElement>()

        for (i in elements.indices) {
            if (elements[i].className.contains(FILTER_BY_CLASS_NAME) && !elements[i].className.contains(HELPER_NAME)) {
                wanted.add(elements[i])
            }
        }

        var loc = "\n"

        for (i in wanted.indices.reversed()) {
            val fullClassName = wanted[i].className
            //get the method name
            val methodName = wanted[i].methodName
            //get the file name
            val fileName = wanted[i].fileName
            //get the line number
            val lineNumber = wanted[i].lineNumber
            //add this to location in a format where we can click on the number in the console
            loc += "$fullClassName.$methodName($fileName:$lineNumber)"

            if (wanted.size > 1 && i - 1 >= 0) {
                val typeOfArrow: Char =
                        if (i - 1 > 0)
                            9568.toChar() //middle arrow
                        else
                            9562.toChar() //ending arrow
                loc += "\n\t$typeOfArrow$arrow"
            }
        }

        logged += loc

        Log.println(level, tag + if(threadName)  "/${Thread.currentThread().name}" else "", logged)
    }

    //---------------------------------EVERYTHING BELOW WORKS FINE----------------------------------

    private fun log(tag: String, msg: Any, level: Int, threadName: Boolean) {
        val stackTraceElement = Thread.currentThread().stackTrace
        var currentIndex = -1
        for (i in stackTraceElement.indices) {
            if (stackTraceElement[i].methodName.compareTo("log") == 0) {
                currentIndex = i + 1
                break
            }
        }
        currentIndex+=2

        val fullClassName = stackTraceElement[currentIndex].className
        val methodName = stackTraceElement[currentIndex].methodName
        val fileName = stackTraceElement[currentIndex].fileName
        val lineNumber = stackTraceElement[currentIndex].lineNumber
        val logged = "$msg\tat $fullClassName.$methodName($fileName:$lineNumber)"

        Log.println(level, tag + if(threadName)  "/${Thread.currentThread().name}" else "", logged)
    }

    //Console Log-------------------------------------------------

    /**
     * Error log
     *
     * @param msg the message to log
     */
    fun e(msg: Any, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
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
    fun i(msg: Any, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
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
    fun a(msg: Any, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
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
    fun wtf(msg: Any, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
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
    fun w(msg: Any, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
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
    fun d(msg: Any, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
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
    fun v(msg: Any, tag: String = TAG, showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME) {
        when (showPretty) {
            true -> prettyLog(tag, msg, Log.VERBOSE, threadName)
            false -> log(tag, msg, Log.VERBOSE, threadName)
        }
    }

}