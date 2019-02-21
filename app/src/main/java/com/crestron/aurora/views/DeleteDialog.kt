package com.crestron.aurora.views

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import com.agik.AGIKSwipeButton.Controller.OnSwipeCompleteListener
import com.agik.AGIKSwipeButton.View.Swipe_Button_View
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.otherfun.FetchingUtils
import com.tonyodev.fetch2.Download
import kotlinx.android.synthetic.main.delete_dialog_layout.*
import java.io.File

class DeleteDialog(context: Context?, var title: String = "", val download: Download? = null, val file: File? = null, var listener: DeleteDialogListener? = null) : Dialog(context!!) {

    companion object {
        fun deleteDialog(context: Context?, download: Download? = null, file: File? = null, block: DeleteDialog.() -> Unit): DeleteDialog = DeleteDialog(context, file = file, download = download).apply(block)
        fun deleteDialog(context: Context?, title: String, block: DeleteDialog.() -> Unit): DeleteDialog = DeleteDialog(context, title = title).apply(block)
    }

    class DialogBuilder {
        var title = ""
        var download: Download? = null
        var file: File? = null
        var dialogListener: DeleteDialogListener? = null
        var context: Context? = null

        fun deleteDialog(block: DialogBuilder.() -> Unit): DeleteDialog {
            return DialogBuilder().apply(block).build()
        }

        fun build(): DeleteDialog = DeleteDialog(context, title, download, file, dialogListener)
    }

    interface DeleteDialogListener {
        fun onDelete()
        fun onCancel() {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.delete_dialog_layout)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        if (download != null)
            FetchingUtils.pause(download)
        setTitle("Delete $title")
        textView6.append(title)

        if (download != null) {
            val info = "Current Progress: ${context.getString(R.string.percent_progress, download.progress)}"
            all_download_info.text = info
        }

        if (file != null) {
            all_download_info.text = file.path
        }

        slide_button.setOnSwipeCompleteListener_forward_reverse(object : OnSwipeCompleteListener {
            override fun onSwipe_Forward(p0: Swipe_Button_View?) {
                Loged.w("Forward")
                if (download != null) {
                    FetchingUtils.delete(download)
                    FetchingUtils.downloadCount--
                }
                file?.delete()
                this@DeleteDialog.dismiss()
                listener?.onDelete()
            }

            override fun onSwipe_Reverse(p0: Swipe_Button_View?) {
                Loged.w("Reverse")
            }
        })

        delete_dismiss_button.setOnClickListener {
            dismiss()
        }

        setOnDismissListener {
            listener?.onCancel()
            if (download != null)
                FetchingUtils.resume(download.id)
        }
    }

}