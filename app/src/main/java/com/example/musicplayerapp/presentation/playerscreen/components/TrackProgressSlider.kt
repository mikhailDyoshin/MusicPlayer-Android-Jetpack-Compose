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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.presentation.playerscreen.state.SliderControlState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderProgressState
import com.example.musicplayerapp.utils.millisToMinutesSeconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

@Composable
fun TrackProgressSlider(
    playbackState: StateFlow<SliderProgressState>,
    sliderControlState: SliderControlState,
    onSeekBarPositionChanging: () -> Unit,
    onSeekBarPositionChanged: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val playbackStateValue = playbackState.collectAsState(
        initial = SliderProgressState()
    ).value

    val positionAuto = playbackStateValue.currentPlaybackPosition.toFloat()

    val manualSliderProgressState = remember {
        mutableStateOf(ManualSliderProgressState())
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Slider(
            value = when (sliderControlState) {
                SliderControlState.AUTO -> positionAuto
                SliderControlState.MANUAL -> manualSliderProgressState.value.position
            },
            onValueChange = {
                onSeekBarPositionChanging()
                manualSliderProgressState.value =
                    ManualSliderProgressState(
                        position = it,
                        time = it.toLong().millisToMinutesSeconds()
                    )
            },
            onValueChangeFinished = {
                onSeekBarPositionChanged(manualSliderProgressState.value.position.toLong())
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
                text = when (sliderControlState) {
                    SliderControlState.AUTO -> playbackStateValue.currentPlaybackPosition.millisToMinutesSeconds()
                    SliderControlState.MANUAL -> manualSliderProgressState.value.time
                },
            )
            Text(
                text = playbackStateValue.currentTrackDuration.millisToMinutesSeconds(),
            )
        }
    }


}


data class ManualSliderProgressState(
    val position: Float = 0f,
    val time: String = 0L.millisToMinutesSeconds()
)

@Preview
@Composable
fun TrackProgressSliderPreview() {

    val currentPosition = 50000L
    val trackDuration = 500000L

    val mutableFlow =
        MutableStateFlow(SliderProgressState(currentPosition, trackDuration))
    val flow: StateFlow<SliderProgressState> = mutableFlow

    TrackProgressSlider(
        playbackState = flow,
        sliderControlState = SliderControlState.AUTO,
        onSeekBarPositionChanging = {},
        onSeekBarPositionChanged = {},
        modifier = Modifier.background(color = Color.White)
    )
}
