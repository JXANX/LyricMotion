package com.lyricmotion

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat

class LyricsService : Service() {

    companion object {
        const val CHANNEL_ID      = "LyricsPlayerChannel"
        const val NOTIFICATION_ID = 1
        const val EXTRA_SONG_ID   = "extra_song_id"   // ← NUEVO

        fun startService(context: Context, songId: String, songTitle: String, artist: String, lyrics: String) {
            val intent = Intent(context, LyricsService::class.java).apply {
                putExtra(EXTRA_SONG_ID, songId)          // ← NUEVO
                putExtra("title",  songTitle)
                putExtra("artist", artist)
                putExtra("lyrics", lyrics)
            }
            context.startForegroundService(intent)
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
    private var songId    = ""  // ← NUEVO

    private val advanceLyric = object : Runnable {
        override fun run() {
            if (lines.isEmpty()) return
            lineIndex = (lineIndex + 1) % lines.size
            updateNotification(lines[lineIndex])
            handler.postDelayed(this, 3000L)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        songId    = intent?.getStringExtra(EXTRA_SONG_ID) ?: ""   // ← NUEVO
        songTitle = intent?.getStringExtra("title")  ?: "LyricMotion"
        artist    = intent?.getStringExtra("artist") ?: ""
        val rawLyrics = intent?.getStringExtra("lyrics") ?: ""
        lines     = rawLyrics.split("\n").filter { it.isNotBlank() }.toTypedArray()
        lineIndex = 0

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification(if (lines.isNotEmpty()) lines[0] else ""))
        handler.removeCallbacks(advanceLyric)
        handler.postDelayed(advanceLyric, 3000L)
        return START_NOT_STICKY
    }

    // Construye el Intent que lleva directo a la canción en curso
    private fun buildOpenIntent(): PendingIntent {
        // Pila correcta: MainActivity (con la canción activa) arriba de Home
        val stackBuilder = TaskStackBuilder.create(this).apply {
            addNextIntentWithParentStack(
                Intent(this@LyricsService, MainActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    // El Deep-link lo maneja MainActivity al leer este extra
                    putExtra(EXTRA_SONG_ID, songId)
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
            )
        }
        return stackBuilder.getPendingIntent(
            0,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )!!
    }

    private fun buildNotification(line: String): android.app.Notification =
        androidx.core.app.NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("▶ $songTitle — $artist")
            .setContentText(line)
            .setStyle(androidx.core.app.NotificationCompat.BigTextStyle().bigText(line))
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setOngoing(true)
            .setPriority(androidx.core.app.NotificationCompat.PRIORITY_LOW)
            .setContentIntent(buildOpenIntent())
            .build()

    private fun updateNotification(line: String) {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NOTIFICATION_ID, buildNotification(line))
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID, "Lyrics Player", NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    override fun onDestroy() {
        handler.removeCallbacks(advanceLyric)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}