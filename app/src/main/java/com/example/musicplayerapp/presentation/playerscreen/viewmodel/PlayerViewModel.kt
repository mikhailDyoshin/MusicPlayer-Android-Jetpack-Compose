package com.example.musicplayerapp.presentation.playerscreen.viewmodel

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayerapp.player.PlayerState
import com.example.musicplayerapp.player.controller.PlayerController
import com.example.musicplayerapp.player.PlaylistManager
import com.example.musicplayerapp.player.state.TrackState
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerBarState
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerBarVisibility
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerUIState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderControlState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderProgressState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val playerController: PlayerController,
    private val playlistManager: PlaylistManager
) : ViewModel() {

    /**
     * State that stores a list of all tracks.
     */
    val playlistState: Flow<List<TrackUIState>> = getFlowOfTracks()

    /**
     * It emits updated playback state to observers.
     */
//    private val _sliderProgressState = MutableStateFlow(SliderProgressState())
    val sliderProgressState: StateFlow<SliderProgressState> = getSliderProgressStateFlow()

    private val _playerBarState = MutableStateFlow(PlayerBarState())
    val playerBarState: StateFlow<PlayerBarState> get() = _playerBarState

    private val _sliderControlState = mutableStateOf(SliderControlState.AUTO)

    init {
        getPlayerState()
    }

    /**
     * Loads tracks from content provider
     */
    fun addTracks(urisList: List<Uri>) {
        playlistManager.addTracks(urisList)
    }

    private fun getFlowOfTracks(): Flow<List<TrackUIState>> {
        return playlistManager.tracksState.map { list -> list.map { track -> track.toTrackUIState() } }
    }

    private fun seekToSelectedTrack(selectedTrackIndex: Int) {
        playerController.seekToTrack(selectedTrackIndex)
    }

    private fun TrackState.toTrackUIState(): TrackUIState {
        return TrackUIState(
            trackName = this.trackName,
            trackUrl = this.trackUrl,
            isSelected = this.isSelected
        )

    }

    private fun TrackUIState.toTrackState(): TrackState {
        return TrackState(
            trackName = this.trackName,
            trackUrl = this.trackUrl,
            isSelected = this.isSelected
        )

    }

    private fun getPlayerState() {
            playerController.playerState.onEach { playerState ->
                when(playerState) {
                    PlayerState.STATE_IDLE -> updatePlayerState(PlayerUIState.PAUSED)
                    PlayerState.STATE_ERROR -> updatePlayerState(PlayerUIState.ERROR)
                    PlayerState.STATE_ENDED -> updatePlayerState(PlayerUIState.PAUSED)
                    PlayerState.STATE_PLAYING -> updatePlayerState(PlayerUIState.PLAYING)
                    PlayerState.STATE_PAUSE -> updatePlayerState(PlayerUIState.PAUSED)
                    PlayerState.STATE_NEXT_TRACK_AUTO -> {
                        playlistManager.updateIndex(playerController.getCurrentTrackIndex())
                    }
                    PlayerState.STATE_TRACK_CHANGED_BY_USER -> {
                        playlistManager.updateIndex(playerController.getCurrentTrackIndex())
                    }
                    PlayerState.PLAYLIST_CHANGED -> {
                        // Do nothing yet
                    }
                    PlayerState.TRANSITION_REASON_REPEAT -> {
                        // Do nothing yet
                    }
                }
            }.launchIn(viewModelScope)

    }

    private fun getSliderProgressStateFlow(): StateFlow<SliderProgressState> {
        return playerController.positionState.map {
            SliderProgressState(
                currentPlaybackPosition = it.position,
                currentTrackDuration = playerController.getCurrentTrackDuration()
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), SliderProgressState())
    }

    private fun updatePlayerState(playerState: PlayerUIState) {
        _playerBarState.value =
            _playerBarState.value.copy(playerState = playerState)
    }

    fun setSliderToManualState() {
        _sliderControlState.value = SliderControlState.MANUAL
    }

    fun setSliderToAutoState() {
        _sliderControlState.value = SliderControlState.AUTO
    }

    fun onPlayClick() {
        playerController.play()
    }

    fun onPauseClick() {
        playerController.pause()
    }

    fun onPreviousClick() {
        playerController.previous { index -> playlistManager.updateIndex(index) }
    }

    fun onNextClick() {
        playerController.next { index -> playlistManager.updateIndex(index) }
    }

    fun onTrackClick(track: TrackUIState) {

        when (_playerBarState.value.barVisibility) {
            PlayerBarVisibility.VISIBLE -> {
                // Do nothing
            }

            PlayerBarVisibility.INVISIBLE -> {
                _playerBarState.value =
                    _playerBarState.value.copy(barVisibility = PlayerBarVisibility.VISIBLE)
            }
        }

        val selectedTrackIndex = playlistManager.setActiveTrack(track.toTrackState())
        seekToSelectedTrack(selectedTrackIndex)
        playerController.play()
    }

    fun onSeekBarPositionChanged(position: Long) {
        playerController.seekToPosition(position)
    }

    /**
     * Releases the media controller
     * and stops the StateUpdater when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()

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
//        const val MEDIA_CONTROLLER_TAG = "PlayerViewModel_MediaController"
        const val VM_TAG = "PlayerVM"
    }
}
