package com.example.musicplayerapp.data

import android.content.Context
import android.media.RingtoneManager
import com.example.musicplayerapp.domain.models.TrackModel
import com.example.musicplayerapp.domain.repository.MusicPlayerRepository
import javax.inject.Inject

class MusicPlayerRepositoryImpl @Inject constructor(private val context: Context) : MusicPlayerRepository {
    override fun getTrackList(): List<TrackModel> {

        val audioUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"

        val track1 =
            TrackModel.Builder()
                .trackId(1)
                .trackName("Audio 1")
                .trackUrl(audioUrl)
                .artistName("Mr. Android")
                .build()

        val track2 =
            TrackModel.Builder()
                .trackId(2)
                .trackName("Audio 2")
                .trackUrl(audioUrl)
                .artistName("Ms. Android")
                .build()

        return listOf(track1, track2)
    }

}