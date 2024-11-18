package com.example.musicplayerapp.player.controller

import android.content.ComponentName
import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayerapp.player.MusicPlayer
import com.example.musicplayerapp.player.PlayerState
import com.example.musicplayerapp.player.service.PlaybackService
import com.example.musicplayerapp.player.state.TrackPosition
import com.example.musicplayerapp.utils.modulo
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PlayerController @Inject constructor(
    context: Context,
    private val player: MusicPlayer
) {

    val playerState: StateFlow<PlayerState> = player.playerState

    val positionState: StateFlow<TrackPosition> = player.positionState

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

    fun getCurrentTrackDuration(): Long {
        val duration = controllerFuture.get().duration
        return if (duration > 0L) duration else 0L
    }

    fun getCurrentTrackIndex(): Int {
        return player.getCurrentTrackIndex()
    }

    fun next(onIndexUpdated: (nextIndex: Int) -> Unit) {
        controllerFuture.addListener({
            val controller = controllerFuture.get()

            val currentItemIndex = controller.currentMediaItemIndex
            val totalNumberOfMediaItems = controller.mediaItemCount
            val nextItemIndex = modulo(currentItemIndex + 1, totalNumberOfMediaItems)

            seekToTrack(nextItemIndex)
            onIndexUpdated(nextItemIndex)

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

    fun previous(onIndexUpdated: (previousIndex: Int) -> Unit) {
        controllerFuture.addListener({
            val controller = controllerFuture.get()

            val currentItemIndex = controller.currentMediaItemIndex
            val totalNumberOfMediaItems = controller.mediaItemCount
            val previousItemIndex = modulo(currentItemIndex - 1, totalNumberOfMediaItems)

            seekToTrack(previousItemIndex)
            onIndexUpdated(previousItemIndex)

            if (controller.isPlaying) {
                play()
            }

        }, MoreExecutors.directExecutor())
    }

    fun release() {
        MediaController.releaseFuture(controllerFuture)
        player.releasePlayer()
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

}