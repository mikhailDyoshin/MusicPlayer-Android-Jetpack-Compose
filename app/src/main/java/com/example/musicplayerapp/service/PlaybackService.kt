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
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.MediaStyleNotificationHelper
import com.google.common.collect.ImmutableList
import javax.inject.Inject

class PlaybackService @Inject constructor(private val mediaSession: MediaSession): MediaSessionService() {

    private lateinit var notificationManager: NotificationManager
    private lateinit var nBuilder: NotificationCompat.Builder

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createNotification(mediaSession)

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
    }
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        TODO("Not yet implemented")
    }

    @OptIn(UnstableApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(session: MediaSession) {

        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
        )

        // NotificationCompat.Builder here.
        nBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSmallIcon(androidx.media3.ui.R.drawable.exo_icon_vr)
            .setStyle(
                MediaStyleNotificationHelper.MediaStyle(session)
                    .setShowActionsInCompactView(
                        1, /* #1: previous button \*/
                        2, /* #2: play/pause button \*/
                        3, /* #3: next button \*/
                    )
            )

    }

    fun updateNotificationOnPlayPause(isPlaying: Boolean) {

        // Define intents
        val repeatPendingIntent =
            createActionIntent(PlayerNotificationAction.ACTION_SEEK_BACK.actionString)

        val prevPendingIntent =
            createActionIntent(PlayerNotificationAction.ACTION_PREVIOUS.actionString)

        val pauseIntent = createActionIntent(PlayerNotificationAction.ACTION_PAUSE.actionString)

        val playIntent = createActionIntent(PlayerNotificationAction.ACTION_PLAY.actionString)

        val playPausePendingIntent = if (isPlaying) pauseIntent else playIntent

        val nextPendingIntent =
            createActionIntent(PlayerNotificationAction.ACTION_NEXT.actionString)

        // Define icons
        val playPauseIcon =
            if (isPlaying) {
                androidx.media3.ui.R.drawable.exo_notification_pause
            } else {
                androidx.media3.ui.R.drawable.exo_notification_play
            }

        // Clear all actions in the notification
        nBuilder.clearActions()

            .addAction(
                androidx.media3.ui.R.drawable.exo_styled_controls_rewind,
                "Repeat all",
                repeatPendingIntent
            )
            .addAction(
                androidx.media3.ui.R.drawable.exo_notification_previous,
                "Previous",
                prevPendingIntent
            ) // #0
            .addAction(
                playPauseIcon,
                "Pause",
                playPausePendingIntent
            ) // #1
            .addAction(
                androidx.media3.ui.R.drawable.exo_notification_next,
                "Next",
                nextPendingIntent
            ) // #2

        notificationManager.notify(NOTIFICATION_ID, nBuilder.build())
    }

    private fun createActionIntent(action: String): PendingIntent {
        return PendingIntent.getService(
            this,
            0,
            Intent(this, PlaybackService::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "PlaybackServiceChannel"
    }

    enum class PlayerNotificationAction(val actionString: String) {
        ACTION_SEEK_BACK("com.example.notificationsapp.ACTION_SEEK_BACK"),
        ACTION_PLAY("com.example.notificationsapp.ACTION_PLAY"),
        ACTION_PAUSE("com.example.notificationsapp.ACTION_PAUSE"),
        ACTION_PREVIOUS("com.example.notificationsapp.ACTION_PREVIOUS"),
        ACTION_NEXT("com.example.notificationsapp.ACTION_NEXT"),
    }
}