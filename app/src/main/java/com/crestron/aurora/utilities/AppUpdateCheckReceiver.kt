package com.crestron.aurora.utilities

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.crestron.aurora.otherfun.AppInfo
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL

class AppUpdateCheckReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val notificationId = intent?.getIntExtra(ConstantValues.NOTIFICATION_ID, 0) ?: 0
        Log.d("", "NotificationBroadcastReceiver: notificationId = $notificationId")
        GlobalScope.launch {
            val url = URL(ConstantValues.VERSION_URL).readText()
            val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
            val pInfo = context!!.packageManager.getPackageInfo(context.packageName, 0)
            val version = pInfo.versionName
            Loged.i("version is ${version.toDouble()} and info is ${info.version}")
            if (version.toDouble() < info.version) {
                KUtility.shouldGetUpdate = true
            }
        }
    }
}