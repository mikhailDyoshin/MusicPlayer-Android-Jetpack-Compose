package com.example.musicplayerapp.presentation.playerscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.presentation.playerscreen.components.AddButton
import com.example.musicplayerapp.presentation.playerscreen.components.PlayerBottomBar
import com.example.musicplayerapp.presentation.playerscreen.components.TrackList
import com.example.musicplayerapp.presentation.playerscreen.previewData.PlayerScreenPreviewData
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerBarState
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerBarVisibility
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerUIState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderControlState
import com.example.musicplayerapp.presentation.playerscreen.state.SliderProgressState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackUIState
import com.example.musicplayerapp.ui.theme.PurpleGrey80
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerScreen(
    playlistState: List<TrackUIState>,
    playerBarState: PlayerBarState,
    sliderControlState: SliderControlState,
    sliderProgressState: StateFlow<SliderProgressState>,
    onTrackClick: (track: TrackUIState) -> Unit,
    onSeekBarPositionChanged: (currentProgress: Long) -> Unit,
    onSeekBarPositionChanging: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    launchActivity: (launcherType: LauncherTypeForActivityResult) -> Unit,
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = PurpleGrey80)
    ) {
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            TrackList(tracks = playlistState, onTrackClick = {
                onTrackClick(it)
            })
            AddButton(
                onClick = { launchActivity(LauncherTypeForActivityResult.AUDIO) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            )
        }

        PlayerBottomBar(
            sliderControlState = sliderControlState,
            playbackState = sliderProgressState,
            onSeekBarPositionChanging = { onSeekBarPositionChanging() },
            onSeekBarPositionChanged = { onSeekBarPositionChanged(it) },
            playerBarState = playerBarState,
            onPlay = { onPlay() },
            onPause = { onPause() },
            onNext = { onNext() },
            onPrev = { onPrev() },
        )
    }
}

enum class LauncherTypeForActivityResult(val string: String) {
    AUDIO(string = "audio/*")
}

@Preview(showSystemUi = true)
@Composable
fun PlayerScreenPreview() {

    PlayerScreen(
        playlistState = PlayerScreenPreviewData.listOfTracks,
        playerBarState = PlayerBarState(),
        sliderControlState = SliderControlState.AUTO,
        sliderProgressState = PlayerScreenPreviewData.sliderProgressStateFlow,
        onTrackClick = {},
        onSeekBarPositionChanged = {},
        onSeekBarPositionChanging = {},
        onPlay = {},
        onPause = {},
        onNext = {},
        onPrev = {},
        launchActivity = {}
    )
}

@Preview(showSystemUi = true)
@Composable
fun PlayerScreenPlayingPreview() {

    PlayerScreen(
        playlistState = PlayerScreenPreviewData.listOfTracks,
        playerBarState = PlayerBarState(
            playerState = PlayerUIState.PLAYING,
            barVisibility = PlayerBarVisibility.VISIBLE
        ),
        sliderControlState = SliderControlState.AUTO,
        sliderProgressState = PlayerScreenPreviewData.sliderProgressStateFlow,
        onTrackClick = {},
        onSeekBarPositionChanged = {},
        onSeekBarPositionChanging = {},
        onPlay = {},
        onPause = {},
        onNext = {},
        onPrev = {},
        launchActivity = {}
    )
}