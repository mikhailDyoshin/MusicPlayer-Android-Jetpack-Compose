package com.example.musicplayerapp.player.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaStyleNotificationHelper
import com.example.musicplayerapp.R
import com.example.musicplayerapp.player.service.PlaybackService

@UnstableApi
@RequiresApi(Build.VERSION_CODES.O)
class PlayerNotification(private val context: Context, private val session: MediaSession, private val notificationManager: NotificationManager) {

    private val notificationBuilder = mutableStateOf(NotificationCompat.Builder(context, CHANNEL_ID))

    fun setUpNotification() {
        createChannel()
        createNotification()
    }

    fun notify() {
        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.value.build())
    }

    fun getMediaNotification(): MediaNotification {
        return MediaNotification(NOTIFICATION_ID, notificationBuilder.value.build())
    }


    fun updateNotification(isPlaying: Boolean) {

        // Define intents
        val repeatPendingIntent =
            createActionIntent(PlayerNotificationAction.ACTION_REWIND)

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

        // Clear all actions in the notification and add new ones
        notificationBuilder.value = notificationBuilder.value.clearActions()
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

    }


    private fun createChannel() {
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "PlayerNotificationChannel",
                NotificationManager.IMPORTANCE_NONE
            )
        )
    }

    private fun createNotification() {
        notificationBuilder.value = NotificationCompat.Builder(context, CHANNEL_ID)
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
    }


    private fun createActionIntent(action: PlayerNotificationAction): PendingIntent {
        return PendingIntent.getService(
            context,
            0,
            Intent(context, PlaybackService::class.java).setAction(action.actionString),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "PlaybackServiceChannel"

        enum class PlayerNotificationAction(val actionString: String) {
            ACTION_REWIND("com.example.musicplayerapp.ACTION_REWIND"),
            ACTION_PLAY("com.example.musicplayerapp.ACTION_PLAY"),
            ACTION_PAUSE("com.example.musicplayerapp.ACTION_PAUSE"),
            ACTION_PREVIOUS("com.example.musicplayerapp.ACTION_PREVIOUS"),
            ACTION_NEXT("com.example.musicplayerapp.ACTION_NEXT"),
        }
    }

}