package com.crestron.aurora.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.crestron.aurora.Loged
import org.jetbrains.anko.defaultSharedPreferences
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*

class KUtility {

    companion object Util {

        fun getSharedPref(context: Context): SharedPreferences {
            return context.defaultSharedPreferences
        }

        private fun sendNotification(context: Context, smallIconId: Int, title: String, message: String, channel_id: String, gotoActivity: Class<*>, notification_id: Int) {
            // The id of the channel.
            val mBuilder = NotificationCompat.Builder(context, channel_id)
                    .setSmallIcon(smallIconId)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setStyle(NotificationCompat.InboxStyle())
                    .setChannelId(channel_id)
            // Creates an explicit intent for an Activity in your app
            val resultIntent = Intent(context, gotoActivity)

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // your app to the Home screen.
            val stackBuilder = TaskStackBuilder.create(context)
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(gotoActivity)
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent)
            val resultPendingIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            mBuilder.setContentIntent(resultPendingIntent)
            val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // mNotificationId is a unique integer your app uses to identify the
            // notification. For example, to cancel the notification, you can pass its ID
            // number to NotificationManager.cancel().
            mNotificationManager.notify(notification_id, mBuilder.build())
        }
    }

    private fun longestSequence(a: IntArray): Int {
        Arrays.sort(a)
        var longest = 0
        var sequence = 0
        for (i in 1 until a.size) {
            val d = a[i] - a[i - 1]
            when (d) {
                0 -> {/*ignore duplicates*/}
                1 -> sequence += 1
                else -> if (sequence > longest) {
                    longest = sequence
                }
            }
        }
        return Math.max(longest, sequence)
    }

    private fun writeToFile(data: String, context: Context) {
        try {
            val outputStreamWriter = OutputStreamWriter(context.openFileOutput("config.txt", Context.MODE_PRIVATE))
            outputStreamWriter.write(data)
            outputStreamWriter.close()

            for (i in context.filesDir.list())
                Loged.wtf(i)
        } catch (e: IOException) {
            Loged.e(tag = "Exception", msg = "File write failed: " + e.toString())
        }

    }
}