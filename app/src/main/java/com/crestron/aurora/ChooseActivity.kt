package com.crestron.aurora

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import antonkozyriatskyi.devdrawer.DevDrawer
import com.crashlytics.android.Crashlytics
import com.crestron.aurora.boardgames.chess.MainActivity
import com.crestron.aurora.boardgames.tictactoe.TTTActivity
import com.crestron.aurora.boardgames.yahtzee.YahtzeeActivity
import com.crestron.aurora.cardgames.BlackJackActivity
import com.crestron.aurora.cardgames.calculation.CalculationActivity
import com.crestron.aurora.cardgames.hilo.HiLoActivity
import com.crestron.aurora.cardgames.matching.MatchingActivity
import com.crestron.aurora.cardgames.solitaire.SolitaireActivity
import com.crestron.aurora.cardgames.videopoker.VideoPokerActivity
import com.crestron.aurora.otherfun.DownloadViewerActivity
import com.crestron.aurora.otherfun.ShowListActivity
import com.github.jinatonic.confetti.CommonConfetti
import com.nabinbhandari.android.permissions.PermissionHandler
import com.nabinbhandari.android.permissions.Permissions
import io.kimo.konamicode.KonamiCode
import kotlinx.android.synthetic.main.activity_choose.*
import org.jetbrains.anko.defaultSharedPreferences
import java.util.*


class ChooseActivity : AppCompatActivity() {

    private var colored = false
    private lateinit var konami: KonamiCode
    private val PERSON_OF_INTEREST = arrayOf("Ilene Rein", "Tina Gross")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose)

        Crashlytics.log(Log.ASSERT, Loged.TAG, "a;lkdsfj")

        //Color.rgb(20, 46, 18)

        DevDrawer.attachTo(this, gravity = Gravity.START, enableInRelease = true) {

            checkbox {
                text = "Enable logging"
                onCheckedChange { isChecked -> Loged.i("Logging enabled: $isChecked") }
            }

            switch {
                text = "God mode"
                onCheckedChange { isChecked -> Loged.i("God mode switched: $isChecked") }
            }

            button {
                text = "Crash"
                onClick { throw Exception("Intended crash") }
            }

            spinner {
                item { "Auto" }
                addItem("Dark")
                item { "Light" }

                onItemSelected { item, position -> Loged.i("$item at $position") }
            }

        }

        chessChoice.background.setTint(getColor(R.color.alizarin))
        solitaireChoice.background.setTint(getColor(R.color.emeraldGreen))
        blackjackChoice.background.setTint(getColor(R.color.emeraldGreen))
        matchingChoice.background.setTint(getColor(R.color.emeraldGreen))
        recentChoice.background.setTint(getColor(R.color.peter_river))
        termChoice.background.setTint(getColor(R.color.peter_river))
        settingChoice.background.setTint(getColor(R.color.peter_river))
        downloadChoice.background.setTint(getColor(R.color.peter_river))

        val length = defaultSharedPreferences.getFloat(ConstantValues.UPDATE_CHECK, 1f)
        //FunApplication.checkUpdater(this, length)

        /*Permissions.check(this@ChooseActivity, arrayOf(Manifest.permission.READ_CONTACTS),
                "Need this for my mom",
                Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                object : PermissionHandler() {
                    override fun onGranted() {
                        //do your task
                        val c = contentResolver.query(ContactsContract.Profile.CONTENT_URI, null, null, null, null)
                        val count = c!!.count
                        val columnNames = c.columnNames
                        c.moveToFirst()
                        val position = c.position
                        var wantedPerson = false
                        if (count == 1 && position == 0) {
                            for (j in columnNames.indices) {
                                val columnName = columnNames[j]
                                val columnValue = c.getString(c.getColumnIndex(columnName))
                                // consume the values here
                                Loged.i("ColumnName is: $columnName and ColumnValue is: $columnValue")
                                if(columnName=="display_name" && columnValue in PERSON_OF_INTEREST) {
                                    wantedPerson = true
                                    break
                                }
                            }
                        }
                        c.close()

                        if(wantedPerson) {
                            val intent = Intent(this@ChooseActivity, ChoiceActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    }

                    override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                        super.onDenied(context, deniedPermissions)
                        val permArray = deniedPermissions!!.toTypedArray()
                        Permissions.check(this@ChooseActivity, permArray,
                                "Need this for my mom",
                                Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                                this)
                    }
                })*/

        /*recentChoice.visibility = View.GONE
        termChoice.visibility = View.GONE
        settingChoice.visibility = View.GONE
        downloadChoice.visibility = View.GONE*/

        Loged.FILTER_BY_CLASS_NAME = "crestron"

        chooseOne.setOnClickListener {
            Loged.wtf("Nope!", "Nothing here")
            Loged.wtf("Nope!", tag = "Nothing here")
            Loged.wtf(msg = "Nope!", tag = "Nothing here")
            Loged.wtf("a;slkdfj")
        }

        chooseOne.setOnLongClickListener {
            val intent = Intent(this@ChooseActivity, ChoiceActivity::class.java)
            startActivity(intent)
            true
        }

        chessChoice.setOnClickListener {
            val intent = if (colored) {
                Intent(this, TTTActivity::class.java)
            } else {
                Intent(this, MainActivity::class.java)
            }
            startActivity(intent)
        }

        chessChoice.setOnLongClickListener {
            val intent = Intent(this, YahtzeeActivity::class.java)
            startActivity(intent)
            true
        }

        /*termChoice.setOnClickListener {
            val intent = if (colored)
                Intent(this, TerminalActivity::class.java)
            else
                Intent(this, TerminalActivity::class.java)
            startActivity(intent)
        }*/

        fun permissionCheck(clazz: Class<out Any>, rec: Boolean? = null, movie: Boolean? = null, cartoon: Boolean? = null, dubbed: Boolean? = null) {
            Permissions.check(this@ChooseActivity, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                    "Storage permissions are required because so we can download videos",
                    Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                    object : PermissionHandler() {
                        override fun onGranted() {
                            //do your task
                            val intent = Intent(this@ChooseActivity, clazz)
                            if (rec != null)
                                intent.putExtra(ConstantValues.RECENT_OR_NOT, rec)
                            if (movie != null)
                                intent.putExtra("movie", movie)
                            if (cartoon != null)
                                intent.putExtra("cartoon", cartoon)
                            if (dubbed != null)
                                intent.putExtra("dubbed", dubbed)
                            startActivity(intent)
                        }

                        override fun onDenied(context: Context?, deniedPermissions: ArrayList<String>?) {
                            super.onDenied(context, deniedPermissions)
                            val permArray = deniedPermissions!!.toTypedArray()
                            Permissions.check(this@ChooseActivity, permArray,
                                    "Storage permissions are required because so we can download videos",
                                    Permissions.Options().setSettingsDialogTitle("Warning!").setRationaleDialogTitle("Info"),
                                    this)
                        }
                    })
        }


        termChoice.setOnClickListener {
            permissionCheck(ShowListActivity::class.java, false, false, true)
        }

        termChoice.setOnLongClickListener {
            permissionCheck(ShowListActivity::class.java, false)
            true
        }

        /*settingChoice.setOnClickListener {
            permissionCheck(SettingsActivity::class.java)
        }*/
        settingChoice.setOnClickListener {
            permissionCheck(SettingsActivity2::class.java)
        }

        settingChoice.setOnLongClickListener {
            permissionCheck(SettingsActivity::class.java)
            true
        }

        recentChoice.setOnClickListener {
            permissionCheck(ShowListActivity::class.java, true)
        }

        recentChoice.setOnLongClickListener {
            permissionCheck(ShowListActivity::class.java, false, dubbed = true)
            true
        }

        cartoonChoice.setOnClickListener {
            permissionCheck(ShowListActivity::class.java, false, true)
        }

        cartoonChoice.setOnLongClickListener {
            permissionCheck(ShowListActivity::class.java, false, true, true)
            true
        }

        blackjackChoice.setOnClickListener {
            val intent = if (colored) {
                Intent(this, CalculationActivity::class.java)
            } else {
                Intent(this, BlackJackActivity::class.java)
            }
            startActivity(intent)
        }

        blackjackChoice.setOnLongClickListener {
            val intent = Intent(this, CalculationActivity::class.java)
            startActivity(intent)
            true
        }

        draw_amount.setText("${defaultSharedPreferences.getInt(ConstantValues.DRAW_AMOUNT, 1)}")

        solitaireChoice.setOnClickListener {
            val intent = Intent(this, SolitaireActivity::class.java)
            val num = "${draw_amount.text}".toInt()
            val edit = defaultSharedPreferences.edit()
            edit.putInt(ConstantValues.DRAW_AMOUNT, num)
            edit.apply()
            intent.putExtra(ConstantValues.DRAW_AMOUNT, num)
            startActivity(intent)
        }

        solitaireChoice.setOnLongClickListener {
            val intent = Intent(this, VideoPokerActivity::class.java)
            startActivity(intent)
            true
        }

        matchingChoice.setOnClickListener {
            val intent = Intent(this, MatchingActivity::class.java)
            startActivity(intent)
        }

        matchingChoice.setOnLongClickListener {
            val intent = Intent(this, HiLoActivity::class.java)
            startActivity(intent)
            true
        }

        downloadChoice.setOnClickListener {
            val intent = Intent(this@ChooseActivity, DownloadViewerActivity::class.java)
            startActivity(intent)
        }

        val colors = IntArray(11)
        colors[0] = Color.BLUE
        colors[1] = Color.YELLOW
        colors[2] = Color.RED
        colors[3] = Color.BLACK
        colors[4] = Color.CYAN
        colors[5] = Color.DKGRAY
        colors[6] = Color.GRAY
        colors[7] = Color.GREEN
        colors[8] = Color.LTGRAY
        colors[9] = Color.MAGENTA
        colors[10] = Color.WHITE

        funButton.setOnClickListener {
            CommonConfetti.rainingConfetti(layout, colors)
                    .stream(1000)
                    .setNumInitialCount(100)
                    .setTouchEnabled(true).animate()
        }

        konami = KonamiCode.Installer(this)
                .on(this)
                .callback {
                    Toast.makeText(this@ChooseActivity, "Super TSR Mode Activated!", Toast.LENGTH_LONG).show()
                }
                .install()

    }

}
