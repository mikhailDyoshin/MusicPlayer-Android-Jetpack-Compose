package com.example.musicplayerapp.domain.usecases

import com.example.musicplayerapp.domain.models.TrackModel
import javax.inject.Inject

class GetTracksUseCase @Inject constructor() {

    fun execute(): List<TrackModel> {
        return emptyList()
    }

}