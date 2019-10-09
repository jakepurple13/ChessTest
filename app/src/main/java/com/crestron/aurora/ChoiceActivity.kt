package com.crestron.aurora

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Color
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.TaskStackBuilder
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.box.shelfview.BookModel
import com.box.shelfview.ShelfView
import com.bumptech.glide.Glide
import com.crestron.aurora.boardgames.chess.MainActivity
import com.crestron.aurora.boardgames.musicGame.MusicGameActivity
import com.crestron.aurora.boardgames.pong.PongActivity
import com.crestron.aurora.boardgames.yahtzee.YahtzeeActivity
import com.crestron.aurora.cardgames.BlackJackActivity
import com.crestron.aurora.cardgames.calculation.CalculationActivity
import com.crestron.aurora.cardgames.hilo.HiLoActivity
import com.crestron.aurora.cardgames.matching.MatchingActivityTwo
import com.crestron.aurora.cardgames.solitaire.SolitaireActivity
import com.crestron.aurora.cardgames.videopoker.VideoPokerActivity
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.firebaseserver.FirebaseDB
import com.crestron.aurora.otherfun.*
import com.crestron.aurora.server.ChatActivity
import com.crestron.aurora.server.QuizShowActivity
import com.crestron.aurora.server.toJson
import com.crestron.aurora.showapi.EpisodeApi
import com.crestron.aurora.showapi.ShowInfo
import com.crestron.aurora.showapi.Source
import com.crestron.aurora.utilities.KUtility
import com.crestron.aurora.utilities.SharedPrefVariables
import com.crestron.aurora.utilities.Utility
import com.crestron.aurora.utilities.ViewUtil
import com.crestron.aurora.viewtesting.ViewTesting
import com.github.florent37.inlineactivityresult.kotlin.startForResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.kaopiz.kprogresshud.KProgressHUD
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.materialdrawer.AccountHeader
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
import com.programmerbox.dragswipe.Direction
import com.programmerbox.dragswipe.DragSwipeActions
import com.programmerbox.dragswipe.DragSwipeAdapter
import com.programmerbox.dragswipe.DragSwipeUtils
import com.programmerbox.dragswipeex.set
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2core.Func
import io.kimo.konamicode.KonamiCode
import kotlinx.android.synthetic.main.activity_choice.*
import kotlinx.android.synthetic.main.material_card_hub_item.view.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
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
        PONG("pong", "Pong"),
        MUSIC_MATCH("music_match", "Music Match"),
        SETTINGS("settings", "Settings"),
        ANIME("anime", "Anime"),
        CARTOON("cartoon", "Cartoon"),
        DUBBED("dubbed", "Dubbed"),
        ANIME_MOVIES("anime_movies", "Anime Movies"),
        CARTOON_MOVIES("cartoon_movies", "Cartoon Movies"),
        RECENT_ANIME("recent_anime", "Recent Anime"),
        RECENT_CARTOON("recent_cartoon", "Recent Cartoon"),
        LIVE_ACTION("live_action", "TV Shows"),
        RECENT_LIVE_ACTION("recent_live_action", "Recent TV Shows"),
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
        VIEW_TESTING("view_testing", "View Test"),
        CHAT("chat", "Chat"),
        SHOW_QUIZ("show_quiz", "Show Quiz");

        companion object {
            fun getChoiceFromTitle(title: String): ChoiceButton? {
                for (i in values()) {
                    if (i.title == title) {
                        return i
                    }
                }
                return null
            }
        }
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
    var br: BroadcastReceiverDownload? = null
    lateinit var adapter: MaterialAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)
        //setSupportActionBar(toolbar)
        FirebaseApp.initializeApp(this)

        mAuth = FirebaseAuth.getInstance()

        hud = KProgressHUD.create(this)
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Signing")
                .setDetailsLabel("Logging In/Out")
                .setAnimationSpeed(2)
                .setDimAmount(0.5f)
                .setCancellable(false)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setUpDrawer(savedInstanceState)

        if (!packageManager.canRequestPackageInstalls()) {
            val builder = AlertDialog.Builder(this@ChoiceActivity)
            builder.setTitle("Some Permissions Needed")
            builder.setMessage("Please Allow Install from Unknown Sources so you can stay up to date!")
            builder.setPositiveButton("Take me there!") { _, _ ->
                startActivity(Intent(android.provider.Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:com.crestron.aurora")))
            }
            builder.setNegativeButton("Nah") { _, _ ->
                Toast.makeText(this, "Please Allow Install from Unknown Sources so you can stay up to date!", Toast.LENGTH_SHORT).show()
            }
            builder.show()
        }

        if (Utility.isNetworkToast(this@ChoiceActivity))
            GlobalScope.launch {
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName
                if (!SharedPrefVariables.hasShownForLatest) {
                    SharedPrefVariables.latestVersion = version.toFloat()
                    if (SharedPrefVariables.hasShownForLatest) {
                        val url = URL(ConstantValues.VERSION_URL).readText()
                        Loged.i(url)
                        val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                        Loged.w("$info")
                        val builder = AlertDialog.Builder(this@ChoiceActivity)
                        builder.setTitle("New Version Notes!")
                        builder.setMessage("Version: ${info.version}\n${info.devNotes}")
                        builder.setNeutralButton("Cool!") { _, _ -> }
                        builder.setOnDismissListener {
                            SharedPrefVariables.hasShownForLatest = true
                        }
                        runOnUiThread {
                            val dialog = builder.create()
                            dialog.show()
                        }
                    }
                }
            }

        //Loged.d(FirebaseInstanceId.getInstance().token!!)
        if (KUtility.canAppUpdate(this) && KUtility.shouldGetUpdate)
        //if (!defaultSharedPreferences.getBoolean(ConstantValues.WIFI_ONLY, false)) {
            GlobalScope.launch {
                val url = URL(ConstantValues.VERSION_URL).readText()
                val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName

                Loged.i("version is ${version.toDouble()} and info is ${info.version}")

                if (version.toDouble() < info.version) {
                    getAppPermissions(info)
                }
            }
        //}
        //FunApplication.scheduleAlarm(this, length)
        /*if (KUtility.currentUpdateTime != length )
            FunApplication.scheduleAlarm(this, length)
        else
            FunApplication.seeNextAlarm(this)*/

        KonamiCode.Installer(this)
                .on(this)
                .callback {
                    Toast.makeText(this@ChoiceActivity, "Super TSR Mode Activated!", Toast.LENGTH_LONG).show()
                }
                .install()

        //shelfView.setOnBookClicked(listener)

        (material_rv.layoutManager as StaggeredGridLayoutManager).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        }

        adapter = MaterialAdapter(arrayListOf(), this) {
            try {
                when (this.hubType) {
                    ChoiceButton.BLACKJACK -> {
                        startActivity(Intent(this@ChoiceActivity, BlackJackActivity::class.java))
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, BlackJackActivity::class.java))
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

                        val builder = MaterialAlertDialogBuilder(this@ChoiceActivity)
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
                            //ViewUtil.presentActivity(view, this@ChoiceActivity, intent)
                            startActivity(intent)
                        }
                        builder.setNegativeButton("Never Mind") { _, _ ->

                        }
                        val dialog = builder.create()
                        dialog.show()

                    }
                    ChoiceButton.CALCULATION -> {
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, CalculationActivity::class.java))
                        startActivity(Intent(this@ChoiceActivity, CalculationActivity::class.java))
                    }
                    ChoiceButton.CHAT -> {
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, CalculationActivity::class.java))
                        startActivity(Intent(this@ChoiceActivity, ChatActivity::class.java))
                    }
                    ChoiceButton.SHOW_QUIZ -> {
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, CalculationActivity::class.java))
                        startActivity(Intent(this@ChoiceActivity, QuizShowActivity::class.java))
                    }
                    ChoiceButton.VIDEO_POKER -> {
                        startActivity(Intent(this@ChoiceActivity, VideoPokerActivity::class.java))
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, VideoPokerActivity::class.java))
                    }
                    ChoiceButton.MATCHING -> {
                        startActivity(Intent(this@ChoiceActivity, MatchingActivityTwo::class.java))
                    }
                    ChoiceButton.HILO -> {
                        startActivity(Intent(this@ChoiceActivity, HiLoActivity::class.java))
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, HiLoActivity::class.java))
                    }
                    ChoiceButton.CHESS -> {
                        startActivity(Intent(this@ChoiceActivity, MainActivity::class.java))
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, MainActivity::class.java))
                    }
                    ChoiceButton.YAHTZEE -> {
                        startActivity(Intent(this@ChoiceActivity, YahtzeeActivity::class.java))
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, Intent(this@ChoiceActivity, YahtzeeActivity::class.java))
                    }
                    ChoiceButton.MUSIC_MATCH -> {
                        startActivity(Intent(this@ChoiceActivity, MusicGameActivity::class.java))
                    }
                    ChoiceButton.SETTINGS -> {
                        permissionCheck(SettingsActivity2::class.java, shouldFinish = true)
                    }
                    ChoiceButton.ANIME -> {
                        permissionCheck(ShowListActivity::class.java, url = Source.ANIME.link)
                    }
                    ChoiceButton.CARTOON -> {
                        permissionCheck(ShowListActivity::class.java, url = Source.CARTOON.link)
                    }
                    ChoiceButton.DUBBED -> {
                        permissionCheck(ShowListActivity::class.java, url = Source.DUBBED.link)
                    }
                    ChoiceButton.ANIME_MOVIES -> {
                        permissionCheck(ShowListActivity::class.java, url = Source.ANIME_MOVIES.link, movie = true)
                    }
                    ChoiceButton.CARTOON_MOVIES -> {
                        permissionCheck(ShowListActivity::class.java, url = Source.CARTOON_MOVIES.link, movie = true)
                    }
                    ChoiceButton.LIVE_ACTION -> {
                        permissionCheck(ShowListActivity::class.java, url = Source.LIVE_ACTION.link)
                    }
                    ChoiceButton.RECENT_ANIME -> {
                        permissionCheck(ShowListActivity::class.java, true, url = Source.RECENT_ANIME.link)
                    }
                    ChoiceButton.RECENT_LIVE_ACTION -> {
                        permissionCheck(ShowListActivity::class.java, true, url = Source.RECENT_LIVE_ACTION.link)
                    }
                    ChoiceButton.RECENT_CARTOON -> {
                        //defaultSharedPreferences.edit().putInt(ConstantValues.UPDATE_COUNT, 0).apply()
                        permissionCheck(ShowListActivity::class.java, true, url = Source.RECENT_CARTOON.link)
                    }
                    ChoiceButton.UPDATE_APP -> {
                        if (Utility.isNetwork(this@ChoiceActivity))
                            GlobalScope.launch {
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
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, intent)
                        startActivity(intent)
                    }
                    ChoiceButton.VIEW_VIDEOS -> {
                        val intent = Intent(this@ChoiceActivity, ViewVideosActivity::class.java)
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, intent)
                        startActivity(intent)
                    }
                    ChoiceButton.UPDATE_NOTES -> {
                        if (Utility.isNetwork(this@ChoiceActivity))
                            GlobalScope.launch {
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
                        if (Utility.isNetwork(this@ChoiceActivity))
                            GlobalScope.launch {

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
                        if (Utility.isNetwork(this@ChoiceActivity))
                            GlobalScope.launch {
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
                        //Loged.wtf(bookTitle!!)
                        val intented = Intent(this@ChoiceActivity, EpisodeActivity::class.java)
                        intented.putExtra(ConstantValues.URL_INTENT, this.detail)
                        intented.putExtra(ConstantValues.NAME_INTENT, this.title)
                        startActivity(intented)
                    }
                    ChoiceButton.VIEW_FAVORITES -> {
                        val intented = Intent(this@ChoiceActivity, FavoriteShowsActivity::class.java)
                        intented.putExtra("displayText", "Your Favorites")
                        startForResult(intented) {
                            val shouldReset = it.data?.extras?.getBoolean("restart") ?: false
                            if (shouldReset)
                                this@ChoiceActivity.recreate()
                        }
                    }
                    ChoiceButton.RSS_FEED -> {
                        val intented = Intent(this@ChoiceActivity, RssActivity::class.java)
                        startActivity(intented)
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, intented)
                    }
                    ChoiceButton.FEEDBACK -> {
                        val intented = Intent(this@ChoiceActivity, FormActivity::class.java)
                        startActivity(intented)
                        //ViewUtil.presentActivity(view, this@ChoiceActivity, intented)
                    }
                    ChoiceButton.VIEW_TESTING -> {
                        val intented = Intent(this@ChoiceActivity, ViewTesting::class.java)
                        startActivity(intented)
                    }
                    ChoiceButton.PONG -> {
                        val intented = Intent(this@ChoiceActivity, PongActivity::class.java)
                        startActivity(intented)
                    }
                }
            } catch (e: IllegalArgumentException) {
                val intented = Intent(this@ChoiceActivity, EpisodeActivity::class.java)
                intented.putExtra(ConstantValues.URL_INTENT, this.detail)
                intented.putExtra(ConstantValues.NAME_INTENT, this.title)
                startActivity(intented)
            }
        }

        material_rv.adapter = adapter

        val modelList = arrayListOf<MaterialItem>()
        //All games here
        modelList += MaterialItem(ChoiceButton.BLACKJACK, "Play Blackjack", R.drawable.blackjacklogo, bgImage = R.drawable.drkgreen)
        modelList += MaterialItem(ChoiceButton.SOLITAIRE, "Play Solitaire", R.drawable.solitairelogo, bgImage = R.drawable.drkgreen,
                buttonTextOne = "Draw 1", actionOne = {
            startActivity(Intent(this@ChoiceActivity, SolitaireActivity::class.java).apply {
                putExtra(ConstantValues.DRAW_AMOUNT, 1)
            })
        }, buttonTextTwo = "Draw 3", actionTwo = {
            startActivity(Intent(this@ChoiceActivity, SolitaireActivity::class.java).apply {
                putExtra(ConstantValues.DRAW_AMOUNT, 3)
            })
        })
        modelList += MaterialItem(ChoiceButton.CALCULATION, "Play Calculation", R.drawable.calculationlogo, bgImage = R.drawable.drkgreen)
        modelList += MaterialItem(ChoiceButton.VIDEO_POKER, "Play Video Poker", R.drawable.pokerlogo, bgImage = R.drawable.drkgreen)
        modelList += MaterialItem(ChoiceButton.HILO, "Play HiLo", R.drawable.hilologo, bgImage = R.drawable.drkgreen)
        modelList += MaterialItem(ChoiceButton.YAHTZEE, "Play Yahtzee", R.drawable.yahtzeelogo, bgImage = R.drawable.drkgreen)
        modelList += MaterialItem(ChoiceButton.CHESS, "Play Chess", R.drawable.black_chess_knight, bgImage = R.drawable.drkgreen)
        modelList += MaterialItem(ChoiceButton.PONG, "Play Pong", R.drawable.apk, bgImage = R.drawable.drkgreen)
        modelList += MaterialItem(ChoiceButton.MATCHING, "Play a Matching Game", R.drawable.matchinglogo, bgImage = R.drawable.drkgreen)
        modelList += MaterialItem(ChoiceButton.MUSIC_MATCH, "Play a Music Quiz", R.drawable.matchinglogo, bgImage = R.drawable.drkgreen)
        //All server kind of stuff here
        modelList += MaterialItem(ChoiceButton.SHOW_QUIZ, "Play a Quiz based off of your Favorites", R.drawable.b_normal)
        modelList += MaterialItem(ChoiceButton.CHAT, "Enter a Chat Server", R.drawable.a_normal)
        //All video stuff here
        modelList += MaterialItem(ChoiceButton.RSS_FEED, "Look at upcoming Anime", android.R.drawable.ic_menu_today)
        modelList += MaterialItem(ChoiceButton.RECENT_ANIME, "View Recent Anime", R.drawable.recents)
        modelList += MaterialItem(ChoiceButton.ANIME, "View Anime", R.drawable.ten2)
        modelList += MaterialItem(ChoiceButton.ANIME_MOVIES, "View Anime Movies", R.drawable.mov)
        modelList += MaterialItem(ChoiceButton.RECENT_CARTOON, "View Recent Cartoons", R.drawable.cartoon_recent_cover)
        modelList += MaterialItem(ChoiceButton.CARTOON, "View Cartoons", R.drawable.cartoon_cover)
        modelList += MaterialItem(ChoiceButton.CARTOON_MOVIES, "View Cartoon Movies", R.drawable.cartoon_movies_cover)
        modelList += MaterialItem(ChoiceButton.DUBBED, "View Dubbed Anime", R.drawable.ten4)
        modelList += MaterialItem(ChoiceButton.RECENT_LIVE_ACTION, "View TV Shows", R.drawable.recents)
        modelList += MaterialItem(ChoiceButton.LIVE_ACTION, "View Recent TV Shows", R.drawable.mov)

        modelList += MaterialItem(ChoiceButton.VIEW_DOWNLOADS, "View Downloading Shows", R.drawable.mov)
        modelList += MaterialItem(ChoiceButton.VIEW_VIDEOS, "View Downloaded Videos", R.drawable.mov)

        /*val models = ArrayList<BookModel>()
        //models.add(drawableModel(R.drawable.blackjacklogo, ChoiceButton.VIEW_TESTING))

        models.add(drawableModel(R.drawable.blackjacklogo, ChoiceButton.BLACKJACK))
        models.add(drawableModel(R.drawable.solitairelogo, ChoiceButton.SOLITAIRE))
        models.add(drawableModel(R.drawable.calculationlogo, ChoiceButton.CALCULATION))
        models.add(drawableModel(R.drawable.pokerlogo, ChoiceButton.VIDEO_POKER))
        models.add(drawableModel(R.drawable.hilologo, ChoiceButton.HILO))
        models.add(drawableModel(R.drawable.yahtzeelogo, ChoiceButton.YAHTZEE))
        models.add(drawableModel(R.drawable.black_chess_knight, ChoiceButton.CHESS))
        models.add(drawableModel(R.drawable.apk, ChoiceButton.PONG))
        models.add(drawableModel(R.drawable.matchinglogo, ChoiceButton.MATCHING))
        models.add(drawableModel(R.drawable.matchinglogo, ChoiceButton.MUSIC_MATCH))
        models.add(drawableModel(R.drawable.b_normal, ChoiceButton.SHOW_QUIZ))
        models.add(drawableModel(R.drawable.a_normal, ChoiceButton.CHAT))

        models.add(drawableModel(android.R.drawable.ic_menu_today, ChoiceButton.RSS_FEED))
        models.add(drawableModel(R.drawable.recents, ChoiceButton.RECENT_ANIME, defaultSharedPreferences.getInt(ConstantValues.UPDATE_COUNT, 0)))
        models.add(drawableModel(R.drawable.ten2, ChoiceButton.ANIME))
        models.add(drawableModel(R.drawable.mov, ChoiceButton.ANIME_MOVIES))
        models.add(drawableModel(R.drawable.cartoon_recent_cover, ChoiceButton.RECENT_LIVE_ACTION))
        models.add(drawableModel(R.drawable.mov, ChoiceButton.LIVE_ACTION))
        models.add(drawableModel(R.drawable.ten4, ChoiceButton.DUBBED))
        models.add(drawableModel(R.drawable.cartoon_recent_cover, ChoiceButton.RECENT_CARTOON))
        models.add(drawableModel(R.drawable.cartoon_cover, ChoiceButton.CARTOON))
        models.add(drawableModel(R.drawable.cartoon_movies_cover, ChoiceButton.CARTOON_MOVIES))
        models.add(drawableModel(R.drawable.mov, ChoiceButton.VIEW_DOWNLOADS))
        models.add(drawableModel(R.drawable.mov, ChoiceButton.VIEW_VIDEOS))*/

        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.SETTINGS))
        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.FEEDBACK))
        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.UPDATE_APP))
        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.UPDATE_NOTES))

        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.DELETE_OLD_FILE))
        //models.add(drawableModel(android.R.drawable.ic_menu_preferences, ChoiceButton.DOWNLOAD_APK))

        //shelfView.loadData(models)

        adapter.setListNotify(modelList)

        GlobalScope.launch {
            val show = ShowDatabase.getDatabase(this@ChoiceActivity).showDao()
            val showList = show.allShows
            if (showList.size > 0) {
                //models.add(drawableModel(android.R.drawable.ic_input_get, ChoiceButton.VIEW_FAVORITES))
                modelList += MaterialItem(ChoiceButton.VIEW_FAVORITES, "View Favorited Shows", android.R.drawable.ic_input_get)
            }

            runOnUiThread {
                loadAdapterLocations()
            }

            showList.shuffle()

            val list = defaultSharedPreferences.getString("homeScreenAdding", "{\"list\" : []}")

            Loged.i(list!!)

            val showList1 = Gson().fromJson<FavoriteShowsActivity.NameList>(list, FavoriteShowsActivity.NameList::class.java)

            showList1.list.sortBy { it.name }

            for (i in showList1.list) {
                try {
                    if (KUtility.canShowCovers(this@ChoiceActivity)) {
                        val link = async {
                            val episodeApi = EpisodeApi(ShowInfo(i.name, i.url))
                            episodeApi.image
                        }
                        val s1 = link.await()
                        //models.add(BookModel.urlBookModel(s1, i.url, i.name))
                        //modelList +=
                        runOnUiThread {
                            //shelfView.loadData(models)
                            adapter.addItem(MaterialItem(ChoiceButton.QUICK_CHOICE, i.url, image = s1).apply { title = i.name })
                            //adapter.setListNotify(modelList)
                        }
                    } else {
                        //models.add(BookModel.drawableBookModel(R.drawable.apk, i.url, i.name))
                        //modelList += MaterialItem(ChoiceButton.QUICK_CHOICE, i.url, image = R.drawable.apk).apply { title = i.name }
                        runOnUiThread {
                            adapter.addItem(MaterialItem(ChoiceButton.QUICK_CHOICE, i.url, image = R.drawable.apk).apply { title = i.name })
                            //shelfView.loadData(models)
                            //adapter.setListNotify(modelList)
                        }
                    }
                } catch (e: Exception) {
                    continue
                }
            }

            if (defaultSharedPreferences.getBoolean(ConstantValues.RANDOM_SHOW, true)) {
                val numberOfShowsToDisplay = defaultSharedPreferences.getString(ConstantValues.NUMBER_OF_RANDOM, "1")!!.toInt()
                val randomShowsToDisplay = showList.filterNot { it -> it.name in showList1.list.groupBy { it.name }.keys }
                for (i in 0 until if (randomShowsToDisplay.size > numberOfShowsToDisplay) numberOfShowsToDisplay else randomShowsToDisplay.size) {
                    val s = randomShowsToDisplay[i]
                    Loged.e(s.name)
                    if (KUtility.canShowCovers(this@ChoiceActivity)) {
                        val link = async {
                            val epApi = EpisodeApi(ShowInfo(s.name, s.link))
                            epApi.image
                        }
                        val s1 = link.await()
                        //modelList += MaterialItem(ChoiceButton.QUICK_CHOICE, s.link, image = s1).apply { title = s.name }
                        //models.add(BookModel.urlBookModel(s1, s.link, s.name))
                        runOnUiThread {
                            adapter.addItem(MaterialItem(ChoiceButton.QUICK_CHOICE, s.link, image = s1).apply { title = s.name })
                            //shelfView.loadData(models)
                            //adapter.setListNotify(modelList)
                        }
                    } else {
                        //models.add(BookModel.drawableBookModel(R.drawable.apk, s.link, s.name))
                        //modelList += MaterialItem(ChoiceButton.QUICK_CHOICE, s.name, image = R.drawable.apk).apply { title = s.name }
                        runOnUiThread {
                            adapter.addItem(MaterialItem(ChoiceButton.QUICK_CHOICE, s.name, image = R.drawable.apk).apply { title = s.name })
                            //shelfView.loadData(models)
                            //adapter.setListNotify(modelList)
                        }
                    }
                }
            }
            runOnUiThread {
                //All testing stuff/
                //adapter.addItem(MaterialItem(ChoiceButton.VIEW_TESTING, "Some actions that are WIP", android.R.drawable.ic_menu_help))
            }
        }

        if (defaultSharedPreferences.getBoolean("delete_file", false)) {
            if (Utility.isNetwork(this@ChoiceActivity))
                GlobalScope.launch {
                    val url = URL(ConstantValues.VERSION_URL).readText()
                    val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)
                    val strApkToInstall = getNameFromUrl(info.link)!!.replace(".png", ".apk")
                    val path1 = File(File(Environment.getExternalStorageDirectory(), "Download"), strApkToInstall)
                    if (path1.exists()) {
                        runOnUiThread {
                            Toast.makeText(this@ChoiceActivity, "Deleted Old File", Toast.LENGTH_SHORT).show()
                        }
                        path1.delete()
                    }
                }
        }

        DragSwipeUtils.setDragSwipeUp(adapter, material_rv, Direction.START + Direction.END + Direction.UP.value + Direction.DOWN.value,
                dragSwipeActions = object : DragSwipeActions<MaterialItem, ViewHolder> {
                    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder, dragSwipeAdapter: DragSwipeAdapter<MaterialItem, ViewHolder>) {
                        Collections.swap(adapter.list, viewHolder.adapterPosition, target.adapterPosition)
                        adapter.notifyItemMoved(viewHolder.adapterPosition, target.adapterPosition)
                        material_rv.post { (material_rv.layoutManager as StaggeredGridLayoutManager).invalidateSpanAssignments() }
                    }
                }
        )

        /*runOnUiThread {
            //Toast.makeText(this, "New task created", Toast.LENGTH_LONG).show()
            //getTodoList()
            //this will send the broadcast to update the appwidget
            if (DownloadsWidget.isWidgetActive(this@ChoiceActivity))
                DownloadsWidget.sendRefreshBroadcast(this@ChoiceActivity)
        }*/
    }

    private fun loadAdapterLocations() {
        if (defaultSharedPreferences.contains("home_screen_adapter_locations")) {
            val d = defaultSharedPreferences.getString("home_screen_adapter_locations", null)
            if (d != null) {
                val adapterList = arrayListOf<MaterialItem>()
                val f = Gson().fromJson(d, Array<ChoiceButton>::class.java)
                for (i in f) {
                    adapter.list.find { it.hubType == i }?.let {
                        adapterList += it
                    }
                }
                for (a in adapterList.withIndex()) {
                    adapter[a.index] = a.value
                }
            }
        }
    }

    private fun saveAdapterLocations() {
        val t = adapter.list.filter { it.hubType !in arrayOf(ChoiceButton.QUICK_CHOICE, ChoiceButton.VIEW_TESTING) }.map { it.hubType }
        Loged.e(t)
        defaultSharedPreferences.edit().putString("home_screen_adapter_locations", t.toJson()).apply()
    }

    data class MaterialItem(val hubType: ChoiceButton, val detail: String, val image: Any?,
                            val buttonTextOne: String? = null, val actionOne: (() -> Unit)? = null,
                            val buttonTextTwo: String? = null, val actionTwo: (() -> Unit)? = null,
                            @DrawableRes val bgImage: Int? = null, val actionOrientation: Int = LinearLayout.VERTICAL) {
        var title: String = hubType.title
    }

    class MaterialAdapter(
            list: ArrayList<MaterialItem>,
            private val context: Context,
            private val onPress: MaterialItem.() -> Unit
    ) : DragSwipeAdapter<MaterialItem, ViewHolder>(list) {

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            list[position].apply {
                holder.title.text = title
                holder.detail.text = detail
                Glide.with(context).load(image).error(R.drawable.apk).into(holder.image)
                holder.actionOne.visibility = if (buttonTextOne.isNullOrBlank()) View.GONE else View.VISIBLE
                holder.actionTwo.visibility = if (buttonTextTwo.isNullOrBlank()) View.GONE else View.VISIBLE
                holder.actionOne.text = buttonTextOne
                holder.actionTwo.text = buttonTextTwo
                holder.actionOne.setOnClickListener { actionOne?.invoke() }
                holder.actionTwo.setOnClickListener { actionTwo?.invoke() }
                holder.itemView.setOnClickListener { this.onPress() }
                holder.image.setBackgroundResource(0)
                bgImage?.let {
                    holder.image.setBackgroundResource(it)
                }
                holder.actionLayout.orientation = actionOrientation
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                    LayoutInflater.from(context).inflate(
                            R.layout.material_card_hub_item,
                            parent,
                            false
                    )
            )
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.material_Image!!
        val title: TextView = view.material_title!!
        val detail: TextView = view.material_detail!!
        val actionOne: MaterialButton = view.action_one!!
        val actionTwo: MaterialButton = view.action_two!!
        val actionLayout: LinearLayout = view.action_layout!!
    }

    override fun onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result.isDrawerOpen) {
            result.closeDrawer()
        } else {
            if (br != null)
                unregisterReceiver(br)
            super.onBackPressed()
        }
    }

    override fun onDestroy() {
        saveAdapterLocations()
        try {
            if (br != null)
                unregisterReceiver(br)
        } catch (e: IllegalArgumentException) {

        }
        super.onDestroy()
    }

    fun sendProgressNotification(title: String, text: String, progress: Int, context: Context, gotoActivity: Class<*>, notification_id: Int) {

        val mBuilder = NotificationCompat.Builder(this@ChoiceActivity, ConstantValues.CHANNEL_ID)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon)
                .setOngoing(true)
                .setContentTitle(title)
                .setChannelId("update_notification")
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

    private fun getAppPermissions(info: AppInfo) {
        Permissions.check(this@ChoiceActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                "Storage permissions are required because so we can download videos",
                Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                object : PermissionHandler() {
                    override fun onGranted() {
                        //do your task
                        getNewApp(info)
                    }

                    override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
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
        if (resetChoice) {
            resetChoice = false
            recreate()
        }
    }

    private lateinit var mAuth: FirebaseAuth

    private lateinit var googleSignInClient: GoogleSignInClient

    private val loginOutItem: PrimaryDrawerItem = PrimaryDrawerItem()
            .withIcon(GoogleMaterial.Icon.gmd_local_gas_station)
            .withSelectable(false)
            .withIdentifier(23)


    lateinit var headerResult: AccountHeader

    private fun updateUI(user: FirebaseUser?) {
        val pInfo = packageManager.getPackageInfo(packageName, 0)
        val version = pInfo.versionName
        loginOutItem.withName(if (user != null) "Logout" else "Login")
        val profile = ProfileDrawerItem()
                .withName("For Us Nerds")
                .withEmail("App Version: $version")
                .withIdentifier(32)
        if (user != null) {
            if(user.photoUrl==null) {
                profile.withIcon(GoogleMaterial.Icon.gmd_android)
            } else {
                profile.withIcon(user.photoUrl)
            }
        } else {
            profile.withIcon(GoogleMaterial.Icon.gmd_android)
        }
        headerResult.updateProfile(profile)
        result.updateItem(loginOutItem)
    }

    override fun onStart() {
        super.onStart()
        val currentUser = mAuth.currentUser
        updateUI(currentUser)
    }

    lateinit var hud: KProgressHUD

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun signOut() {
        mAuth.signOut()

        googleSignInClient.signOut().addOnCompleteListener(this) {
            updateUI(null)
        }
    }

    val RC_SIGN_IN = 34

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Loged.d("firebaseAuthWithGoogle:" + acct.id!!)
        // [START_EXCLUDE silent]
        hud.setDetailsLabel("Logging In")
        hud.show()
        // [END_EXCLUDE]

        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Loged.d("signInWithCredential:success")
                        val user = mAuth.currentUser
                        updateUI(user)
                    } else {
                        // If sign in fails, display a message to the user.
                        Loged.w("signInWithCredential:failure ${task.exception}")
                        Snackbar.make(material_rv, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                        updateUI(null)
                    }

                    // [START_EXCLUDE]
                    hud.dismiss()
                    // [END_EXCLUDE]
                }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Loged.w("Google sign in failed $e")
                // ...
            }
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
                    val intented = Intent(this@ChoiceActivity, FavoriteShowsActivity::class.java)
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
        val shortCutItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_router)
                .withSelectable(false)
                .withIdentifier(13)
                .withName("Add A Shortcut")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    var choice: ChoiceButton? = null
                    val items = ChoiceButton.values()
                            .filter {
                                it !in arrayOf(ChoiceButton.QUICK_CHOICE,
                                        ChoiceButton.VIEW_TESTING,
                                        ChoiceButton.SETTINGS,
                                        ChoiceButton.RECENT_ANIME,
                                        ChoiceButton.RECENT_CARTOON,
                                        ChoiceButton.UPDATE_APP,
                                        ChoiceButton.UPDATE_NOTES,
                                        ChoiceButton.DOWNLOAD_APK,
                                        ChoiceButton.DELETE_OLD_FILE,
                                        ChoiceButton.VIEW_DOWNLOADS,
                                        ChoiceButton.VIEW_VIDEOS,
                                        ChoiceButton.QUICK_CHOICE,
                                        ChoiceButton.FEEDBACK,
                                        ChoiceButton.VIEW_TESTING,
                                        ChoiceButton.CHAT)
                            }
                            .map { it.title }
                            .toTypedArray()
                    MaterialAlertDialogBuilder(this)
                            .setTitle("Choose an item to add a shortcut")
                            .setSingleChoiceItems(items, -1) { _, num ->
                                choice = try {
                                    ChoiceButton.getChoiceFromTitle(items[num])
                                } catch (e: Exception) {
                                    null
                                }
                            }
                            .setPositiveButton("Ok!") { _, _ ->
                                if (choice != null) {
                                    val shortcut = ShortcutInfo.Builder(this, choice!!.id)
                                            .setShortLabel(choice!!.title.take(9))
                                            .setLongLabel(choice!!.title)
                                    val intent = when (choice) {
                                        ChoiceButton.BLACKJACK -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.blackjacklogo))
                                            Intent(this, BlackJackActivity::class.java)
                                        }
                                        ChoiceButton.SOLITAIRE -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.solitairelogo))
                                            Intent(this, SolitaireActivity::class.java).apply {
                                                putExtra(ConstantValues.DRAW_AMOUNT, KUtility.getSharedPref(this@ChoiceActivity).getInt(ConstantValues.DRAW_AMOUNT, 1))
                                            }
                                        }
                                        ChoiceButton.CALCULATION -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.calculationlogo))
                                            Intent(this, CalculationActivity::class.java)
                                        }
                                        ChoiceButton.VIDEO_POKER -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.pokerlogo))
                                            Intent(this, VideoPokerActivity::class.java)
                                        }
                                        ChoiceButton.MATCHING -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.matchinglogo))
                                            Intent(this, MatchingActivityTwo::class.java)
                                        }
                                        ChoiceButton.HILO -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.hilologo))
                                            Intent(this, HiLoActivity::class.java)
                                        }
                                        ChoiceButton.CHESS -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.black_chess_knight))
                                            Intent(this, MainActivity::class.java)
                                        }
                                        ChoiceButton.YAHTZEE -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.yahtzeelogo))
                                            Intent(this, YahtzeeActivity::class.java)
                                        }
                                        ChoiceButton.PONG -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.apk))
                                            Intent(this, PongActivity::class.java)
                                        }
                                        ChoiceButton.MUSIC_MATCH -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.matchinglogo))
                                            Intent(this, MusicGameActivity::class.java)
                                        }
                                        ChoiceButton.ANIME -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.ten2))
                                            Intent(this, ShowListActivity::class.java).apply {
                                                putExtra(ConstantValues.SHOW_LINK, Source.ANIME.link)
                                            }
                                        }
                                        ChoiceButton.CARTOON -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.cartoon_cover))
                                            Intent(this, ShowListActivity::class.java).apply {
                                                putExtra(ConstantValues.SHOW_LINK, Source.CARTOON.link)
                                            }
                                        }
                                        ChoiceButton.DUBBED -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.ten4))
                                            Intent(this, ShowListActivity::class.java).apply {
                                                putExtra(ConstantValues.SHOW_LINK, Source.DUBBED.link)
                                            }
                                        }
                                        ChoiceButton.ANIME_MOVIES -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.mov))
                                            Intent(this, ShowListActivity::class.java).apply {
                                                putExtra(ConstantValues.SHOW_LINK, Source.ANIME_MOVIES.link)
                                                putExtra(ConstantValues.SHOW_MOVIE, true)
                                            }
                                        }
                                        ChoiceButton.CARTOON_MOVIES -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.cartoon_movies_cover))
                                            Intent(this, ShowListActivity::class.java).apply {
                                                putExtra(ConstantValues.SHOW_LINK, Source.CARTOON_MOVIES.link)
                                                putExtra(ConstantValues.SHOW_MOVIE, true)
                                            }
                                        }
                                        ChoiceButton.LIVE_ACTION -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.mov))
                                            Intent(this, ShowListActivity::class.java).apply {
                                                putExtra(ConstantValues.SHOW_LINK, Source.LIVE_ACTION.link)
                                            }
                                        }
                                        ChoiceButton.RECENT_LIVE_ACTION -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.recents))
                                            Intent(this, ShowListActivity::class.java).apply {
                                                putExtra(ConstantValues.SHOW_LINK, Source.RECENT_LIVE_ACTION.link)
                                                putExtra(ConstantValues.RECENT_OR_NOT, true)
                                            }
                                        }
                                        ChoiceButton.VIEW_FAVORITES -> {
                                            shortcut.setIcon(Icon.createWithResource(this, android.R.drawable.ic_input_get))
                                            Intent(this, FavoriteShowsActivity::class.java)
                                        }
                                        ChoiceButton.RSS_FEED -> {
                                            shortcut.setIcon(Icon.createWithResource(this, android.R.drawable.ic_menu_today))
                                            Intent(this, RssActivity::class.java)
                                        }
                                        ChoiceButton.SHOW_QUIZ -> {
                                            shortcut.setIcon(Icon.createWithResource(this, R.drawable.b_normal))
                                            Intent(this, QuizShowActivity::class.java)
                                        }
                                        else -> Intent(this, ChoiceActivity::class.java)
                                    }.apply {
                                        action = Intent.ACTION_VIEW
                                    }
                                    shortcut.setIntent(intent)
                                    val short = shortcut.build()
                                    val shortcutManager = getSystemService(ShortcutManager::class.java)!!
                                    if (shortcutManager.isRequestPinShortcutSupported) {
                                        shortcutManager.requestPinShortcut(short, null)
                                    } else {
                                        Toast.makeText(this, "Pinned shortcuts are not supported!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                            .show()
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
        val clearNotiItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_notifications_off)
                .withSelectable(false)
                .withIdentifier(2)
                .withName("Clear Notifications")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    mNotificationManager.cancelAll()
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
                    if (Utility.isNetworkToast(this@ChoiceActivity))
                        GlobalScope.launch {
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
                    if (Utility.isNetworkToast(this@ChoiceActivity))
                        GlobalScope.launch {
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
            if (Utility.isNetworkToast(this@ChoiceActivity))
                GlobalScope.launch {
                    val showCheck = Intent(this@ChoiceActivity, ShowCheckIntentService::class.java)
                    startService(showCheck)
                }
            true
        }
        loginOutItem
                .withName(if (mAuth.currentUser != null) "Logout" else "Login")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    if (mAuth.currentUser != null) {
                        MaterialAlertDialogBuilder(this@ChoiceActivity)
                                .setTitle("Sign Out")
                                .setMessage("Are you sure?")
                                .setPositiveButton("Yes") { _, _ ->
                                    signOut()
                                }
                                .setNegativeButton("No") { _, _ ->

                                }
                                .show()
                    } else {
                        signIn()
                    }
                    true
                }
        val syncDataItem = PrimaryDrawerItem()
                .withIcon(GoogleMaterial.Icon.gmd_save)
                .withSelectable(false)
                .withIdentifier(25)
                .withName("Sync Data")
                .withOnDrawerItemClickListener { _, _, _ ->
                    result.closeDrawer()
                    FirebaseDB(this).getAndStore()
                    FirebaseDB(this).storeAllSettings()
                    FirebaseDB(this).loadAllSettings()
                    true
                }

        // Create the AccountHeader
        headerResult = AccountHeaderBuilder()
                .withActivity(this)
                .withDividerBelowHeader(true)
                .withCurrentProfileHiddenInList(true)
                .withOnlyMainProfileImageVisible(false)
                .addProfiles(
                        ProfileDrawerItem()
                                .withName("For Us Nerds")
                                .withEmail("App Version: $version")
                                .withIcon(GoogleMaterial.Icon.gmd_android)
                                .withIdentifier(32)
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
                        shortCutItem,
                        DividerDrawerItem(),
                        clearNotiItem,
                        DividerDrawerItem(),
                        updateNotesItem,
                        DividerDrawerItem(),
                        updateAppItem,
                        loginOutItem,
                        syncDataItem
                )
                .withDisplayBelowStatusBar(true)
                .withTranslucentStatusBar(true)
                .withCloseOnClick(false)
                .withSelectedItem(-1)
                .withSavedInstance(savedInstanceState)
                .withInnerShadow(true)
                .build()

        GlobalScope.launch {
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

        if (Utility.isNetwork(this@ChoiceActivity))
            GlobalScope.launch {
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

                br = BroadcastReceiverDownload(object : DownloadBroadcast {
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
                            val downloaded = (intent.getStringExtra("view_download_item_count") ?: "0").toInt()
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

        var resetChoice = false

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

    class BroadcastReceiverUpdate : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Loged.i("Starting up")
            //start activity
            val i = Intent()
            i.setClassName(context.packageName, context.packageName + ".ChoiceActivity")
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

    fun permissionCheck(clazz: Class<out Any>, rec: Boolean = false, url: String? = null, movie: Boolean? = null, shouldFinish: Boolean = false, view: View? = null) {
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
                        if (movie != null)
                            intent.putExtra(ConstantValues.SHOW_MOVIE, movie)
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

                    override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
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
