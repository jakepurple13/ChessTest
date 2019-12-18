package com.crestron.aurora

import androidx.annotation.IntRange

class Frame internal constructor(
        var top: String = "", var bottom: String = "",
        var left: String = "", var right: String = "",
        var topLeft: String = "", var topRight: String = "",
        var bottomLeft: String = "", var bottomRight: String = "",
        var topFillIn: String = "", var bottomFillIn: String = ""
)

enum class FrameType(val frame: Frame) {
    /**
     * BOX Frame
     * Will look like this
     *
    ```
    ╔==========================╗
    ║ Hello World              ║
    ╚==========================╝
    or
    If the top if modified
    ╔==========Hello===========╗
    ║ World                    ║
    ╚==========================╝
    or
    If the bottom is modified
    ╔==========================╗
    ║ World                    ║
    ╚==========Hello===========╝
    ```
     */
    BOX(Frame("=", "=", "║", "║", "╔", "╗", "╚", "╝", "=", "=")),
    /**
     * ASTERISK Frame
     * Will look like this
     *
    ```
    1.   ****************************
    2.   * Hello World              *
    3.   ****************************
    or
    If the top if modified
    1.   ***********Hello************
    2.   * World                    *
    3.   ****************************
    or
    If the bottom is modified
    1.   ****************************
    2.   * World                    *
    3.   ***********Hello************
    ```
     */
    ASTERISK(Frame("*", "*", "*", "*", "*", "*", "*", "*", "*", "*")),
    /**
     * Plus Frame
     * Will look like this
     *
    ```
    ++++++++++++++++++++++++++++
    + Hello World              +
    ++++++++++++++++++++++++++++
    or
    If the top if modified
    +++++++++++Hello++++++++++++
    + World                    +
    ++++++++++++++++++++++++++++
    or
    If the bottom is modified
    ++++++++++++++++++++++++++++
    + World                    +
    +++++++++++Hello++++++++++++
    ```
     */
    PLUS(Frame("+", "+", "+", "+", "+", "+", "+", "+", "+", "+")),
    /**
     * DIAGONAL Frame
     * Will look like this
     *
    ```
    ╱--------------------------╲
    | Hello World              |
    ╲--------------------------╱
    or
    If the top if modified
    ╱----------Hello-----------╲
    | World                    |
    ╲--------------------------╱
    or
    If the bottom is modified
    ╱--------------------------╲
    | World                    |
    ╲----------Hello-----------╱
    ```
     */
    DIAGONAL(Frame("-", "-", "│", "│", "╱", "╲", "╲", "╱", "-", "-")),
    /**
     * OVAL Frame
     * Will look like this
     *
    ```
    ╭--------------------------╮
    | Hello World              |
    ╰--------------------------╯
    or
    If the top if modified
    ╭----------Hello-----------╮
    | World                    |
    ╰--------------------------╯
    or
    If the bottom is modified
    ╭--------------------------╮
    | World                    |
    ╰----------Hello-----------╯
    ```
     */
    OVAL(Frame("-", "-", "│", "│", "╭", "╮", "╰", "╯", "-", "-")),
    /**
     * BOXED Frame
     * Will look like this
     *
    ```
    ▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜
    ▌ Hello World              ▐
    ▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟
    or
    If the top if modified
    ▛▀▀▀▀▀▀▀▀▀▀Hello▀▀▀▀▀▀▀▀▀▀▀▜
    ▌ World                    ▐
    ▙▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▟
    or
    If the bottom is modified
    ▛▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▜
    ▌ World                    ▐
    ▙▄▄▄▄▄▄▄▄▄▄Hello▄▄▄▄▄▄▄▄▄▄▄▟
    ```
     */
    BOXED(Frame("▀", "▄", "▌", "▐", "▛", "▜", "▙", "▟", "▀", "▄")),
    /**
     * CUSTOM Frame
     * You decide how all of it looks
     */
    CUSTOM(Frame());

    companion object {
        /**
         * Use this to create a custom [FrameType]
         */
        fun CUSTOM(frame: Frame.() -> Unit) = CUSTOM.frame.apply(frame)
    }
}

fun String.frame(frameType: FrameType, rtl: Boolean = false) = split("\n").frame(
        top = frameType.frame.top, bottom = frameType.frame.bottom,
        left = frameType.frame.left, right = frameType.frame.right,
        topLeft = frameType.frame.topLeft, topRight = frameType.frame.topRight,
        bottomLeft = frameType.frame.bottomLeft, bottomRight = frameType.frame.bottomRight,
        topFillIn = frameType.frame.topFillIn, bottomFillIn = frameType.frame.bottomFillIn, rtl = rtl
)

fun <T> Iterable<T>.frame(frameType: FrameType, rtl: Boolean = false, transform: (T) -> String = { it.toString() }) = frame(
        top = frameType.frame.top, bottom = frameType.frame.bottom,
        left = frameType.frame.left, right = frameType.frame.right,
        topLeft = frameType.frame.topLeft, topRight = frameType.frame.topRight,
        bottomLeft = frameType.frame.bottomLeft, bottomRight = frameType.frame.bottomRight,
        topFillIn = frameType.frame.topFillIn, bottomFillIn = frameType.frame.bottomFillIn, rtl = rtl, transform = transform
)

fun <T> Iterable<T>.frame(
        top: String, bottom: String,
        left: String, right: String,
        topLeft: String, topRight: String,
        bottomLeft: String, bottomRight: String,
        topFillIn: String = "", bottomFillIn: String = "",
        rtl: Boolean = false, transform: (T) -> String = { it.toString() }
): String {
    val fullLength = mutableListOf(top, bottom).apply { addAll(this@frame.map(transform)) }.maxBy { it.length }!!.length + 2
    val space: (String) -> String = { " ".repeat(fullLength - it.length - 1) }
    val mid = joinToString(separator = "\n") { "$left${if (rtl) space(transform(it)) else " "}$it${if (rtl) " " else space(transform(it))}$right" }
    val space2: (String, Boolean) -> String = { spacing, b -> (if (b) topFillIn else bottomFillIn).repeat((fullLength - spacing.length) / 2) }
    val topBottomText: (String, Boolean) -> String = { s, b ->
        if (s.length == 1) s.repeat(fullLength)
        else space2(s, b).let { spaced -> "$spaced$s${if ((fullLength - s.length) % 2 == 0) "" else (if (b) topFillIn else bottomFillIn)}$spaced" }
    }
    return "$topLeft${topBottomText(top, true)}$topRight\n$mid\n$bottomLeft${topBottomText(bottom, false)}$bottomRight"
}

internal fun frameLog(tag: String): FrameType.() -> Unit = {
    frame.top = tag
    frame.bottomLeft = "╠"
}

/**
 * [Loged.r] but adds a frame around the [msg] using the [String.frame] method
 */
fun Loged.f(
        msg: Any? = null, tag: String = TAG,
        infoText: String = TAG,
        showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME,
        @IntRange(from = 2, to = 7) vararg choices: Int = intArrayOf(2, 3, 4, 5, 6, 7)
) = r("${if (UNIT_TESTING) "" else "$infoText\n"}${msg.toString().frame(FrameType.BOX.apply(frameLog(tag)))}", tag, showPretty, threadName, *choices)

/**
 * [Loged.r] but adds a frame around the [msg] using the [Collection.frame] method
 */
fun Loged.f(
        msg: Iterable<*>, tag: String = TAG,
        infoText: String = TAG,
        showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME,
        @IntRange(from = 2, to = 7) vararg choices: Int = intArrayOf(2, 3, 4, 5, 6, 7)
) = r("${if (UNIT_TESTING) "" else "$infoText\n"}${msg.frame(FrameType.BOX.apply(frameLog(tag)))}", tag, showPretty, threadName, *choices)

/**
 * [Loged.r] but adds a frame around the [msg] using the [Iterable.frame] method
 */
fun <T> Loged.f(
        msg: Iterable<T>, tag: String = TAG,
        infoText: String = TAG,
        showPretty: Boolean = SHOW_PRETTY, threadName: Boolean = WITH_THREAD_NAME,
        @IntRange(from = 2, to = 7) vararg choices: Int = intArrayOf(2, 3, 4, 5, 6, 7),
        transform: (T) -> String = { it.toString() }
) = r("$infoText\n${msg.frame(FrameType.BOX.apply(frameLog(tag)), transform = transform)}", tag, showPretty, threadName, *choices)
