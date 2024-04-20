package com.example.musicplayerapp.presentation.playerscreen.viewmodel

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.musicplayerapp.config.UPDATE_DELAY
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import com.example.musicplayerapp.player.MusicPlayer
import com.example.musicplayerapp.player.MusicPlayerInterface
import com.example.musicplayerapp.player.PlayerState
import com.example.musicplayerapp.presentation.playerscreen.state.PlaybackState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import com.example.musicplayerapp.service.PlaybackService
import com.example.musicplayerapp.utils.StateUpdater
import com.google.common.util.concurrent.MoreExecutors
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    context: Context,
    private val getTracksUseCase: GetTracksUseCase,
    private val player: MusicPlayer
) : ViewModel(), MusicPlayerInterface {

    /**
     * A mutable state list of all tracks.
     */
    private val _tracks = mutableStateListOf<TrackState>()

    /**
     * An immutable snapshot of the current list of tracks.
     */
    val tracks: List<TrackState> get() = _tracks

    /**
     * A private [MutableStateFlow] that holds the current [PlaybackState].
     * It is used to emit updates about the playback state to observers.
     */
    private val _playbackState = MutableStateFlow(PlaybackState(false, 0L, 0L))

    /**
     * A public property that exposes the [_playbackState] as an immutable [StateFlow] for observers.
     */
    val playbackState: StateFlow<PlaybackState> get() = _playbackState

    /**
     * A private Boolean variable to know whether a track is currently being played or not.
     */
    private val _isTrackPlaying = mutableStateOf(false)

    /**
     * A public property that exposes the [_isTrackPlaying] as an immutable [State] for observers.
     */
    val isTrackPlaying: State<Boolean> = _isTrackPlaying

    /**
     * The [stateUpdater] is used to start and stop updates (which happens after each frame)
     * of the [player]'s state.
     */
    private val stateUpdater = StateUpdater(
        callBack = { updateControllersStateCallback() },
        updatePeriodMillis = UPDATE_DELAY
    )

    private val sessionToken =
        SessionToken(context, ComponentName(context, PlaybackService::class.java))

    private val controllerFuture =
        MediaController.Builder(context, sessionToken).buildAsync()

    private val sliderIsInChangingState = mutableStateOf(false)

    /**
     * A public property backed by mutable state that holds the currently selected [TrackState].
     * It can only be set within the [PlayerViewModel] class.
     */
    var selectedTrack: TrackState? by mutableStateOf(null)
        private set

    /**
     * A private property that holds the index of the currently selected track.
     */
    private var selectedTrackIndex = 0

    /**
     * Converts a list of [TrackState] objects into a mutable list of [MediaItem] objects.
     *
     * @return A mutable list of [MediaItem] objects.
     */
    private fun List<TrackState>.toMediaItemList(): MutableList<MediaItem> {
        return this.map { MediaItem.fromUri(it.trackUrl) }.toMutableList()
    }

    /**
     * Loads tracks from content provider
     */
    fun getTracks(urisList: List<Uri>) {

        _tracks.addAll(
            getTracksUseCase.execute(AudioUrisListModel(urisList)).map {
                Log.d("Music tracks", it.trackUri)
                TrackState(
                    trackId = it.trackId,
                    trackName = it.trackName,
                    trackUrl = it.trackUri,
                    trackImage = it.trackImage,
                    artistName = it.artistName,
                    isSelected = it.isSelected,
                )
            })

        if (tracks.isNotEmpty()) {
            controllerFuture.addListener({
                val controller = controllerFuture.get()
                controller.addMediaItems(tracks.toMediaItemList())
            }, MoreExecutors.directExecutor())
        }
    }

    /**
     * Plays selected track in the list.
     */
    private fun playSelectedTrack() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.seekTo(selectedTrackIndex, 0)
            controller.play()
        }, MoreExecutors.directExecutor())
        stateUpdater.start()
    }

    /**
     * Resets the state of each track in the list to the default state.
     */
    private fun MutableList<TrackState>.resetTracks() {
        this.forEach { track ->
            track.isSelected = false
        }
    }

    private fun commitTrackListUpdate() {
        val updatedTracksList = _tracks.toList()
        _tracks.clear()
        _tracks.addAll(updatedTracksList)
    }

    private fun updateControllersStateCallback() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()

            val playerState = player.playerState.value

            val state = controller.playbackState

            when (state) {
                Player.STATE_READY -> {
                    if (controller.isPlaying) {
                        _isTrackPlaying.value = true
                        emitPlaybackState()
                        Log.d(MEDIA_CONTROLLER_TAG, "Player is playing")
                    } else {
                        _isTrackPlaying.value = false
                        emitPlaybackState()
                        Log.d(MEDIA_CONTROLLER_TAG, "Player is paused")
                    }
                }

                Player.STATE_ENDED -> {
                    Log.d(MEDIA_CONTROLLER_TAG, "Playlist is ended")
                }

                Player.STATE_BUFFERING -> {
                    Log.d(MEDIA_CONTROLLER_TAG, "Player is buffering")
                }

                Player.STATE_IDLE -> {
                    Log.d(MEDIA_CONTROLLER_TAG, "Player is idle")
                }
            }

            when (playerState) {
                PlayerState.STATE_NEXT_TRACK_AUTO -> {
                    updateCurrentTrackPlayingState(controller.currentMediaItemIndex)
                }

                PlayerState.STATE_TRACK_CHANGED_BY_USER -> {
                    updateCurrentTrackPlayingState(controller.currentMediaItemIndex)
                }

                PlayerState.PLAYLIST_CHANGED -> {}
                PlayerState.TRANSITION_REASON_REPEAT -> {}

                PlayerState.STATE_IDLE -> { }
                PlayerState.STATE_ERROR -> {
                    Log.d(MEDIA_CONTROLLER_TAG, "Player error")
                }
            }

        }, MoreExecutors.directExecutor())
    }

    private fun updateCurrentTrackPlayingState(index: Int) {
        _tracks.resetTracks()
        selectedTrackIndex = index
        _tracks[index].isSelected = true
        commitTrackListUpdate()
    }

    private fun emitPlaybackState() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            _playbackState.tryEmit(
                value = PlaybackState(
                    isInChangingState = sliderIsInChangingState.value,
                    currentPlaybackPosition = controller.currentPosition,
                    currentTrackDuration = controller.duration
                )
            )
        }, MoreExecutors.directExecutor())
    }

    fun putSliderInChangingState() {
        sliderIsInChangingState.value = true
    }

    fun pullSliderFromChangingState() {
        sliderIsInChangingState.value = false
    }

    private fun playController() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.prepare()
            controller.play()
        }, MoreExecutors.directExecutor())
    }

    private fun pauseController() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.pause()
        }, MoreExecutors.directExecutor())
    }

    private fun startPlaying() {
        playController()
        stateUpdater.start()
    }

    private fun stopPlaying() {
        pauseController()
    }

    override fun onPlayClick() {
        startPlaying()
    }

    override fun onPauseClick() {
        stopPlaying()
    }

    /**
     * Implementation of [MusicPlayerInterface.onPreviousClick].
     * Switches to the previous track if one exists.
     */
    override fun onPreviousClick() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()

            if (selectedTrackIndex > 0) {
                updateCurrentTrackPlayingState(selectedTrackIndex - 1)
            }
            if (controller.isPlaying) {
                playSelectedTrack()
            }

        }, MoreExecutors.directExecutor())

    }

    /**
     * Implementation of [MusicPlayerInterface.onNextClick].
     * Switches to the next track in the list if one exists.
     */
    override fun onNextClick() {
        controllerFuture.addListener({
            val controller = controllerFuture.get()

            if (selectedTrackIndex < tracks.size - 1) {
                updateCurrentTrackPlayingState(selectedTrackIndex + 1)
            }

            if (controller.isPlaying) {
                playSelectedTrack()
            }

        }, MoreExecutors.directExecutor())
    }

    /**
     * Implementation of [MusicPlayerInterface.onTrackClick].
     * Selects the clicked track from the track list.
     *
     * @param track The track that was clicked.
     */
    override fun onTrackClick(track: TrackState) {
        updateCurrentTrackPlayingState(tracks.indexOf(track))
        playSelectedTrack()
    }

    /**
     * Implementation of [MusicPlayerInterface.onSeekBarPositionChanged].
     * Seeks to the specified position in the current track.
     *
     * @param position The position to seek to.
     */
    override fun onSeekBarPositionChanged(position: Long) {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.seekTo(position)
        }, MoreExecutors.directExecutor())
    }

    /**
     * Releases the media player and the media controller, stops the StateUpdater when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        stateUpdater.stop()
        player.releasePlayer()
        MediaController.releaseFuture(controllerFuture)
    }

    companion object {
        const val MEDIA_CONTROLLER_TAG = "My media-controller"
    }
}
