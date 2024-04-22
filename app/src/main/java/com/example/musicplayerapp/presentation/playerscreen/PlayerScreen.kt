package com.example.musicplayerapp.presentation.playerscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.musicplayerapp.presentation.playerscreen.components.PlayerBottomBar
import com.example.musicplayerapp.presentation.playerscreen.components.TrackList
import com.example.musicplayerapp.presentation.playerscreen.state.PlaybackState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import com.example.musicplayerapp.ui.theme.PurpleGrey80
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerScreen(
    tracks: List<TrackState>,
    isBottomBarDisplayed: Boolean,
    isPlaying: Boolean,
    playbackState: StateFlow<PlaybackState>,
    onTrackClick: (track: TrackState) -> Unit,
    onSeekBarPositionChanged: (currentProgress: Long) -> Unit,
    onSeekBarPositionChanging: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    launchActivity: (input: String) -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = PurpleGrey80)
    ) {
        TrackList(tracks = tracks, onTrackClick = {
            onTrackClick(it)
        })
        FloatingActionButton(
            onClick = { launchActivity("audio/*") },
            modifier = Modifier
                .wrapContentSize()
                .zIndex(2f)
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = if (isBottomBarDisplayed) 170.dp else 20.dp)
        ) {
            Text("+")
        }
        if (isBottomBarDisplayed) {
            PlayerBottomBar(
                playbackState = playbackState,
                onSeekBarPositionChanging = { onSeekBarPositionChanging() },
                onSeekBarPositionChanged = { onSeekBarPositionChanged(it) },
                isPlaying = isPlaying,
                onPlay = { onPlay() },
                onPause = { onPause() },
                onNext = { onNext() },
                onPrev = { onPrev() },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}


@Preview(showSystemUi = true)
@Composable
fun PlayerScreenPreview() {
    val trackList = listOf(
        TrackState(trackName = "Track 1", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 2", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 3", artistName = "Android", isSelected = true),
        TrackState(trackName = "Track 4", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),

        )

    val currentPosition = 50000L
    val trackDuration = 500000L

    val mutableFlow = MutableStateFlow(PlaybackState(false, currentPosition, trackDuration))
    val flow: StateFlow<PlaybackState> = mutableFlow

    PlayerScreen(
        tracks = trackList,
        isBottomBarDisplayed = false,
        isPlaying = false,
        playbackState = flow,
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
    val trackList = listOf(
        TrackState(trackName = "Track 1", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 2", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 3", artistName = "Android", isSelected = true),
        TrackState(trackName = "Track 4", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackState(trackName = "Track 5", artistName = "Android", isSelected = false),

        )

    val currentPosition = 50000L
    val trackDuration = 500000L

    val mutableFlow = MutableStateFlow(PlaybackState(false, currentPosition, trackDuration))
    val flow: StateFlow<PlaybackState> = mutableFlow

    PlayerScreen(
        tracks = trackList,
        isBottomBarDisplayed = true,
        isPlaying = true,
        playbackState = flow,
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