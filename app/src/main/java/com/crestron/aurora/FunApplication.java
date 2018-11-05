package com.crestron.aurora;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.WallpaperManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
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
import com.crestron.aurora.otherfun.ShowCheckService;
import com.crestron.aurora.otherfun.ShowListActivity;
import com.crestron.aurora.otherfun.UpdateCheckService;
import com.crestron.aurora.otherfun.ViewVideosActivity;
import com.crestron.aurora.showapi.Source;
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

import java.util.ArrayList;
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
            //app update
            UtilNotification.createNotificationChannel(this, "update_notification",
                    "update_notification",
                    "update_notification");
            UtilNotification.createNotificationGroup(this,
                    "update_notification_group",
                    "update_notification_group");
        }
        float length = getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, MODE_PRIVATE).getFloat("updateCheck", 1f);

        //JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        //if (jobScheduler != null && jobScheduler.getPendingJob(1) == null)
        //checkUpdater(this, length);

        Loged.INSTANCE.wtf(FetchingUtils.Fetched.getETAString((long) (1000 * 60 * 60 * 0.0169), false), "TAG", true);

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

            shortcutManager.setDynamicShortcuts(scl);
        }

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

    /*public static void setUpdateAlarm(Context context) {
        Calendar cur_cal = new GregorianCalendar();
        cur_cal.setTimeInMillis(System.currentTimeMillis());//set the current time and date for this calendar

        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.DAY_OF_YEAR, cur_cal.get(Calendar.DAY_OF_YEAR));
        cal.set(Calendar.HOUR_OF_DAY, 18);
        cal.set(Calendar.MINUTE, 32);
        cal.set(Calendar.SECOND, cur_cal.get(Calendar.SECOND));
        cal.set(Calendar.MILLISECOND, cur_cal.get(Calendar.MILLISECOND));
        cal.set(Calendar.DATE, cur_cal.get(Calendar.DATE));
        cal.set(Calendar.MONTH, cur_cal.get(Calendar.MONTH));
        Intent intent = new Intent(context, UpdateCheckService.class);
        PendingIntent pintent = PendingIntent.getService(ProfileList.this, 0, intent, 0);
        AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 30*1000, pintent);
    }*/

    public static void startUpdate(Context context) {
        cancelUpdate(context);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(new JobInfo.Builder(2,
                    new ComponentName(context, UpdateCheckService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setMinimumLatency((long) (1000 * 60 * 60 * 0.0169))
                    .build());
            Loged.INSTANCE.wtf(FetchingUtils.Fetched.getETAString((long) (1000 * 60 * 60 * 0.0169), false), "TAG", true);
        }
    }

    public static void cancelUpdate(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.cancel(2);
        }
    }

    public static void checkUpdater(Context context, Number time) {
        cancelChecker(context);
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.schedule(new JobInfo.Builder(1,
                    new ComponentName(context, ShowCheckService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setMinimumLatency((long) (1000 * 60 * 60 * time.doubleValue()))
                    .build());
            Loged.INSTANCE.wtf(FetchingUtils.Fetched.getETAString((long) (1000 * 60 * 60 * time.doubleValue()), false), "TAG", true);
        }
    }

    public static void cancelChecker(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            jobScheduler.cancel(1);
        }
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
