package com.crestron.aurora.otherfun

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.crestron.aurora.ConstantValues
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Status

class NotificationBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra(ConstantValues.NOTIFICATION_ID, 0) ?: 0
        Log.d("", "NotificationBroadcastReceiver: notificationId = $notificationId")
        (context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?)?.cancel(notificationId)
        Fetch.getDefaultInstance().cancel(notificationId).deleteAllWithStatus(Status.CANCELLED)
    }
}