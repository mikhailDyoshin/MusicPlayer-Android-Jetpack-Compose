package com.example.musicplayerapp.data

import android.util.Log
import com.example.musicplayerapp.data.contentProvider.AudioContentProvider
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.models.TrackModel
import com.example.musicplayerapp.domain.repository.MusicPlayerRepository
import javax.inject.Inject

class MusicPlayerRepositoryImpl @Inject constructor(private val contentProvider: AudioContentProvider) :
    MusicPlayerRepository {
    override fun getTrackList(audioUrisList: AudioUrisListModel): List<TrackModel> {

        return audioUrisList.urisList.mapNotNull { uri ->
            contentProvider.getContent(uri)?.let { audioModel ->
                Log.d(MUSIC_PLAYER_REPOSITORY_TAG, audioModel.toString())
                TrackModel(
                    trackName = audioModel.name,
                    trackUri = audioModel.uri.toString()
                )
            }
        }
    }

    companion object {
        private const val MUSIC_PLAYER_REPOSITORY_TAG = "MusicPlayerRepositoryImpl"
    }

}