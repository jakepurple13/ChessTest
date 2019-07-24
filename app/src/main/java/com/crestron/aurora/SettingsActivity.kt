package com.crestron.aurora

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.codekidlabs.storagechooser.StorageChooser
import com.crestron.aurora.otherfun.DownloadViewerActivity
import com.crestron.aurora.otherfun.FetchingUtils
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2.HttpUrlConnectionDownloader
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2core.Downloader
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.defaultSharedPreferences


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        folder_location_info.text = FetchingUtils.folderLocation

        folder_chooser.setOnClickListener {

            val st = StorageChooser.Theme(this@SettingsActivity)

            st.scheme = resources.getIntArray(R.array.paranoid_theme)

            // Initialize Builder
            val chooser = StorageChooser.Builder()
                    .withActivity(this@SettingsActivity)
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
                Toast.makeText(this@SettingsActivity, "FOLDER: $path/", Toast.LENGTH_SHORT).show()
                FetchingUtils.folderLocation = "$path/"
                folder_location_info.text = FetchingUtils.folderLocation
            }

        }

        auto_retry.isChecked = defaultSharedPreferences.getBoolean(ConstantValues.AUTO_RETRY, false)

        auto_retry.setOnCheckedChangeListener { _, isChecked ->
            defaultSharedPreferences.edit().putBoolean(ConstantValues.AUTO_RETRY, isChecked).apply()
        }

        wifi_only.isChecked = defaultSharedPreferences.getBoolean(ConstantValues.WIFI_ONLY, false)

        wifi_only.setOnCheckedChangeListener { _, isChecked ->
            defaultSharedPreferences.edit().putBoolean(ConstantValues.WIFI_ONLY, isChecked).apply()
        }

        download_viewer.setOnClickListener {
            val intent = Intent(this@SettingsActivity, DownloadViewerActivity::class.java)
            startActivity(intent)
        }

        download_number.setText("${getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, Context.MODE_PRIVATE).getInt("downloadNumber", 1)}")

        download_number_chooser.setOnClickListener {
            getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, Context.MODE_PRIVATE).edit().putInt("downloadNumber", download_number.text.toString().toInt()).apply()
            //Toast.makeText(this@SettingsActivity, "Will be in effect when app is restarted", Toast.LENGTH_SHORT).show()
            val fetchConfiguration = FetchConfiguration.Builder(this)
                    .enableAutoStart(true)
                    .enableRetryOnNetworkGain(true)
                    .setProgressReportingInterval(1000L)
                    .setGlobalNetworkType(NetworkType.ALL)
                    .setHttpDownloader(HttpUrlConnectionDownloader(Downloader.FileDownloaderType.PARALLEL))
                    .setDownloadConcurrentLimit(getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, Context.MODE_PRIVATE).getInt("downloadNumber", 1))
                    .build()
            Fetch.setDefaultInstanceConfiguration(fetchConfiguration)
        }

        val timed = defaultSharedPreferences.getFloat(ConstantValues.UPDATE_CHECK, 1f)

        update_check_number.setText("$timed")

        update_check_number.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val length = try {
                    update_check_number.text.toString().toFloat()
                } catch (e: NumberFormatException) {
                    0.0f
                }
                check_time.text = FetchingUtils.getETAString((1000 * 60 * 60 * length.toDouble()).toLong(), false)
            }

        })

        check_time.text = FetchingUtils.getETAString((1000 * 60 * 60 * timed.toDouble()).toLong(), false)

        update_check_updater.setOnClickListener {

            val length = if (!update_check_number.text.toString().isEmpty()) update_check_number.text.toString().toFloat() else 1.0f

            getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, Context.MODE_PRIVATE).edit().putFloat(ConstantValues.UPDATE_CHECK, length).apply()
            //FunApplication.cancelChecker(this@SettingsActivity)
            //FunApplication.checkUpdater(this@SettingsActivity, length)

            check_time.text = FetchingUtils.getETAString((1000 * 60 * 60 * length.toDouble()).toLong(), false)

        }

    }

}
