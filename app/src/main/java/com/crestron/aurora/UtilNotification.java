package com.crestron.aurora;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;

public class UtilNotification {
    /**
     * createNotificationChannel - creates a notification channel
     * @param context the context
     * @param channelName The user-visible name of the channel.
     * @param channel_description The user-visible description of the channel.
     * @param channel_id The id of the channel.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationChannel(Context context, String channelName, String channel_description, String channel_id) {

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = channel_id;
        // The user-visible name of the channel.
        CharSequence name = channelName;
        // The user-visible description of the channel.
        String description = channel_description;
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.enableLights(true);
        // Sets the notification light color for notifications posted to this
        // channel, if the device supports this feature.
        //mChannel.setLightColor(color);
        mChannel.enableVibration(true);
        //mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        mNotificationManager.createNotificationChannel(mChannel);

    }

    /**
     * createNotificationGroup - creates a notification group
     * @param context the context
     * @param group_id The id of the group.
     * @param group_name The user-visible name of the group.
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void createNotificationGroup(Context context, String group_id, String group_name) {
        // The id of the group.
        String group = group_id;
        // The user-visible name of the group.
        CharSequence name = group_name;
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannelGroup(new NotificationChannelGroup(group, name));
    }

    /**
     * deleteNotificationChannel - deletes a notification channel
     * @param context the context
     * @param channel_id the id of the channel you want deleted
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void deleteNotificationChannel(Context context, String channel_id) {
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = channel_id;
        mNotificationManager.deleteNotificationChannel(id);
    }
}
