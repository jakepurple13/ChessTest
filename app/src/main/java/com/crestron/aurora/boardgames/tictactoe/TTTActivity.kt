package com.crestron.aurora.boardgames.tictactoe

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import com.crestron.aurora.R
import kotlinx.android.synthetic.main.activity_ttt.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TTTActivity : AppCompatActivity() {

    var turn = false
    lateinit var boarded: Array<Array<Button?>>
    private var TTTBoard = TTTBoard()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ttt)

        boarded = Array(3) { arrayOfNulls<Button>(3) }

        fun updateViews() {
            for (i in 0..2) {
                for (j in 0..2) {
                    boarded[i][j]!!.text = TTTBoard.toArray()[i][j].name
                }
            }
        }

        fun listener(x: Int, y: Int) = View.OnClickListener {
            if (it.isEnabled) {
                it.isEnabled = false
                try {
                    if (TTTBoard.move(x, y)) {
                        GlobalScope.launch(Dispatchers.Main) {
                            delay(1500)
                            AI.run(TTTBoard.turn, TTTBoard, 2.0)
                        }
                    }
                } catch(e: IllegalArgumentException) {
                    e.printStackTrace()
                }
                updateViews()
            }
        }

        topLeft.setOnClickListener(listener(0, 0))
        topMid.setOnClickListener(listener(0, 1))
        topRight.setOnClickListener(listener(0, 2))

        midLeft.setOnClickListener(listener(1, 0))
        midMid.setOnClickListener(listener(1, 1))
        midRight.setOnClickListener(listener(1, 2))

        bottomLeft.setOnClickListener(listener(2, 0))
        bottomMid.setOnClickListener(listener(2, 1))
        bottomRight.setOnClickListener(listener(2, 2))

        boarded[0][0] = topLeft
        boarded[0][1] = topMid
        boarded[0][2] = topRight

        boarded[1][0] = midLeft
        boarded[1][1] = midMid
        boarded[1][2] = midRight

        boarded[2][0] = bottomLeft
        boarded[2][1] = bottomMid
        boarded[2][2] = bottomRight

    }

    fun hasWon(): Boolean {
        //horizontal
        for (i in 0..2) {
            if (boarded[i][0]!!.text == boarded[i][1]!!.text && boarded[i][1]!!.text == boarded[i][2]!!.text) {
                return true
            }
        }
        //vertical
        for (i in 0..2) {
            if (boarded[0][i]!!.text == boarded[1][i]!!.text && boarded[1][i]!!.text == boarded[2][i]!!.text) {
                return true
            }
        }
        //diagonal
        return boarded[0][0]!!.text == boarded[1][1]!!.text && boarded[1][1]!!.text == boarded[2][2]!!.text
                || boarded[2][0]!!.text == boarded[1][1]!!.text && boarded[1][1]!!.text == boarded[0][2]!!.text
    }

}
