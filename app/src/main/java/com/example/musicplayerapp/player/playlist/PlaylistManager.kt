package com.example.musicplayerapp.player.playlist

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.usecases.GetAudioDurationFromSAFUseCase
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import com.example.musicplayerapp.player.controller.PlayerController
import com.example.musicplayerapp.player.state.TrackState
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class PlaylistManager @Inject constructor(
    private val getTracksUseCase: GetTracksUseCase,
    private val getAudioDurationFromSAFUseCase: GetAudioDurationFromSAFUseCase,
    private val playerController: PlayerController
) {
    private val playlist = Playlist()

    val playlistState: StateFlow<List<TrackState>> get() = playlist.playlistState

    fun addTracks(listOfURIs: List<Uri>) {
        val newTracks = getTracksFromURIs(listOfURIs)
        addTracksToPlayer(newTracks)
        addTracksToPlaylist(newTracks)

    }

    fun selectByIndex(index: Int) {
        playlist.selectByIndex(index)
    }

//    fun selectByTrack(track: TrackState): Int? {
//        return playlist.selectByTrack(track)
//    }

    private fun addTracksToPlayer(newTracks: List<TrackState>) {
        if (newTracks.isNotEmpty()) {
            playerController.addTracks(newTracks.toMediaItemList())
        }
    }

    private fun addTracksToPlaylist(newTracks: List<TrackState>) {
        playlist.add(*newTracks.toTypedArray())
    }

    private fun getTracksFromURIs(listOfURIs: List<Uri>): List<TrackState> {
        return getTracksUseCase(AudioUrisListModel(listOfURIs)).map {
            TrackState(
                trackName = it.trackName,
                trackUrl = it.trackUri,
                durationInMillis = getAudioDurationFromSAFUseCase(it.trackUri.toUri())
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
