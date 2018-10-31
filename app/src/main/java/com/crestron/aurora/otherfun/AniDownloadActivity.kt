package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.showapi.ShowInfo
import com.google.gson.Gson
import com.tonyodev.fetch2.Download
import com.tonyodev.fetch2core.DownloadBlock
import kotlinx.android.synthetic.main.activity_ani_download.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import org.json.JSONArray
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.URL


class AniDownloadActivity : AppCompatActivity() {

    operator fun JSONArray.iterator(): Iterator<JSONObject> = (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

    private val listOfLinks = arrayListOf<String>()
    private val listOfNames = arrayListOf<String>()

    private var file: String? = null
    //private val url = "https://www.sample-videos.com/video/mp4/720/big_buck_bunny_720p_1mb.mp4"
    //private val url = "http://s8.videozoo.me/S/satsuriku_no_tenshi_-_05.mp4?st=il3Xz7vzvpAfHDnOVA2TfQ&e=1533330906&start=0"
    private val url = "http://www.animeplus.tv/satsuriku-no-tenshi-episode-5-online"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ani_download)

        val fetching = FetchingUtils(this, object : FetchingUtils.FetchAction {

            override fun onStarted(download: Download, downloadBlocks: List<DownloadBlock>, totalBlocks: Int) {
                Loged.i("${totalBlocks / download.total}%")
                progressBar.max = 100
            }

            override fun onCompleted(download: Download) {
                super.onCompleted(download)
                Loged.d("Completed")
                open_stuff.isEnabled = true
            }

            @SuppressLint("SetTextI18n")
            override fun onProgress(download: Download, etaInMilliSeconds: Long, downloadedBytesPerSecond: Long) {
                super.onProgress(download, etaInMilliSeconds, downloadedBytesPerSecond)
                val progress = "%.2f".format(FetchingUtils.getProgress(download.downloaded, download.total))
                val info = "$progress% " +
                        "at ${FetchingUtils.getDownloadSpeedString(downloadedBytesPerSecond)} " +
                        "with ${FetchingUtils.getETAString(etaInMilliSeconds)}"
                runOnUiThread {
                    progressBar.setProgress(download.progress, true)
                    progress_info.text = info
                }
            }
        })

        fun getVideo(urlToUse: String) = GlobalScope.async {
            //Loged.d("${fetching.canReachSite(urlToUse).await()}")

            //ex "http://www.animeplus.tv/satsuriku-no-tenshi-episode-5-online"

            //http://videowing.gogoanime.to/embed?w=600&h=438&video=ongoing/satsuriku_no_tenshi_-_05.mp4
            val htmld = getHtml(urlToUse)
            val m = "<iframe src=\"([^\"]+)\"[^<]+</iframe>".toRegex().toPattern().matcher(htmld)
            var s = ""
            val list = arrayListOf<String>()
            while (m.find()) {
                val g = m.group(1)
                s += g + "\n"
                list.add(g)
            }

            Loged.wtf(s)

            val htmlc = getHtml(list[0])

            Loged.i("Starting video part")

            val reg = "var video_links = (\\{.*?});".toRegex().toPattern().matcher(htmlc)

            while (reg.find()) {
                val d = reg.group(1)
                Loged.wtf(d)
                Loged.e("We in here")
                val g = Gson()
                val d1 = g.fromJson<NormalLink>(d, NormalLink::class.java)

                Loged.wtf(d1.toString())

                /*if (d1.normal.storage[0].link.isNullOrBlank())
                    continue
                else
                    fetching.fetchIt(d1.normal.storage[0].link, true)*/

            }

            Loged.d("DONE!")

        }

        fun getListOfAnime() {
            val urlToUse = "http://www.animeplus.tv/anime-list"

            Loged.i("We are beginning")

            val doc = Jsoup.connect(urlToUse).get()

            val lists = doc.allElements

            val listOfStuff = lists.select("td").select("a[href^=http]")

            for ((i, element) in listOfStuff.withIndex()) {
                //Loged.wtf("$i: ${element.html()}")
                listOfNames.add(element.text())
                listOfLinks.add(element.attr("abs:href"))
            }

            runOnUiThread {
                anime_list.adapter = AListAdapter(listOfNames, listOfLinks, this@AniDownloadActivity, object : LinkAction {
                    override fun hit(name: String, url: String) {
                        super.hit(name, url)

                        val intented = Intent(this@AniDownloadActivity, EpisodeActivity::class.java)
                        intented.putExtra("url", url)
                        startActivity(intented)
                    }
                })
            }

            Loged.d("${(anime_list.adapter!! as AListAdapter).itemCount}")
        }

        anime_list.layoutManager = LinearLayoutManager(this)
        anime_list.adapter = AListAdapter(listOfNames, listOfLinks, this)

        open_stuff.isEnabled = false

        download_stuff.setOnClickListener { _ ->

            GlobalScope.async {
                getListOfAnime()
            }
        }

        cancel_stuff.setOnClickListener {
            try {
                fetching.cancelAll()
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
        }

        delete_stuff.setOnClickListener {
            try {
                deleteFile(fetching.filePath)
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: java.lang.NullPointerException) {
                e.printStackTrace()
            }
            fetching.deleteAll()
            progressBar.progress = 0
            progress_info.text = "0% at 0 b/s with 0 secs left"
        }

        open_stuff.setOnClickListener {
            try {
                openFile(File(file))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    private fun openFile(url: File) {

        try {

            val uri = Uri.fromFile(url)

            val intent = Intent(Intent.ACTION_VIEW)
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword")
            } else if (url.toString().contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf")
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint")
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel")
            } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
                // WAV audio file
                intent.setDataAndType(uri, "application/x-wav")
            } else if (url.toString().contains(".rtf")) {
                // RTF file
                intent.setDataAndType(uri, "application/rtf")
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav")
            } else if (url.toString().contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif")
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg")
            } else if (url.toString().contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain")
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                    url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*")
            } else {
                intent.setDataAndType(uri, "*/*")
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No application found which can open the file", Toast.LENGTH_SHORT).show()
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

    interface LinkAction {
        fun hit(name: String, url: String) {
            Loged.wtf("$name: $url")
        }

        fun longhit(info: ShowInfo, vararg views: View) {

        }
    }

}
