package com.example.musicplayerapp.presentation.playerscreen

import androidx.compose.runtime.Composable
import com.example.musicplayerapp.player.MusicPlayerInterface
import com.example.musicplayerapp.presentation.playerscreen.components.TrackList
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState

@Composable
fun PlayerScreen(tracks: List<TrackState>, playerInterface: MusicPlayerInterface) {

    TrackList(tracks = tracks, onTrackClick = {
        playerInterface.onTrackClick(it)
    })

}
