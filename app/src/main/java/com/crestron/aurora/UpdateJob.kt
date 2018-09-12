package com.crestron.aurora

import com.crestron.aurora.otherfun.AppInfo
import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import com.google.gson.Gson
import kotlinx.coroutines.experimental.async
import org.jetbrains.anko.defaultSharedPreferences
import programmer.box.utilityhelper.UtilNotification
import java.net.URL
import java.util.concurrent.TimeUnit

class UpdateJob : Job() {

    companion object {
        const val TAG = "update_job_tag"
        fun scheduleJob(): Int {
            return JobRequest.Builder(UpdateJob.TAG)
                    .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.MINUTES.toMillis(5))
                    .setUpdateCurrent(true)
                    .build()
                    .schedule()
        }
        fun cancelJob(num: Int) {
            JobManager.instance().cancel(num)
        }
    }

    override fun onRunJob(params: Params): Result {
        async {
            val url = URL(ConstantValues.VERSION_URL).readText()

            val info: AppInfo = Gson().fromJson(url, AppInfo::class.java)

            val pInfo = this@UpdateJob.context.packageManager.getPackageInfo(this@UpdateJob.context.packageName, 0)
            val version = pInfo.versionName

            Loged.i("version is ${version.toDouble()} and info is ${info.version}")

            UtilNotification.sendNotification(this@UpdateJob.context, R.drawable.apk, "Checked!", "We did check",
                    ConstantValues.CHANNEL_ID, ChoiceActivity::class.java, 90)

            this@UpdateJob.context.defaultSharedPreferences.edit().putBoolean(ConstantValues.APP_UPDATE, version.toDouble() < info.version).apply()
        }
        return Result.SUCCESS
    }

}