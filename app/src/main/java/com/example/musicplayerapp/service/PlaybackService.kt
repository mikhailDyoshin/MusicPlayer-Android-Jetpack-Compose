package com.example.musicplayerapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.musicplayerapp.R
import com.example.musicplayerapp.utils.modulo
import com.google.common.collect.ImmutableList
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PlaybackService : MediaSessionService(), Player.Listener {

    @Inject
    lateinit var mediaSession: MediaSession

    private lateinit var player: Player
    private lateinit var notificationManager: NotificationManager
    private lateinit var nBuilder: NotificationCompat.Builder

    private var isPlaying = false

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        player = mediaSession.player

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotification(mediaSession)

        player.addListener(this)

        this.setMediaNotificationProvider(object : MediaNotification.Provider {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun createNotification(
                mediaSession: MediaSession,// this is the session we pass to style
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                createNotification(mediaSession)
                // notification should be created before you return here
                return MediaNotification(NOTIFICATION_ID, nBuilder.build())
            }

            override fun handleCustomCommand(
                session: MediaSession,
                action: String,
                extras: Bundle
            ): Boolean {
                TODO("Not yet implemented")
            }
        })

        Log.d(PLAYBACK_SERVICE_TAG, "Service was created")
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession =
        mediaSession

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(session: MediaSession) {

        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Channel",
                NotificationManager.IMPORTANCE_NONE
            )
        )

        // NotificationCompat.Builder here.
        nBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(R.drawable.note_svg)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(
                        1, /* #1: previous button \*/
                        2, /* #2: play/pause button \*/
                        3, /* #3: next button \*/
                    )
            )

        updateNotificationOnPlayPause()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                PlayerNotificationAction.ACTION_SEEK_BACK.actionString -> {
                    player.seekTo(0)
                }

                PlayerNotificationAction.ACTION_PLAY.actionString -> {
                    player.play()
                }

                PlayerNotificationAction.ACTION_PAUSE.actionString -> {
                    player.pause()
                }

                PlayerNotificationAction.ACTION_PREVIOUS.actionString -> {
                    seekToPrevious()
                }

                PlayerNotificationAction.ACTION_NEXT.actionString -> {
                    seekToNext()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf()
            if (!player.playWhenReady) {
                Log.d(PLAYBACK_SERVICE_TAG, "Service was stopped: playWhenReady = false")
            }

            if (player.mediaItemCount == 0) {
                Log.d(PLAYBACK_SERVICE_TAG, "Service was stopped: no media items")
            }

            notificationManager.cancel(NOTIFICATION_ID)
        }

        Log.d(PLAYBACK_SERVICE_TAG, "App was closed")
    }

    @OptIn(UnstableApi::class)
    override fun onDestroy() {
        mediaSession.run {
            player.release()
            release()
        }
        super.onDestroy()
        Log.d(PLAYBACK_SERVICE_TAG, "Service is being destroyed")
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_READY -> {
                if (player.playWhenReady) {
                    isPlaying = true
                    updateNotificationOnPlayPause()
                } else {
                    isPlaying = false
                    updateNotificationOnPlayPause()
                }
            }

            Player.STATE_IDLE -> {
                isPlaying = false
                updateNotificationOnPlayPause()
            }

            Player.STATE_ENDED -> {
                isPlaying = false
                updateNotificationOnPlayPause()
            }

            Player.STATE_BUFFERING -> {

            }
        }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        if (playWhenReady) {
            isPlaying = true
            updateNotificationOnPlayPause()
        } else {
            isPlaying = false
            updateNotificationOnPlayPause()
        }
    }

    private fun updateNotificationOnPlayPause() {

        // Define intents
        val repeatPendingIntent =
            createActionIntent(PlayerNotificationAction.ACTION_SEEK_BACK)

        val prevPendingIntent =
            createActionIntent(PlayerNotificationAction.ACTION_PREVIOUS)

        val pauseIntent = createActionIntent(PlayerNotificationAction.ACTION_PAUSE)

        val playIntent = createActionIntent(PlayerNotificationAction.ACTION_PLAY)

        val playPausePendingIntent = if (isPlaying) pauseIntent else playIntent

        val nextPendingIntent =
            createActionIntent(PlayerNotificationAction.ACTION_NEXT)

        // Define icons
        val playPauseIcon =
            if (isPlaying) {
                R.drawable.pause_notif_icon
            } else {
                R.drawable.play_notif_icon
            }

        // Clear all actions in the notification
        nBuilder.clearActions()

            .addAction(
                R.drawable.rewind_icon,
                "Repeat all",
                repeatPendingIntent
            )
            .addAction(
                R.drawable.arrow_left,
                "Previous",
                prevPendingIntent
            ) // #0
            .addAction(
                playPauseIcon,
                "Pause",
                playPausePendingIntent
            ) // #1
            .addAction(
                R.drawable.arrow_right,
                "Next",
                nextPendingIntent
            ) // #2

        notificationManager.notify(NOTIFICATION_ID, nBuilder.build())
    }

    private fun seekToNext() {
        val currentItemIndex = player.currentMediaItemIndex
        val totalNumberOfMediaItems = player.mediaItemCount

        val nextItemIndex = modulo(currentItemIndex + 1, totalNumberOfMediaItems)

        player.seekTo(nextItemIndex, 0)

    }

    private fun seekToPrevious() {
        val currentItemIndex = player.currentMediaItemIndex
        val totalNumberOfMediaItems = player.mediaItemCount

        val previousItemIndex = modulo(currentItemIndex - 1, totalNumberOfMediaItems)

        player.seekTo(previousItemIndex, 0)
    }

    private fun createActionIntent(action: PlayerNotificationAction): PendingIntent {
        return PendingIntent.getService(
            this,
            0,
            Intent(this, PlaybackService::class.java).setAction(action.actionString),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "PlaybackServiceChannel"
        private const val PLAYBACK_SERVICE_TAG = "My playback service"
    }

    enum class PlayerNotificationAction(val actionString: String) {
        ACTION_SEEK_BACK("com.example.musicplayerapp.ACTION_SEEK_BACK"),
        ACTION_PLAY("com.example.musicplayerapp.ACTION_PLAY"),
        ACTION_PAUSE("com.example.musicplayerapp.ACTION_PAUSE"),
        ACTION_PREVIOUS("com.example.musicplayerapp.ACTION_PREVIOUS"),
        ACTION_NEXT("com.example.musicplayerapp.ACTION_NEXT"),
    }
}