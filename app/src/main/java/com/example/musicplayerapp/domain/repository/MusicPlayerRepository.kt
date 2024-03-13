package com.example.musicplayerapp.domain.repository

import com.example.musicplayerapp.domain.models.TrackModel

interface MusicPlayerRepository {

    /**
     * Retrieves a list of all tracks.
     *
     * @return a list of [TrackModel] objects.
     */
    fun getTrackList(): List<TrackModel>

}