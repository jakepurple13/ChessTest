package com.crestron.aurora.boardgames.pong

import android.graphics.RectF


class Bat
// This is the constructor method
// When we create an object from this class we will pass
// in the screen width and mHeight
(x: Int, y: Int) {

    // RectF is an object that holds four coordinates - just what we need
    private var mRect: RectF

    // How long and high our mBat will be
    private var mLength: Float = 0.toFloat()
    private var mHeight: Float = 0.toFloat()

    // X is the far left of the rectangle which forms our mBat
    private var mXCoord: Float = 0.toFloat()

    // Y is the top coordinate
    private var mYCoord: Float = 0.toFloat()

    // This will hold the pixels per second speed that
    // the mBat will move
    private var mBatSpeed: Float = 0.toFloat()

    // Which ways can the mBat move
    val STOPPED = 0
    val LEFT = 1
    val RIGHT = 2

    // Is the mBat moving and in which direction
    private var mBatMoving = STOPPED

    // The screen length and width in pixels
    private var mScreenX: Int = x
    private var mScreenY: Int = y

    init {
        mScreenX = x
        mScreenY = y

        // 1/8 screen width wide
        mLength = mScreenX / 8.toFloat()

        // 1/25 screen mHeight high
        mHeight = mScreenY / 25.toFloat()

        // Start mBat in roughly the sceen centre
        mXCoord = mScreenX / 2.toFloat()
        mYCoord = mScreenY - 20.toFloat()

        mRect = RectF(mXCoord, mYCoord, mXCoord + mLength, mYCoord + mHeight)

        // How fast is the mBat in pixels per second
        mBatSpeed = mScreenX.toFloat()
        // Cover entire screen in 1 second
    }

    // This is a getter method to make the rectangle that
    // defines our bat available in PongView class
    fun getRect(): RectF {
        return mRect
    }

    // This method will be used to change/set if the mBat is going
    // left, right or nowhere
    fun setMovementState(state: Int) {
        mBatMoving = state
    }

    // This update method will be called from update in PongView
    // It determines if the Bat needs to move and changes the coordinates
    // contained in mRect if necessary
    fun update(fps: Long) {

        if (mBatMoving == LEFT) {
            mXCoord -= mBatSpeed / fps
        }

        if (mBatMoving == RIGHT) {
            mXCoord += mBatSpeed / fps
        }

        // Make sure it's not leaving screen
        if (mRect.left < 0) {
            mXCoord = 0.toFloat()
        }
        if (mRect.right > mScreenX) {
            mXCoord = mScreenX -
                    // The width of the Bat
                    (mRect.right - mRect.left)
        }

        // Update the Bat graphics
        mRect.left = mXCoord
        mRect.right = mXCoord + mLength
    }

}