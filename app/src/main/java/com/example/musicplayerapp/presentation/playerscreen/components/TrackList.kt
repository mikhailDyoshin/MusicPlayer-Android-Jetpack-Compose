package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.player.PlaylistState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState

@Composable
fun TrackList(
    playlistState: PlaylistState,
    onTrackClick: (track: TrackState) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        items(playlistState.tracks) { track ->
            if (playlistState.tracks.indexOf(track) == playlistState.currentIndex) {
                TrackListItem(
                    track = track.copy(isSelected = true),
                    onTrackClick = { onTrackClick(track) })
            } else {
                TrackListItem(track = track, onTrackClick = { onTrackClick(track) })
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun TrackListPreview() {

    val currentIndex = 2

    val trackList = listOf(
        TrackState(trackName = "Track 1", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 2", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 3", artistName = "Android", isSelected = true),
        TrackState(trackName = "Track 4", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
    )

    val playlistState = PlaylistState(currentIndex = currentIndex, tracks = trackList)

    TrackList(playlistState = playlistState, onTrackClick = {})
}
