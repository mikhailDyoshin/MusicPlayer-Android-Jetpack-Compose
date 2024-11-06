package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.presentation.playerscreen.state.PlaybackState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderControlState
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
        initial = PlaybackState()
    ).value

    val positionAuto = playbackStateValue.currentPlaybackPosition.toFloat()

    var positionManual by rememberSaveable { mutableFloatStateOf(0f) }
    var timeManual by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Slider(
            value = when (playbackStateValue.sliderControlState) {
                SliderControlState.AUTO -> positionAuto
                SliderControlState.MANUAL -> positionManual
            },
            onValueChange = {
                onSeekBarPositionChanging()
                positionManual = it
                timeManual = it.toLong().formatTime()
            },
            onValueChangeFinished = {
                onSeekBarPositionChanged(positionManual.toLong())
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
                text = when (playbackStateValue.sliderControlState) {
                    SliderControlState.AUTO -> playbackStateValue.currentPlaybackPosition.formatTime()
                    SliderControlState.MANUAL -> timeManual
                },
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

    val mutableFlow =
        MutableStateFlow(PlaybackState(SliderControlState.AUTO, currentPosition, trackDuration))
    val flow: StateFlow<PlaybackState> = mutableFlow

    TrackProgressSlider(
        playbackState = flow,
        onSeekBarPositionChanging = {},
        onSeekBarPositionChanged = {},
        modifier = Modifier.background(color = Color.White)
    )
}
