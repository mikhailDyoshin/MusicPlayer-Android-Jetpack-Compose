package com.example.musicplayerapp.player.playlist

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.example.musicplayerapp.player.state.TrackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Playlist {
    private val _playlistState: MutableStateFlow<List<TrackState>> = MutableStateFlow(emptyList())
    val playlistState: StateFlow<List<TrackState>> get() = _playlistState

    private val trackMap: LinkedHashMap<Int, TrackState> = LinkedHashMap()
    private var nextId = 0

    private val currentTrack: MutableState<TrackState?> = mutableStateOf(null)

    fun add(vararg tracksToAdd: TrackState) {
        prepareNewTracks(*tracksToAdd)
        updatePlaylist()
    }

    fun delete(vararg tracksToDelete: TrackState) {
        removeTracksByValues(*tracksToDelete)
        updateIndexesAfterDeletion()
        updatePlaylist()
    }

    fun selectByTrack(track: TrackState): Int? {
        resetPreviouslySelectedTrack()
        val newCurrentTrack = track.copy(isSelected = true)
        trackMap.entries.find { it.value == track }?.key.let { key ->
            if (key != null) {
                trackMap[key] = newCurrentTrack
                updateCurrentTrack(newCurrentTrack)
                updatePlaylist()
                return trackMap[key]?.index
            }
        }
        return null
    }

    fun selectByIndex(index: Int) {
        val selectedTrack = trackMap.entries.find { it.value.index ==  index}?.value
        if (selectedTrack != null) {
            selectByTrack(selectedTrack)
        }
    }

    private fun resetPreviouslySelectedTrack() {
        val currentTrack = currentTrack.value
        if (currentTrack != null) {
            trackMap.entries.find { it.value == currentTrack }?.key.let { key ->
                if (key != null) {
                    trackMap[key] = currentTrack.copy(isSelected = false)
                }
            }
        }
    }

    private fun updateCurrentTrack(track: TrackState) {
        currentTrack.value = track
    }

    /**
     * Removes tracks from the [trackMap] by comparing their values
     */
    private fun removeTracksByValues(vararg tracksToDelete: TrackState) {
        tracksToDelete.forEach { track ->
            trackMap.entries.find { it.value == track }?.key?.let { trackMap.remove(it) }
        }
    }

    /**
     * Prepares new tracks with updated indexes and serial numbers before adding them to the playlist.
     * Adds prepared tracks to the [trackMap]
     */
    private fun prepareNewTracks(vararg tracksToPrepare: TrackState) {
        val currentSizeOfPlaylist = trackMap.size

        tracksToPrepare.forEachIndexed { index, track ->
            val newTrack = track.copy(
                index = currentSizeOfPlaylist + index,
                serialNumber = currentSizeOfPlaylist + index + 1
            )
            trackMap[nextId++] = newTrack // Use an internally generated ID as the key
        }
    }

    /**
     * Recompute indexes and serial numbers for the remaining tracks
     */
    private fun updateIndexesAfterDeletion() {
        trackMap.entries.forEachIndexed { index, entry ->
            val updatedTrack = entry.value.copy(
                index = index,
                serialNumber = index + 1
            )
            trackMap[entry.key] = updatedTrack
        }
    }

    private fun updatePlaylist() {
        _playlistState.tryEmit(trackMap.values.toList())
    }
}