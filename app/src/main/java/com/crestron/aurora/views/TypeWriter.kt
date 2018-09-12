package com.crestron.aurora.views

import android.content.Context
import android.graphics.Typeface
import android.os.Handler
import android.util.AttributeSet
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.utilities.ViewUtil

class TypeWriter : android.support.v7.widget.AppCompatTextView {

    private var mText: CharSequence? = null
    private var mIndex = 0
    private var mDelay: Long = 50 //Default 150ms delay

    private val mHandler = Handler()
    private val characterAdder = object : Runnable {
        override fun run() {
            text = mText!!.subSequence(0, mIndex++)
            if (mIndex <= mText!!.length) {
                mHandler.postDelayed(this, mDelay)
            }
        }
    }

    private val characterAdderSides = object : Runnable {
        override fun run() {
            val seq = ViewUtil.seq(" ")
            val endText = "${seq.take(mText!!.length - mIndex).joinToString("")}${mText!!.substring(mText!!.length - mIndex)}"
            val text = "${mText!!.subSequence(0, mIndex)}${endText.substring(mIndex)}"
            Loged.wtf(text)
            setText(text)
            mIndex++
            if (mIndex <= mText!!.length) {
                mHandler.postDelayed(this, mDelay)
            }
        }
    }

    private val characterRemoveSides = object : Runnable {
        override fun run() {
            val seq = ViewUtil.seq(" ")
            val endText = "${seq.take(mText!!.length - mIndex).joinToString("")}${mText!!.substring(mText!!.length - mIndex)}"
            val text = "${mText!!.subSequence(0, mIndex)}${endText.substring(mIndex)}"
            Loged.wtf(text)
            setText(text)
            mIndex--
            if (mIndex >= 0) {
                mHandler.postDelayed(this, mDelay)
            } else {
                mText = ""
                setText("")
            }
        }
    }

    private val characterRemover = object : Runnable {
        override fun run() {
            text = mText!!.subSequence(0, mIndex--)
            if (mIndex >= 0) {
                mHandler.postDelayed(this, mDelay)
            } else {
                mText = ""
                text = ""
            }
        }
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        setUp(context, attrs)
    }

    private fun setUp(context: Context, attrs: AttributeSet) {
        val ta = context.obtainStyledAttributes(attrs, R.styleable.TypeWriter)
        try {
            mDelay = ta.getInt(R.styleable.TypeWriter_typingSpeed, 50).toLong()
        } catch (ignored: Exception) {

        }

        typeface = Typeface.MONOSPACE

        ta.recycle()
    }

    private fun callbackRemovers() {
        mHandler.removeCallbacks(characterRemoveSides)
        mHandler.removeCallbacks(characterRemover)
        mHandler.removeCallbacks(characterAdderSides)
        mHandler.removeCallbacks(characterAdder)
    }

    fun animateText(text: CharSequence) {
        mText = text
        mIndex = 0
        setText("")
        callbackRemovers()
        mHandler.postDelayed(characterAdder, mDelay)
    }

    fun animateTextSides(text: CharSequence) {
        if (!mText.isNullOrEmpty() && mText!! == text) {
            Loged.wtf("Nope")
        } else {
            mText = text
            mIndex = 0
            setText("")
            callbackRemovers()
            mHandler.postDelayed(characterAdderSides, mDelay)
        }
    }

    fun removeText() {
        if (!mText.isNullOrEmpty()) {
            mIndex--
            callbackRemovers()
            mHandler.postDelayed(characterRemover, mDelay)
        }
    }

    fun removeTextSides() {
        if (!mText.isNullOrEmpty()) {
            mIndex = mText!!.length
            callbackRemovers()
            mHandler.postDelayed(characterRemoveSides, mDelay)
        }
    }

    fun setCharacterDelay(millis: Long) {
        mDelay = millis
    }
}