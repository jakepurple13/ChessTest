package com.crestron.aurora.otherfun

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.crestron.aurora.ConstantValues

class PauseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra(ConstantValues.NOTIFICATION_ID, 0) ?: 0
        Log.d("", "NotificationBroadcastReceiver: notificationId = $notificationId")
        FetchingUtils.pause(notificationId)
    }
}