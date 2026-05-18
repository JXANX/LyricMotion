package com.lyricmotion

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class LyricsService : Service() {

    companion object {
        const val CHANNEL_ID      = "LyricsPlayerChannel"
        const val NOTIFICATION_ID = 1

        fun startService(context: Context, songTitle: String, artist: String, lyrics: String) {
            val intent = Intent(context, LyricsService::class.java).apply {
                putExtra("title",  songTitle)
                putExtra("artist", artist)
                putExtra("lyrics", lyrics)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            context.stopService(Intent(context, LyricsService::class.java))
        }
    }

    private val handler   = Handler(Looper.getMainLooper())
    private var lineIndex = 0
    private var lines     = emptyArray<String>()
    private var songTitle = ""
    private var artist    = ""

    private val advanceLyric = object : Runnable {
        override fun run() {
            if (lines.isEmpty()) return
            lineIndex = (lineIndex + 1) % lines.size
            updateNotification(lines[lineIndex])
            handler.postDelayed(this, 3000L) // avanza cada 3 segundos
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        songTitle = intent?.getStringExtra("title")  ?: "LyricMotion"
        artist    = intent?.getStringExtra("artist") ?: ""
        val rawLyrics = intent?.getStringExtra("lyrics") ?: ""
        lines     = rawLyrics.split("\n").filter { it.isNotBlank() }.toTypedArray()
        lineIndex = 0

        createNotificationChannel()

        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("▶ $songTitle — $artist")
            .setContentText(if (lines.isNotEmpty()) lines[0] else "")
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        handler.removeCallbacks(advanceLyric)
        handler.postDelayed(advanceLyric, 3000L)

        return START_NOT_STICKY
    }

    private fun updateNotification(line: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("▶ $songTitle — $artist")
            .setContentText(line)
            .setStyle(NotificationCompat.BigTextStyle().bigText(line))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(openIntent)
            .build()
        nm.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Lyrics Player", NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(advanceLyric)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}