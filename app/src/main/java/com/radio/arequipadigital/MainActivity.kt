package com.radio.arequipadigital

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.radio.arequipadigital.R
import pl.droidsonroids.gif.GifImageView

class   MainActivity : AppCompatActivity() {

    private var player:ExoPlayer?=null
    private var isPlaying = false

    @SuppressLint("MissingInflatedId")
    @OptIn(UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val botonplay=findViewById<Button>(R.id.button_play)
        val botonpause=findViewById<Button>(R.id.button_pausa)
        val playButtonn=findViewById<TextView>(R.id.playButton)
        val loadingIndicator: ProgressBar = findViewById(R.id.loadingIndicator)

        requestNotificationPermission()



        botonplay.setOnClickListener {

            if (!isPlaying) {
                // Iniciar el servicio de radio en segundo plano
                val intent = Intent(this, RadioService::class.java)
                intent.action = "ACTION_PLAY"
                startService(intent)
                playButtonn.text = "Dale Click para Pausar la Radio"
                isPlaying = true
            } else {
                // Detener el servicio de radio
                val intent = Intent(this, RadioService::class.java)
                intent.action = "ACTION_STOP"
                stopService(intent)
                playButtonn.text = "Play Radio"
                isPlaying = false
            }


           //val intent = Intent(this, RadioService::class.java)
          // intent.action = "ACTION_PLAY"
         //  startService(intent)
         //   botonplay.setVisibility(View.GONE)
         //   botonpause.setVisibility(View.VISIBLE)
            /*
            loadingIndicator.visibility = View.VISIBLE
            player =ExoPlayer.Builder(this).build()
            val defaultHttpDataSourceFactory = DefaultHttpDataSource.Factory()
            val mediaItem = MediaItem.fromUri("https://stream.zeno.fm/uixbyq7btsutv")
            val mediaSource = ProgressiveMediaSource.Factory(defaultHttpDataSourceFactory).createMediaSource(mediaItem)
            player?.setMediaSource(mediaSource)
            player?.prepare()

            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_READY) {
                        player?.play()
                        loadingIndicator.visibility = View.GONE // Oculta el indicador cuando comienza la reproducción
                        botonplay.setVisibility(View.GONE)
                        botonpause.setVisibility(View.VISIBLE)
                    }
                }
            })

            //player?.playWhenReady = true

             */

        }

        botonpause.setOnClickListener {
            val intent = Intent(this, RadioService::class.java)
            intent.action = "ACTION_STOP"
            stopService(intent)
            botonplay.setVisibility(View.VISIBLE)
            botonpause.setVisibility(View.GONE)

        //player?.stop()
            //player?.playWhenReady = false
            //botonplay.setVisibility(View.VISIBLE)
            //botonpause.setVisibility(View.GONE)

        }

    }
    private fun requestNotificationPermission() {
        if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }
    }


}