package com.example.musicplayerapp.domain.usecases

import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.models.TrackModel
import com.example.musicplayerapp.domain.repository.MusicPlayerRepository
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(private val repository: MusicPlayerRepository) {

    fun execute(urisList: AudioUrisListModel): List<TrackModel> {
        return repository.getTrackList(urisList)
    }

}