package com.crestron.aurora.boardgames.pong

import android.graphics.Point
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Window
import android.view.WindowManager

class PongActivity : AppCompatActivity() {

    // pongView will be the view of the game
    // It will also hold the logic of the game
    // and respond to screen touches as well
    lateinit var pongView: PongView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get a Display object to access screen details
        val display = windowManager.defaultDisplay

        // Load the resolution into a Point object
        val size = Point()
        display.getSize(size)

        // Initialize pongView and set it as the view
        pongView = PongView(this, size.x, size.y)
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(pongView)
    }

    // This method executes when the player starts the game
    override fun onResume() {
        super.onResume()

        // Tell the pongView resume method to execute
        pongView.resume()
    }

    // This method executes when the player quits the game
    override fun onPause() {
        super.onPause()

        // Tell the pongView pause method to execute
        pongView.pause()
    }
}
