package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState

@Composable
fun TrackList(tracks: List<TrackState>, onTrackClick: (track: TrackState) -> Unit, modifier: Modifier = Modifier) {

    Column(
        modifier = modifier.fillMaxWidth().padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        tracks.forEach {
            TrackListItem(track = it, onTrackClick = { onTrackClick(it) })
        }
    }

}

@Preview(showSystemUi = true)
@Composable
fun TrackListPreview() {

    val trackList = listOf(
        TrackState(trackName = "Track 1", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 2", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 3", artistName = "Android", isSelected = true),
        TrackState(trackName = "Track 4", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
    )

    TrackList(tracks = trackList, onTrackClick = {})
}
