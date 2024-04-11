package com.example.musicplayerapp.data

import android.net.Uri
import com.example.musicplayerapp.data.contentProvider.AudioContentProvider
import com.example.musicplayerapp.domain.models.AudioUrisListModel
import com.example.musicplayerapp.domain.models.TrackModel
import com.example.musicplayerapp.domain.repository.MusicPlayerRepository
import javax.inject.Inject

class MusicPlayerRepositoryImpl @Inject constructor(private val contentProvider: AudioContentProvider) :
    MusicPlayerRepository {
    override fun getTrackList(audioUrisList: AudioUrisListModel): List<TrackModel> {

//        val audioUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"
//
//        val track1 =
//            TrackModel.Builder()
//                .trackId(1)
//                .trackName("Audio 1")
//                .trackUrl(audioUrl)
//                .artistName("Mr. Android")
//                .build()
//
//        val track2 =
//            TrackModel.Builder()
//                .trackId(2)
//                .trackName("Audio 2")
//                .trackUrl(audioUrl)
//                .artistName("Ms. Android")
//                .build()
//
//        return listOf(track1, track2)

        return audioUrisList.urisList.map {uri ->
            val audioModel = contentProvider.getContent(uri)
            TrackModel(
                trackName = audioModel?.name ?: "No name",
                trackUri = audioModel?.uri.toString()
            )

        }
    }

}