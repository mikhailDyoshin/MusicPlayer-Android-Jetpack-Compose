package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import com.example.musicplayerapp.ui.theme.PurpleGrey40

/**
 * A composable function that displays a list item for a track.
 * The list item includes the track's image, name, and artist.
 * Also includes a click action for the track.
 *
 * @param track The track to be displayed.
 * @param onTrackClick The action to be performed when the track item is clicked.
 */
@Composable
fun TrackListItem(track: TrackState, onTrackClick: () -> Unit) {
    val bgColor = if (track.isSelected) PurpleGrey40 else Color.White
    val textColor = Color.Black
//        if (track.isSelected) Color.Gray else Color.Black
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 5.dp, horizontal = 5.dp)
            .clip(shape = RoundedCornerShape(15.dp))
            .background(color = bgColor)
            .clickable(onClick = { onTrackClick() })
    ) {
//        TrackImage(trackImage = track.trackImage, modifier = Modifier.size(size = 64.dp))
        Column(
            modifier = Modifier
                .padding(vertical = 5.dp, horizontal = 10.dp)
                .weight(weight = 1f)
        ) {

            Text(text = track.trackName, color = textColor)
            Text(text = track.artistName, color = textColor)
        }
//        if (track.state == STATE_PLAYING) LottieAudioWave()
    }
}

@Preview
@Composable
fun TrackListItemPreview() {

    TrackListItem(
        track = TrackState(
            trackName = "Test track",
            artistName = "Android",
            isSelected = false
        ), onTrackClick = {})

}

@Preview
@Composable
fun TrackListItemSelectedPreview() {

    TrackListItem(
        track = TrackState(
            trackName = "Test track",
            artistName = "Android",
            isSelected = true
        ), onTrackClick = {})

}