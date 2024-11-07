package com.example.musicplayerapp.presentation.playerscreen.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
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
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerBarState
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerBarVisibility
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerUIState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderControlState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderProgressState
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
     * State that stores a list of all tracks.
     */
    private val _tracks = mutableStateListOf<TrackState>()
    val tracks: List<TrackState> get() = _tracks

    /**
     * It emits updated playback state to observers.
     */
    private val _sliderProgressState = MutableStateFlow(SliderProgressState())
    val sliderProgressState: StateFlow<SliderProgressState> get() = _sliderProgressState

    private val _playerBarState = mutableStateOf(PlayerBarState())
    val playerBarState = _playerBarState


    private val _sliderControlState = mutableStateOf(SliderControlState.AUTO)

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
                _playerBarState.value =
                    _playerBarState.value.copy(playerState = PlayerUIState.PLAYING)
                emitPlaybackState()
                Log.d(MEDIA_CONTROLLER_TAG, "Player is playing")
            },
            onPaused = {
                _playerBarState.value =
                    _playerBarState.value.copy(playerState = PlayerUIState.PAUSED)
                emitPlaybackState()
                Log.d(MEDIA_CONTROLLER_TAG, "Player is paused")
            },
            onEnded = {
                _playerBarState.value =
                    _playerBarState.value.copy(playerState = PlayerUIState.PAUSED)
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
                _playerBarState.value =
                    _playerBarState.value.copy(playerState = PlayerUIState.ERROR)
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
        _sliderProgressState.tryEmit(
            value = SliderProgressState(
                currentPlaybackPosition = playerController.getCurrentPosition(),
                currentTrackDuration = playerController.getCurrentTrackDuration()
            )
        )
        _playerBarState.value = _playerBarState.value.copy(sliderControlState = _sliderControlState.value)
    }

    fun setSliderToManualState() {
        _sliderControlState.value = SliderControlState.MANUAL
    }

    fun setSliderToAutoState() {
        _sliderControlState.value = SliderControlState.AUTO
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

        when (_playerBarState.value.barVisibility) {
            PlayerBarVisibility.VISIBLE -> {
                // Do nothing
            }
            PlayerBarVisibility.INVISIBLE -> {
                _playerBarState.value =
                    _playerBarState.value.copy(barVisibility = PlayerBarVisibility.VISIBLE)
            }
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
     * Releases the media player and the media controller,
     * stops the StateUpdater when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        stateUpdater.stop()

        when(_playerBarState.value.playerState) {
            PlayerUIState.PLAYING -> {
                // Do nothing
            }
            PlayerUIState.PAUSED -> {
                player.releasePlayer()
                playerController.release()
            }
            PlayerUIState.ERROR -> {
                player.releasePlayer()
                playerController.release()
            }
        }

        Log.d(VM_TAG, "View-model is cleared")
    }

    companion object {
        const val MEDIA_CONTROLLER_TAG = "My media-controller"
        const val VM_TAG = "PlayerVM"
    }
}
