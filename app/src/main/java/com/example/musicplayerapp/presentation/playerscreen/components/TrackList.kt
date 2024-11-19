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
import com.example.musicplayerapp.presentation.playerscreen.previewData.PlayerScreenPreviewData
import com.example.musicplayerapp.presentation.playerscreen.state.TrackUIState

@Composable
fun TrackList(
    tracks: List<TrackUIState>,
    onTrackClick: (track: TrackUIState) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        items(tracks) { track ->
                TrackListItem(track = track, onTrackClick = { onTrackClick(track) })
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun TrackListPreview() {
    TrackList(tracks = PlayerScreenPreviewData.listOfTracks, onTrackClick = {})
}
