package com.example.musicplayerapp.presentation.playerscreen.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
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
    private val _playbackState = MutableStateFlow(PlaybackState(0L, 0L))

    /**
     * A private Boolean variable to know whether a track is currently being played or not.
     */
    private var isTrackPlaying: Boolean = false

    /**
     * A nullable [Job] instance that represents the ongoing process of updating the playback state.
     */
    private var playbackStateJob: Job? = null

    /**
     * A public property backed by mutable state that holds the currently selected [TrackState].
     * It can only be set within the [PlayerViewModel] class.
     */
    var selectedTrack: TrackState? by mutableStateOf(null)
        private set

    /**
     * A private property that holds the index of the currently selected track.
     */
    private var selectedTrackIndex = -1

    /**
     * A private Boolean variable to keep track of whether the track selection is automatic (i.e., due to the completion of a track) or manual.
     */
    private var isAutoSwitch: Boolean = false

    init {
        _tracks.addAll(
            getTracksUseCase.execute().map { it ->
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
        player.initPlayer(tracks.toMediaItemList())
        observePlayerState()
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
        if (selectedTrackIndex == -1) isTrackPlaying = true
        if (selectedTrackIndex == -1 || selectedTrackIndex != index) {
            _tracks.resetTracks()
            selectedTrackIndex = index
            Log.d(
                "Track selection",
                "Track with index: $index selected. Index in VM was changed to $selectedTrackIndex"
            )
            setUpTrack()
        }
    }

    /**
     * Plays selected track in the list.
     */
    private fun setUpTrack() {
        if (!isAutoSwitch) player.setUpTrack(selectedTrackIndex, isTrackPlaying)
        isAutoSwitch = false
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

    /**
     * Updates the playback state and launches or cancels the playback state job accordingly.
     *
     * @param state The new player state.
     */
    private fun updateState(state: PlayerState) {
        if (selectedTrackIndex != -1) {
            isTrackPlaying = state == PlayerState.STATE_PLAYING
            _tracks[selectedTrackIndex].state = state
            _tracks[selectedTrackIndex].isSelected = true
            commitTrackListUpdate()
            Log.d(
                "Track selection",
                "Track with index: $selectedTrackIndex is selected. Its state changed: ${_tracks[selectedTrackIndex].isSelected}"
            )
            selectedTrack = null
            selectedTrack = _tracks[selectedTrackIndex]

            updatePlaybackState(state)
            if (state == PlayerState.STATE_NEXT_TRACK) {
                isAutoSwitch = true
                onNextClick()
            }
            if (state == PlayerState.STATE_END) onTrackSelected(0)
        }
    }

    private fun updatePlaybackState(state: PlayerState) {
        playbackStateJob?.cancel()
        playbackStateJob = viewModelScope.launchPlaybackStateJob(_playbackState, state, player)
    }

    private fun observePlayerState() {
        viewModelScope.collectPlayerState(player, ::updateState)
    }

    /**
     * Collects the player state from [player] and provides updates via the [updateState] function.
     *
     * @param player The player whose state is to be collected.
     * @param updateState A function to process the player state updates.
     */
    private fun CoroutineScope.collectPlayerState(
        player: MusicPlayer, updateState: (PlayerState) -> Unit
    ) {
        this.launch {
            player.playerState.collect {
                updateState(it)
            }
        }
    }

    /**
     * Launches a coroutine to periodically update the [playbackStateFlow] with the current
     * playback position and track duration from [myPlayer] as long as the player state is [PlayerState.STATE_PLAYING].
     *
     * @param playbackStateFlow The MutableStateFlow to be updated.
     * @param state The current player state.
     * @param myPlayer The player whose playback information is to be collected.
     */
    private fun CoroutineScope.launchPlaybackStateJob(
        playbackStateFlow: MutableStateFlow<PlaybackState>,
        state: PlayerState,
        myPlayer: MusicPlayer
    ) = launch {
        do {
            playbackStateFlow.emit(
                PlaybackState(
                    currentPlaybackPosition = myPlayer.currentPlaybackPosition,
                    currentTrackDuration = myPlayer.currentTrackDuration
                )
            )
            delay(UPDATE_DELAY)
        } while (state == PlayerState.STATE_PLAYING && isActive)
    }


    /**
     * Implementation of [MusicPlayerInterface.onPlayPauseClick].
     * Toggles play/pause state of the current track.
     */
    override fun onPlayPauseClick() {
        player.playPause()
    }

    /**
     * Implementation of [MusicPlayerInterface.onPreviousClick].
     * Switches to the previous track if one exists.
     */
    override fun onPreviousClick() {
        if (selectedTrackIndex > 0) onTrackSelected(selectedTrackIndex - 1)
    }

    /**
     * Implementation of [MusicPlayerInterface.onNextClick].
     * Switches to the next track in the list if one exists.
     */
    override fun onNextClick() {
        if (selectedTrackIndex < tracks.size - 1) onTrackSelected(selectedTrackIndex + 1)
    }

    /**
     * Implementation of [MusicPlayerInterface.onTrackClick].
     * Selects the clicked track from the track list.
     *
     * @param track The track that was clicked.
     */
    override fun onTrackClick(track: TrackState) {
        onTrackSelected(tracks.indexOf(track))
    }

    /**
     * Implementation of [MusicPlayerInterface.onSeekBarPositionChanged].
     * Seeks to the specified position in the current track.
     *
     * @param position The position to seek to.
     */
    override fun onSeekBarPositionChanged(position: Long) {
        viewModelScope.launch { player.seekToPosition(position) }
    }

    /**
     * Cleans up the media player when the ViewModel is cleared.
     */
    override fun onCleared() {
        super.onCleared()
        player.releasePlayer()
    }
}
