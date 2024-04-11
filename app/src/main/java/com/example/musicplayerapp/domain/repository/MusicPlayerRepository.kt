package com.example.musicplayerapp.domain.repository

import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.models.TrackModel

interface MusicPlayerRepository {

    /**
     * Retrieves a list of all tracks.
     *
     * @return a list of [TrackModel] objects.
     */
    fun getTrackList(audioUrisList: AudioUrisListModel): List<TrackModel>

}