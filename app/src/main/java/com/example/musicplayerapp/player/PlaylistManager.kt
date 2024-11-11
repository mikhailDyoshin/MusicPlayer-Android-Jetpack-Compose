package com.example.musicplayerapp.player

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import com.example.musicplayerapp.player.controller.PlayerController
import com.example.musicplayerapp.player.state.TrackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PlaylistManager @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val playerController: PlayerController
) {

    private val currentIndex: MutableState<Int?> = mutableStateOf(null)

    private val _tracksState: MutableStateFlow<List<TrackState>> = MutableStateFlow(emptyList())
    val tracksState: StateFlow<List<TrackState>> get() = _tracksState

    fun addTracks(listOfURIs: List<Uri>) {
        val newTracks = getTracksFromURIs(listOfURIs)
        addTracksToPlayer(newTracks)
        addTracksToPlaylist(newTracks)

    }

    fun updateIndex(newIndex: Int) {
        currentIndex.value = newIndex
        setTrackToSelectedState(newIndex)
    }

    fun setActiveTrack(track: TrackState): Int {
        val currentPlaylist = tracksState.value
        val selectedTrackIndex = currentPlaylist.indexOf(track)
        updateIndex(selectedTrackIndex)
        return selectedTrackIndex
    }

    private fun addTracksToPlayer(newTracks: List<TrackState>) {
        if (newTracks.isNotEmpty()) {
            playerController.addTracks(newTracks.toMediaItemList())
        }
    }

    private fun addTracksToPlaylist(newTracks: List<TrackState>) {
        val newPlaylist = _tracksState.value.toMutableList()
        newPlaylist.addAll(newTracks)

        _tracksState.value = newPlaylist.toList()
    }

    private fun getTracksFromURIs(listOfURIs: List<Uri>): List<TrackState> {
        return getTracksUseCase.execute(AudioUrisListModel(listOfURIs)).map {
            TrackState(
                trackName = it.trackName,
                trackUrl = it.trackUri,
            )
        }

    }

    private fun setTrackToSelectedState(index: Int) {
        _tracksState.value = _tracksState.value.mapIndexed { trackIndex, track ->
            track.copy(
                isSelected = trackIndex == index
            )
        }
    }

    /**
     * Converts a list of [TrackState] objects into a list of [MediaItem] objects.
     *
     * @return A list of [MediaItem] objects.
     */
    private fun List<TrackState>.toMediaItemList(): List<MediaItem> {
        return this.map { MediaItem.fromUri(it.trackUrl) }.toMutableList()
    }

}
