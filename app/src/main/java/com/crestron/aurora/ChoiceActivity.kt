package com.crestron.aurora

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import com.box.shelfview.BookModel
import com.box.shelfview.ShelfView
import com.crestron.aurora.boardgames.chess.MainActivity
import com.crestron.aurora.boardgames.yahtzee.YahtzeeActivity
import com.crestron.aurora.cardgames.BlackJackActivity
import com.crestron.aurora.cardgames.calculation.CalculationActivity
import com.crestron.aurora.cardgames.hilo.HiLoActivity
import com.crestron.aurora.cardgames.matching.MatchingActivity
import com.crestron.aurora.cardgames.solitaire.SolitaireActivity
import com.crestron.aurora.cardgames.videopoker.VideoPokerActivity
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.otherfun.*
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.showapi.Source
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.viewtesting.ViewTesting
import com.github.florent37.inlineactivityresult.kotlin.startForResult
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.holder.BadgeStyle
import com.mikepenz.materialdrawer.model.DividerDrawerItem
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2core.Func
import io.kimo.konamicode.KonamiCode
import kotlinx.android.synthetic.main.activity_choice.*
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.net.SocketTimeoutException
import java.net.URL
import java.util.*


class ChoiceActivity : AppCompatActivity() {

    enum class ChoiceButton(val id: String, var title: String) {
        BLACKJACK("blackjack", "BlackJack"),
        SOLITAIRE("solitaire", "Solitaire"),
        CALCULATION("calculation", "Calculation"),
        VIDEO_POKER("video_poker", "Video Poker"),
        MATCHING("matching", "Matching"),
        HILO("hilo", "HiLo"),
        CHESS("chess", "Chess"),
        YAHTZEE("yahtzee", "Yahtzee"),
        SETTINGS("settings", "Settings"),
        ANIME("anime", "Anime"),
        CARTOON("cartoon", "Cartoon"),
        DUBBED("dubbed", "Dubbed"),
        ANIME_MOVIES("anime_movies", "Anime Movies"),
        CARTOON_MOVIES("cartoon_movies", "Cartoon Movies"),
        RECENT_ANIME("recent_anime", "Recent Anime"),
        RECENT_CARTOON("recent_cartoon", "Recent Cartoon"),
        UPDATE_APP("update_app", "Update App"),
        VIEW_DOWNLOADS("view_downloads", "View Downloads"),
        VIEW_VIDEOS("view_videos", "View Videos"),
        UPDATE_NOTES("update_notes", "Update Notes"),
        DOWNLOAD_APK("download_apk", "Download Apk"),
        DELETE_OLD_FILE("delete_old_file", "Delete Old File\n(Sorry still working on this)"),
        QUICK_CHOICE("quick_choice", ""),
        VIEW_FAVORITES("view_favorites", "View Favorites"),
        RSS_FEED("rss_feed", "Schedule"),
        FEEDBACK("feedback", "Feedback"),
        VIEW_TESTING("view_testing", "View Test")
    }

    private fun drawableModel(id: Int, button: ChoiceButton, count: Int = 0): BookModel {
        return BookModel.drawableBookModel(id, button.id, button.title, count)
    }

    interface BookListener : ShelfView.BookClickListener {
        override fun onBookClicked(position: Int, bookId: String?, bookTitle: String?, view: View) {

        }

        fun getBook(bookId: String?, bookTitle: String?): ChoiceButton {
            return ChoiceButton.valueOf(bookId!!.toUpperCase())
        }
    }

    lateinit var result: Drawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)
        //setSupportActionBar(toolbar)
        FirebaseApp.initializeApp(this)

        setUpDrawer(savedInstanceState)

        //Loged.d(FirebaseInstanceId.getInstance().token!!)

        if (!defaultSharedPreferences.getBoolean(ConstantValues.WIFI_ONLY, false)) {
            launch {
                val url = URL(ConstantValues.VERSION_URL).readText()
                val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName

                Loged.i("version is ${version.toDouble()} and info is ${info.version}")

                if (version.toDouble() < info.version) {
                    getAppPermissions(info)
                }
            }
        }

        //FunApplication.startUpdate(this)
        //UpdateJob.scheduleJob()
        //UpdateJob.cancelJob(UpdateJob.scheduleJob())

        val length = defaultSharedPreferences.getFloat(ConstantValues.UPDATE_CHECK, 1f)
        FunApplication.checkUpdater(this, length)

        KonamiCode.Installer(this)
                .on(this)
                .callback {
                    Toast.makeText(this@ChoiceActivity, "Super TSR Mode Activated!", Toast.LENGTH_LONG).show()
                }
                .install()

        val listener = object : BookListener {

            override fun onBookClicked(position: Int, bookId: String?, bookTitle: String?, view: View) {
                Loged.wtf("position $position id $bookId title $bookTitle")
                try {
                    val book = getBook(bookId, bookTitle)
                    when (book) {
                        ChoiceButton.BLACKJACK -> {
                            //startActivity(Intent(this@ChoiceActivity, BlackJackActivity::class.java))
                            ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, BlackJackActivity::class.java))
                        }
                        ChoiceButton.SOLITAIRE -> {
                            val intent = Intent(this@ChoiceActivity, SolitaireActivity::class.java)

                            val input = EditText(this@ChoiceActivity)
                            val lp = LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT)
                            input.layoutParams = lp
                            input.hint = "${defaultSharedPreferences.getInt(ConstantValues.DRAW_AMOUNT, 1)}"
                            input.inputType = InputType.TYPE_CLASS_NUMBER

                            val builder = AlertDialog.Builder(this@ChoiceActivity)
                            builder.setView(input)
                            builder.setTitle("What Kind of Draw?")
                            builder.setMessage("Choose the amount to draw")
                            // Add the buttons
                            builder.setPositiveButton("This Amount") { _, _ ->
                                val num = try {
                                    val numTemp = "${input.text}".toInt()
                                    when {
                                        numTemp >= 3 -> 3
                                        numTemp <= 1 -> 1
                                        else -> numTemp
                                    }
                                } catch (e: Exception) {
                                    defaultSharedPreferences.getInt(ConstantValues.DRAW_AMOUNT, 1)
                                }
                                val edit = defaultSharedPreferences.edit()
                                edit.putInt(ConstantValues.DRAW_AMOUNT, num)
                                edit.apply()
                                intent.putExtra(ConstantValues.DRAW_AMOUNT, num)
                                ViewUtil.presentActivity(view, this@ChoiceActivity, intent)
                                //startActivity(intent)
                            }
                            builder.setNegativeButton("Never Mind") { _, _ ->

                            }
                            val dialog = builder.create()
                            dialog.show()

                        }
                        ChoiceButton.CALCULATION -> {
                            ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, CalculationActivity::class.java))
                            //startActivity(Intent(this@ChoiceActivity, CalculationActivity::class.java))
                        }
                        ChoiceButton.VIDEO_POKER -> {
                            //startActivity(Intent(this@ChoiceActivity, VideoPokerActivity::class.java))
                            ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, VideoPokerActivity::class.java))
                        }
                        ChoiceButton.MATCHING -> {
                            //startActivity(Intent(this@ChoiceActivity, MatchingActivity::class.java))
                            ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, MatchingActivity::class.java))
                        }
                        ChoiceButton.HILO -> {
                            //startActivity(Intent(this@ChoiceActivity, HiLoActivity::class.java))
                            ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, HiLoActivity::class.java))
                        }
                        ChoiceButton.CHESS -> {
                            //startActivity(Intent(this@ChoiceActivity, MainActivity::class.java))
                            ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, MainActivity::class.java))
                        }
                        ChoiceButton.YAHTZEE -> {
                            //startActivity(Intent(this@ChoiceActivity, YahtzeeActivity::class.java))
                            ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, YahtzeeActivity::class.java))
                        }
                        ChoiceButton.SETTINGS -> {
                            permissionCheck(SettingsActivity2::class.java, shouldFinish = true)
                        }
                        ChoiceButton.ANIME -> {
                            permissionCheck(ShowListActivity::class.java, url = Source.ANIME.link, view = view)
                        }
                        ChoiceButton.CARTOON -> {
                            permissionCheck(ShowListActivity::class.java, url = Source.CARTOON.link, view = view)
                        }
                        ChoiceButton.DUBBED -> {
                            permissionCheck(ShowListActivity::class.java, url = Source.DUBBED.link, view = view)
                        }
                        ChoiceButton.ANIME_MOVIES -> {
                            permissionCheck(ShowListActivity::class.java, url = Source.ANIME_MOVIES.link, view = view)
                        }
                        ChoiceButton.CARTOON_MOVIES -> {
                            permissionCheck(ShowListActivity::class.java, url = Source.CARTOON_MOVIES.link, view = view)
                        }
                        ChoiceButton.RECENT_ANIME -> {
                            defaultSharedPreferences.edit().putInt(ConstantValues.UPDATE_COUNT, 0).apply()
                            permissionCheck(ShowListActivity::class.java, true, url = Source.RECENT_ANIME.link, view = view)
                        }
                        ChoiceButton.RECENT_CARTOON -> {
                            //defaultSharedPreferences.edit().putInt(ConstantValues.UPDATE_COUNT, 0).apply()
                            permissionCheck(ShowListActivity::class.java, true, url = Source.RECENT_CARTOON.link, view = view)
                        }
                        ChoiceButton.UPDATE_APP -> {
                            launch {
                                val url = URL(ConstantValues.VERSION_URL).readText()

                                val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)

                                Loged.wtf("$info")

                                try {
                                    val pInfo = packageManager.getPackageInfo(packageName, 0)
                                    val version = pInfo.versionName

                                    Loged.i("version is ${version.toDouble()} and info is ${info.version}")

                                    if (version.toDouble() < info.version) {
                                        getAppPermissions(info)
                                    } else {
                                        Loged.e("Nope")
                                        runOnUiThread {
                                            Toast.makeText(this@ChoiceActivity, "You are up to date!", Toast.LENGTH_LONG).show()
                                        }
                                    }

                                } catch (e: PackageManager.NameNotFoundException) {
                                    e.printStackTrace()
                                }

                            }
                        }
                        ChoiceButton.VIEW_DOWNLOADS -> {
                            val intent = Intent(this@ChoiceActivity, DownloadViewerActivity::class.java)
                            intent.putExtra(ConstantValues.DOWNLOAD_NOTIFICATION, true)
                            ViewUtil.presentActivity(view, this@ChoiceActivity, intent)
                            //startActivity(intent)
                        }
                        ChoiceButton.VIEW_VIDEOS -> {
                            val intent = Intent(this@ChoiceActivity, ViewVideosActivity::class.java)
                            ViewUtil.presentActivity(view, this@ChoiceActivity, intent)
                        }
                        ChoiceButton.UPDATE_NOTES -> {
                            launch {
                                val url = URL(ConstantValues.VERSION_URL).readText()
                                Loged.i(url)
                                val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                                Loged.w("$info")

                                val pInfo = packageManager.getPackageInfo(packageName, 0)
                                val version = pInfo.versionName

                                runOnUiThread {
                                    val builder = AlertDialog.Builder(this@ChoiceActivity)
                                    builder.setTitle("Notes for version ${info.version}")
                                    builder.setMessage("Your version: $version\n${info.devNotes}")
                                    builder.setNeutralButton("Cool!") { _, _ ->
                                        //FunApplication.cancelUpdate(this@ChoiceActivity)
                                    }
                                    val dialog = builder.create()
                                    dialog.show()
                                }
                            }
                        }
                        ChoiceButton.DOWNLOAD_APK -> {

                            launch {

                                val url = URL(ConstantValues.VERSION_URL).readText()

                                val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)

                                val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getNameFromUrl(info.link)!!.replace(".png", ".apk")
                                val request = Request(info.link, filePath)

                                val fetchConfiguration = FetchConfiguration.Builder(this@ChoiceActivity)
                                        .enableAutoStart(true)
                                        .enableRetryOnNetworkGain(true)
                                        .setProgressReportingInterval(1000L)
                                        .setHttpDownloader(HttpUrlConnectionDownloader(Downloader.FileDownloaderType.PARALLEL))
                                        .setDownloadConcurrentLimit(1)
                                        .build()

                                val fetch = fetchConfiguration.getNewFetchInstanceFromConfiguration()

                                fetch.addListener(object : FetchingUtils.FetchAction {
                                    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                                        super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
                                        val progress = "%.2f".format(FetchingUtils.getProgress(download.downloaded, download.total))
                                        val info1 = "$progress% " +
                                                "at ${FetchingUtils.getDownloadSpeedString(downloadedBytesPerSecond)} " +
                                                "with ${FetchingUtils.getETAString(etaInMilliSeconds)}"

                                        sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                                                info1,
                                                download.progress,
                                                this@ChoiceActivity,
                                                DownloadViewerActivity::class.java,
                                                download.id)
                                    }

                                    override fun onCompleted(download: Download) {
                                        super.onCompleted(download)

                                        val mNotificationManager: NotificationManager = this@ChoiceActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                        mNotificationManager.cancel(download.id)
                                    }
                                })

                                fetch.enqueue(request, Func {

                                }, Func {

                                })
                            }

                        }
                        ChoiceButton.DELETE_OLD_FILE -> {
                            launch {
                                val url = URL(ConstantValues.VERSION_URL).readText()
                                val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                                val strApkToInstall = getNameFromUrl(info.link)!!.replace(".png", ".apk")
                                val path1 = File(File(Environment.getExternalStorageDirectory(), "Download"), strApkToInstall)
                                if (path1.exists()) {
                                    runOnUiThread {
                                        Toast.makeText(this@ChoiceActivity, "Deleted", Toast.LENGTH_SHORT).show()
                                    }
                                    path1.delete()
                                } else {
                                    runOnUiThread {
                                        Toast.makeText(this@ChoiceActivity, "It's not there", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                        ChoiceButton.QUICK_CHOICE -> {
                            Loged.wtf(bookTitle!!)
                            val intented = Intent(this@ChoiceActivity, EpisodeActivity::class.java)
                            intented.putExtra(ConstantValues.URL_INTENT, bookId)
                            intented.putExtra(ConstantValues.NAME_INTENT, bookTitle)
                            startActivity(intented)
                        }
                        ChoiceButton.VIEW_FAVORITES -> {
                            val intented = Intent(this@ChoiceActivity, SettingsShowActivity::class.java)
                            intented.putExtra("displayText", "Your Favorites")
                            startForResult(intented) {
                                val shouldReset = it.data?.extras?.getBoolean("restart") ?: false
                                if (shouldReset)
                                    this@ChoiceActivity.recreate()
                            }
                        }
                        ChoiceButton.RSS_FEED -> {
                            val intented = Intent(this@ChoiceActivity, RssActivity::class.java)
                            //startActivity(intented)
                            ViewUtil.presentActivity(view, this@ChoiceActivity, intented)
                        }
                        ChoiceButton.FEEDBACK -> {
                            val intented = Intent(this@ChoiceActivity, FormActivity::class.java)
                            //startActivity(intented)
                            ViewUtil.presentActivity(view, this@ChoiceActivity, intented)
                        }
                        ChoiceButton.VIEW_TESTING -> {
                            val intented = Intent(this@ChoiceActivity, ViewTesting::class.java)
                            startActivity(intented)
                        }
                    }
                } catch (e: IllegalArgumentException) {
                    val intented = Intent(this@ChoiceActivity, EpisodeActivity::class.java)
                    intented.putExtra(ConstantValues.URL_INTENT, bookId)
                    intented.putExtra(ConstantValues.NAME_INTENT, bookTitle)
                    //startActivity(intented)
                    ViewUtil.presentActivity(view, this@ChoiceActivity, intented)
                    //ViewUtil.revealing(findViewById(android.R.id.content), intent)

                }
            }
        }

        shelfView.setOnBookClicked(listener)

        val models = ArrayList<BookModel>()
        //models.add(drawableModel(R.drawable.blackjacklogo, ChoiceButton.VIEW_TESTING))

        models.add(drawableModel(R.drawable.blackjacklogo, ChoiceButton.BLACKJACK))
        models.add(drawableModel(R.drawable.solitairelogo, ChoiceButton.SOLITAIRE))
        models.add(drawableModel(R.drawable.calculationlogo, ChoiceButton.CALCULATION))
        models.add(drawableModel(R.drawable.pokerlogo, ChoiceButton.VIDEO_POKER))
        models.add(drawableModel(R.drawable.hilologo, ChoiceButton.HILO))
        models.add(drawableModel(R.drawable.yahtzeelogo, ChoiceButton.YAHTZEE))
        models.add(drawableModel(R.drawable.black_chess_knight, ChoiceButton.CHESS))
        models.add(drawableModel(R.drawable.matchinglogo, ChoiceButton.MATCHING))

        models.add(drawableModel(android.R.drawable.ic_menu_today, ChoiceButton.RSS_FEED))
        models.add(drawableModel(R.drawable.recents, ChoiceButton.RECENT_ANIME, defaultSharedPreferences.getInt(ConstantValues.UPDATE_COUNT, 0)))
        models.add(drawableModel(R.drawable.ten2, ChoiceButton.ANIME))
        models.add(drawableModel(R.drawable.mov, ChoiceButton.ANIME_MOVIES))
        models.add(drawableModel(R.drawable.recents, ChoiceButton.RECENT_CARTOON))
        models.add(drawableModel(R.drawable.ten4, ChoiceButton.DUBBED))
        models.add(drawableModel(R.drawable.jack1, ChoiceButton.CARTOON))
        models.add(drawableModel(R.drawable.mov, ChoiceButton.CARTOON_MOVIES))
        models.add(drawableModel(R.drawable.mov, ChoiceButton.VIEW_DOWNLOADS))
        models.add(drawableModel(R.drawable.mov, ChoiceButton.VIEW_VIDEOS))

        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.SETTINGS))
        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.FEEDBACK))
        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.UPDATE_APP))
        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.UPDATE_NOTES))

        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.DELETE_OLD_FILE))
        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.DOWNLOAD_APK))

        shelfView.loadData(models)

        launch {
            val show = ShowDatabase.getDatabase(this@ChoiceActivity).showDao()
            val showList = show.allShows
            if (showList.size > 0) {
                models.add(drawableModel(android.R.drawable.ic_input_get, ChoiceButton.VIEW_FAVORITES))
            }
            showList.shuffle()

            val list = defaultSharedPreferences.getString("homeScreenAdding", "{\"list\" : []}")

            Loged.i(list!!)

            val showList1 = Gson().fromJson<SettingsShowActivity.NameList>(list, SettingsShowActivity.NameList::class.java)

            showList1.list.sortBy { it.name }

            for (i in showList1.list) {
                try {
                    val link = async {
                        val episodeApi = EpisodeApi(ShowInfo(i.name, i.url))
                        //val doc1 = Jsoup.connect(i.url).get()
                        //doc1.select("div.left_col").select("img[src^=http]#series_image").attr("abs:src")
                        episodeApi.image
                    }
                    val s1 = link.await()
                    models.add(BookModel.urlBookModel(s1, i.url, i.name))
                    runOnUiThread {
                        shelfView.loadData(models)
                    }
                } catch (e: SocketTimeoutException) {

                }
            }

            if (defaultSharedPreferences.getBoolean(ConstantValues.RANDOM_SHOW, true)) {
                val numberOfShowsToDisplay = defaultSharedPreferences.getString(ConstantValues.NUMBER_OF_RANDOM, "1")!!.toInt()
                val randomShowsToDisplay = showList.filterNot { it -> it.name in showList1.list.groupBy { it.name }.keys }
                for (i in 0 until if (randomShowsToDisplay.size > numberOfShowsToDisplay) numberOfShowsToDisplay else randomShowsToDisplay.size) {
                    val s = randomShowsToDisplay[i]
                    Loged.e(s.name)
                    val link = async {
                        val epApi = EpisodeApi(ShowInfo(s.name, s.link))
                        epApi.image
                    }
                    val s1 = link.await()
                    models.add(BookModel.urlBookModel(s1, s.link, s.name))
                    runOnUiThread {
                        shelfView.loadData(models)
                    }
                }
            }
        }

        if (defaultSharedPreferences.getBoolean("delete_file", false)) {
            launch {
                val url = URL(ConstantValues.VERSION_URL).readText()
                val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                val strApkToInstall = getNameFromUrl(info.link)!!.replace(".png", ".apk")
                val path1 = File(File(Environment.getExternalStorageDirectory(), "Download"), strApkToInstall)
                if (path1.exists()) {
                    runOnUiThread {
                        Toast.makeText(this@ChoiceActivity, "Deleted Old File", Toast.LENGTH_SHORT).show()
                    }
                    path1.delete()
                } else {
                    /*runOnUiThread {
                        Toast.makeText(this@ChoiceActivity, "It's not there", Toast.LENGTH_SHORT).show()
                    }*/
                }
            }
        }
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result.isDrawerOpen) {
            result.closeDrawer()
        } else {
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        try {
            unregisterReceiver(BroadcastReceiverDownload())
        } catch (e: IllegalArgumentException) {

        }
        super.onDestroy()
    }

    fun sendProgressNotification(title: String, text: String, progress: Int, context: Context, gotoActivity: Class<*>, notification_id: Int) {

        val mBuilder = NotificationCompat.Builder(this@ChoiceActivity, ConstantValues.CHANNEL_ID)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setOngoing(true)
                .setContentTitle(title)
                .setStyle(NotificationCompat.BigTextStyle().bigText(text))
                .setProgress(100, progress, false)
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

        // mNotificationId is a unique integer your app uses to identify the
        // notification. For example, to cancel the notification, you can pass its ID
        // number to NotificationManager.cancel().
        val mNotificationManager: NotificationManager = this@ChoiceActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        mNotificationManager.notify(notification_id, mBuilder.build())
    }

    fun getNameFromUrl(url: String): String? {
        return Uri.parse(url).lastPathSegment
    }

    fun getAppPermissions(info: AppInfo) {
        Permissions.check(this@ChoiceActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                "Storage permissions are required because so we can download videos",
                Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                object : PermissionHandler() {
                    override fun onGranted() {
                        //do your task
                        getNewApp(info)
                    }

                    override fun onDenied(context: Context?, deniedPermissions: java.util.ArrayList<String>?) {
                        super.onDenied(context, deniedPermissions)
                        val permArray = deniedPermissions!!.toTypedArray()
                        Permissions.check(this@ChoiceActivity, permArray,
                                "Storage permissions are required because so we can download videos",
                                Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                                this)
                    }
                })
    }

    fun getNewApp(info: AppInfo) {
        runOnUiThread {
            val pInfo = packageManager.getPackageInfo(packageName, 0)
            val version = pInfo.versionName
            val builder = AlertDialog.Builder(this@ChoiceActivity)
            builder.setTitle("There's a new update! Version: ${info.version}")
            builder.setMessage("You are on version $version\nDo you want to update?\n${info.devNotes}")
            // Add the buttons
            builder.setPositiveButton("Yes Update Please!") { _, _ ->

                val filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + getNameFromUrl(info.link)!!.replace(".png", ".apk")
                val request = Request(info.link, filePath)

                val fetchConfiguration = FetchConfiguration.Builder(this@ChoiceActivity)
                        .enableAutoStart(true)
                        .enableRetryOnNetworkGain(true)
                        .setProgressReportingInterval(1000L)
                        .setHttpDownloader(HttpUrlConnectionDownloader(Downloader.FileDownloaderType.PARALLEL))
                        .setDownloadConcurrentLimit(1)
                        .build()

                val fetch = fetchConfiguration.getNewFetchInstanceFromConfiguration()

                fetch.addListener(object : FetchingUtils.FetchAction {

                    override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                        super.onStarted(download, downloadBlocks, totalBlocks)
                        defaultSharedPreferences.edit().putBoolean("delete_file", false).apply()
                    }

                    override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                        super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
                        val progress = "%.2f".format(FetchingUtils.getProgress(download.downloaded, download.total))
                        val info1 = "$progress% " +
                                "at ${FetchingUtils.getDownloadSpeedString(downloadedBytesPerSecond)} " +
                                "with ${FetchingUtils.getETAString(etaInMilliSeconds)}"

                        sendProgressNotification(download.file.substring(download.file.lastIndexOf("/") + 1),
                                info1,
                                download.progress,
                                this@ChoiceActivity,
                                DownloadViewerActivity::class.java,
                                download.id)
                    }

                    override fun onError(download: Download, error: Error, throwable: Throwable?) {
                        super.onError(download, error, throwable)
                        FetchingUtils.retry(download)
                    }

                    override fun onCompleted(download: Download) {
                        super.onCompleted(download)

                        val mNotificationManager: NotificationManager = this@ChoiceActivity.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        mNotificationManager.cancel(download.id)
                        val strApkToInstall = getNameFromUrl(info.link)!!.replace(".png", ".apk")
                        val path1 = File(File(Environment.getExternalStorageDirectory(), "Download"), strApkToInstall)

                        val apkUri = GenericFileProvider.getUriForFile(this@ChoiceActivity, applicationContext.packageName + ".otherfun.GenericFileProvider", path1)
                        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                        intent.data = apkUri
                        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        //intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
                        startActivity(intent)
                        defaultSharedPreferences.edit().putBoolean("delete_file", true).apply()
                    }
                })

                fetch.enqueue(request, Func {

                }, Func {

                })
            }
            builder.setNegativeButton("Not Now") { _, _ ->

            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            val downloadCountItem = PrimaryDrawerItem()
                    .withIcon(GoogleMaterial.Icon.gmd_cloud_download)
                    .withSelectable(false)
                    .withIdentifier(8)
                    .withName("Downloads: ${FetchingUtils.downloadCount}")
            result.updateItem(downloadCountItem)
        } catch (e: IndexOutOfBoundsException) {

        }
    }

    private fun setUpDrawer(savedInstanceState: Bundle?) {

        //if you want to update the items at a later time it is recommended to keep it in a variable
        val downloadCountItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_cloud_download)
                .withSelectable(false)
                .withIdentifier(8)
                .withName("Downloads: ${FetchingUtils.downloadCount}")
        val versionItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_android)
                .withSelectable(false)
                .withIdentifier(6)
                .withName("App Version Notes")
        val favoritesItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_favorite)
                .withSelectable(false)
                .withIdentifier(5)
                .withName("View Favorites")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    val intented = Intent(this@ChoiceActivity, SettingsShowActivity::class.java)
                    intented.putExtra("displayText", "Your Favorites")
                    startForResult(intented) {
                        val shouldReset = it.data?.extras?.getBoolean("restart") ?: false
                        if (shouldReset)
                            this@ChoiceActivity.recreate()
                    }
                    true
                }
        val viewDownloadsItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_file_download)
                .withSelectable(false)
                .withIdentifier(0)
                .withName("View Downloads")
                .withOnDrawerItemClickListener { _, _, _ ->
                    val viewDownloadsItem = PrimaryDrawerItem()
                            .withIcon(GoogleMaterial.Icon.gmd_file_download)
                            .withSelectable(false)
                            .withIdentifier(0)
                            .withName("View Downloads")
                            .withOnDrawerItemClickListener { _, _, _ ->
                                result.closeDrawer()
                                val intent = Intent(this@ChoiceActivity, DownloadViewerActivity::class.java)
                                intent.putExtra(ConstantValues.DOWNLOAD_NOTIFICATION, true)
                                ViewUtil.presentActivity(toolbar, this@ChoiceActivity, intent)
                                true
                            }
                    runOnUiThread {
                        result.updateItem(viewDownloadsItem)
                    }
                    result.closeDrawer()
                    val intent = Intent(this@ChoiceActivity, DownloadViewerActivity::class.java)
                    intent.putExtra(ConstantValues.DOWNLOAD_NOTIFICATION, true)
                    ViewUtil.presentActivity(toolbar, this@ChoiceActivity, intent)
                    true
                }
        val viewVideosItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_video_library)
                .withSelectable(false)
                .withIdentifier(11)
                .withName("View Videos")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    //val intent = Intent(this@ChoiceActivity, ViewVideosActivity::class.java)
                    //ViewUtil.presentActivity(toolbar, this@ChoiceActivity, intent)
                    permissionCheck(view = toolbar, clazz = ViewVideosActivity::class.java)
                    true
                }
        val settingsItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_settings)
                .withSelectable(false)
                .withIdentifier(1)
                .withName("Settings")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    permissionCheck(SettingsActivity2::class.java, shouldFinish = true)
                    true
                }
        val feedbackItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_feedback)
                .withSelectable(false)
                .withIdentifier(2)
                .withName("Feedback")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    val intented = Intent(this@ChoiceActivity, FormActivity::class.java)
                    ViewUtil.presentActivity(toolbar, this@ChoiceActivity, intented)
                    true
                }
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName
        val updateAppItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_system_update)
                .withSelectable(false)
                .withIdentifier(3)
                .withName("Update App")
                .withOnDrawerItemClickListener { _, _, _ ->
                    launch {
                        val url = URL(ConstantValues.VERSION_URL).readText()
                        val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                        Loged.wtf("$info")
                        try {
                            Loged.i("version is ${version.toDouble()} and info is ${info.version}")
                            if (version.toDouble() < info.version) {
                                result.closeDrawer()
                                getAppPermissions(info)
                            } else {
                                Loged.e("Nope")
                                runOnUiThread {
                                    Toast.makeText(this@ChoiceActivity, "You are up to date!", Toast.LENGTH_LONG).show()
                                }
                            }
                        } catch (e: PackageManager.NameNotFoundException) {
                            e.printStackTrace()
                        }
                    }
                    true
                }
        val updateNotesItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_update)
                .withSelectable(false).withIdentifier(4)
                .withName("Update Notes")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    launch {
                        val url = URL(ConstantValues.VERSION_URL).readText()
                        Loged.i(url)
                        val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                        Loged.w("$info")
                        runOnUiThread {
                            val builder = AlertDialog.Builder(this@ChoiceActivity)
                            builder.setTitle("Notes for version ${info.version}")
                            builder.setMessage("Your version: $version\n${info.devNotes}")
                            builder.setNeutralButton("Cool!") { _, _ ->

                            }
                            val dialog = builder.create()
                            dialog.show()
                        }
                    }
                    true
                }
        val checkForUpdateItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_cloud_upload)
                .withSelectable(false).withIdentifier(9)
                .withName("Check For Show Updates")
        checkForUpdateItem.withOnDrawerItemClickListener { _, _, _ ->
            //result.closeDrawer()
            //val mNotificationManager = this@ShowCheckService.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            //mNotificationManager.activeNotifications.filter { it.id == 1 }[0].notification.
            val showDatabase = ShowDatabase.getDatabase(this@ChoiceActivity)
            launch {
                var count = 0
                val showApi = ShowApi(Source.RECENT_ANIME).showInfoList
                showApi.addAll(ShowApi(Source.RECENT_CARTOON).showInfoList)
                val filteredList = showApi.distinctBy { it.url }
                val shows = showDatabase.showDao().allShows

                val aColIds = shows.asSequence().map { it.link }.toSet()
                val bColIds = filteredList.filter { it.url in aColIds }

                for (i in bColIds) {
                    Loged.i(i.name)
                    val showList = EpisodeApi(i).episodeList.size
                    if (showDatabase.showDao().getShow(i.name).showNum < showList) {
                        try {
                            Loged.i(i.name)
                            val show = showDatabase.showDao().getShow(i.name)
                            show.showNum = showList
                            showDatabase.showDao().updateShow(show)
                            count++
                            /*checkForUpdateItem.withSubItems(SecondaryDrawerItem()
                                    .withLevel(2)
                                    .withName(i.name)
                                    .withSelectable(false)
                                    .withIcon(GoogleMaterial.Icon.gmd_adb))*/
                        } catch (e: SocketTimeoutException) {
                            continue
                        }
                    }
                }
                if (count > 0) {
                    runOnUiThread {
                        checkForUpdateItem.withBadge("$count")
                        checkForUpdateItem.withBadgeStyle(BadgeStyle(Color.RED, Color.RED))
                        result.updateItem(checkForUpdateItem)
                    }
                }
            }
            true
        }

        // Create the AccountHeader
        val headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withDividerBelowHeader(true)
                .withCurrentProfileHiddenInList(true)
                .withOnlyMainProfileImageVisible(false)
                .addProfiles(
                        ProfileDrawerItem()
                                .withName("For Us Nerds")
                                .withEmail("App Version: $version")
                                .withIcon(GoogleMaterial.Icon.gmd_android)
                ).build()

        //create the drawer and remember the `Drawer` result object
        result = DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        versionItem,
                        downloadCountItem,
                        DividerDrawerItem(),
                        settingsItem,
                        DividerDrawerItem(),
                        viewDownloadsItem,
                        viewVideosItem,
                        DividerDrawerItem(),
                        favoritesItem,
                        checkForUpdateItem,
                        DividerDrawerItem(),
                        feedbackItem,
                        DividerDrawerItem(),
                        updateNotesItem,
                        DividerDrawerItem(),
                        updateAppItem
                )
                .withDisplayBelowStatusBar(true)
                .withTranslucentStatusBar(true)
                .withCloseOnClick(false)
                .withSelectedItem(-1)
                .withSavedInstance(savedInstanceState)
                .withInnerShadow(true)
                .build()

        launch {
            val show = ShowDatabase.getDatabase(this@ChoiceActivity).showDao()
            val showList = show.allShows
            if (showList.size > 0) {
                val favoriteCountItem = PrimaryDrawerItem()
                        .withIcon(GoogleMaterial.Icon.gmd_star)
                        .withSelectable(false)
                        .withIdentifier(7)
                        .withName("Number of favorites: ${showList.size} ")
                try {
                    runOnUiThread {
                        result.addItemsAtPosition(2, favoriteCountItem)
                    }
                } catch (e: IllegalStateException) {

                }
            }
        }

        launch {
            val url = URL(ConstantValues.PAST_VERSION_URL).readText()
            val info = Gson().fromJson(url, PastAppInfo::class.java)
            for (appVersion in info.versions) {
                versionItem.withSubItems(SecondaryDrawerItem()
                        .withName("Version ${appVersion.version} Notes")
                        .withSelectable(false)
                        .withLevel(2)
                        .withIcon(GoogleMaterial.Icon.gmd_android)
                        .withOnDrawerItemClickListener { _, _, _ ->
                            Loged.w("$info")
                            runOnUiThread {
                                val builder = AlertDialog.Builder(this@ChoiceActivity)
                                builder.setTitle("Notes for version ${appVersion.version}")
                                builder.setMessage(appVersion.devNotes)
                                builder.setNeutralButton("Cool!") { _, _ ->
                                    //FunApplication.cancelUpdate(this@ChoiceActivity)
                                }
                                val dialog = builder.create()
                                dialog.show()
                                runOnUiThread {
                                    result.updateItem(versionItem)
                                }
                            }
                            true
                        })
            }
            runOnUiThread {
                result.updateItem(versionItem)
            }

            val br: BroadcastReceiver = BroadcastReceiverDownload(object : DownloadBroadcast {
                override fun onCall(intent: Intent) {
                    val viewDownloadsItemUpdate = PrimaryDrawerItem()
                            .withIcon(GoogleMaterial.Icon.gmd_file_download)
                            .withSelectable(false)
                            .withIdentifier(0)
                            .withName("View Downloads")
                            .withOnDrawerItemClickListener { _, _, _ ->
                                result.closeDrawer()
                                val intent1 = Intent(this@ChoiceActivity, DownloadViewerActivity::class.java)
                                intent1.putExtra(ConstantValues.DOWNLOAD_NOTIFICATION, true)
                                ViewUtil.presentActivity(toolbar, this@ChoiceActivity, intent1)
                                true
                            }

                    try {
                        val downloaded = intent.getStringExtra("view_download_item_count").toInt()
                        if (downloaded > 0) {
                            viewDownloadsItemUpdate.withBadge("$downloaded")
                                    .withBadgeStyle(BadgeStyle(Color.RED, Color.RED))
                        }
                    } catch (e: Exception) {

                    }
                    runOnUiThread {
                        result.updateItem(viewDownloadsItemUpdate)
                    }
                }
            })
            val filter = IntentFilter().apply {
                addAction(ConstantValues.BROADCAST_DOWNLOAD)
            }
            registerReceiver(br, filter)

        }

    }

    companion object BroadCastInfo {

        interface DownloadBroadcast {
            fun onCall(intent: Intent)
        }

        open class KVObject(val key: String, val value: String)

        fun downloadCast(context: Context, vararg keyAndValue: KVObject) {
            Intent().also { intent ->
                intent.action = ConstantValues.BROADCAST_DOWNLOAD
                for (kv in keyAndValue) {
                    intent.putExtra(kv.key, kv.value)
                }
                intent.putExtra("data", "Notice me senpai!")
                context.sendBroadcast(intent)
            }
        }
    }

    class BroadcastReceiverDownload(private val listener: DownloadBroadcast? = null) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            listener?.onCall(intent)
            StringBuilder().apply {
                append("Action: ${intent.action}\n")
                append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
                toString().also { log ->
                    Loged.d(log)
                    //Toast.makeText(context, log, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    class BroadcastReceiverUpdate() : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            //start activity
            val i = Intent()
            i.setClassName(context.packageName, context.packageName + "ChoiceActivity")
            i.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(i)
            StringBuilder().apply {
                append("Action: ${intent.action}\n")
                append("URI: ${intent.toUri(Intent.URI_INTENT_SCHEME)}\n")
                toString().also { log ->
                    Loged.d(log)
                    //Toast.makeText(context, log, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun permissionCheck(clazz: Class<out Any>, rec: Boolean = false, url: String? = null, shouldFinish: Boolean = false, view: View? = null) {
        Permissions.check(this@ChoiceActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                "Storage permissions are required because so we can download videos",
                Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                object : PermissionHandler() {
                    override fun onGranted() {
                        //do your task
                        val intent = Intent(this@ChoiceActivity, clazz)
                        intent.putExtra(ConstantValues.RECENT_OR_NOT, rec)
                        if (url != null)
                            intent.putExtra(ConstantValues.SHOW_LINK, url)
                        if (shouldFinish) {
                            startForResult(intent) {
                                val shouldReset = it.data?.extras?.getBoolean("restart")
                                        ?: false
                                if (shouldReset)
                                    this@ChoiceActivity.recreate()
                            }.onFailed {

                            }
                        } else {
                            if (view == null)
                                startActivity(intent)
                            else
                                ViewUtil.presentActivity(view, this@ChoiceActivity, intent)
                        }
                    }

                    override fun onDenied(context: Context?, deniedPermissions: java.util.ArrayList<String>?) {
                        super.onDenied(context, deniedPermissions)
                        val permArray = deniedPermissions!!.toTypedArray()
                        Permissions.check(this@ChoiceActivity, permArray,
                                "Storage permissions are required because so we can download videos",
                                Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                                this)
                    }
                })
    }

}
