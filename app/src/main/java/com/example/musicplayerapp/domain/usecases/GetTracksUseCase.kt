package com.example.musicplayerapp.domain.usecases

import com.example.musicplayerapp.domain.models.TrackModel
import com.example.musicplayerapp.domain.repository.MusicPlayerRepository
import javax.inject.Inject

class GetTracksUseCase @Inject constructor(private val repository: MusicPlayerRepository) {

    fun execute(): List<TrackModel> {
        return repository.getTrackList()
    }

}