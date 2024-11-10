package com.example.musicplayerapp.player

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.MediaItem
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import com.example.musicplayerapp.player.controller.PlayerController
import com.example.musicplayerapp.player.state.TrackState
import javax.inject.Inject

class PlaylistManager @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val playerController: PlayerController
) {

    private val currentIndex: MutableState<Int?> = mutableStateOf(null)

    private val _tracksState: MutableState<List<TrackState>> = mutableStateOf(emptyList())
    val tracksState: State<List<TrackState>> = _tracksState

    fun addTracks(listOfURIs: List<Uri>) {
        val newTracks = getTracksFromURIs(listOfURIs)
        addTracksToPlayer(newTracks)
        addTracksToPlaylist(newTracks)

    }

    fun updateIndex(newIndex: Int) {
        currentIndex.value = newIndex
        setTrackToSelectedState(newIndex)
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
                trackId = it.trackId,
                trackName = it.trackName,
                trackUrl = it.trackUri,
                trackImage = it.trackImage,
                artistName = it.artistName,
                isSelected = it.isSelected,
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
