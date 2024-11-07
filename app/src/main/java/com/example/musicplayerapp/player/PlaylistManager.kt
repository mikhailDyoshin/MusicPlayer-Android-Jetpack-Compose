package com.example.musicplayerapp.player

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import javax.inject.Inject

class PlaylistManager @Inject constructor(private val getTracksUseCase: GetTracksUseCase) {

    private val currentIndex: MutableState<Int?> = mutableStateOf(null)

    private val _tracksState: MutableState<List<TrackState>> = mutableStateOf(emptyList())
    val tracksState: State<List<TrackState>> = _tracksState

    fun addTracks(listOfURIs: List<Uri>) {

        val newTracks = getTracksFromURIs(listOfURIs)

        _tracksState.value = newTracks

    }

    fun updateIndex(newIndex: Int) {
        currentIndex.value = newIndex
        setTrackToSelectedState(newIndex)
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

}
