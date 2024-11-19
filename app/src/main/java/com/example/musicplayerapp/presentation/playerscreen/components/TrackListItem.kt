package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.musicplayerapp.presentation.playerscreen.previewData.PlayerScreenPreviewData
import com.example.musicplayerapp.presentation.playerscreen.state.TrackUIState
import com.example.musicplayerapp.ui.theme.PurpleGrey40

/**
 * A composable function that displays a list-item for a track.
 * The list item includes the track's name and artist.
 * Also includes a click action for the track.
 *
 * @param track The track to be displayed.
 * @param onTrackClick The action to be performed when the track item is clicked.
 */
@Composable
fun TrackListItem(track: TrackUIState, onTrackClick: () -> Unit) {
    val bgColor = if (track.isSelected) PurpleGrey40 else Color.White
    val textColor = Color.Black

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color = bgColor)
            .clickable(onClick = { onTrackClick() })
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 20.dp)
                .weight(weight = 1f),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = track.trackName,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp
            )
        }
    }
}

@Preview
@Composable
fun TrackListItemPreview() {

    TrackListItem(
        track = PlayerScreenPreviewData.track,
        onTrackClick = {}
    )

}

@Preview
@Composable
fun TrackListItemSelectedPreview() {

    TrackListItem(
        track = PlayerScreenPreviewData.activeTrack,
        onTrackClick = {}
    )

}