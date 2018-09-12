package com.crestron.aurora.boardgames.checkers

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.widget.Button
import android.widget.GridLayout
import android.widget.TableLayout
import android.widget.TableRow
import com.crestron.aurora.R
import kotlinx.android.synthetic.main.activity_checkers.*

class CheckersActivity : AppCompatActivity() {

    lateinit var boarded: Array<Array<Button?>>
    lateinit var backButton: Button
    var selX = 0
    var selY = 0
    lateinit var buttonSel: Button
    var colorValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkers)

        setUp()

    }

    private fun setUp() {
        boarded = Array(8) { arrayOfNulls<Button>(8) }
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val tr1 = TableRow(this)
        backButton = Button(this)
        backButton.text = "Back"
        backButton.setOnClickListener { finish() }
        val lp = TableLayout.LayoutParams()
        //lp.gravity = 17
        lp.height = height / 9
        lp.width = width / 8
        tr1.addView(backButton, width / 8, height / 9)
        checkersLayout.addView(tr1)
        for (i in 0 until 8) {
            val tr = TableRow(this)
            for (j in 0 until 8) {
                boarded[i][j] = Button(this)
                if ((i % 2 == 0 && j % 2 == 0) || (i % 2 == 1 && j % 2 == 1)) {
                    boarded[i][j]!!.setBackgroundColor(Color.DKGRAY)
                } else {
                    boarded[i][j]!!.setBackgroundColor(Color.GRAY)
                }
                boarded[i][j]!!.text = "X"
                boarded[i][j]!!.tag = "$i$j"
                boarded[i][j]!!.width = GridLayout.LayoutParams.WRAP_CONTENT
                boarded[i][j]!!.height = GridLayout.LayoutParams.WRAP_CONTENT
                boarded[i][j]!!.setOnLongClickListener {

                    false
                }
                boarded[i][j]!!.setOnClickListener {
                    val sss: String = it.tag as String


                }
                tr.addView(boarded[i][j], width / 8, height / 12)
            }
            checkersLayout.addView(tr)
        }
        buttonSel = boarded[0][0]!!
        if (buttonSel.background is ColorDrawable)
            colorValue = (buttonSel.background as ColorDrawable).color
    }

}
