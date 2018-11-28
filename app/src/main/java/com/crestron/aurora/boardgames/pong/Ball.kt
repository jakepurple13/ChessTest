package com.crestron.aurora.boardgames.pong

import android.graphics.RectF
import java.util.*

class Ball(screenX: Int, screenY: Int) {

    private var mRect: RectF
    private var mXVelocity: Float = 0.toFloat()
    private var mYVelocity: Float = 0.toFloat()
    private var mBallWidth: Float = 0.toFloat()
    private var mBallHeight: Float = 0.toFloat()

    init {
        // Make the mBall size relative to the screen resolution
        mBallWidth = (screenX / 100).toFloat()
        mBallHeight = mBallWidth
        /*Start the ball travelling straight up
        at a quarter of the screen height per second*/
        mYVelocity = (screenY / 4).toFloat()
        mXVelocity = mYVelocity
        // Initialize the Rect that represents the mBall
        mRect = RectF()
    }

    // Give access to the Rect
    fun getRect(): RectF {
        return mRect
    }

    // Change the position each frame
    fun update(fps: Long) {
        mRect.left = mRect.left + mXVelocity / fps
        mRect.top = mRect.top + mYVelocity / fps
        mRect.right = mRect.left + mBallWidth
        mRect.bottom = mRect.top - mBallHeight
    }

    // Reverse the vertical heading
    fun reverseYVelocity() {
        mYVelocity = -mYVelocity
    }

    // Reverse the horizontal heading
    fun reverseXVelocity() {
        mXVelocity = -mXVelocity
    }

    fun setRandomXVelocity() {

        // Generate a random number either 0 or 1
        val generator = Random()
        val answer = generator.nextInt(2)

        if (answer == 0) {
            reverseXVelocity()
        }
    }

    // Speed up by 10%
    // A score of over 20 is quite difficult
    // Reduce or increase 10 to make this easier or harder
    fun increaseVelocity() {
        mXVelocity += mXVelocity / 100
        mYVelocity += mYVelocity / 100
    }

    fun clearObstacleY(y: Float) {
        mRect.bottom = y
        mRect.top = y - mBallHeight
    }

    fun clearObstacleX(x: Float) {
        mRect.left = x
        mRect.right = x + mBallWidth
    }

    fun reset(x: Int, y: Int) {
        mRect.left = (x / 2).toFloat()
        mRect.top = (y - 20).toFloat()
        mRect.right = x / 2 + mBallWidth
        mRect.bottom = y - 20 - mBallHeight
    }

}