package com.crestron.aurora

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import android.util.Log
import com.crestron.aurora.otherfun.AppInfo
import com.crestron.aurora.otherfun.DownloadUpdateReceiver
import com.crestron.aurora.utilities.KUtility
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL


class MessagingFirebase : FirebaseMessagingService() {

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage?.from}")
        Loged.wtf(remoteMessage!!.notification!!.body!!)

        // Check if message contains a data payload.
        remoteMessage.data?.isNotEmpty()?.let {
            Log.d(TAG, "Message data payload: " + remoteMessage.data)
            if(remoteMessage.data.containsKey("update")) {
                if(remoteMessage.data["update"]=="true") {
                    KUtility.shouldGetUpdate = true
                    sendDownloadUpdateNotification("For Us Nerds has an Update!", remoteMessage.notification!!.body!!,
                            this@MessagingFirebase, ChoiceActivity::class.java, 50)
                }
            } else {
                /*if (*//* Check if data needs to be processed by long running job *//* false) {
                // For long-running tasks (10 seconds or more) use Firebase Job Dispatcher.
                //scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }*/
                //sendNotification(remoteMessage.notification!!.body!!)
                sendNotification(this@MessagingFirebase, R.drawable.apk,
                        "For Us Nerds", remoteMessage.notification!!.body!!, "firebase_channel_id",
                        ChoiceActivity::class.java, 50)
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")
        Loged.d("Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */
    /*private fun scheduleJob() {
        // [START dispatch_job]
        val dispatcher = FirebaseJobDispatcher(GooglePlayDriver(this))
        val myJob = dispatcher.newJobBuilder()
                .setService(DownloadUpdateReceiver::)
                .setTag("my-job-tag")
                .build()
        dispatcher.schedule(myJob)
        // [END dispatch_job]
    }*/

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Loged.i("$token")
    }

    private fun sendNotification(context: Context, smallIconId: Int, title: String, messages: String, channel_id: String, gotoActivity: Class<*>, notification_id: Int) {
        // The id of the channel.
        val mBuilder = NotificationCompat.Builder(context, "episodeUpdate")
                .setSmallIcon(smallIconId)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messages))
                //.setContentText(messages)
                .setAutoCancel(true)
                .setChannelId(channel_id)
        // Creates an explicit intent for an Activity in your app
        val resultIntent = Intent(context, gotoActivity)
        resultIntent.putExtra(ConstantValues.RECENT_OR_NOT, true)

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

    private fun sendDownloadUpdateNotification(title: String, text: String, context: Context, gotoActivity: Class<*>, notification_id: Int) = GlobalScope.launch {

            val texts = URL(ConstantValues.VERSION_URL).readText()
            val info: AppInfo = Gson().fromJson(texts, AppInfo::class.java)
            val textToUse = "$text\n${info.devNotes}"

            val mBuilder = NotificationCompat.Builder(this@MessagingFirebase, ConstantValues.CHANNEL_ID)
                    .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setChannelId("update_notification")
                    .setGroup("update_notification_group")
                    .setStyle(NotificationCompat.BigTextStyle().bigText(textToUse))
                    .setOnlyAlertOnce(true)

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

            val downloadIntent = Intent(applicationContext, DownloadUpdateReceiver::class.java).apply {
                action = "fun.com.crestron.UPDATE_APP"
                putExtra("firebase_channel_id", notification_id)
            }
            val pendingIntent = PendingIntent.getBroadcast(applicationContext, 1, downloadIntent, PendingIntent.FLAG_UPDATE_CURRENT)
            mBuilder.addAction(android.R.drawable.ic_menu_upload, "Update", pendingIntent)

            val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // mNotificationId is a unique integer your app uses to identify the
            // notification. For example, to cancel the notification, you can pass its ID
            // number to NotificationManager.cancel().
            mNotificationManager.notify(notification_id, mBuilder.build())
    }

    companion object {

        private val TAG = "MyFirebaseMsgService"
    }
}
