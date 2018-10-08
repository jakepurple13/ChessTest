package com.crestron.aurora.utilities

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityOptionsCompat
import android.view.View
import android.view.ViewAnimationUtils
import android.view.ViewTreeObserver
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import com.crestron.aurora.Loged
import kotlin.coroutines.experimental.buildSequence


class ViewUtil {
    companion object Utils {
        fun seq(character: CharSequence = "\u2007") = buildSequence {
            while (true)
                yield(character)
        }

        fun presentActivity(view: View, activity: Activity, intent: Intent) {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, view, "transition")
            val revealX = (view.x + view.width / 2).toInt()
            val revealY = (view.y + view.height / 2).toInt()

            intent.putExtra("EXTRA_CIRCULAR_REVEAL_X", revealX)
            intent.putExtra("EXTRA_CIRCULAR_REVEAL_Y", revealY)

            ActivityCompat.startActivity(activity, intent, options.toBundle())
        }

        fun revealing(rootLayout: View, intent: Intent, animationListener: Animator.AnimatorListener? = null) {

            rootLayout.visibility = View.INVISIBLE

            val revealX = intent.getIntExtra("EXTRA_CIRCULAR_REVEAL_X", 0)
            val revealY = intent.getIntExtra("EXTRA_CIRCULAR_REVEAL_Y", 0)

            val viewTreeObserver = rootLayout.viewTreeObserver
            if (viewTreeObserver.isAlive) {
                viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        revealActivity(revealX, revealY, rootLayout, animationListener)
                        rootLayout.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    }
                });
            }
        }

        fun revealActivity(x: Int, y: Int, rootLayout: View, animationListener: Animator.AnimatorListener?) {
            val finalRadius = (Math.max(rootLayout.width, rootLayout.height) * 1.1).toFloat()
            // create the animator for this view (the start radius is zero)
            val circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, x, y, 0f, finalRadius)
            circularReveal.duration = 400
            circularReveal.interpolator = AccelerateInterpolator()
            if (animationListener != null)
                circularReveal.addListener(animationListener)
            // make the view visible and start the animation
            rootLayout.visibility = View.VISIBLE
            circularReveal.start()
        }

        fun unRevealActivity(rootLayout: View, activity: Activity) {
            val revealX: Int = 0
            val revealY: Int = 0
            val finalRadius = (Math.max(rootLayout.width, rootLayout.height) * 1.1).toFloat()
            val circularReveal = ViewAnimationUtils.createCircularReveal(
                    rootLayout, revealX, revealY, finalRadius, 0f)
            circularReveal.setDuration(400)
            circularReveal.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootLayout.visibility = View.INVISIBLE
                    activity.finish()
                }
            })
            circularReveal.start()
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