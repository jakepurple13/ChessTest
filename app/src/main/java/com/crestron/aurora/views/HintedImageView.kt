package com.crestron.aurora.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Toast

class HintedImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ImageView(context, attrs), View.OnLongClickListener {

    init {
        setOnLongClickListener(this)
    }

    private var toastLength = Toast.LENGTH_SHORT

    fun setToastLength(length: Int): HintedImageView {
        toastLength = length
        return this
    }

    override fun onLongClick(v: View?): Boolean {
        showHint()
        return true
    }

    fun showHint() {
        val positions = IntArray(2)
        getLocationOnScreen(positions)

        val xOffset = positions[0] - contentDescription.length / 6
        val yOffset = positions[1] - 128
        Toast.makeText(context, contentDescription, toastLength).apply {
            setGravity(Gravity.TOP or Gravity.LEFT, xOffset, yOffset)
            show()
        }
    }

}