package com.crestron.aurora.utilities

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import com.crestron.aurora.FunApplication
import com.crestron.aurora.Loged
import com.crestron.aurora.otherfun.FetchingUtils
import com.crestron.aurora.otherfun.ShowCheckReceiver
import com.crestron.aurora.otherfun.ShowInfosList
import com.google.gson.Gson
import org.jetbrains.anko.defaultSharedPreferences
import java.io.IOException
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*

enum class NetworkTypes(val type: String) {
    WIFI("wifi"),
    MOBILE("mobile"),
    NONE("noNetwork")
}

class KUtility {

    companion object Util {

        private fun wifiDataCheck(key: String, context: Context): Boolean {
            val type = checkNetworkStatus(context)
            return when (context.defaultSharedPreferences.getBoolean(key, false)) {
                true -> type == NetworkTypes.WIFI
                false -> (type == NetworkTypes.WIFI) || (type == NetworkTypes.MOBILE)
            } && Utility.isNetwork(context)
        }

        fun canDownload(context: Context): Boolean {
            return wifiDataCheck("downloading_wifi", context)
        }

        fun canShowUpdateCheck(context: Context): Boolean {
            return wifiDataCheck("show_update_check", context)
        }

        fun canAppUpdate(context: Context): Boolean {
            return wifiDataCheck("app_update_check", context)
        }

        fun canShowCovers(context: Context): Boolean {
            return wifiDataCheck("episode_covers", context)
        }

        private fun checkNetworkStatus(context: Context): NetworkTypes {
            val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            //Check Wifi
            val wifi = manager.activeNetworkInfo
            //Check for mobile data
            val mobile = manager.activeNetworkInfo
            return when {
                wifi != null && wifi.type == ConnectivityManager.TYPE_WIFI -> NetworkTypes.WIFI
                mobile != null && mobile.type == ConnectivityManager.TYPE_MOBILE -> NetworkTypes.MOBILE
                else -> NetworkTypes.NONE
            }
        }

        /*fun commitNotiList(list: MutableSet<String>) {
            FunApplication.getAppContext().defaultSharedPreferences.edit().putStringSet("notiList", list).apply()
        }

        fun getNotifyList(): MutableSet<String> {
            return FunApplication.getAppContext().defaultSharedPreferences.getStringSet("notiList", mutableSetOf<String>())
        }

        fun clearNotiList() {
            val list = getNotifyList()
            list.clear()
            commitNotiList(list)
        }*/

        fun commitNotiJsonList(list: ShowInfosList) {
            FunApplication.getAppContext().defaultSharedPreferences.edit().putString("notilists", Gson().toJson(list)).apply()
        }

        fun getNotiJsonList(): ShowInfosList {
            val list = FunApplication.getAppContext().defaultSharedPreferences.getString("notilists", "{\"list\" : []}")
            return Gson().fromJson<ShowInfosList>(list, ShowInfosList::class.java)
        }

        fun clearNotiJsonList() {
            //val list = getNotiJsonList()
            //list.list.clear()
            //commitNotiJsonList(list)
            commitNotiJsonList(ShowInfosList(arrayListOf()))
            //FunApplication.getAppContext().defaultSharedPreferences.edit().remove("notilists").apply()
        }

        fun removeItemFromNotiJsonList(url: String) {
            val list = getNotiJsonList()
            Loged.i("Before: ${list.list}")
            list.list.removeIf { it.url == url }
            Loged.i("After: ${list.list}")
            commitNotiJsonList(list)
        }

        var shouldGetUpdate = false
            set(value) {
                FunApplication.getAppContext().defaultSharedPreferences.edit().putBoolean("shouldGetUpdate", value).apply()
                field = value
            }
            get() {
                return FunApplication.getAppContext().defaultSharedPreferences.getBoolean("shouldGetUpdate", false)
            }

        fun setAlarmUp(context: Context) {
            //val length = context.defaultSharedPreferences.getFloat(ConstantValues.UPDATE_CHECK, 1f)
            val length = context.defaultSharedPreferences.getLong("pref_duration", 3_600_000)
            //FunApplication.checkUpdater(this, length)
            Loged.d("length: $length and currentTime: $currentDurationTime")
            //PendingIntent.getBroadcast(this, 1, Intent(this@ChoiceActivity, ShowCheckReceiver::class.java), PendingIntent.FLAG_NO_CREATE) != null
            val alarmUp = AlarmUtils.hasAlarm(context, Intent(context, ShowCheckReceiver::class.java), 90)
            Loged.i("Alarm up: $alarmUp")
            if (!alarmUp || currentDurationTime != length) {
                Loged.i("Setting")
                scheduleAlarm(context, length)
                //FunApplication.seeNextAlarm(this@ChoiceActivity)
            } else {
                Loged.i("Nope, already set")
                //FunApplication.seeNextAlarm(this@ChoiceActivity)
            }
            //FunApplication.seeNextAlarm(context)
        }

        // Setup a recurring alarm every half hour
        fun scheduleAlarm(context: Context, time: Number) {
            //Loged.wtf(FetchingUtils.getETAString((1000.0 * 60.0 * 60.0 * time.toDouble()).toLong(), false), "TAG", true)
            Loged.wtf(FetchingUtils.getETAString(time.toLong(), false), "TAG", true)
            // Construct an intent that will execute the AlarmReceiver
            val intent = Intent(context, ShowCheckReceiver::class.java)

            // Create a PendingIntent to be triggered when the alarm goes off
            val pIntent = PendingIntent.getBroadcast(context, 90,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)

            val wantedTime = time.toLong()//(1000.0 * 60.0 * 60.0 * time.toDouble()).toLong()

            //long millis = System.currentTimeMillis() + wantedTime;

            val calendar = Calendar.getInstance()
            calendar.timeInMillis = System.currentTimeMillis()
            var timeToSet = 5000L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                timeToSet = KUtility.timeToNextHourOrHalf()
            }
            val firstMillis = calendar.timeInMillis + timeToSet // alarm is set right away
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
            // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
            alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                    wantedTime, pIntent)

            KUtility.currentUpdateTime = time.toFloat()
            currentDurationTime = time.toLong()
            KUtility.nextCheckTime = firstMillis
            Loged.d(SimpleDateFormat("MM/dd/yyyy E hh:mm:ss a").format(KUtility.nextCheckTime))

        }

        fun cancelAlarm(context: Context) {
            val intent = Intent(context, ShowCheckReceiver::class.java)
            val pIntent = PendingIntent.getBroadcast(context, 90,
                    intent, PendingIntent.FLAG_UPDATE_CURRENT)
            val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarm.cancel(pIntent)
        }

        fun setUpdateCheckAlarm(context: Context) {
            val alarmUp = AlarmUtils.hasAlarm(context, Intent(context, AppUpdateCheckReceiver::class.java), 5)
            Loged.wtf("Alarm is up? $alarmUp")
            if (!alarmUp) {
                val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val alarmIntent = Intent(context, AppUpdateCheckReceiver::class.java).let { intent ->
                    PendingIntent.getBroadcast(context, 5, intent, 0)
                }

                // Set the alarm to start at approximately 12:00 p.m.
                val calendar: Calendar = Calendar.getInstance().apply {
                    timeInMillis = System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, 12)
                }

                // With setInexactRepeating(), you have to use one of the AlarmManager interval
                // constants--in this case, AlarmManager.INTERVAL_DAY.
                alarmMgr.setInexactRepeating(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY,
                        alarmIntent
                )
            }
        }

        var currentDurationTime: Long = 3_600_000
            set(value) {
                FunApplication.getAppContext().defaultSharedPreferences.edit().putLong("currentDurationTime", value).apply()
                field = value
            }
            get() {
                return FunApplication.getAppContext().defaultSharedPreferences.getLong("currentDurationTime", 3_600_000)
            }

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
            Loged.e(tag = "Exception", msg = "File write failed: $e")
        }

    }

    val loremIpsum = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed turpis elit, dictum vel bibendum vitae, auctor pharetra sem. Nullam fermentum lacus et mollis mollis. Integer elementum placerat nulla non pharetra. In est quam, mollis eget metus ac, laoreet ornare nisi. In facilisis elit et turpis dictum, eu suscipit lorem ultrices. Nullam tempus aliquam neque, in accumsan ligula luctus a. Cras pretium neque non justo gravida dignissim. Proin congue blandit tempor. Sed accumsan leo eu malesuada vestibulum. Cras molestie porta lacus, eget posuere leo consequat in. Maecenas faucibus vulputate sem, eget venenatis erat lobortis sed. Nullam purus mi, feugiat at lorem non, ultricies rhoncus lorem. Vivamus erat urna, lacinia at porttitor pretium, tristique in ex. Ut pulvinar, ligula id fermentum sagittis, sem diam mollis sapien, vel pretium enim diam convallis erat. Morbi finibus turpis vitae vestibulum vulputate.\n" +
            "\n" +
            "Suspendisse potenti. Nam ultrices odio id magna ultrices rutrum non non nisi. Etiam eget magna fermentum, dignissim ligula sed, venenatis mi. Nam id lacus nec tortor efficitur gravida vitae id nulla. Fusce non mauris a tellus eleifend sagittis. Nam sed consectetur libero, ut finibus lacus. Donec iaculis varius nisi, a sagittis augue pellentesque quis. Integer semper dapibus odio euismod interdum. Donec porttitor dolor nec nisi tempor, ac fringilla nisl facilisis. Nulla luctus massa arcu. Proin ut diam auctor, aliquam odio ac, rhoncus enim. Vivamus vestibulum nunc sed mauris cursus viverra. Suspendisse egestas malesuada est posuere cursus. Vestibulum ac augue eget augue volutpat tincidunt eget tempor arcu. Vivamus id nisi tempus, sagittis metus sed, tempor leo. Mauris fermentum iaculis mi, id viverra est ullamcorper in.\n" +
            "\n" +
            "Proin vulputate, dui at viverra imperdiet, tellus ex ultrices eros, vel congue nibh dolor ornare nunc. Suspendisse tempor libero risus, nec mattis elit tincidunt nec. Ut in malesuada nisi. Proin blandit ornare lorem. Mauris a augue et leo vestibulum tincidunt. Sed ut felis et nulla ultricies porta. Morbi eu leo felis. Praesent volutpat ac metus iaculis congue. In vestibulum hendrerit dui at fermentum. Suspendisse eu faucibus magna. Morbi dapibus consectetur lacus, in scelerisque felis fringilla gravida. Proin vel magna posuere, egestas sapien eu, gravida erat. Nullam sed auctor sem. Nullam a dapibus massa.\n" +
            "\n" +
            "Donec lacinia elementum arcu, efficitur ornare nibh. Donec ac erat ante. Duis lobortis mauris vitae ligula feugiat, et vehicula dolor elementum. Sed semper ipsum ipsum, at elementum neque euismod eget. In eget fringilla nulla. Nam nec massa vestibulum, dapibus leo vel, iaculis ante. Integer nec maximus erat. Morbi eget sapien sed enim volutpat suscipit. Sed commodo nibh sit amet nisi tempus tempor. Proin convallis justo enim, eu elementum orci imperdiet sit amet. Quisque lacinia sapien nibh, in vestibulum orci rutrum vel. Proin sit amet egestas neque. Curabitur egestas dolor ligula, at aliquam sem tempus eget. Aenean maximus metus diam, nec finibus ante varius vitae. Suspendisse maximus aliquam arcu nec pellentesque.\n" +
            "\n" +
            "Maecenas nec metus pellentesque felis mattis euismod tincidunt sed urna. In placerat dolor nec convallis eleifend. Mauris gravida porta rutrum. Morbi risus arcu, fermentum pellentesque sapien nec, varius ultricies mi. Aenean neque metus, lobortis ut consectetur sit amet, bibendum fermentum urna. Donec non velit in ante convallis lacinia. Donec placerat justo in libero luctus congue. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Nam quis urna faucibus nunc egestas semper. Vivamus aliquam turpis orci, vel laoreet nunc ultricies lobortis. Ut imperdiet turpis in mauris dictum, et mollis lacus porta."

}