package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.musicplayerapp.R
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerUIState

@Composable
fun PlayerControlsBar(
    playerUIState: PlayerUIState,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier
) {

    val playButtonDrawable = when (playerUIState) {
        PlayerUIState.PLAYING -> R.drawable.pause_icon

        PlayerUIState.PAUSED -> R.drawable.play_icon

        PlayerUIState.ERROR -> R.drawable.play_icon
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .padding(top = 5.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly

    ) {
        // Previous button
        IconButton(
            onClick = { onPrev() },
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = null,
                tint = Color.Black
            )
        }

        // Play/Pause button
        IconButton(onClick = {
            when (playerUIState) {
                PlayerUIState.PLAYING -> {
                    onPause()
                }

                PlayerUIState.PAUSED -> onPlay()
                PlayerUIState.ERROR -> {
                    // Do nothing
                }
            }
        }, modifier = Modifier.size(50.dp)) {
            Icon(
                painter = painterResource(playButtonDrawable),
                null,
                tint = Color.Black
            )
        }

        // Next button
        IconButton(
            onClick = { onNext() },
            modifier = Modifier.size(50.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_right),
                contentDescription = null,
                tint = Color.Black
            )
        }
    }

}


@Preview
@Composable
fun PlayerControlsBarOnPausePreview() {
    PlayerControlsBar(
        playerUIState = PlayerUIState.PAUSED,
        onPlay = {},
        onPause = {},
        onNext = {},
        onPrev = {},
        modifier = Modifier.background(color = Color.White)
    )
}

@Preview
@Composable
fun PlayerControlsBarPlayingPreview() {
    PlayerControlsBar(
        playerUIState = PlayerUIState.PLAYING,
        onPlay = {},
        onPause = {},
        onNext = {},
        onPrev = {},
        modifier = Modifier.background(color = Color.White)
    )
}