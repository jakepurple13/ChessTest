package com.crestron.aurora.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.agik.swipe_button.Controller.OnSwipeCompleteListener
import com.agik.swipe_button.View.Swipe_Button_View
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.otherfun.FetchingUtils
import com.tonyodev.fetch2.Download
import kotlinx.android.synthetic.main.delete_dialog_layout.*

class DeleteDialog(context: Context?, val title: String, val download: Download? = null) : Dialog(context!!) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.delete_dialog_layout)

        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (download != null)
            FetchingUtils.pause(download)
        setTitle("Delete $title")
        textView6.append(title)

        if (download != null) {
            val info = "Current Progress: ${context.getString(R.string.percent_progress, download.progress)}"//\n" +
            //"Current Downloaded: ${download.downloaded}/${download.total}"

            all_download_info.text = info
        }

        slide_button.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
            override fun onSwipe_Forward(p0: Swipe_Button_View?) {
                Loged.w("Forward")
                //actionListener.onRemoveDownload(download.id)
                if (download != null)
                    FetchingUtils.delete(download)
                this@DeleteDialog.dismiss()
            }

            override fun onSwipe_Reverse(p0: Swipe_Button_View?) {
                Loged.w("Reverse")
            }
        })

        delete_dismiss_button.setOnClickListener {
            dismiss()
        }

        setOnDismissListener {
            if (download != null)
                FetchingUtils.resume(download.id)
        }
    }

}