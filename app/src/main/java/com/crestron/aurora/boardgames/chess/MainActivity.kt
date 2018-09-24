package com.crestron.aurora.boardgames.chess

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.view.View
import android.widget.*
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay

class MainActivity : AppCompatActivity() {

    lateinit var boarded: Array<Array<Button?>>
    val board = Board()
    var locHold = false
    var locationHold: Location? = null
    var turn = true
    lateinit var tv: TextView
    var selX = 0
    var selY = 0
    lateinit var buttonSel: Button
    lateinit var backButton: Button
    var colorValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Loged.SHOW_PRETTY = true
        Loged.FILTER_BY_CLASS_NAME = baseContext.packageName

        fun playing() = async(UI) {

            board.fromFEN("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1")
            delay(1000)
            board.fromFEN("rnbqkbnr/ppppppp1/8/7p/4P3/8/PPPP1PPP/RNBQKBNR w KQkq h6 0 2")
            delay(1000)
            board.fromFEN("rnbqkbnr/ppppppp1/8/7p/4P3/5Q2/PPPP1PPP/RNB1KBNR b KQkq - 1 2")
            delay(1000)
            board.fromFEN("rnbqkbnr/1pppppp1/8/p6p/4P3/5Q2/PPPP1PPP/RNB1KBNR w KQkq a6 0 3")
            delay(1000)
            board.fromFEN("rnbqkbnr/1pppppp1/8/p6p/2B1P3/5Q2/PPPP1PPP/RNB1K1NR b KQkq - 1 3")
            delay(1000)
            board.fromFEN("rnbqkbnr/2ppppp1/8/pp5p/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq b6 0 4")
            delay(1000)
            board.fromFEN("rnbqkbnr/2pppQp1/8/pp5p/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 4")
            true
        }

        //tv = findViewById(R.id.textView) as TextView

        println("\n" + board.toString())

        //tv.text = board.toString()

        boarded = Array(8) { arrayOfNulls<Button>(8) }

        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val height = displayMetrics.heightPixels
        val width = displayMetrics.widthPixels

        val tr1 = TableRow(this)
        backButton = Button(this)
        backButton.text = "Back"
        backButton.setOnClickListener { finish() }
        tv = TextView(this)
        tv.textAlignment = View.TEXT_ALIGNMENT_CENTER
        val lp = TableLayout.LayoutParams()
        //lp.gravity = 17
        lp.height = height / 9
        lp.width = width / 8
        tr1.addView(backButton, width / 8, height / 9)
        tr1.addView(tv, width / 8, height / 9)
        layout.addView(tr1)
        for (i in 0 until 8) {
            val tr = TableRow(this)
            for (j in 0 until 8) {
                boarded[i][j] = Button(this)
                if ((i % 2 == 0 && j % 2 == 0) || (i % 2 == 1 && j % 2 == 1)) {
                    boarded[i][j]!!.setBackgroundColor(Color.DKGRAY)
                } else {
                    boarded[i][j]!!.setBackgroundColor(Color.GRAY)
                }
                boarded[i][j]!!.text = "${board.board[i][j].icon}"
                boarded[i][j]!!.tag = "$i$j"
                boarded[i][j]!!.width = GridLayout.LayoutParams.WRAP_CONTENT
                boarded[i][j]!!.height = GridLayout.LayoutParams.WRAP_CONTENT
                boarded[i][j]!!.setOnLongClickListener {
                    //val b: com.github.bhlangonijr.chesslib.Board = com.github.bhlangonijr.chesslib.Board()
                    //board.fromFEN("rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2")
                    //playing()
                    false
                }
                boarded[i][j]!!.setOnClickListener {
                    val sss: String = it.tag as String
                    val l = Location.toLocation(sss)
                    Loged.i("$l")
                    if (locHold && board.getLocation(locationHold).checkMove(l)) {
                        Loged.wtf("We are able to move? ${board.getLocation(locationHold).checkMove(l)}")
                        Loged.d("Moved ${board.getLocation(locationHold)} from $locationHold" +
                                " to $l")
                        tv.text = board.move(locationHold, l, turn)
                        locHold = false
                        locationHold = null
                        Loged.e("-----\n" + board.toString())
                        turn = !turn
                        //tv.text = aiMove(board.boards, board)

                        /*fun waitMove() = async {
                            delay(1000)
                            val moves: MoveList = MoveGenerator.generateLegalMoves(board.boards)
                            val from = Location.toLocation(moves[0].from.value(), true)
                            val to = Location.toLocation(moves[0].to.value(), true)
                            board.boards.doMove(moves[0])
                            //tv.text = board.move(from, to, false)
                            //updateUI()
                        }*/

                        //waitMove()
                        //aiMoves(board)

                    } else if (!board.isEmpty(l) && !locHold) {
                        Loged.e("Picked up ${board.getLocation(l)} at $l")
                        locationHold = l
                        locHold = true
                    } else {
                        locationHold = null
                        locHold = false
                    }

                    updateUI()

                }
                tr.addView(boarded[i][j], width / 8, height / 12)
            }
            layout.addView(tr)
        }

        buttonSel = boarded[0][0]!!
        if (buttonSel.background is ColorDrawable)
            colorValue = (buttonSel.background as ColorDrawable).color

        Loged.wtf(buttonList())

        //corout()
        //coroutLearn(this)
        //hold()

    }

    private fun buttonList(): String {
        var s = "---\n"
        for (i in 0..7) {
            s += "|"
            for (j in 0..7) {
                s += "${boarded[i][j]!!.tag}|"
            }
            s += "\n"
        }
        return s
    }

    private fun updateUI() {
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                boarded[i][j]!!.text = "${board.board[i][j].icon}"
            }
        }

        Loged.e(board.toFEN())
        //Loged.e(board.boards.fen)

        //--------------------------

    }

}