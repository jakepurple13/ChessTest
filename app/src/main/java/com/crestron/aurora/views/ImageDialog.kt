package com.crestron.aurora.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.view.Window
import com.crestron.aurora.R
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.image_dialog_layout.*
import org.jetbrains.anko.runOnUiThread

class ImageDialog(context: Context?, val title: String, val description: String, private val episodeNumber: String, private val imageLink: String) : Dialog(context!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.image_dialog_layout)

        setTitle(title)
        title_dialog.text = Html.fromHtml("<b>$title<b>", Html.FROM_HTML_MODE_COMPACT)
        title_dialog.setTextColor(Color.WHITE)
        //description_dialog.text = description
        episode_number_dialog.text = episodeNumber
        ftv.text = description
        ftv.textColor = Color.WHITE
        ftv.setTextSize(episode_number_dialog.textSize)

        context.runOnUiThread {
            try {
                Picasso.get().load(imageLink).resize((600 * .6).toInt(), (800 * .6).toInt()).error(android.R.drawable.stat_notify_error).into(image_dialog)
            } catch (ignored: IllegalArgumentException) {
            }
        }

        button_dialog.setOnClickListener {
            dismiss()
        }
    }

}