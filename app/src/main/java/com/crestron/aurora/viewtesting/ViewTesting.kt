package com.crestron.aurora.viewtesting

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import android.widget.TextView
import android.widget.ViewSwitcher
import com.crestron.aurora.R
import kotlinx.android.synthetic.main.activity_view_testing.*


class ViewTesting : AppCompatActivity() {

    var num = 0

    private val mFactory = ViewSwitcher.ViewFactory {
        // Create a new TextView
        val t = TextView(this@ViewTesting)
        t.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        //t.setTextAppearance(this@ViewTesting, android.R.style.TextAppearance_Large)
        t
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_testing)

        /*val wheelView = wheelview as WheelView<Card>
        wheelView.setWheelAdapter(MyWheelAdapter(this))
        wheelView.skin = WheelView.Skin.Common
        val d = Deck()
        d.sortBySuit()
        wheelView.setWheelData(d.getDeck())

        wheelView.setOnWheelItemSelectedListener(object : WheelView.OnWheelItemSelectedListener<Card> {
            override fun onItemSelected(position: Int, t: Card?) {
                imageView2.setImageResource(t!!.getImage(this@ViewTesting))
            }

        })*/

        text_switcher.setFactory(mFactory)

        val inAnimation = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_in)
        val out = AnimationUtils.loadAnimation(this,
                android.R.anim.fade_out)
        text_switcher.inAnimation = inAnimation
        text_switcher.outAnimation = out

        /*button.setOnClickListener {
            text_switcher.setText("Num: ${num++}")
            DeleteDialog(this@ViewTesting, "Title", null).show()
        }*/

        /*button.setOnClickListener {
            text_switcher.setText("Num: ${num++}")
            VideoPokerDialog(this@ViewTesting, "Titled", object : WheelView.OnWheelItemSelectedListener<Card> {
                override fun onItemSelected(position: Int, t: Card?) {
                    imageView2.setImageResource(t!!.getImage(this@ViewTesting))
                }
            }).show()
        }*/

        text_switcher.setCurrentText("Num: $num")

        seekBar.progress = button.alpha.toInt()

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                runOnUiThread {
                    button.alpha = progress / 100.0f
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }

        })

    }
}
