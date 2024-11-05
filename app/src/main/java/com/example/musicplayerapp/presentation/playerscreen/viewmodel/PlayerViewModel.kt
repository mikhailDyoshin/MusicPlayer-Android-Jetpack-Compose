package com.example.musicplayerapp.presentation.playerscreen.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import com.example.musicplayerapp.config.UPDATE_DELAY
import com.example.musicplayerapp.controller.PlayerController
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import com.example.musicplayerapp.player.MusicPlayer
import com.example.musicplayerapp.player.MusicPlayerInterface
import com.example.musicplayerapp.presentation.playerscreen.state.PlaybackState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import com.example.musicplayerapp.utils.StateUpdater
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
        callBack = {
            checkOutControllerState()
            checkoutPlayerState()
        },
        updatePeriodMillis = UPDATE_DELAY
    )

    private val playerController = PlayerController(context, player)

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
            playerController.addTracks(newTracks.toMediaItemList())
        }
    }

    private fun seekToSelectedTrack(selectedTrackIndex: Int) {
        playerController.seekToTrack(selectedTrackIndex)
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

    private fun checkOutControllerState() {

        playerController.controllerStateCallbacks(
            onPlaying = {
                _isTrackPlaying.value = true
                emitPlaybackState()
                Log.d(MEDIA_CONTROLLER_TAG, "Player is playing")
            },
            onPaused = {
                _isTrackPlaying.value = false
                emitPlaybackState()
                Log.d(MEDIA_CONTROLLER_TAG, "Player is paused")
            },
            onEnded = {
                _isTrackPlaying.value = false
                playerController.pause()
                Log.d(MEDIA_CONTROLLER_TAG, "Playlist is ended")
            },
            onBuffering = {
                Log.d(MEDIA_CONTROLLER_TAG, "Player is buffering")
            },
            onIdle = {
                Log.d(MEDIA_CONTROLLER_TAG, "Player is idle")
            }
        )
    }

    private fun checkoutPlayerState() {
        playerController.playerStateCallbacks(
            onNextTrackAuto = {
                updateCurrentTrackPlayingState(playerController.getCurrentTrackIndex())
            },
            onTrackChangedByUser = {
                updateCurrentTrackPlayingState(playerController.getCurrentTrackIndex())
            },
            onPlaylistChanged = {
                Log.d(MEDIA_CONTROLLER_TAG, "Playlist changed")
            },
            onIdle = {
                Log.d(MEDIA_CONTROLLER_TAG, "Player idle")
            },
            onTransitionReasonRepeat = {
                Log.d(MEDIA_CONTROLLER_TAG, "Player repeat")
            },
            onError = {
                Log.d(MEDIA_CONTROLLER_TAG, "Player error")
            }
        )
    }

    private fun updateCurrentTrackPlayingState(index: Int) {
        _tracks.resetTracks()
        _tracks[index].isSelected = true
        commitTrackListUpdate()
    }

    private fun emitPlaybackState() {
        _playbackState.tryEmit(
            value = PlaybackState(
                isInChangingState = sliderIsInChangingState.value,
                currentPlaybackPosition = playerController.getCurrentPosition(),
                currentTrackDuration = playerController.getCurrentTrackDuration()
            )
        )
    }

    fun setSliderToManualState() {
        sliderIsInChangingState.value = true
    }

    fun setSliderToAutoState() {
        sliderIsInChangingState.value = false
    }

    override fun onPlayClick() {
        playerController.play()
        stateUpdater.start()
    }

    override fun onPauseClick() {
        playerController.pause()
    }

    /**
     * Implementation of [MusicPlayerInterface.onPreviousClick].
     * Switches to the previous track if one exists.
     */
    override fun onPreviousClick() {
        playerController.previous { index -> updateCurrentTrackPlayingState(index) }
    }

    /**
     * Implementation of [MusicPlayerInterface.onNextClick].
     * Switches to the next track in the list if one exists.
     */
    override fun onNextClick() {
        playerController.next { index -> updateCurrentTrackPlayingState(index) }
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
        playerController.play()
    }

    /**
     * Implementation of [MusicPlayerInterface.onSeekBarPositionChanged].
     * Seeks to the specified position in the current track.
     *
     * @param position The position to seek to.
     */
    override fun onSeekBarPositionChanged(position: Long) {
        playerController.seekToPosition(position)
    }

    /**
     * Releases the media player and the media controller, stops the StateUpdater when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        stateUpdater.stop()
        if (!_isTrackPlaying.value) {
            player.releasePlayer()
            playerController.release()
        }
        Log.d(VM_TAG, "View-model is cleared")
    }

    companion object {
        const val MEDIA_CONTROLLER_TAG = "My media-controller"
        const val VM_TAG = "PlayerVM"
    }
}
