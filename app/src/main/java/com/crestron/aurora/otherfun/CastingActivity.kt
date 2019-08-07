package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.crestron.aurora.views.ENPlayView
import com.google.android.gms.cast.Cast
import com.google.gson.Gson
import com.mradzinski.caster.Caster
import com.mradzinski.caster.ExpandedControlsStyle
import com.mradzinski.caster.MediaData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_casting.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class CastingActivity : AppCompatActivity() {

    private var VIDEO_URL = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_1MB.mp4"

    private var caster: Caster? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_casting)

        val castInfo = Gson().fromJson(intent.getStringExtra("cast_info"), CastVideoInfo::class.java)

        Loged.i("$castInfo")

        caster = Caster.create(this)
        caster!!.addMiniController(R.layout.custom_mini_controller)

        GlobalScope.launch {
            val s = if (canReachSiteAsync(castInfo.video_url).await()) "You can continue" else "Please Retry"
            runOnUiThread {
                Toast.makeText(this@CastingActivity, s, Toast.LENGTH_SHORT).show()
            }
        }
        val style = ExpandedControlsStyle.Builder()
                .setSeekbarLineColor(Color.BLUE)
                .setSeekbarThumbColor(Color.WHITE)
                .setStatusTextColor(Color.BLUE)
                .build()

        caster!!.setExpandedPlayerStyle(style)

        setUpPlayButton(castInfo)
        setUpMediaRouteButton()

        cast_des.text = castInfo.video_des
        cast_title.text = castInfo.video_name

        try {
            Picasso.get().load(castInfo.video_image)
                    .error(R.drawable.apk)
                    .resize((600 * .6).toInt(), (800 * .6).toInt())
                    .into(cast_image)
        } catch (e: java.lang.IllegalArgumentException) {
            Picasso.get().load(android.R.drawable.stat_notify_error).resize((600 * .6).toInt(), (800 * .6).toInt()).into(cast_image)
        }

    }

    private fun canReachSiteAsync(url: String) = GlobalScope.async {
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

    @SuppressLint("SetTextI18n")
    private fun setUpPlayButton(castVideoInfo: CastVideoInfo) {

        button_play.setOnClickListener { caster!!.player.loadMediaAndPlay(createMediaData(castVideoInfo)) }

        button_resume!!.setOnClickListener {
            if (caster!!.player.isPaused) {
                caster!!.player.togglePlayPause()
            }
        }

        play_pause_button.listener = object : ENPlayView.ENPlayListener {
            override fun onPlay() {
                caster!!.player.play()
            }

            override fun onPause() {
                caster!!.player.pause()
            }
        }

        /*caster!!.castSession!!.addCastListener(object : Cast.Listener() {
            override fun onVolumeChanged() {
                super.onVolumeChanged()
                volume_view.updateVolumeValue(caster!!.castSession!!.volume.toInt()*100)
            }
        })*/

        /*volume_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                caster?.castSession?.volume = p1.toDouble()/100.0
            }
        })*/

        caster!!.setOnConnectChangeListener(object : Caster.OnConnectChangeListener {

            override fun onConnected() {
                button_play!!.isEnabled = true

                caster!!.castSession!!.addCastListener(object : Cast.Listener() {
                    override fun onVolumeChanged() {
                        super.onVolumeChanged()
                        volume_view.updateVolumeValue(caster!!.castSession!!.volume.toInt()*100)
                    }
                })

                volume_view.updateVolumeValue(caster!!.castSession!!.volume.toInt()*100)
            }

            override fun onDisconnected() {
                button_play!!.isEnabled = false
                button_resume!!.isEnabled = false
            }
        })

        caster!!.setOnCastSessionProgressUpdateListener { progressMs, durationMs ->
            time_bar.max = durationMs.toInt()
            time_bar.progress = progressMs.toInt()
            time_bar_info.text = "${getTimeString(progressMs)}/${getTimeString(durationMs)}"
        }

        caster!!.setOnCastSessionStateChanged(object : Caster.OnCastSessionStateChanged {

            override fun onCastSessionBegan() {
                button_play!!.isEnabled = false
                button_resume!!.isEnabled = false
                Log.e("Caster", "Began playing video")
            }

            override fun onCastSessionFinished() {
                button_play!!.isEnabled = true
                button_resume!!.isEnabled = false
                Log.e("Caster", "Finished playing video")
            }

            override fun onCastSessionPlaying() {
                val playingURL = caster!!.player.currentPlayingMediaUrl

                button_play!!.isEnabled = !(playingURL != null && playingURL == VIDEO_URL)

                button_resume!!.isEnabled = false
                Log.e("Caster", "Playing video")
            }

            override fun onCastSessionPaused() {
                button_play!!.isEnabled = false
                button_resume!!.isEnabled = true
                Log.e("Caster", "Paused video")
            }
        })
    }

    private fun getTimeString(etaInMilliSeconds: Long): String {
        if (etaInMilliSeconds < 0) {
            return ""
        }
        var seconds = (etaInMilliSeconds / 1000).toInt()
        val hours = (seconds / 3600).toLong()
        seconds -= (hours * 3600).toInt()
        val minutes = (seconds / 60).toLong()
        seconds -= (minutes * 60).toInt()
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            minutes > 0 -> String.format("%02d:%02d", minutes, seconds)
            else -> "$seconds"
        }
    }

    private fun setUpMediaRouteButton() {
        //val mediaRouteButton = findViewById<MediaRouteButton>(R.id.media_route_button)
        caster!!.setupMediaRouteButton(media_route_button, true)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        //caster!!.addMediaRouteMenuItem(menu, true)
        menuInflater.inflate(R.menu.cast_menu, menu)
        /*menu.findItem(R.id.paste_new_link).setOnMenuItemClickListener {
            val clip = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clip != null) {
                VIMEO_URL = clip.primaryClip!!.toString()
            }
            true
        }*/
        return true
    }

    private fun createMediaData(castVideoInfo: CastVideoInfo): MediaData {
        VIDEO_URL = castVideoInfo.video_url
        return MediaData.Builder(VIDEO_URL)
                .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                //.setContentType("application/x-mpegURL")
                .setContentType("videos/mp4")
                .setMediaType(MediaData.MEDIA_TYPE_MOVIE)
                .setTitle(castVideoInfo.video_name)
                .setDescription(castVideoInfo.video_des)
                .setThumbnailUrl(castVideoInfo.video_image)
                //.setThumbnailUrl("https://www.google.com/url?sa=i&source=images&cd=&ved=2ahUKEwiZjNznyOLjAhVKhOAKHVEGAAYQjRx6BAgBEAU&url=https%3A%2F%2Fwww.polygon.com%2F2019%2F4%2F6%2F18291042%2Fbest-new-anime-spring-2019-watch-streaming&psig=AOvVaw3iBm_KCrnzvtpbeVMJSiB5&ust=1564779706691909")
                .setPlaybackRate(MediaData.PLAYBACK_RATE_NORMAL)
                .setAutoPlay(true)
                .build()
    }
}
