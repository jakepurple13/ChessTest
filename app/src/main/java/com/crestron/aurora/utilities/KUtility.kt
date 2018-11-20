package com.crestron.aurora.utilities

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import com.crestron.aurora.FunApplication
import com.crestron.aurora.Loged
import org.jetbrains.anko.defaultSharedPreferences
import java.io.IOException
import java.io.OutputStreamWriter
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*


class KUtility {

    companion object Util {

        var currentUpdateTime: Float = 9f
            set(value) {
                FunApplication.getAppContext().defaultSharedPreferences.edit().putFloat("currentUpdateTime", value).apply()
                field = value
            }
            get() {
                return FunApplication.getAppContext().defaultSharedPreferences.getFloat("currentUpdateTime", 9f)
            }

        var nextCheckTime: Long = 0L
            set(value) {
                val cal = Calendar.getInstance()
                cal.timeInMillis = value
                cal.set(Calendar.SECOND, 0)
                Loged.wtf("$value")
                FunApplication.getAppContext().defaultSharedPreferences.edit().putLong("nextUpdateCheckTime", cal.timeInMillis).apply()
                field = value
            }
            get() {
                return FunApplication.getAppContext().defaultSharedPreferences.getLong("nextUpdateCheckTime", 0L)
            }

        fun getSharedPref(context: Context): SharedPreferences {
            return context.defaultSharedPreferences
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun timeToNextHour(): Long {
            val start = ZonedDateTime.now()
            // Hour + 1, set Minute and Second to 00
            val end = start.plusHours(1).truncatedTo(ChronoUnit.HOURS)

            // Get Duration
            val duration = Duration.between(start, end)
            return duration.toMillis()
        }

        @RequiresApi(Build.VERSION_CODES.O)
        fun timeToNextHourOrHalf(): Long {
            val start = ZonedDateTime.now()
            // Hour + 1, set Minute and Second to 00
            val hour = start.plusHours(1).truncatedTo(ChronoUnit.HOURS)
            val minute = start.plusHours(0).truncatedTo(ChronoUnit.HOURS)
                    .plusMinutes(30).truncatedTo(ChronoUnit.MINUTES).plusSeconds(1)

            // Get Duration
            val durationHour = Duration.between(start, hour).toMillis()
            val durationMinute = Duration.between(start, minute).toMillis()
            return if (durationHour <= durationMinute) durationHour else durationMinute
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
                0 -> {/*ignore duplicates*/
                }
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