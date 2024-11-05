package com.example.musicplayerapp.presentation.playerscreen.viewmodel

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import com.example.musicplayerapp.utils.modulo
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

    private val _isBottomBarDisplayed = mutableStateOf(false)

    val isBottomBarDisplayed = _isBottomBarDisplayed

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
     * A private property that holds the index of the currently selected track.
     */
//    private var selectedTrackIndex = 0

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

        val newTracks = getTracksUseCase.execute(AudioUrisListModel(urisList)).map {
            Log.d("Music tracks", it.trackUri)
            TrackState(
                trackId = it.trackId,
                trackName = it.trackName,
                trackUrl = it.trackUri,
                trackImage = it.trackImage,
                artistName = it.artistName,
                isSelected = it.isSelected,
            )
        }

        _tracks.addAll(newTracks)

        if (tracks.isNotEmpty()) {
            controllerFuture.addListener({
                val controller = controllerFuture.get()
                controller.addMediaItems(newTracks.toMediaItemList())
            }, MoreExecutors.directExecutor())
        }
    }

    private fun seekToSelectedTrack(selectedTrackIndex: Int) {
        controllerFuture.addListener({
            val controller = controllerFuture.get()
            controller.seekTo(selectedTrackIndex, 0)
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

    private fun getPlaybackState() {
        controllerFuture.addListener(
            { _playbackStateFlow.value = controllerFuture.get().playbackState },
            MoreExecutors.directExecutor()
        )
    }

    private val _playbackStateFlow = MutableStateFlow(0)
    val playbackStateFlow: StateFlow<Int> = _playbackStateFlow

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
                    _isTrackPlaying.value = false
                    pauseController()
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

                PlayerState.STATE_IDLE -> {}
                PlayerState.STATE_ERROR -> {
                    Log.d(MEDIA_CONTROLLER_TAG, "Player error")
                }
            }

        }, MoreExecutors.directExecutor())
    }

    private fun updateCurrentTrackPlayingState(index: Int) {
        _tracks.resetTracks()
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

            val currentItemIndex = controller.currentMediaItemIndex
            val totalNumberOfMediaItems = controller.mediaItemCount
            val previousItemIndex = modulo(currentItemIndex - 1, totalNumberOfMediaItems)

            // Update the UI: change tracks' list state
            updateCurrentTrackPlayingState(previousItemIndex)

            // Use the player to seek to the selected track
            seekToSelectedTrack(previousItemIndex)

            if (controller.isPlaying) {
                playController()
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

            val currentItemIndex = controller.currentMediaItemIndex
            val totalNumberOfMediaItems = controller.mediaItemCount
            val nextItemIndex = modulo(currentItemIndex + 1, totalNumberOfMediaItems)

            updateCurrentTrackPlayingState(nextItemIndex)

            seekToSelectedTrack(nextItemIndex)
            if (controller.isPlaying) {
                playController()
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

        if (!_isBottomBarDisplayed.value) {
            _isBottomBarDisplayed.value = true
        }

        val selectedTrackIndex = tracks.indexOf(track)
        updateCurrentTrackPlayingState(selectedTrackIndex)
        seekToSelectedTrack(selectedTrackIndex)
        playController()
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
        if (!_isTrackPlaying.value) {
            player.releasePlayer()
            MediaController.releaseFuture(controllerFuture)
        }
        Log.d(VM_TAG, "View-model is cleared")
    }

    companion object {
        const val MEDIA_CONTROLLER_TAG = "My media-controller"
        const val VM_TAG = "PlayerVM"
    }
}
