package com.crestron.aurora.otherfun

import android.app.job.JobParameters
import android.app.job.JobService
import android.widget.Toast
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.Loged
import com.google.gson.Gson
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.defaultSharedPreferences
import java.net.URL

class UpdateCheckService : JobService() {

    private companion object {
        var starting = true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        if(starting) {
            starting = false
            async {
                val url = URL(ConstantValues.VERSION_URL).readText()

                val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)

                val pInfo = packageManager.getPackageInfo(packageName, 0)
                val version = pInfo.versionName

                Loged.i("version is ${version.toDouble()} and info is ${info.version}")

                defaultSharedPreferences.edit().putBoolean(ConstantValues.APP_UPDATE, version.toDouble() < info.version).apply()
                Toast.makeText(this@UpdateCheckService, "UP TO DATE!", Toast.LENGTH_LONG).show()
                starting = true
                jobFinished(params, true)
            }
        } else {
            jobFinished(params, true)
        }
        return true
    }

}