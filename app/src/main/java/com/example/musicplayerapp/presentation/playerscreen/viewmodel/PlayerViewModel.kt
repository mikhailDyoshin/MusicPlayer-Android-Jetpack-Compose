package com.example.musicplayerapp.presentation.playerscreen.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import com.example.musicplayerapp.config.UPDATE_DELAY
import com.example.musicplayerapp.controller.PlayerController
import com.example.musicplayerapp.player.MusicPlayerInterface
import com.example.musicplayerapp.player.PlaylistManager
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
    private val playerController: PlayerController,
    private val playlistManager: PlaylistManager
) : ViewModel(), MusicPlayerInterface {

    /**
     * State that stores a list of all tracks.
     */
    val playlistState: State<List<TrackState>> = playlistManager.tracksState

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
     * of the [playerController]'s state.
     */
    private val stateUpdater = StateUpdater(
        callBack = {
            checkOutControllerState()
            checkoutPlayerState()
        },
        updatePeriodMillis = UPDATE_DELAY
    )

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
    fun addTracks(urisList: List<Uri>) {

        playlistManager.addTracks(urisList)

        if (playlistState.value.isNotEmpty()) {
            playerController.addTracks(playlistState.value.toMediaItemList())
        }

    }

    private fun seekToSelectedTrack(selectedTrackIndex: Int) {
        playerController.seekToTrack(selectedTrackIndex)
        stateUpdater.start()
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
                playlistManager.updateIndex(playerController.getCurrentTrackIndex())
            },
            onTrackChangedByUser = {
                playlistManager.updateIndex(playerController.getCurrentTrackIndex())
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

    private fun emitPlaybackState() {
        _sliderProgressState.tryEmit(
            value = SliderProgressState(
                currentPlaybackPosition = playerController.getCurrentPosition(),
                currentTrackDuration = playerController.getCurrentTrackDuration()
            )
        )
        _playerBarState.value =
            _playerBarState.value.copy(sliderControlState = _sliderControlState.value)
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
        playerController.previous { index -> playlistManager.updateIndex(index) }
    }

    /**
     * Implementation of [MusicPlayerInterface.onNextClick].
     * Switches to the next track in the list if one exists.
     */
    override fun onNextClick() {
        playerController.next { index -> playlistManager.updateIndex(index) }
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

        val selectedTrackIndex = playlistState.value.indexOf(track)
        playlistManager.updateIndex(selectedTrackIndex)
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
     * Releases the media controller
     * and stops the StateUpdater when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        stateUpdater.stop()

        when (_playerBarState.value.playerState) {
            PlayerUIState.PLAYING -> {
                // Do nothing
            }

            PlayerUIState.PAUSED -> {
                playerController.release()
            }

            PlayerUIState.ERROR -> {
                playerController.release()
            }
        }

        Log.d(VM_TAG, "PlayerViewModel-class is cleared")
    }

    companion object {
        const val MEDIA_CONTROLLER_TAG = "PlayerViewModel_MediaController"
        const val VM_TAG = "PlayerVM"
    }
}
