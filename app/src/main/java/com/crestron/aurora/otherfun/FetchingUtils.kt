package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap
import com.crestron.aurora.ConstantValues
import com.crestron.aurora.FunApplication
import com.crestron.aurora.Loged
import com.crestron.aurora.showapi.EpisodeInfo
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Func
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.text.DecimalFormat


class FetchingUtils(val context: Context, private var fetchAction: FetchAction = object : FetchAction {}) {

    var fetch: Fetch = Fetch.getDefaultInstance().apply {
        addListener(fetchAction)
    }

    private fun fetchIt(url: String, ap: Boolean = false, networkType: NetworkType = NetworkType.ALL, keyAndValue: Array<out EpisodeActivity.KeyAndValue>) {

        fetch.setGlobalNetworkType(networkType)

        fun getNameFromUrl(url: String): String? {
            return Uri.parse(url).lastPathSegment
        }

        val filePath = folderLocation + getNameFromUrl(url) + ".mp4"
        Loged.wtf("${File(filePath).exists()}")
        val request = Request(url, filePath!!)
        request.priority = Priority.HIGH
        request.networkType = networkType
        //request.enqueueAction = EnqueueAction.DO_NOT_ENQUEUE_IF_EXISTING
        //request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")

        for (keyValue in keyAndValue) {
            request.extras.map.toProperties()[keyValue.key] = keyValue.value
        }

        if (ap) {
            request.addHeader("Accept-Language", "en-US,en;q=0.5")
            request.addHeader("User-Agent", "\"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0\"")
            request.addHeader("Accept", "text/html,video/mp4,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            request.addHeader("Referer", "http://thewebsite.com")
            request.addHeader("Connection", "keep-alive")
        }

        //fetch.addListener(fetchAction)

        fetch.enqueue(request, Func {

        }, Func {

        })
    }

    private fun getPutlockerOrigin(url: String): String {
        val doc = Jsoup.connect(url.trim()).get()
        return "<iframe[^>]+src=\"([^\"]+)\"[^>]*><\\/iframe>".toRegex().find(doc.toString())!!.groups[1]!!.value
    }

    private fun fetchIt(ep: EpisodeInfo, networkType: NetworkType = NetworkType.ALL, keyAndValue: Array<out EpisodeActivity.KeyAndValue>) {

        fetch.setGlobalNetworkType(networkType)

        fun getNameFromUrl(url: String): String {
            return Uri.parse(url).lastPathSegment?.let { if (it.isNotEmpty()) it else ep.name } ?: ep.name
        }

        val requestList = arrayListOf<Request>()
        val url = ep.getVideoLinks()
        for (i in url) {

            val filePath = folderLocation + getNameFromUrl(i) + ".mp4"
            Loged.wtf("${File(filePath).exists()}")
            val request = Request(i, filePath)
            request.priority = Priority.HIGH
            request.networkType = networkType
            //request.enqueueAction = EnqueueAction.DO_NOT_ENQUEUE_IF_EXISTING
            //request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")

            for (keyValue in keyAndValue) {
                request.extras.map.toProperties()[keyValue.key] = keyValue.value
            }

            request.addHeader("Accept-Language", "en-US,en;q=0.5")
            request.addHeader("User-Agent", "\"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0\"")
            request.addHeader("Accept", "text/html,video/mp4,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            request.addHeader("Access-Control-Allow-Origin", "*")
            if (ep.url.contains("putlocker", true)) {
                request.addHeader("Referer", getPutlockerOrigin(ep.url))
                request.addHeader("Origin", "http://mixdrop.co")
                request.addHeader("sec-fetch-site", "same-origin")
                request.addHeader("sec-fetch-mode", "navigate")
                request.addHeader("sec-fetch-user", "?1")
            } else {
                request.addHeader("Referer", "http://thewebsite.com")
            }
            request.addHeader("Connection", "keep-alive")

            requestList.add(request)

        }

        //fetch.addListener(fetchAction)

        fetch.enqueue(requestList, Func {})
    }

    private fun fetchIt(ep: EpisodeInfo, networkType: NetworkType = NetworkType.ALL, recent: Boolean, keyAndValue: Array<out EpisodeActivity.KeyAndValue>) {

        fetch.setGlobalNetworkType(networkType)

        fun getNameFromUrl(url: String): String {
            return Uri.parse(url).lastPathSegment?.let { if (it.isNotEmpty()) it else ep.name } ?: ep.name
        }

        val requestList = arrayListOf<Request>()
        val url = ep.getVideoLinks()
        for (i in url) {

            val filePath = folderLocation + getNameFromUrl(i) + ".mp4"
            Loged.wtf("${File(filePath).exists()}")
            val request = Request(i, filePath)
            request.priority = Priority.HIGH
            request.networkType = networkType
            //request.enqueueAction = EnqueueAction.DO_NOT_ENQUEUE_IF_EXISTING
            //request.addHeader("clientKey", "SD78DF93_3947&MVNGHE1WONG")

            for (keyValue in keyAndValue) {
                request.extras.map.toProperties()[keyValue.key] = keyValue.value
            }

            request.addHeader("Accept-Language", "en-US,en;q=0.5")
            request.addHeader("User-Agent", "\"Mozilla/5.0 (Windows NT 10.0; WOW64; rv:40.0) Gecko/20100101 Firefox/40.0\"")
            request.addHeader("Accept", "text/html,video/mp4,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            request.addHeader("Access-Control-Allow-Origin", "*")
            request.addHeader("Connection", "keep-alive")
            request.addHeader("Referer", "http://thewebsite.com")

            requestList.add(request)

        }

        //fetch.addListener(fetchAction)

        fetch.enqueue(requestList, Func {})
    }

    fun cancelAll() {
        fetch.cancelAll()
    }

    fun deleteAll() {
        fetch.deleteAll()
    }

    fun deleteById(id: Int) {
        fetch.delete(id)
    }

    fun createFile(filePath: String): File {
        val file = File(filePath)
        if (!file.exists()) {
            val parent = file.parentFile
            if (!parent.exists()) {
                parent.mkdirs()
            }
            try {
                file.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return file
    }

    fun getVideo(urlToUse: EpisodeInfo, networkType: NetworkType = NetworkType.ALL, vararg keyAndValue: EpisodeActivity.KeyAndValue) = GlobalScope.launch {
        fetchIt(urlToUse, networkType, keyAndValue)
    }

    fun getVideo(urlToUse: EpisodeInfo, recent: Boolean, networkType: NetworkType = NetworkType.ALL, vararg keyAndValue: EpisodeActivity.KeyAndValue) = GlobalScope.launch {
        fetchIt(urlToUse, networkType, recent, keyAndValue)
    }

    fun getVideo(urlToUse: Collection<EpisodeInfo>, networkType: NetworkType = NetworkType.ALL, vararg keyAndValue: EpisodeActivity.KeyAndValue) = GlobalScope.launch {
        urlToUse.forEach { fetchIt(it, networkType, keyAndValue) }
        Loged.d("DONE!")

    }

    companion object Fetched {

        fun retry(download: Download) {
            Fetch.getDefaultInstance().retry(download.id)
        }

        fun retry(download: Int) {
            Fetch.getDefaultInstance().retry(download)
        }

        fun retryAll() {
            Fetch.getDefaultInstance().getDownloadsWithStatus(Status.DOWNLOADING, Func {
                for (i in it) {
                    retry(i)
                }
            })
        }

        fun pause(download: Download) {
            Fetch.getDefaultInstance().pause(download.id)
        }

        fun pause(download: Int) {
            Fetch.getDefaultInstance().pause(download)
        }

        fun resume(download: Download) {
            Fetch.getDefaultInstance().resume(download.id)
        }

        fun resume(download: Int) {
            Fetch.getDefaultInstance().resume(download)
        }

        fun delete(download: Download) {
            Fetch.getDefaultInstance().delete(download.id)
        }

        fun delete(download: Int) {
            Fetch.getDefaultInstance().delete(download)
        }

        fun remove(download: Download) {
            Fetch.getDefaultInstance().remove(download.id)
        }

        fun remove(download: Int) {
            Fetch.getDefaultInstance().remove(download)
        }

        var folderLocation = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/Fun/"
            set(value) {
                field = value
                try {
                    val shared = FunApplication.getAppContext().getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, Context.MODE_PRIVATE).edit()
                    shared.putString(ConstantValues.FOLDER_LOCATION, value)
                    shared.apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (e: java.lang.Error) {
                    e.printStackTrace()
                }
            }
            get() = FunApplication.getAppContext()
                    .getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, Context.MODE_PRIVATE).getString(ConstantValues.FOLDER_LOCATION, null)
                    ?: (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).toString() + "/Fun/")

        var downloadCount = 0
            set(value) {
                field = value
                val shared = FunApplication.getAppContext().getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, Context.MODE_PRIVATE).edit()
                shared.putInt(ConstantValues.DOWNLOAD_COUNT, value)
                shared.apply()
            }
            get() = FunApplication.getAppContext()
                    .getSharedPreferences(ConstantValues.DEFAULT_APP_PREFS_NAME, Context.MODE_PRIVATE)
                    .getInt(ConstantValues.DOWNLOAD_COUNT, 0)

        fun getMimeType(context: Context, uri: Uri): String {
            val cR = context.contentResolver
            val mime = MimeTypeMap.getSingleton()
            var type = mime.getExtensionFromMimeType(cR.getType(uri))
            if (type == null) {
                type = "*/*"
            }
            return type
        }

        fun getETAString(etaInMilliSeconds: Long, needLeft: Boolean = true): String {
            if (etaInMilliSeconds < 0) {
                return ""
            }
            var seconds = (etaInMilliSeconds / 1000).toInt()
            val hours = (seconds / 3600).toLong()
            seconds -= (hours * 3600).toInt()
            val minutes = (seconds / 60).toLong()
            seconds -= (minutes * 60).toInt()
            return when {
                hours > 0 -> String.format("%02d:%02d:%02d hours", hours, minutes, seconds)
                minutes > 0 -> String.format("%02d:%02d mins", minutes, seconds)
                else -> "$seconds secs"
            } + (if (needLeft) " left" else "")
        }

        fun getDownloadSpeedString(downloadedBytesPerSecond: Long): String {
            if (downloadedBytesPerSecond < 0) {
                return ""
            }
            val kb = downloadedBytesPerSecond.toDouble() / 1000.toDouble()
            val mb = kb / 1000.toDouble()
            val gb = mb / 1000
            val tb = gb / 1000
            val decimalFormat = DecimalFormat(".##")
            return when {
                tb >= 1 -> "${decimalFormat.format(tb)} tb/s"
                gb >= 1 -> "${decimalFormat.format(gb)} gb/s"
                mb >= 1 -> "${decimalFormat.format(mb)} mb/s"
                kb >= 1 -> "${decimalFormat.format(kb)} kb/s"
                else -> "$downloadedBytesPerSecond b/s"
            }
        }

        fun getProgress(downloaded: Long, total: Long): Double {
            return when {
                total < 1 -> -1.0
                downloaded < 1 -> 0.0
                downloaded >= total -> 100.0
                else -> (downloaded.toDouble() / total.toDouble() * 100)
            }
        }

        fun canReachSite(url: String) = GlobalScope.async {
            val urlChecker = URL(url)
            val connection = urlChecker.openConnection() as HttpURLConnection
            val code = connection.responseCode

            if (code == 200) {
                // reachable
                Loged.d("Yeah! We can get it")
                true
            } else {
                Loged.wtf("Nope!")
                false
            }
        }

        @Throws(IOException::class)
        fun getHtml(url: String): String {
            // Build and set timeout values for the request.
            val connection = URL(url).openConnection()
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.connect()

            // Read and store the result line by line then return the entire string.
            val in1 = connection.getInputStream()
            val reader = BufferedReader(InputStreamReader(in1))
            val html = StringBuilder()
            var line: String? = ""
            while (line != null) {
                line = reader.readLine()
                html.append(line)
            }
            in1.close()

            return html.toString()
        }
    }

    interface FetchAction : FetchListener {

        override fun onAdded(download: Download) {
            Loged.i("Added ${download.file}")
        }

        override fun onCancelled(download: Download) {
            Loged.e("Cancelled ${download.file}")
        }

        override fun onCompleted(download: Download) {
            Loged.d("Completed ${download.file}")
            Fetch.getDefaultInstance().removeAllWithStatus(Status.COMPLETED)
        }

        override fun onDeleted(download: Download) {
            Loged.e("Deleted ${download.file}")
        }

        override fun onDownloadBlockUpdated(download: Download, downloadBlock: DownloadBlock, totalBlocks: Int) {
            Loged.i("Download Block Updated")
        }

        override fun onError(download: Download, error: Error, throwable: Throwable?) {
            Loged.wtf("${download.error} with code ${download.error.httpResponse?.code}")
        }

        override fun onPaused(download: Download) {
            Loged.i("Paused ${download.file}")
        }

        override fun onQueued(download: Download, waitingOnNetwork: Boolean) {
            Loged.i("Queued ${download.file}")
        }

        override fun onRemoved(download: Download) {
            Loged.e("Removed ${download.file}")
        }

        override fun onResumed(download: Download) {
            Loged.i("Resumed ${download.file}")
        }

        override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
            Loged.d("Started ${download.file}")

        }

        override fun onWaitingNetwork(download: Download) {
            Loged.e("Waiting Network ${download.file}")
        }

        @SuppressLint("SetTextI18n")
        override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
            val progress = "%.2f".format(getProgress(download.downloaded, download.total))
            val info = "$progress% " +
                    "at ${getDownloadSpeedString(downloadedBytesPerSecond)} " +
                    "with ${getETAString(etaInMilliSeconds)}"
            Loged.v(info)
        }

    }

}