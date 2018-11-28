package com.crestron.aurora.boardgames.pong

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import com.crestron.aurora.Loged
import java.io.IOException


class PongView(context: Context, x: Int, y: Int) : SurfaceView(context), Runnable {

    // This is our thread
    var mGameThread: Thread? = null

    // We need a SurfaceHolder object
    // We will see it in action in the draw method soon.
    var mOurHolder: SurfaceHolder? = null

    // A boolean which we will set and unset
    // when the game is running- or not
    // It is volatile because it is accessed from inside and outside the thread
    @Volatile
    var mPlaying: Boolean = false

    // Game is mPaused at the start
    var mPaused = true

    // A Canvas and a Paint object
    var mCanvas: Canvas? = null
    var mPaint: Paint

    // This variable tracks the game frame rate
    var mFPS: Long = 0

    // The size of the screen in pixels
    var mScreenX: Int = x
    var mScreenY: Int = y

    // The players mBat
    var mBat: Bat

    // A mBall
    var mBall: Ball

    // For sound FX
    var sp: SoundPool
    var beep1ID = -1
    var beep2ID = -1
    var beep3ID = -1
    var loseLifeID = -1

    // The mScore
    var mScore = 0

    // Lives
    var mLives = 3

    /*
    When the we call new() on pongView
    This custom constructor runs*/

    init {
        mOurHolder = holder
        mPaint = Paint()
        mBat = Bat(mScreenX, mScreenY)
        mBall = Ball(mScreenX, mScreenY)
        val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        sp = SoundPool.Builder()
                .setMaxStreams(5)
                .setAudioAttributes(audioAttributes)
                .build()
        try {
            // Create objects of the 2 required classes
            val assetManager = context.assets
            var descriptor: AssetFileDescriptor

            // Load our fx in memory ready for use
            /*descriptor = assetManager.openFd("beep1.ogg")
            beep1ID = sp.load(descriptor, 0)

            descriptor = assetManager.openFd("beep2.ogg")
            beep2ID = sp.load(descriptor, 0)

            descriptor = assetManager.openFd("beep3.ogg")
            beep3ID = sp.load(descriptor, 0)

            descriptor = assetManager.openFd("loseLife.ogg")
            loseLifeID = sp.load(descriptor, 0)

            descriptor = assetManager.openFd("explode.ogg")*/
            //explodeID = sp.load(descriptor, 0)

        } catch (e: IOException) {
            // Print an error message to the console
            Loged.e("failed to load sound files")
        }
        setupAndRestart()
    }

    fun setupAndRestart() {

        // Put the mBall back to the start
        mBall.reset(mScreenX, mScreenY)

        // if game over reset scores and mLives
        if (mLives == 0) {
            mScore = 0
            mLives = 3
        }

    }

    override fun run() {
        while (mPlaying) {

            // Capture the current time in milliseconds in startFrameTime
            val startFrameTime = System.currentTimeMillis()

            // Update the frame
            // Update the frame
            if (!mPaused) {
                update()
            }

            // Draw the frame
            draw()

            /*
            Calculate the FPS this frame
            We can then use the result to
            time animations in the update methods.
        */
            val timeThisFrame = System.currentTimeMillis() - startFrameTime
            if (timeThisFrame >= 1) {
                mFPS = 1000 / timeThisFrame
            }

        }

    }

    // Everything that needs to be updated goes in here
    // Movement, collision detection etc.
    fun update() {

        // Move the mBat if required
        mBat.update(mFPS)

        mBall.update(mFPS)

        // Check for mBall colliding with mBat
        if (RectF.intersects(mBat.getRect(), mBall.getRect())) {
            mBall.setRandomXVelocity();
            mBall.reverseYVelocity();
            mBall.clearObstacleY(mBat.getRect().top - 2);

            mScore++;
            mBall.increaseVelocity();

            sp.play(beep1ID, 1f, 1f, 0, 0, 1f);
        }

        // Bounce the mBall back when it hits the bottom of screen
        if (mBall.getRect().bottom > mScreenY) {
            mBall.reverseYVelocity();
            mBall.clearObstacleY(mScreenY - 2.toFloat());

            // Lose a life
            mLives--;
            sp.play(loseLifeID, 1f, 1f, 0, 0, 1f)

            if (mLives == 0) {
                mPaused = true;
                setupAndRestart();
            }
        }

        // Bounce the mBall back when it hits the top of screen
        if (mBall.getRect().top < 0) {
            mBall.reverseYVelocity();
            mBall.clearObstacleY(12f);

            sp.play(beep2ID, 1f, 1f, 0, 0, 1f);
        }

        // If the mBall hits left wall bounce
        if (mBall.getRect().left < 0) {
            mBall.reverseXVelocity();
            mBall.clearObstacleX(2f);

            sp.play(beep3ID, 1f, 1f, 0, 0, 1f);
        }

        // If the mBall hits right wall bounce
        if (mBall.getRect().right > mScreenX) {
            mBall.reverseXVelocity();
            mBall.clearObstacleX(mScreenX - 22f);

            sp.play(beep3ID, 1f, 1f, 0, 0, 1f);
        }

    }

    // Draw the newly updated scene
    fun draw() {

        // Make sure our drawing surface is valid or we crash
        if (mOurHolder!!.surface.isValid) {

            // Draw everything here

            // Lock the mCanvas ready to draw
            mCanvas = mOurHolder!!.lockCanvas()

            // Clear the screen with my favorite color
            mCanvas!!.drawColor(Color.rgb(69, 103, 227))

            // Choose the brush color for drawing
            mPaint.color = Color.argb(255, 255, 255, 255)

            // Draw the mBat
            mCanvas!!.drawRect(mBat.getRect(), mPaint)

            // Draw the mBall
            mCanvas!!.drawRect(mBall.getRect(), mPaint)


            // Change the drawing color to white
            mPaint.color = Color.argb(255, 255, 255, 255)

            // Draw the mScore
            mPaint.textSize = 40f
            mCanvas!!.drawText("Score: $mScore   Lives: $mLives", 10f, 50f, mPaint)

            // Draw everything to the screen
            mOurHolder!!.unlockCanvasAndPost(mCanvas)
        }

    }

    // If the Activity is paused/stopped
    // shutdown our thread.
    fun pause() {
        mPlaying = false
        try {
            mGameThread!!.join()
        } catch (e: InterruptedException) {
            Loged.e("joining thread")
        }

    }

    // If the Activity starts/restarts
    // start our thread.
    fun resume() {
        mPlaying = true
        mGameThread = Thread(this)
        mGameThread!!.start()
    }

    // The SurfaceView class implements onTouchListener
    // So we can override this method and detect screen touches.
    override fun onTouchEvent(motionEvent: MotionEvent): Boolean {

        when (motionEvent.action and MotionEvent.ACTION_MASK) {

            // Player has touched the screen
            MotionEvent.ACTION_DOWN -> {

                mPaused = false

                // Is the touch on the right or left?
                if (motionEvent.x > mScreenX / 2) {
                    mBat.setMovementState(mBat.RIGHT)
                } else {
                    mBat.setMovementState(mBat.LEFT)
                }
            }

            // Player has removed finger from screen
            MotionEvent.ACTION_UP -> mBat.setMovementState(mBat.STOPPED)
        }
        return true
    }

}