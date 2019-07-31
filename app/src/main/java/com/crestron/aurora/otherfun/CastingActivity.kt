package com.crestron.aurora.otherfun

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.crestron.aurora.Loged
import com.crestron.aurora.R
import com.google.gson.Gson
import com.hmomeni.verticalslider.VerticalSlider
import com.mikepenz.materialize.util.UIUtils
import com.mradzinski.caster.Caster
import com.mradzinski.caster.ExpandedControlsStyle
import com.mradzinski.caster.MediaData
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_casting.*

class CastingActivity : AppCompatActivity() {

    private var VIDEO_URL = "http://gateway.play44.net:3010/old/at_harmonquest_2_-_10.mp4?st=MmE0YmJkMDMwYzIwMjVmZjUwY2Y4YmM5MGY2MGZhOGE&e=1564433897"

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

    private fun setUpPlayButton(castVideoInfo: CastVideoInfo) {
        playButton = findViewById(R.id.button_play)
        resumeButton = findViewById(R.id.button_resume)

        playButton!!.setOnClickListener { caster!!.player.loadMediaAndPlay(createMediaData(castVideoInfo)) }

        resumeButton!!.setOnClickListener {
            if (caster!!.player.isPaused) {
                caster!!.player.togglePlayPause()
            }
        }

        /*volume_bar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStartTrackingTouch(p0: SeekBar?) {

            }

            override fun onStopTrackingTouch(p0: SeekBar?) {

            }

            override fun onProgressChanged(p0: SeekBar?, p1: Int, p2: Boolean) {
                caster?.castSession?.volume = p1.toDouble()/100.0
            }
        })*/

        volume_bar.cornerRadius = UIUtils.convertDpToPixel(10f, this)

        volume_bar.onProgressChangeListener = object : VerticalSlider.OnSliderProgressChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onChanged(progress: Int, max: Int) {
                caster?.castSession?.volume = progress.toDouble() / max
                volume_info.text = "${((progress.toDouble() / max) * 100).toInt()}%"
            }
        }

        caster!!.setOnConnectChangeListener(object : Caster.OnConnectChangeListener {

            override fun onConnected() {
                playButton!!.isEnabled = true
                volume_bar.isEnabled = true
                volume_bar.progress = (caster!!.castSession!!.volume * 100).toInt()
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
        caster!!.setupMediaRouteButton(media_route_button, false)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        caster!!.addMediaRouteMenuItem(menu, true)
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
        return MediaData.Builder(castVideoInfo.video_url)
                .setStreamType(MediaData.STREAM_TYPE_BUFFERED)
                .setContentType("application/x-mpegURL")
                .setMediaType(MediaData.MEDIA_TYPE_MOVIE)
                .setTitle(castVideoInfo.video_name)
                .setDescription(castVideoInfo.video_des)
                .setThumbnailUrl(castVideoInfo.video_image)
                .setPlaybackRate(MediaData.PLAYBACK_RATE_NORMAL)
                .setAutoPlay(true)
                .build()
    }
}
