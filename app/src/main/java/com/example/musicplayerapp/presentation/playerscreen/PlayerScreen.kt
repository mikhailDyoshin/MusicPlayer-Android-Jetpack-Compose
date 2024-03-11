package com.example.musicplayerapp.presentation.playerscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import com.example.musicplayerapp.player.MusicPlayerInterface
import com.example.musicplayerapp.presentation.playerscreen.components.TrackList
import com.example.musicplayerapp.presentation.playerscreen.components.TrackProgressSlider
import com.example.musicplayerapp.presentation.playerscreen.state.PlaybackState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerScreen(
    tracks: List<TrackState>,
    playbackState: StateFlow<PlaybackState>,
    playerInterface: MusicPlayerInterface,
    onSeekBarPositionChanged: (currentProgress: Long) -> Unit,
    onSeekBarPositionChanging: () -> Unit,
) {

    Column {
        TrackList(tracks = tracks, onTrackClick = {
            playerInterface.onTrackClick(it)
        })
        TrackProgressSlider(
            playbackState = playbackState,
            onSeekBarPositionChanged = { currentProgress -> onSeekBarPositionChanged(currentProgress) },
            onSeekBarPositionChanging = { onSeekBarPositionChanging() }
            )
    }

}
