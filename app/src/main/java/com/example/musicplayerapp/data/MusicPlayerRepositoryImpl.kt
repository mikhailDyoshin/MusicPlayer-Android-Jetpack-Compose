package com.example.musicplayerapp.data

import com.example.musicplayerapp.data.contentProvider.AudioContentProvider
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.models.TrackModel
import com.example.musicplayerapp.domain.repository.MusicPlayerRepository
import javax.inject.Inject

class MusicPlayerRepositoryImpl @Inject constructor(private val contentProvider: AudioContentProvider) :
    MusicPlayerRepository {
    override fun getTrackList(audioUrisList: AudioUrisListModel): List<TrackModel> {

        return audioUrisList.urisList.map {uri ->
            val audioModel = contentProvider.getContent(uri)
            TrackModel(
                trackName = audioModel?.name ?: "No name",
                trackUri = audioModel?.uri.toString()
            )

        }
    }

}