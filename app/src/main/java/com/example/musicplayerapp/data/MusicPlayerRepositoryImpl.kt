package com.example.musicplayerapp.data

import android.media.RingtoneManager
import com.example.musicplayerapp.domain.models.TrackModel
import com.example.musicplayerapp.domain.repository.MusicPlayerRepository
import javax.inject.Inject

class MusicPlayerRepositoryImpl @Inject constructor() : MusicPlayerRepository {
    override fun getTrackList(): List<TrackModel> {
//        val alarmUrl = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()
        val ringtoneUrl = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE).toString()

        val alarmUrl = "https://storage.googleapis.com/exoplayer-test-media-0/play.mp3"

        val alarmTrack =
            TrackModel.Builder()
                .trackId(1)
                .trackName("Alarm")
                .trackUrl(alarmUrl)
                .artistName("Mr. Android")
                .build()

        val ringtoneTrack =
            TrackModel.Builder()
                .trackId(2)
                .trackName("Ringtone")
                .trackUrl(ringtoneUrl)
                .artistName("Mr. Android")
                .build()

        return listOf(alarmTrack, ringtoneTrack)
    }

}