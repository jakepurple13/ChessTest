package com.crestron.aurora.views

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.crestron.aurora.FunApplication
import com.crestron.aurora.R
import com.crestron.aurora.otherfun.DownloadViewerActivity
import com.crestron.aurora.otherfun.FetchingUtils
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2core.Func


/**
 * Implementation of App Widget functionality.
 */
class DownloadsWidget : AppWidgetProvider() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent?.action
        if (action == AppWidgetManager.ACTION_APPWIDGET_UPDATE) {
            // refresh all your widgets
            val mgr = AppWidgetManager.getInstance(context)
            val cn = ComponentName(context!!, DownloadsWidget::class.java)
            mgr.notifyAppWidgetViewDataChanged(mgr.getAppWidgetIds(cn), R.id.download_list_widget)
        }
        super.onReceive(context, intent)
    }

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        /*for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }*/
        for (appWidgetId in appWidgetIds) {
            val views = RemoteViews(
                    context.packageName,
                    R.layout.downloads_widget
            )
            /*Fetch.getDefaultInstance().getDownloads(Func {
                views.setTextViewText(R.id.widget_download_count, "${it.size} Downloads")
                //appWidgetManager.updateAppWidget(appWidgetId, views)
            })*/
            views.setTextViewText(R.id.widget_download_count, "")
            val titleIntent = Intent(context, DownloadViewerActivity::class.java)
            val titlePendingIntent = PendingIntent.getActivity(context, 0, titleIntent, 0)
            views.setOnClickPendingIntent(R.id.widgetTitleLabel, titlePendingIntent)

            val intent = Intent(context, MyWidgetRemoteViewsService::class.java)
            views.setRemoteAdapter(R.id.download_list_widget, intent)

            // template to handle the click listener for each item
            val clickIntentTemplate = Intent(context, PlayPauseWidgetService::class.java)
            /*val clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)*/
            val clickPendingIntentTemplate = PendingIntent.getService(context, 0, clickIntentTemplate, 0)
            views.setPendingIntentTemplate(R.id.download_list_widget, clickPendingIntentTemplate)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context)
        setWidgetActive(true)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        setWidgetActive(false)
        super.onDisabled(context)
    }

    private fun setWidgetActive(active: Boolean) {
        val appContext = FunApplication.getAppContext()
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        val edit = prefs.edit()
        edit.putBoolean("widgetActive", active)
        edit.apply()
    }

    companion object {

        fun sendRefreshBroadcast(context: Context) {

            val intent = Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
            intent.component = ComponentName(context, DownloadsWidget::class.java)
            context.sendBroadcast(intent)
        }

        internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager,
                                     appWidgetId: Int) {

            val widgetText = "Current Downloads"
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.downloads_widget)
            views.setTextViewText(R.id.widgetTitleLabel, widgetText)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun isWidgetActive(context: Context): Boolean {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            return prefs.getBoolean("widgetActive", false)
        }
    }
}

class MyWidgetRemoteViewsFactory(private val mContext: Context, intent: Intent) : RemoteViewsService.RemoteViewsFactory {

    private var downloadList: List<Download> = listOf()

    override fun onCreate() {
        /*Fetch.getDefaultInstance().getDownloads(Func {
            downloadList.addAll(it)
        })*/
    }

    override fun onDataSetChanged() {
        Fetch.getDefaultInstance().getDownloads(Func {
            /*val currentList: ArrayList<Download> = arrayListOf()
            currentList.addAll(downloadList)
            currentList.addAll(it)
            val newList = currentList.distinctBy { it.file }*/
            downloadList = it
        })
    }

    override fun onDestroy() {

    }

    override fun getCount(): Int {
        return downloadList.size
    }

    override fun getViewAt(position: Int): RemoteViews? {
        var prog: Double = 0.0
        var progress: String = ""
        var name = ""
        try {
            prog = FetchingUtils.getProgress(downloadList[position].downloaded, downloadList[position].total)
            progress = "%.2f".format(prog)
            name = downloadList[position].file.substring(downloadList[position].file.lastIndexOf("/") + 1)
        } catch (e: IndexOutOfBoundsException) {

        }
        val rv = RemoteViews(mContext.packageName, R.layout.widget_item)
        rv.setTextViewText(R.id.widgetItemTaskNameLabel, name)
        rv.setTextViewText(R.id.info_speed, if (prog < 0) downloadList[position].status.name else "$progress%")
        rv.setProgressBar(R.id.download_progress, 100, prog.toInt(), false)
        try {
            rv.setImageViewResource(R.id.play_pause_button, if (downloadList[position].status == Status.DOWNLOADING) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play)
        } catch (e: java.lang.IndexOutOfBoundsException) {
        }
        val fillInIntent = Intent()
        fillInIntent.putExtra("show_to_pause_or_play", downloadList[position].id)
        rv.setOnClickFillInIntent(R.id.play_pause_button, fillInIntent)

        return rv
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }

}

//this is having trouble
class MyWidgetRemoteViewsService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory {
        return MyWidgetRemoteViewsFactory(this.applicationContext, intent)
    }
}


