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
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.media3.exoplayer.ExoPlayer
import com.radio.arequipadigital.R

class RadioService:Service() {

    private lateinit var player: ExoPlayer
    private val CHANNEL_ID = "RadioStreamChannel"
    private val streamUrl = "https://stream.zeno.fm/uixbyq7btsutv"

    override fun onCreate() {


        super.onCreate()
        player=ExoPlayer.Builder(this).build()
        
        val mediaItem = androidx.media3.common.MediaItem.fromUri(streamUrl)
        player.setMediaItem(mediaItem)
        player.prepare()
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationchannel()

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent=PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this,CHANNEL_ID)
            .setContentTitle("AREQUIPA DIGITAL")
            .setContentText("Reproduccion de tu radio favorita")
            .setSmallIcon(R.drawable.ic_play)  // Añade un ícono adecuado
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_pause, "Play", getPendingIntent("ACTION_PLAY")) // Control de pausa
            .addAction(R.drawable.ic_pause, "Pausar", getPendingIntent("ACTION_PAUSE")) // Control de pausa
            .addAction(R.drawable.ic_stop, "Detener", getPendingIntent("ACTION_STOP")) // Control de detener
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

        val intent = Intent(this, RadioService::class.java).apply {
            this.action = action  // Asignar la acción al intent
        }
        return PendingIntent.getService(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
    }


    private fun stopRadio() {
        if (::player.isInitialized) {
            player.stop()
        }
        stopForeground(true)
        stopSelf()
    }

    private fun pauseRadio() {

        if (::player.isInitialized && player.isPlaying) {
            player.pause()

        }
    }

    private fun playRadio() {
        if(::player.isInitialized && !player.isPlaying){
            player.play()
        }
    }

    private fun createNotificationchannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Radio Streaming Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::player.isInitialized) {
            player.release()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }


}
