package com.example.musicplayerapp.presentation.playerscreen.viewmodel

import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import com.example.musicplayerapp.config.UPDATE_DELAY
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import com.example.musicplayerapp.player.MusicPlayer
import com.example.musicplayerapp.player.MusicPlayerInterface
import com.example.musicplayerapp.player.PlayerState
import com.example.musicplayerapp.presentation.playerscreen.state.PlaybackState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import com.example.musicplayerapp.utils.StateUpdater
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    getTracksUseCase: GetTracksUseCase,
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
        callBack = { updateStateCallback() },
        updatePeriodMillis = UPDATE_DELAY
    )

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

    init {
        _tracks.addAll(
            getTracksUseCase.execute().map {
                TrackState(
                    trackId = it.trackId,
                    trackName = it.trackName,
                    trackUrl = it.trackUrl,
                    trackImage = it.trackImage,
                    artistName = it.artistName,
                    isSelected = it.isSelected,
                    state = it.state
                )
            })
        if (tracks.isNotEmpty()) {
            player.initPlayer(tracks.toMediaItemList())
            onTrackSelected(selectedTrackIndex)
        }
    }

    /**
     * Converts a list of [TrackState] objects into a mutable list of [MediaItem] objects.
     *
     * @return A mutable list of [MediaItem] objects.
     */
    private fun List<TrackState>.toMediaItemList(): MutableList<MediaItem> {
        return this.map { MediaItem.fromUri(it.trackUrl) }.toMutableList()
    }

    /**
     * Handles track selection.
     *
     * @param index The index of the selected track in the track list.
     */
    private fun onTrackSelected(index: Int) {
            _tracks.resetTracks()
            selectedTrackIndex = index
            _tracks[index].isSelected = true
            _tracks[index].state = PlayerState.STATE_PLAYING
            commitTrackListUpdate()
            setUpTrack()
    }

    /**
     * Plays selected track in the list.
     */
    private fun setUpTrack() {
        player.setUpTrack(selectedTrackIndex)
    }

    /**
     * Resets the state of each track in the list to the default state.
     */
    private fun MutableList<TrackState>.resetTracks() {
        this.forEach { track ->
            track.isSelected = false
            track.state = PlayerState.STATE_IDLE
        }
    }

    private fun commitTrackListUpdate() {
        val updatedTracksList = _tracks.toList()
        _tracks.clear()
        _tracks.addAll(updatedTracksList)
    }

    private fun updateStateCallback() {
        viewModelScope.launch {
            val playerState = player.playerState.value

            when (playerState) {
                PlayerState.STATE_PLAYING -> {
                    _isTrackPlaying.value = true
                    _playbackState.tryEmit(
                        value = PlaybackState(
                            isInChangingState = sliderIsInChangingState.value,
                            currentPlaybackPosition = player.currentPlaybackPosition,
                            currentTrackDuration = player.currentTrackDuration
                        )
                    )
                }
                PlayerState.STATE_PAUSE -> {
                    _isTrackPlaying.value = false
                    _playbackState.tryEmit(
                        value = PlaybackState(
                            isInChangingState = sliderIsInChangingState.value,
                            currentPlaybackPosition = player.currentPlaybackPosition,
                            currentTrackDuration = player.currentTrackDuration
                        )
                    )
                }
                PlayerState.STATE_BUFFERING -> {
                    // Whether a track is playing or not the player's isTrackPlaying-state
                    // stays the same while buffering
                    _isTrackPlaying.value = _isTrackPlaying.value
                }
                PlayerState.STATE_NEXT_TRACK -> {
                        switchToNextTrack()
                }
                else -> {
                    _isTrackPlaying.value = false
                    stateUpdater.stop()
                }
            }
        }
    }

    fun putSliderInChangingState() {
        sliderIsInChangingState.value = true
    }

    fun pullSliderFromChangingState() {
        sliderIsInChangingState.value = false
    }

    private fun startPlaying() {
        player.play()
        stateUpdater.start()
    }

    private fun stopPlaying() {
        player.pause()
        stateUpdater.stop()
    }

    private fun switchToNextTrack() {
        if (selectedTrackIndex < tracks.size - 1) {
            onTrackSelected(selectedTrackIndex + 1)
        }
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
        if (selectedTrackIndex > 0) {
            onTrackSelected(selectedTrackIndex - 1)
        }
        if (isTrackPlaying.value) {
            startPlaying()
        }
    }

    /**
     * Implementation of [MusicPlayerInterface.onNextClick].
     * Switches to the next track in the list if one exists.
     */
    override fun onNextClick() {
        switchToNextTrack()
        if (isTrackPlaying.value) {
            startPlaying()
        }
    }

    /**
     * Implementation of [MusicPlayerInterface.onTrackClick].
     * Selects the clicked track from the track list.
     *
     * @param track The track that was clicked.
     */
    override fun onTrackClick(track: TrackState) {
        onTrackSelected(tracks.indexOf(track))
        startPlaying()
    }

    /**
     * Implementation of [MusicPlayerInterface.onSeekBarPositionChanged].
     * Seeks to the specified position in the current track.
     *
     * @param position The position to seek to.
     */
    override fun onSeekBarPositionChanged(position: Long) {
        player.seekToPosition(position)
    }

    /**
     * Cleans up the media player when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        player.releasePlayer()
    }
}
