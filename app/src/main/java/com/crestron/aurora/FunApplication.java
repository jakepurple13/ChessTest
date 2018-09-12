package com.crestron.aurora;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.migration.Migration;
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothProfile;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Environment;

import com.crashlytics.android.Crashlytics;
import com.crestron.aurora.db.ShowDatabase;
import com.crestron.aurora.otherfun.FetchingUtils;
import com.crestron.aurora.otherfun.ShowCheckService;
import com.crestron.aurora.otherfun.UpdateCheckService;
import com.evernote.android.job.JobManager;
import com.facebook.stetho.Stetho;
import com.google.firebase.FirebaseApp;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchConfiguration;
import com.tonyodev.fetch2.HttpUrlConnectionDownloader;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2core.Downloader;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import programmer.box.utilityhelper.UtilAsyncTask;
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
            UtilNotification.createNotificationChannel(this, "episode_update",
                    "episode_update_info",
                    "episodeUpdate");
            UtilNotification.createNotificationGroup(this,
                    "episode_group_id",
                    "episode_group");
        }
        float length = getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, MODE_PRIVATE).getFloat("updateCheck", 1f);

        //JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        //if (jobScheduler != null && jobScheduler.getPendingJob(1) == null)
        //checkUpdater(this, length);

        Loged.INSTANCE.wtf(FetchingUtils.Fetched.getETAString((long) (1000 * 60 * 60 * 0.0169), false), "TAG", true);

        //startUpdate(this);
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
