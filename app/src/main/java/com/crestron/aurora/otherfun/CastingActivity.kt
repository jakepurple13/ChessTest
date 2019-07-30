package com.crestron.aurora.otherfun

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.google.gson.Gson
import com.mradzinski.caster.Caster
import com.mradzinski.caster.ExpandedControlsStyle
import com.mradzinski.caster.MediaData
import kotlinx.android.synthetic.main.activity_casting.*
import kotlin.math.roundToInt

class CastingActivity : AppCompatActivity() {

    private var VIDEO_URL = "https://test-videos.co.uk/vids/bigbuckbunny/mp4/h264/1080/Big_Buck_Bunny_1080_10s_1MB.mp4"

    private var playButton: Button? = null
    private var resumeButton: Button? = null
    private var caster: Caster? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_casting)

        val castInfo = Gson().fromJson(intent.getStringExtra("cast_info"), CastVideoInfo::class.java)

        Loged.i("$castInfo")

        caster = Caster.create(this)
        caster!!.addMiniController(R.layout.custom_mini_controller)

        val style = ExpandedControlsStyle.Builder()
                .setSeekbarLineColor(Color.GREEN)
                .setSeekbarThumbColor(Color.WHITE)
                .setStatusTextColor(Color.GREEN)
                .build()

        caster!!.setExpandedPlayerStyle(style)

        setUpPlayButton(castInfo)
        setUpMediaRouteButton()
    }

    private fun setUpPlayButton(castVideoInfo: CastVideoInfo) {
        playButton = findViewById(R.id.button_play)
        resumeButton = findViewById(R.id.button_resume)

        playButton!!.setOnClickListener { caster!!.player.loadMediaAndPlay(createMediaData(castVideoInfo)) }

        resumeButton!!.setOnClickListener {
            if (caster!!.player.isPaused) {
                caster!!.player.togglePlayPause()
            }
        }

        volume_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                caster?.castSession?.volume = p1.toDouble()/100.0
            }
        })

        caster!!.setOnConnectChangeListener(object : Caster.OnConnectChangeListener {

            override fun onConnected() {
                playButton!!.isEnabled = true
                volume_bar.isEnabled = true
                volume_bar.progress = (caster!!.castSession!!.volume*100).roundToInt()
            }

            override fun onDisconnected() {
                playButton!!.isEnabled = false
                resumeButton!!.isEnabled = false
                volume_bar.isEnabled = false
            }
        })

        caster!!.setOnCastSessionStateChanged(object : Caster.OnCastSessionStateChanged {

            override fun onCastSessionBegan() {
                playButton!!.isEnabled = false
                resumeButton!!.isEnabled = false
                Log.e("Caster", "Began playing video")
            }

            override fun onCastSessionFinished() {
                playButton!!.isEnabled = true
                resumeButton!!.isEnabled = false
                Log.e("Caster", "Finished playing video")
            }

            override fun onCastSessionPlaying() {
                val playingURL = caster!!.player.currentPlayingMediaUrl

                playButton!!.isEnabled = !(playingURL != null && playingURL == VIDEO_URL)

                resumeButton!!.isEnabled = false
                Log.e("Caster", "Playing video")
            }

            override fun onCastSessionPaused() {
                playButton!!.isEnabled = false
                resumeButton!!.isEnabled = true
                Log.e("Caster", "Paused video")
            }
        })
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
        VIDEO_URL = castVideoInfo.video_url + ".mp4"
        return MediaData.Builder(VIDEO_URL)
                .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                //.setContentType("application/x-mpegURL")
                .setContentType("videos/mp4")
                .setMediaType(MediaData.MEDIA_TYPE_MOVIE)
                .setTitle(castVideoInfo.video_name)
                .setDescription(castVideoInfo.video_des)
                .setThumbnailUrl(castVideoInfo.video_image)
                .setPlaybackRate(MediaData.PLAYBACK_RATE_NORMAL)
                .setAutoPlay(true)
                .build()
    }
}
