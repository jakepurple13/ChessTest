package com.crestron.aurora

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.preference.*
import android.text.TextUtils
import android.view.MenuItem
import android.widget.Toast
import com.codekidlabs.storagechooser.StorageChooser
import com.crestron.aurora.db.Show
import com.crestron.aurora.db.ShowDatabase
import com.crestron.aurora.otherfun.FavoriteShowsActivity
import com.crestron.aurora.otherfun.FetchingUtils
import com.crestron.aurora.utilities.KUtility
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.android.Main
import kotlinx.coroutines.launch
import mobi.upod.timedurationpicker.TimeDurationPicker
import mobi.upod.timedurationpicker.TimeDurationPickerPreference
import mobi.upod.timedurationpicker.TimeDurationUtil
import org.jetbrains.anko.defaultSharedPreferences
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


/**
 * A [PreferenceActivity] that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 *
 * See [Android Design: Settings](http://developer.android.com/design/patterns/settings.html)
 * for design guidelines and the [Settings API Guide](http://developer.android.com/guide/topics/ui/settings.html)
 * for more information on developing a Settings UI.
 */
class SettingsActivity2 : AppCompatPreferenceActivity() {

    var shouldReset = false

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Display the fragment as the main content.
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, GeneralPreferenceFragment())
                .commit()
        setupActionBar()
    }*/

    override fun onBackPressed() {
        //val intent = Intent(this@SettingsActivity2, ChoiceActivity::class.java)
        //startActivity(intent)
        //finish()
        val f = fragmentManager.findFragmentById(android.R.id.content)
        if (f is GeneralPreferenceFragment) {
            val returnIntent = Intent()
            returnIntent.putExtra("restart", shouldReset)
            setResult(Activity.RESULT_OK, returnIntent)
        }
        //finish()
        super.onBackPressed()
    }

    /**
     * Set up the [android.app.ActionBar], if the API is available.
     */
    private fun setupActionBar() {
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onIsMultiPane(): Boolean {
        return isXLargeTablet(this)
    }

    /**
     * {@inheritDoc}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    override fun onBuildHeaders(target: List<PreferenceActivity.Header>) {
        loadHeadersFromResource(R.xml.pref_headers, target)
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    override fun isValidFragment(fragmentName: String): Boolean {
        return PreferenceFragment::class.java.name == fragmentName
                || GeneralPreferenceFragment::class.java.name == fragmentName
                || WifiOnlyPreferenceFragment::class.java.name == fragmentName
    }

    /**
     * This fragment shows general preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    class GeneralPreferenceFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

            when (key) {
                ConstantValues.FOLDER_LOCATION -> {
                    val folder = findPreference(key)
                    // Set summary to be the user-description for the selected value

                    //chooseFolderLocation()

                    folder.summary = sharedPreferences!!.getString(key, FetchingUtils.folderLocation)
                }
                /*ConstantValues.UPDATE_CHECK + "s" -> {
                    val updateCheck = (findPreference(ConstantValues.UPDATE_CHECK + "s") as EditTextPreference)
                    *//*val length = try {
                        updateCheck.text.toString().toFloat()
                    } catch (e: NumberFormatException) {
                        1.0f
                    }*//*

                    val length = sharedPreferences!!.getString(key, "1f")!!.toFloat()

                    defaultSharedPreferences.edit().putFloat(ConstantValues.UPDATE_CHECK, length).apply()
                    //FunApplication.cancelChecker(this@GeneralPreferenceFragment.context)
                    //FunApplication.checkUpdater(this@GeneralPreferenceFragment.context, length)
                    KUtility.currentUpdateTime = length
                    KUtility.cancelAlarm(this@GeneralPreferenceFragment.context)
                    KUtility.scheduleAlarm(this@GeneralPreferenceFragment.context, length)

                    *//*findPreference("next_update_check").summary = try {
                        "≈ ${SimpleDateFormat("MM/dd/yyyy E hh:mm a").format(KUtility.nextCheckTime)}"
                        //SimpleDateFormat("MM/dd/yyyy E hh:mm:ss a").format(alarm.nextAlarmClock.triggerTime)
                    } catch (e: IllegalStateException) {
                        "N/A"
                    } catch (e: NullPointerException) {
                        "N/A"
                    }*//*

                    findPreference(ConstantValues.UPDATE_CHECK + "s").summary = FetchingUtils.getETAString((1000 * 60 * 60 * length.toDouble()).toLong(), false)
                }*/
                "run_update_check" -> {
                    if (sharedPreferences!!.getBoolean(key, false)) {
                        KUtility.cancelAlarm(this.context)
                    } else {
                        KUtility.setAlarmUp(this.context)
                    }
                    findPreference("pref_duration").isEnabled = sharedPreferences.getBoolean(key, true)
                }
                ConstantValues.NUMBER_OF_RANDOM -> {
                    findPreference(key).summary = "The Number of Random Favorites to Display: ${PreferenceManager.getDefaultSharedPreferences(this@GeneralPreferenceFragment.context).getString(key, "1")!!.toInt()}"
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (requestCode == Activity.RESULT_OK || resultCode == 3) {
                if (data?.hasExtra("restart")!!) {
                    (this@GeneralPreferenceFragment.activity as SettingsActivity2).shouldReset = data.getBooleanExtra("restart", false)
                }
            }
        }

        @SuppressLint("SimpleDateFormat")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            //addPreferencesFromResource(R.xml.pref_general)
            addPreferencesFromResource(R.xml.settings_list)
            setHasOptionsMenu(true)

            // Bind the summaries of EditText/List/Dialog/Ringtone preferences
            // to their values. When their values change, their summaries are
            // updated to reflect the new value, per the Android Design
            // guidelines.
            //bindPreferenceSummaryToValue(findPreference("folder_location"))
            //bindPreferenceSummaryToValue(findPreference("wifiOnly"))

            findPreference(ConstantValues.FOLDER_LOCATION).summary = FetchingUtils.folderLocation

            /*(findPreference("pref_duration") as TimeDurationPickerPreference)
                    .timeDurationPicker
                    .setTimeUnits(TimeDurationPicker.HH_MM)*/

            (findPreference("pref_duration") as TimeDurationPickerPreference).setOnPreferenceChangeListener { preference, newValue ->
                val text = TimeDurationUtil.formatHoursMinutesSeconds(newValue as Long)
                val length = context.defaultSharedPreferences.getFloat(ConstantValues.UPDATE_CHECK, 1f)
                Loged.d("$text or $newValue and Current values is ${1000.0 * 60.0 * 60.0 * length}")
                KUtility.currentDurationTime = newValue
                KUtility.cancelAlarm(this@GeneralPreferenceFragment.context)
                KUtility.scheduleAlarm(this@GeneralPreferenceFragment.context, newValue)
                true
            }

            findPreference("pref_duration").isEnabled = defaultSharedPreferences.getBoolean("run_update_check", true)

            //findPreference(ConstantValues.UPDATE_CHECK + "s").summary = FetchingUtils.getETAString((1000 * 60 * 60 * KUtility.currentUpdateTime).toLong(), false)
            //findPreference(ConstantValues.UPDATE_CHECK + "s").summary = FetchingUtils.getETAString(KUtility.currentDurationTime, false)

            //val alarm = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            /*findPreference("next_update_check").summary = try {
                "≈ ${SimpleDateFormat("MM/dd/yyyy E hh:mm a").format(KUtility.nextCheckTime)}"
                //SimpleDateFormat("MM/dd/yyyy E hh:mm:ss a").format(alarm.nextAlarmClock.triggerTime)
            } catch (e: IllegalStateException) {
                "N/A"
            } catch (e: NullPointerException) {
                "N/A"
            }

            findPreference("next_update_check").setOnPreferenceClickListener {
                findPreference("next_update_check").summary = try {
                    "≈ ${SimpleDateFormat("MM/dd/yyyy E hh:mm a").format(KUtility.nextCheckTime)}"
                } catch (e: IllegalStateException) {
                    "N/A"
                } catch (e: NullPointerException) {
                    "N/A"
                }
                true
            }*/

            //findPreference("next_update_check").summary = SimpleDateFormat("MM/dd/yyyy E hh:mm:ss a").format(KUtility.nextCheckTime)

            findPreference(ConstantValues.FOLDER_LOCATION).setOnPreferenceClickListener {
                chooseFolderLocation()
                true
            }

            findPreference("show_show").setOnPreferenceClickListener {
                //(this@GeneralPreferenceFragment.activity as SettingsActivity2).shouldReset = true
                val intent = Intent(this@GeneralPreferenceFragment.context, FavoriteShowsActivity::class.java)
                intent.putExtra("displayText", "Choose What Shows To Display on the Home screen")
                intent.putExtra("homeScreen", true)
                startActivityForResult(intent, 3)
                true
            }

            findPreference(ConstantValues.NUMBER_OF_RANDOM).summary = "The Number of Random Favorites to Display: ${PreferenceManager.getDefaultSharedPreferences(this@GeneralPreferenceFragment.context).getString(ConstantValues.NUMBER_OF_RANDOM, "1")!!.toInt()}"

            findPreference("export_favorites").setOnPreferenceClickListener {

                val st = StorageChooser.Theme(this@GeneralPreferenceFragment.context)

                st.scheme = resources.getIntArray(R.array.paranoid_theme)

                // Initialize Builder
                val chooser = StorageChooser.Builder()
                        .withActivity(this@GeneralPreferenceFragment.activity)
                        .withFragmentManager(fragmentManager)
                        .withMemoryBar(true)
                        .actionSave(true)
                        .allowAddFolder(true)
                        .allowCustomPath(true)
                        .disableMultiSelect()
                        .withPredefinedPath(Environment.DIRECTORY_DOWNLOADS)
                        .setType(StorageChooser.DIRECTORY_CHOOSER)
                        .setTheme(st)
                        .build()

                // Show dialog whenever you want by
                chooser.show()

                // get path that the user has chosen
                chooser.setOnSelectListener { path ->
                    //folder_location_info.text = FetchingUtils.folderLocation
                    GlobalScope.launch {
                        val show = ShowDatabase.getDatabase(this@GeneralPreferenceFragment.context).showDao().allShows
                        val string = Gson().toJson(show)

                        Loged.wtf("$path and $string")

                        val file = File("$path/fun.json")
                        if (!file.exists())
                            file.createNewFile()

                        val stream = FileOutputStream(file)
                        stream.use { streams ->
                            streams.write(string.toByteArray())
                        }
                        GlobalScope.launch(Dispatchers.Main) {
                            Toast.makeText(this@GeneralPreferenceFragment.context, "Finished Exporting", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                true
            }

            findPreference("import_favorites").setOnPreferenceClickListener {

                val st = StorageChooser.Theme(this@GeneralPreferenceFragment.context)

                st.scheme = resources.getIntArray(R.array.paranoid_theme)

                val chooser = StorageChooser.Builder()
                        .withActivity(this@GeneralPreferenceFragment.activity)
                        .withFragmentManager(fragmentManager)
                        .withMemoryBar(true)
                        .actionSave(true)
                        .allowAddFolder(true)
                        .allowCustomPath(true)
                        .disableMultiSelect()
                        .withPredefinedPath(Environment.DIRECTORY_DOWNLOADS)
                        .setType(StorageChooser.FILE_PICKER)
                        .setTheme(st)
                        .build()

                // Show dialog whenever you want by
                chooser.show()

                // get path that the user has chosen
                chooser.setOnSelectListener { path ->
                    if (path.contains("fun.json")) {
                        GlobalScope.launch {
                            val show = ShowDatabase.getDatabase(this@GeneralPreferenceFragment.context).showDao()
                            val g = Gson().fromJson(mReadJsonData(path), Array<Show>::class.java)
                            for (i in g) {
                                if (show.isInDatabase(i.name) <= 0) {
                                    show.insert(i)
                                }
                            }
                        }
                    }
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(this@GeneralPreferenceFragment.context, "Finished Importing", Toast.LENGTH_SHORT).show()
                    }
                }
                true
            }

            findPreference("send_feedback").setOnPreferenceClickListener {
                val intented = Intent(this@GeneralPreferenceFragment.context, FormActivity::class.java)
                startActivity(intented)
                true
            }

            val pInfo = this@GeneralPreferenceFragment.context.packageManager.getPackageInfo(this@GeneralPreferenceFragment.context.packageName, 0)
            val version = pInfo.versionName

            findPreference("user_version").summary = "Version: $version"

        }

        data class Shows(
                @SerializedName("link") val link: String,
                @SerializedName("name") val name: String,
                @SerializedName("showNum") val showNum: Double
        )

        fun mReadJsonData(params: String): String? {
            return try {
                val f = File(params)
                val `is` = FileInputStream(f)
                val size = `is`.available()
                val buffer = ByteArray(size)
                `is`.read(buffer)
                `is`.close()
                String(buffer)
            } catch (e: IOException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
                null
            }

        }

        override fun onResume() {
            super.onResume()
            preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        override fun onPause() {
            preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
            super.onPause()
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val id = item.itemId
            if (id == android.R.id.home) {
                startActivity(Intent(activity, SettingsActivity2::class.java))
                return true
            }
            return super.onOptionsItemSelected(item)
        }

        private fun chooseFolderLocation() {
            val st = StorageChooser.Theme(this@GeneralPreferenceFragment.context)

            st.scheme = resources.getIntArray(R.array.paranoid_theme)

            // Initialize Builder
            val chooser = StorageChooser.Builder()
                    .withActivity(this@GeneralPreferenceFragment.activity)
                    .withFragmentManager(fragmentManager)
                    .withMemoryBar(true)
                    .actionSave(true)
                    .allowAddFolder(true)
                    .allowCustomPath(true)
                    .disableMultiSelect()
                    .withPredefinedPath(FetchingUtils.folderLocation)
                    .setType(StorageChooser.DIRECTORY_CHOOSER)
                    .setTheme(st)
                    .build()

            // Show dialog whenever you want by
            chooser.show()

            // get path that the user has chosen
            chooser.setOnSelectListener { path ->
                Toast.makeText(this@GeneralPreferenceFragment.context, "FOLDER: $path/", Toast.LENGTH_SHORT).show()
                FetchingUtils.folderLocation = "$path/"
                //folder_location_info.text = FetchingUtils.folderLocation
            }
        }

    }

    companion object {

        /**
         * A preference value change listener that updates the preference's summary
         * to reflect its new value.
         */
        private val sBindPreferenceSummaryToValueListener = Preference.OnPreferenceChangeListener { preference, value ->
            val stringValue = value.toString()

            if (preference is ListPreference) {
                // For list preferences, look up the correct display value in
                // the preference's 'entries' list.
                val listPreference = preference
                val index = listPreference.findIndexOfValue(stringValue)

                // Set the summary to reflect the new value.
                preference.setSummary(
                        if (index >= 0)
                            listPreference.entries[index]
                        else
                            null)

            } else if (preference is RingtonePreference) {
                // For ringtone preferences, look up the correct display value
                // using RingtoneManager.
                if (TextUtils.isEmpty(stringValue)) {
                    // Empty values correspond to 'silent' (no ringtone).
                    preference.setSummary(R.string.pref_ringtone_silent)

                } else {
                    val ringtone = RingtoneManager.getRingtone(
                            preference.getContext(), Uri.parse(stringValue))

                    if (ringtone == null) {
                        // Clear the summary if there was a lookup error.
                        preference.setSummary(null)
                    } else {
                        // Set the summary to reflect the new ringtone display
                        // name.
                        val name = ringtone.getTitle(preference.getContext())
                        preference.setSummary(name)
                    }
                }

            } else {
                // For all other preferences, set the summary to the value's
                // simple string representation.
                preference.summary = stringValue
            }
            true
        }

        /**
         * Helper method to determine if the device has an extra-large screen. For
         * example, 10" tablets are extra-large.
         */
        private fun isXLargeTablet(context: Context): Boolean {
            return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_XLARGE
        }

        /**
         * Binds a preference's summary to its value. More specifically, when the
         * preference's value is changed, its summary (line of text below the
         * preference title) is updated to reflect the value. The summary is also
         * immediately updated upon calling this method. The exact display format is
         * dependent on the type of preference.

         * @see .sBindPreferenceSummaryToValueListener
         */
        private fun bindPreferenceSummaryToValue(preference: Preference) {
            // Set the listener to watch for value changes.
            preference.onPreferenceChangeListener = sBindPreferenceSummaryToValueListener

            // Trigger the listener immediately with the preference's
            // current value.
            sBindPreferenceSummaryToValueListener.onPreferenceChange(preference,
                    PreferenceManager
                            .getDefaultSharedPreferences(preference.context)
                            .getString(preference.key, ""))
        }
    }

    class WifiOnlyPreferenceFragment : PreferenceFragment(), SharedPreferences.OnSharedPreferenceChangeListener {

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.wifi_settings_list)
            setHasOptionsMenu(true)
        }

        override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
            when (key) {
                "downloading_wifi" -> FunApplication.fetchSetUp(this@WifiOnlyPreferenceFragment.context)
                else -> {

                }
            }
        }
    }
}
