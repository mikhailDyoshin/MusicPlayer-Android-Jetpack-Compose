package com.example.musicplayerapp.controller

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayerapp.player.MusicPlayer
import com.example.musicplayerapp.player.PlayerState
import com.example.musicplayerapp.service.PlaybackService
import com.example.musicplayerapp.utils.modulo
import com.google.common.util.concurrent.MoreExecutors

class PlayerController(
    context: Context,
    private val player: MusicPlayer
) {

    private val sessionToken =
        SessionToken(context, ComponentName(context, PlaybackService::class.java))

    private val controllerFuture =
        MediaController.Builder(context, sessionToken).buildAsync()

    fun addTracks(tracks: List<MediaItem>) {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.addMediaItems(tracks)
        }, MoreExecutors.directExecutor())
    }

    fun controllerStateCallbacks(
        onBuffering: () -> Unit,
        onEnded: () -> Unit,
        onIdle: () -> Unit,
        onPaused: () -> Unit,
        onPlaying: () -> Unit,
    ) {
        controllerFuture.addListener({
            val controller = controllerFuture.get()

            val controllerState = controller.playbackState

            when (controllerState) {
                Player.STATE_READY -> {
                    if (controller.isPlaying) {
                        onPlaying()
                    } else {
                        onPaused()
                    }
                }

                Player.STATE_ENDED -> {
                    onEnded()
                }

                Player.STATE_BUFFERING -> {
                    onBuffering()
                }

                Player.STATE_IDLE -> {
                    onIdle()
                }
            }

        }, MoreExecutors.directExecutor())
    }

    fun getCurrentPosition(): Long {
        return controllerFuture.get().currentPosition
    }

    fun getCurrentTrackDuration(): Long {
        return controllerFuture.get().duration
    }

    fun next() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()

            val currentItemIndex = controller.currentMediaItemIndex
            val totalNumberOfMediaItems = controller.mediaItemCount
            val nextItemIndex = modulo(currentItemIndex + 1, totalNumberOfMediaItems)

            seekToTrack(nextItemIndex)
            if (controller.isPlaying) {
                play()
            }

        }, MoreExecutors.directExecutor())
    }

    fun pause() {
        controllerFuture.addListener({
            controllerFuture.get().pause()
        }, MoreExecutors.directExecutor())
    }

    fun play() {
        controllerFuture.addListener({
            controllerFuture.get().prepare()
            controllerFuture.get().play()
        }, MoreExecutors.directExecutor())
    }

    fun playerStateCallbacks(
        onNextTrackAuto: () -> Unit,
        onTrackChangedByUser: () -> Unit,
        onPlaylistChanged: () -> Unit,
        onTransitionReasonRepeat: () -> Unit,
        onIdle: () -> Unit,
        onError: () -> Unit,
    ) {

        controllerFuture.addListener({
            val playerState = player.playerState.value

            when (playerState) {
                PlayerState.STATE_NEXT_TRACK_AUTO -> {
                    onNextTrackAuto()
                }

                PlayerState.STATE_TRACK_CHANGED_BY_USER -> {
                    onTrackChangedByUser()
                }

                PlayerState.PLAYLIST_CHANGED -> {
                    onPlaylistChanged()
                }

                PlayerState.TRANSITION_REASON_REPEAT -> {
                    onTransitionReasonRepeat()
                }

                PlayerState.STATE_IDLE -> {
                    onIdle()
                }

                PlayerState.STATE_ERROR -> {
                    onError()
                }
            }

        }, MoreExecutors.directExecutor())

    }

    fun previous() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()

            val currentItemIndex = controller.currentMediaItemIndex
            val totalNumberOfMediaItems = controller.mediaItemCount
            val previousItemIndex = modulo(currentItemIndex - 1, totalNumberOfMediaItems)

            seekToTrack(previousItemIndex)

            if (controller.isPlaying) {
                play()
            }

        }, MoreExecutors.directExecutor())
    }

    fun release() {
        MediaController.releaseFuture(controllerFuture)
    }

    fun seekToTrack(trackIndex: Int) {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.seekTo(trackIndex, 0)
        }, MoreExecutors.directExecutor())
    }

    fun seekToPosition(position: Long) {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.seekTo(position)
        }, MoreExecutors.directExecutor())
    }

    companion object {
        const val MEDIA_CONTROLLER_TAG = "PlayerControllerClass"
    }
}