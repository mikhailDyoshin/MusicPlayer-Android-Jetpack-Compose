package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerBarState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderProgressState
import com.example.musicplayerapp.ui.theme.ControlsBarBackground
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerBottomBar(
    // Slider
    playbackState: StateFlow<SliderProgressState>,
    onSeekBarPositionChanging: () -> Unit,
    onSeekBarPositionChanged: (Long) -> Unit,

    // Controls
    playerBarState: PlayerBarState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,

    modifier: Modifier = Modifier

) {

    Column(modifier = modifier.background(color = ControlsBarBackground)) {
        TrackProgressSlider(
            playbackState = playbackState,
            sliderControlState = playerBarState.sliderControlState,
            onSeekBarPositionChanging = { onSeekBarPositionChanging() },
            onSeekBarPositionChanged = { onSeekBarPositionChanged(it) }
        )

        PlayerControlsBar(
            playerUIState = playerBarState.playerState,
            onPlay = { onPlay() },
            onPause = { onPause() },
            onNext = { onNext() },
            onPrev = { onPrev() })
    }

}

@Preview
@Composable
fun PlayerBottomBarPlayingPreview() {
    val currentPosition = 50000L
    val trackDuration = 500000L

    val mutableFlow = MutableStateFlow(SliderProgressState(currentPosition, trackDuration))
    val flow: StateFlow<SliderProgressState> = mutableFlow

    PlayerBottomBar(
        playbackState = flow,
        onSeekBarPositionChanging = { /*TODO*/ },
        onSeekBarPositionChanged = {},
        playerBarState = PlayerBarState(),
        onPlay = { /*TODO*/ },
        onPause = { /*TODO*/ },
        onNext = { /*TODO*/ },
        onPrev = { /*TODO*/ })
}

@Preview
@Composable
fun PlayerBottomBarOnPausePreview() {
    val currentPosition = 0L
    val trackDuration = 500000L

    val mutableFlow = MutableStateFlow(SliderProgressState(currentPosition, trackDuration))
    val flow: StateFlow<SliderProgressState> = mutableFlow

    PlayerBottomBar(
        playbackState = flow,
        onSeekBarPositionChanging = { /*TODO*/ },
        onSeekBarPositionChanged = {},
        playerBarState = PlayerBarState(),
        onPlay = { /*TODO*/ },
        onPause = { /*TODO*/ },
        onNext = { /*TODO*/ },
        onPrev = { /*TODO*/ })
}