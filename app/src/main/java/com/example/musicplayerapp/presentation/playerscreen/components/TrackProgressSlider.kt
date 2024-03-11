package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.presentation.playerscreen.state.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun TrackProgressSlider(
    playbackState: StateFlow<PlaybackState>,
    onSeekBarPositionChanging: () -> Unit,
    onSeekBarPositionChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val playbackStateValue = playbackState.collectAsState(
        initial = PlaybackState(false, 0L, 0L)
    ).value

    val inChangingState = playbackStateValue.isInChangingState

    val currentMediaProgress = playbackStateValue.currentPlaybackPosition.toFloat()
    var currentPosTemp by rememberSaveable { mutableFloatStateOf(0f) }

    Column(
        modifier = modifier.background(
            color = Color.White,
            shape = RoundedCornerShape(15.dp)
        ), verticalArrangement = Arrangement.Center
    ) {
        Slider(
            value = if (inChangingState) currentPosTemp else currentMediaProgress,
            onValueChange = {
                onSeekBarPositionChanging()
                currentPosTemp = it
            },
            onValueChangeFinished = {
                onSeekBarPositionChanged(currentPosTemp.toLong())
            },
            valueRange = 0f..playbackStateValue.currentTrackDuration.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = playbackStateValue.currentPlaybackPosition.formatTime(),
            )
            Text(
                text = playbackStateValue.currentTrackDuration.formatTime(),
            )
        }
    }


}

/**
 * Formats a long duration value (in milliseconds) into a time string in the format "MM:SS".
 *
 * @return The formatted time string.
 */
private fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}

@Preview
@Composable
fun TrackProgressSliderPreview() {

    val currentPosition = 50000L
    val trackDuration = 500000L

    val mutableFlow = MutableStateFlow(PlaybackState(false, currentPosition, trackDuration))
    val flow: StateFlow<PlaybackState> = mutableFlow

    TrackProgressSlider(
        playbackState = flow,
        onSeekBarPositionChanging = {},
        onSeekBarPositionChanged = {},
//        modifier = Modifier.background(color = Color.White)
    )
}
