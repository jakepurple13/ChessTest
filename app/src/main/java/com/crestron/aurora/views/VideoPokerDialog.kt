package com.crestron.aurora.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import com.crestron.aurora.R
import com.wx.wheelview.adapter.BaseWheelAdapter
import com.wx.wheelview.widget.WheelView
import crestron.com.deckofcards.Card
import crestron.com.deckofcards.CardNotFoundException
import crestron.com.deckofcards.Deck
import crestron.com.deckofcards.Hand
import kotlinx.android.synthetic.main.video_poker_dialog.*

class VideoPokerDialog(context: Context?, val currentCard: Card, val hand: Hand, val listener: WheelView.OnWheelItemSelectedListener<Card>) : Dialog(context!!) {

    private var dismissedBy = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.video_poker_dialog)

        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val wheelView = wheelviews as WheelView<Card>
        wheelView.setWheelAdapter(MyWheelAdapter(context))
        wheelView.skin = WheelView.Skin.Common
        val d = Deck()
        for (c in hand.hand) {
            try {
                d.getCard(c)
            } catch (e: CardNotFoundException) {
                e.printStackTrace()
            }
        }
        d.addCard(currentCard)
        d.sortByValue()
        d.sortBySuit()
        d.sortByColor()
        wheelView.setWheelData(d.getDeck())
        wheelView.setSelection(d.getCardLocation(currentCard))

        wheelView.setOnWheelItemSelectedListener(listener)

        dismiss_button.setOnClickListener {
            dismissedBy = true
            dismiss()
        }

        setOnDismissListener {
            if (!dismissedBy) {
                listener.onItemSelected(0, currentCard)
            }
        }

    }

    internal class MyWheelAdapter(private val mContext: Context) : BaseWheelAdapter<Card>() {

        override fun bindView(position: Int, convertView: View?, parent: ViewGroup): View? {
            var convertView = convertView
            val viewHolder: ViewHolderWheel
            if (convertView == null) {
                viewHolder = ViewHolderWheel()
                convertView = LayoutInflater.from(mContext).inflate(R.layout.single_image_view_item, null)
                viewHolder.textView = convertView!!.findViewById(R.id.imageView)
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolderWheel
            }
            //viewHolder.textView!!.text = mList[position]
            viewHolder.textView!!.setImageResource(mList[position].getImage(mContext))
            return convertView
        }

    }

    internal class ViewHolderWheel {
        var textView: ImageView? = null
    }

}