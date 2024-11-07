package com.example.musicplayerapp.player

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import javax.inject.Inject

class PlaylistManager @Inject constructor(private val getTracksUseCase: GetTracksUseCase) {

    private val _playlistState = mutableStateOf(PlaylistState())
    val playlistState: State<PlaylistState> = _playlistState

    fun addTracks(listOfURIs: List<Uri>) {

        val newTracks = getTracksFromURIs(listOfURIs)

        _playlistState.value = _playlistState.value.copy(tracks = newTracks)

    }

    fun updateIndex(newIndex: Int) {
        _playlistState.value = _playlistState.value.copy(currentIndex = newIndex)
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

}

data class PlaylistState(val currentIndex: Int? = null, val tracks: List<TrackState> = emptyList())