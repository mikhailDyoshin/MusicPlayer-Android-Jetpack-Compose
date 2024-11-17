package com.example.musicplayerapp.player.service

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.musicplayerapp.player.notification.PlayerNotificationManager
import com.example.musicplayerapp.utils.modulo
import com.google.common.collect.ImmutableList
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@UnstableApi
@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class PlaybackService : MediaSessionService(), Player.Listener {

    @Inject
    lateinit var mediaSession: MediaSession

    private lateinit var player: Player
    private lateinit var notificationManager: NotificationManager
    private lateinit var playerNotificationManager: PlayerNotificationManager

    private lateinit var audioManager: AudioManager

    private lateinit var focusListener: FocusListener

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        player = mediaSession.player
        player.addListener(
            object : Player.Listener {

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_IDLE -> {}

                        Player.STATE_BUFFERING -> {}

                        Player.STATE_READY -> {
                            Log.d(
                                PLAYBACK_SERVICE_PLAYER_TAG,
                                "Playback state changed. Player is ready"
                            )
                            if (player.playWhenReady) {
                                Log.d(
                                    PLAYBACK_SERVICE_PLAYER_TAG,
                                    "\tPlayback state changed. Player is playing"
                                )
                                requestAudioFocus()
                            } else {
                                Log.d(
                                    PLAYBACK_SERVICE_PLAYER_TAG,
                                    "\tPlayback state changed. Player is paused"
                                )
                            }
                        }

                        Player.STATE_ENDED -> {}
                    }

                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    if (player.playbackState == Player.STATE_READY) {
                        Log.d(PLAYBACK_SERVICE_PLAYER_TAG, "Player is ready")
                        if (playWhenReady) {
                            Log.d(PLAYBACK_SERVICE_PLAYER_TAG, "\tPlayer is playing")
                            requestAudioFocus()
                        } else {
                            Log.d(PLAYBACK_SERVICE_PLAYER_TAG, "\tPlayer is paused")
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e(PLAYBACK_SERVICE_PLAYER_TAG, "Playback failed: ${error.message}")
                }
            }
        )

        setUpAudioManager()

        focusListener =
            FocusListener(player = player, audioManager = audioManager, mediaSessionService = this)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        playerNotificationManager = PlayerNotificationManager(
            context = this,
            session = mediaSession,
            notificationManager = notificationManager
        )

        playerNotificationManager.setUpNotification()

        playerNotificationManager.setUpNotification()


        this.setMediaNotificationProvider(object : MediaNotification.Provider {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun createNotification(
                mediaSession: MediaSession,// this is the session we pass to style
                customLayout: ImmutableList<CommandButton>,
                actionFactory: MediaNotification.ActionFactory,
                onNotificationChangedCallback: MediaNotification.Provider.Callback
            ): MediaNotification {
                updateNotificationAccordingToPlayerState()
                // notification should be created before you return here
                return playerNotificationManager.getMediaNotification()
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            playerNotificationManager.onAction(
                action = it.action,
                onRewind = { player.seekTo(0) },
                onPrevious = { seekToPrevious() },
                onPause = { player.pause() },
                onPlay = { player.play() },
                onNext = { seekToNext() })
        }

        Log.d(PLAYBACK_SERVICE_TAG, "onStartCommand was called")
        return super.onStartCommand(intent, flags, startId)
    }


    private fun setUpAudioManager() {
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    private fun requestAudioFocus() {
        focusListener.requestAudioFocus { stopSelf() }
    }

    private fun updateNotificationAccordingToPlayerState() {
        player.addListener(
            object : Player.Listener {

                val playingTrackUri = player.currentMediaItem?.localConfiguration?.uri

                override fun onPlaybackStateChanged(playbackState: Int) {
                    super.onPlaybackStateChanged(playbackState)
                    when (playbackState) {
                        Player.STATE_READY -> {
                            if (player.playWhenReady) {
                                playerNotificationManager.updateNotification(
                                    isPlaying = true,
                                    trackUri = playingTrackUri
                                )
                            } else {
                                playerNotificationManager.updateNotification(
                                    isPlaying = false,
                                    trackUri = playingTrackUri
                                )
                            }
                        }

                        Player.STATE_IDLE -> {
                            playerNotificationManager.updateNotification(
                                isPlaying = false,
                                trackUri = playingTrackUri
                            )
                        }

                        Player.STATE_ENDED -> {
                            playerNotificationManager.updateNotification(
                                isPlaying = false,
                                trackUri = playingTrackUri
                            )
                        }

                        Player.STATE_BUFFERING -> {
                            playerNotificationManager.updateNotification(
                                isPlaying = player.playWhenReady,
                                trackUri = playingTrackUri
                            )
                        }
                    }

                }

                override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                    if (playWhenReady) {
                        playerNotificationManager.updateNotification(
                            isPlaying = true,
                            trackUri = playingTrackUri
                        )
                    } else {
                        playerNotificationManager.updateNotification(
                            isPlaying = false,
                            trackUri = playingTrackUri
                        )
                    }
                }
            }
        )
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

            playerNotificationManager.cancel()
            Log.d(PLAYBACK_SERVICE_TAG, "Notifications was removed")
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

    companion object {
        private const val PLAYBACK_SERVICE_TAG = "MyPlaybackService"
        private const val PLAYBACK_SERVICE_PLAYER_TAG = "MyPlaybackServicePlayer"
    }
}