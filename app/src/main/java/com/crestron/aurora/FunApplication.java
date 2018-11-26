package com.crestron.aurora;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v7.graphics.Palette;

import com.crashlytics.android.Crashlytics;
import com.crestron.aurora.otherfun.DownloadViewerActivity;
import com.crestron.aurora.otherfun.FetchingUtils;
import com.crestron.aurora.otherfun.ShowCheckReceiver;
import com.crestron.aurora.otherfun.ShowListActivity;
import com.crestron.aurora.otherfun.ViewVideosActivity;
import com.crestron.aurora.showapi.Source;
import com.crestron.aurora.utilities.CustomFetchNotiManager;
import com.crestron.aurora.utilities.KUtility;
import com.crestron.aurora.utilities.Utility;
import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;
import com.google.firebase.FirebaseApp;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.HttpUrlConnectionDownloader;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2core.Downloader;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import programmer.box.utilityhelper.UtilNotification;

public class FunApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Stetho.initializeWithDefaults(this);
        FirebaseApp.initializeApp(this);
        JobManager.create(this).addJobCreator(new JobCreation());

        Loged.INSTANCE.setFILTER_BY_CLASS_NAME("crestron");

        context = this;

        Loged.INSTANCE.wtf("We are connected: " + Utility.isNetwork(this), Loged.INSTANCE.getTAG(), true);
        Loged.INSTANCE.d(new SimpleDateFormat("MM/dd/yyyy E hh:mm:ss a").format(KUtility.Util.getNextCheckTime()), "TAG", true);

        SharedPreferences sharedPreferences = getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, MODE_PRIVATE);
        FetchingUtils.Fetched.setFolderLocation(sharedPreferences.getString(ConstantValues.FOLDER_LOCATION, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/Fun/"));
        boolean wifiOnly = sharedPreferences.getBoolean(ConstantValues.WIFI_ONLY, false);
        FetchConfiguration fetchConfiguration = new FetchConfiguration.Builder(this)
                .enableAutoStart(true)
                .enableRetryOnNetworkGain(true)
                .setProgressReportingInterval(1000L)
                .setGlobalNetworkType(wifiOnly ? NetworkType.WIFI_ONLY : NetworkType.ALL)
                .setHttpDownloader(new HttpUrlConnectionDownloader(Downloader.FileDownloaderType.PARALLEL))
                .setDownloadConcurrentLimit(sharedPreferences.getInt("downloadNumber", 1))
                .setNotificationManager(new CustomFetchNotiManager(this))
                .build();
        Fetch.Impl.setDefaultInstanceConfiguration(fetchConfiguration);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //episode update
            UtilNotification.createNotificationChannel(this, "episode_update",
                    "episode_update_info",
                    "episodeUpdate");
            UtilNotification.createNotificationGroup(this,
                    "episode_group_id",
                    "episode_group");
            //show check update running
            /*UtilNotification.createNotificationChannel(this, "updateCheckRun",
                    "episode check update",
                    "updateCheckRun");
            UtilNotification.createNotificationGroup(this,
                    "show_check_update",
                    "update_check");*/
            NotificationChannel channel = new NotificationChannel("updateCheckRun", "updateCheckRun", NotificationManager.IMPORTANCE_MIN);
            channel.setShowBadge(false);
            NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
            //app update
            UtilNotification.createNotificationChannel(this, "update_notification",
                    "update_notification",
                    "update_notification");
            UtilNotification.createNotificationGroup(this,
                    "update_notification_group",
                    "update_notification_group");
        }
        //float length = getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, MODE_PRIVATE).getFloat("updateCheck", 1f);
        //JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        //if (jobScheduler != null && jobScheduler.getPendingJob(1) == null)
        //checkUpdater(this, length);
        //Loged.INSTANCE.wtf(FetchingUtils.Fetched.getETAString((long) (1000 * 60 * 60 * 0.0169), false), "TAG", true);
        //startUpdate(this);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N_MR1) {

            List<ShortcutInfo> scl = new ArrayList<>();
            ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

            if (shortcutManager.getDynamicShortcuts().size() == 0) {
                // Application restored. Need to re-publish dynamic shortcuts.
                if (shortcutManager.getPinnedShortcuts().size() > 0) {
                    // Pinned shortcuts have been restored. Use
                    // updateShortcuts() to make sure they contain
                    // up-to-date information.
                    shortcutManager.removeAllDynamicShortcuts();
                }
            }

            Intent intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, ShowListActivity.class);
            intent.putExtra(ConstantValues.RECENT_OR_NOT, true);
            intent.putExtra(ConstantValues.SHOW_LINK, Source.RECENT_ANIME.getLink());

            ShortcutInfo recentAnime = new ShortcutInfo.Builder(context, "id1")
                    .setShortLabel("Recent Anime")
                    .setLongLabel("Recent Anime")
                    .setIcon(Icon.createWithResource(context, R.drawable.apk))
                    .setIntent(intent)
                    .build();

            scl.add(recentAnime);

            intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, ShowListActivity.class);
            intent.putExtra(ConstantValues.RECENT_OR_NOT, true);
            intent.putExtra(ConstantValues.SHOW_LINK, Source.RECENT_CARTOON.getLink());

            ShortcutInfo recentCartoon = new ShortcutInfo.Builder(context, "id2")
                    .setShortLabel("Recent Cartoon")
                    .setLongLabel("Recent Cartoon")
                    .setIcon(Icon.createWithResource(context, R.drawable.cartoon_recent_cover))
                    .setIntent(intent)
                    .build();

            scl.add(recentCartoon);

            /*intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, SolitaireActivity.class);
            intent.putExtra(ConstantValues.DRAW_AMOUNT, KUtility.Util.getSharedPref(this).getInt(ConstantValues.DRAW_AMOUNT, 1));

            ShortcutInfo solitaireSC = new ShortcutInfo.Builder(context, "id3")
                    .setShortLabel("Solitaire")
                    .setLongLabel("Solitaire")
                    .setIcon(Icon.createWithResource(context, R.drawable.solitairelogo))
                    .setIntent(intent)
                    .build();

            scl.add(solitaireSC);*/

            intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, ViewVideosActivity.class);

            try {

                final WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                final Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                Bitmap bitmap = ((BitmapDrawable) wallpaperDrawable).getBitmap();
                int p = Palette.from(bitmap).generate().getDominantColor(Color.BLACK);

                Icon icon = Icon.createWithBitmap(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_video_library)
                        .color(getComplimentColor(p))
                        .toBitmap());

                ShortcutInfo videoView = new ShortcutInfo.Builder(context, "id4")
                        .setShortLabel("View Videos")
                        .setLongLabel("View Videos")
                        .setIcon(icon)
                        .setIntent(intent)
                        .build();

                scl.add(videoView);

                intent = new Intent(Intent.ACTION_MAIN, Uri.EMPTY, this, DownloadViewerActivity.class);

                Icon downloadIcon = Icon.createWithBitmap(new IconicsDrawable(this)
                        .icon(GoogleMaterial.Icon.gmd_file_download)
                        .color(getComplimentColor(p))
                        .toBitmap());

                ShortcutInfo downloadViewer = new ShortcutInfo.Builder(context, "id5")
                        .setShortLabel("View Downloads")
                        .setLongLabel("View Downloads")
                        .setIcon(downloadIcon)
                        .setIntent(intent)
                        .build();

                scl.add(downloadViewer);

            } catch (SecurityException e) {
                Loged.INSTANCE.wtf(e.getMessage(), Loged.INSTANCE.getTAG(), true);
            }

            shortcutManager.setDynamicShortcuts(scl);
        }

        KUtility.Util.setAlarmUp(context);
        KUtility.Util.setUpdateCheckAlarm(context);
    }

    public static int getComplimentColor(int color) {
        // get existing colors
        int alpha = Color.alpha(color);
        int red = Color.red(color);
        int blue = Color.blue(color);
        int green = Color.green(color);

        // find compliments
        red = (~red) & 0xff;
        blue = (~blue) & 0xff;
        green = (~green) & 0xff;

        return Color.argb(alpha, red, green, blue);
    }

    public static void seeNextAlarm(Context context) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        AlarmManager.AlarmClockInfo al = alarm.getNextAlarmClock();
        try {
            Loged.INSTANCE.d(new SimpleDateFormat("MM/dd/yyyy E hh:mm:ss a").format(al.getTriggerTime()), "TAG", true);
        } catch (NullPointerException e) {
            //Loged.INSTANCE.wtf(e.getMessage(), "TAG", true);
            //e.printStackTrace();
        }
    }

    // Setup a recurring alarm every half hour
    public static void scheduleAlarm(Context context, Number time) {
        Loged.INSTANCE.wtf(FetchingUtils.Fetched.getETAString((long) (1000 * 60 * 60 * time.doubleValue()), false), "TAG", true);
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, ShowCheckReceiver.class);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        long wantedTime = (long) (1000 * 60 * 60 * time.doubleValue());

        //long millis = System.currentTimeMillis() + wantedTime;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        long timeToSet = 5000L;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timeToSet = KUtility.Util.timeToNextHourOrHalf();
        }
        long firstMillis = calendar.getTimeInMillis() + timeToSet; // alarm is set right away
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis,
                wantedTime, pIntent);

        KUtility.Util.setCurrentUpdateTime(time.floatValue());
        KUtility.Util.setNextCheckTime(firstMillis);
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, ShowCheckReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(context, 1,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    public static Fetch getDefault() {
        return Fetch.Impl.getDefaultInstance();
    }

    public static void dbMigrate(Context context) {

    }

    public static Context getAppContext() {
        return FunApplication.context;
    }

}
