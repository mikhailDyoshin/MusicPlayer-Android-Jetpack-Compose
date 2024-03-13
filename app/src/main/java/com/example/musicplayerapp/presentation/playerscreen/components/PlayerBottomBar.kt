package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.musicplayerapp.presentation.playerscreen.state.PlaybackState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerBottomBar(
    // Slider
    playbackState: StateFlow<PlaybackState>,
    onSeekBarPositionChanging: () -> Unit,
    onSeekBarPositionChanged: (Long) -> Unit,

    // Controls
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,

    modifier: Modifier = Modifier

) {

    Column(modifier = modifier) {
        TrackProgressSlider(
            playbackState = playbackState,
            onSeekBarPositionChanging = { onSeekBarPositionChanging() },
            onSeekBarPositionChanged = { onSeekBarPositionChanged(it) }
        )

        PlayerControlsBar(
            isPlaying = isPlaying,
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

    val mutableFlow = MutableStateFlow(PlaybackState(false, currentPosition, trackDuration))
    val flow: StateFlow<PlaybackState> = mutableFlow

    PlayerBottomBar(
        playbackState = flow,
        onSeekBarPositionChanging = { /*TODO*/ },
        onSeekBarPositionChanged = {},
        isPlaying = true,
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

    val mutableFlow = MutableStateFlow(PlaybackState(false, currentPosition, trackDuration))
    val flow: StateFlow<PlaybackState> = mutableFlow

    PlayerBottomBar(
        playbackState = flow,
        onSeekBarPositionChanging = { /*TODO*/ },
        onSeekBarPositionChanged = {},
        isPlaying = false,
        onPlay = { /*TODO*/ },
        onPause = { /*TODO*/ },
        onNext = { /*TODO*/ },
        onPrev = { /*TODO*/ })
}