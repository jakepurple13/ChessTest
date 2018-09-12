package com.crestron.aurora.utilities

import android.os.Handler
import android.widget.TextView
import com.crestron.aurora.Loged
import kotlin.coroutines.experimental.buildSequence

class ViewUtil {
    companion object Utils {
        fun seq(character: CharSequence = "\u2007") = buildSequence {
            while (true)
                yield(character)
        }
    }
}

interface TypingAnimationListener {
    fun finishedTyping(text: CharSequence = "") {}
}

class TypingAnimation(private val view: TextView, var mDelay: Long = 50) {
    private var mText: CharSequence? = view.text
    private var mIndex = 0
    private val mHandler = Handler()
    private var listener: TypingAnimationListener = object : TypingAnimationListener {}

    private fun Handler.postDelay(runnable: Runnable, delayMillis: Long, listener: TypingAnimationListener) {
        this@TypingAnimation.listener = listener
        postDelayed(runnable, delayMillis)
    }

    private val characterAdder = object : Runnable {
        override fun run() {
            view.text = mText!!.subSequence(0, mIndex++)
            if (mIndex <= mText!!.length) {
                mHandler.postDelayed(this, mDelay)
            } else {
                listener.finishedTyping(view.text)
            }
        }
    }

    private val characterAdderSides = object : Runnable {
        override fun run() {
            val seq = ViewUtil.seq(" ")
            val endText = "${seq.take(mText!!.length - mIndex).joinToString("")}${mText!!.substring(mText!!.length - mIndex)}"
            val text = "${mText!!.subSequence(0, mIndex)}${endText.substring(mIndex)}"
            //Loged.wtf(text)
            view.text = text
            mIndex++
            if (mIndex <= mText!!.length) {
                mHandler.postDelayed(this, mDelay)
            } else {
                listener.finishedTyping(view.text)
            }
        }
    }

    private val characterRemoveSides = object : Runnable {
        override fun run() {
            val seq = ViewUtil.seq(" ")
            val endText = "${seq.take(mText!!.length - mIndex).joinToString("")}${mText!!.substring(mText!!.length - mIndex)}"
            val text = "${mText!!.subSequence(0, mIndex)}${endText.substring(mIndex)}"
            Loged.wtf(text)
            view.text = text
            mIndex--
            if (mIndex >= 0) {
                mHandler.postDelayed(this, mDelay)
            } else {
                mText = ""
                view.text = ""
                listener.finishedTyping()
            }
        }
    }

    private val characterRemover = object : Runnable {
        override fun run() {
            view.text = mText!!.subSequence(0, mIndex--)
            if (mIndex >= 0) {
                mHandler.postDelayed(this, mDelay)
            } else {
                mText = ""
                view.text = ""
                listener.finishedTyping()
            }
        }
    }

    private fun callbackRemovers() {
        mHandler.removeCallbacks(characterRemoveSides)
        mHandler.removeCallbacks(characterRemover)
        mHandler.removeCallbacks(characterAdderSides)
        mHandler.removeCallbacks(characterAdder)
    }

    fun animateText(text: CharSequence, listener: TypingAnimationListener = object : TypingAnimationListener {}) {
        mText = text
        mIndex = 0
        view.text = ""
        callbackRemovers()
        mHandler.postDelay(characterAdder, mDelay, listener)
    }

    fun animateTextSides(text: CharSequence, listener: TypingAnimationListener = object : TypingAnimationListener {}) {
        if (!mText.isNullOrEmpty() && mText!! == text) {
            Loged.wtf("Nope")
        } else {
            mText = text
            mIndex = 0
            view.text = ""
            callbackRemovers()
            mHandler.postDelay(characterAdderSides, mDelay, listener)
        }
    }

    fun removeText(listener: TypingAnimationListener = object : TypingAnimationListener {}) {
        if (!mText.isNullOrEmpty()) {
            mIndex = mText!!.length - 1
            callbackRemovers()
            mHandler.postDelay(characterRemover, mDelay, listener)
        }
    }

    fun removeTextSides(listener: TypingAnimationListener = object : TypingAnimationListener {}) {
        if (!mText.isNullOrEmpty()) {
            mIndex = mText!!.length
            callbackRemovers()
            mHandler.postDelay(characterRemoveSides, mDelay, listener)
        }
    }
}