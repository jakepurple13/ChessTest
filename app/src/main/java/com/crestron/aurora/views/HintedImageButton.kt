package com.crestron.aurora.views

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageButton
import android.widget.Toast

class HintedImageButton @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : ImageButton(context, attrs), View.OnLongClickListener {

    init {
        setOnLongClickListener(this)
    }

    private var toastLength = Toast.LENGTH_LONG

    fun setToastLength(length: Int): HintedImageButton {
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