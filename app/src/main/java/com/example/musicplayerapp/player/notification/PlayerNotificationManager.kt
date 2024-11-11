package com.example.musicplayerapp.player.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
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
class PlayerNotificationManager(
    private val context: Context,
    private val session: MediaSession?,
    private val notificationManager: NotificationManager
) {

    private val notificationBuilder =
        mutableStateOf(NotificationCompat.Builder(context, CHANNEL_ID))

    private val actionIntentsMap =
        PlayerNotificationAction.entries.associateWith { createActionIntent(it) }

    fun setUpNotification() {
        createChannel()
        createNotification()
    }

    fun getMediaNotification(): MediaNotification {
        return MediaNotification(NOTIFICATION_ID, notificationBuilder.value.build())
    }

    fun cancel() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    fun onAction(
        action: String?,
        onRewind: () -> Unit,
        onPrevious: () -> Unit,
        onPause: () -> Unit,
        onPlay: () -> Unit,
        onNext: () -> Unit
    ) {
        when (action) {
            PlayerNotificationAction.ACTION_REWIND.actionString -> {
                onRewind()
            }

            PlayerNotificationAction.ACTION_PREVIOUS.actionString -> {
                onPrevious()
            }

            PlayerNotificationAction.ACTION_PAUSE.actionString -> {
                onPause()
            }

            PlayerNotificationAction.ACTION_PLAY.actionString -> {
                onPlay()
            }

            PlayerNotificationAction.ACTION_NEXT.actionString -> {
                onNext()
            }
        }
    }


    fun updateNotification(isPlaying: Boolean, trackUri: Uri?) {


        val playPausePendingIntent = when (isPlaying) {
            true -> actionIntentsMap[PlayerNotificationAction.ACTION_PAUSE]
            false -> actionIntentsMap[PlayerNotificationAction.ACTION_PLAY]
        }

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
                actionIntentsMap[PlayerNotificationAction.ACTION_REWIND]
            )
            .addAction(
                R.drawable.arrow_left,
                "Previous",
                actionIntentsMap[PlayerNotificationAction.ACTION_PREVIOUS]
            ) // #0
            .addAction(
                playPauseIcon,
                "Play/Pause",
                playPausePendingIntent
            ) // #1
            .addAction(
                R.drawable.arrow_right,
                "Next",
                actionIntentsMap[PlayerNotificationAction.ACTION_NEXT]
            ) // #2
            .setContentTitle(getTrackNameFromUri(trackUri))

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
                session?.let {
                    MediaStyleNotificationHelper.MediaStyle(it)
                        .setShowActionsInCompactView(
                            1, /* #1: previous button \*/
                            2, /* #2: play/pause button \*/
                            3, /* #3: next button \*/
                        )
                }
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

    private fun getTrackNameFromUri(trackUri: Uri?): String {
        return trackUri?.lastPathSegment?.substringAfterLast('/') ?: "Null"
    }

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "PlaybackServiceChannel"

        private enum class PlayerNotificationAction(val actionString: String) {
            ACTION_REWIND("com.example.musicplayerapp.ACTION_REWIND"),
            ACTION_PLAY("com.example.musicplayerapp.ACTION_PLAY"),
            ACTION_PAUSE("com.example.musicplayerapp.ACTION_PAUSE"),
            ACTION_PREVIOUS("com.example.musicplayerapp.ACTION_PREVIOUS"),
            ACTION_NEXT("com.example.musicplayerapp.ACTION_NEXT"),
        }

    }

}