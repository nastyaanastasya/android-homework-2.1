package com.itis.android_homework.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.Binder
import android.os.IBinder
import com.itis.android_homework.models.Song
import com.itis.android_homework.repositories.SongRepository

class MusicService : Service() {
    private var currentId: Int? = null
    private lateinit var list: List<Song>
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var musicBinder: MusicBinder
    private lateinit var notificationService: NotificationService

    override fun onBind(intent: Intent?): IBinder = musicBinder

    override fun onCreate() {
        super.onCreate()
        currentId = SongRepository.DEFAULT_SONG_ID
        list = SongRepository.songs
        mediaPlayer = MediaPlayer()
        musicBinder = MusicBinder()
        notificationService = NotificationService(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                if (mediaPlayer.isPlaying) stop()
                currentId?.let {
                    notificationService.rebuildNotification(it)
                }
            }
            ACTION_PLAY -> {
                if (!mediaPlayer.isPlaying) play()
                currentId?.let {
                    notificationService.buildNotification(it)
                }
            }
            ACTION_NEXT -> {
                next()
                currentId?.let {
                    notificationService.buildNotification(it)
                }
            }
            ACTION_PREV -> {
                prev()
                currentId?.let {
                    notificationService.buildNotification(it)
                }
            }
            ACTION_PAUSE -> {
                pause()
                currentId?.let {
                    notificationService.rebuildNotification(it)
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun prev() {
        currentId?.let {
            currentId = if (it - 1 >= 0) it - 1
            else SongRepository.songs.size - 1
        }
        setSong(currentId ?: SongRepository.DEFAULT_SONG_ID)
        play()
    }

    fun next() {
        currentId?.also {
            currentId = if (it + 1 < list.size) it + 1
            else SongRepository.DEFAULT_SONG_ID
        }
        setSong(currentId ?: SongRepository.DEFAULT_SONG_ID)
        play()
    }

    fun pause() {
        mediaPlayer.pause()
    }

    fun play() {
        mediaPlayer.start()
    }

    fun stop() {
        mediaPlayer.stop()
        setSong(currentId ?: SongRepository.DEFAULT_SONG_ID)
    }

    fun setSong(id: Int) {
        with(mediaPlayer) {
            if (isPlaying) stop()
        }
        mediaPlayer = MediaPlayer.create(
            applicationContext,
            list[id].audio
        )
        currentId = id
        currentId?.let {
            notificationService.buildNotification(it)
        }
    }

    fun getCurrentId() = currentId

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
    }

    inner class MusicBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }
}
