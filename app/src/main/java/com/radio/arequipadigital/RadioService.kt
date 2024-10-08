package com.radio.arequipadigital

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.browse.MediaBrowser.MediaItem
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem.fromUri
import androidx.media3.exoplayer.ExoPlayer
import com.radio.arequipadigital.R


class RadioService:Service() {

    private lateinit var player: ExoPlayer
    private val CHANNEL_ID = "RadioStreamChannel"
    private var streamUrl: String? = null


    override fun onCreate() {
        super.onCreate()
        Log.d("RadioService", "Servicio creado")

        // Inicializamos el reproductor solo una vez
        player = ExoPlayer.Builder(this).build()
        Log.d("RadioService", "ExoPlayer inicializado")
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        streamUrl = intent?.getStringExtra("radio_url")
        if (streamUrl == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val mediaItem = androidx.media3.common.MediaItem.fromUri(streamUrl!!)
        player.setMediaItem(mediaItem)
        player.prepare()

        createNotificationChannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Reproduciendo radio")
            .setContentText("Estás escuchando tu estación favorita")
            .setSmallIcon(R.drawable.ic_play)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_pause, "Pausar", getPendingIntent("ACTION_PAUSE"))
            .addAction(R.drawable.ic_stop, "Detener", getPendingIntent("ACTION_STOP"))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        when (intent?.action) {
            "ACTION_PLAY" -> playRadio()
            "ACTION_PAUSE" -> pauseRadio()
            "ACTION_STOP" -> stopRadio()
        }

        return START_NOT_STICKY
    }



    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, RadioService::class.java).apply { this.action = action }
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun playRadio() {
        if (!player.isPlaying) player.play()
    }

    private fun pauseRadio() {
        if (player.isPlaying) player.pause()
    }

    private fun stopRadio() {
        player.stop()
        stopForeground(true)
        stopSelf()
    }


    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID, "Radio Streaming Service", NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
    override fun onBind(intent: Intent?): IBinder? = null
   

}
