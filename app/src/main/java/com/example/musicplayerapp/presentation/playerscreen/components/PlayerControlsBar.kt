package com.example.musicplayerapp.presentation.playerscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.Transparent)
            .padding(top = 5.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.Center

    ) {
        // Previous button
        IconButton(
            onClick = {
                onPrev()
            },
            modifier = Modifier.padding(end = 20.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowLeft, null)
        }

        // Play/Pause button
        IconButton(onClick = { when (playerUIState) {
            PlayerUIState.PLAYING -> {
                onPause()
            }
            PlayerUIState.PAUSED -> onPlay()
            PlayerUIState.ERROR -> {
                // Do nothing
            }
        } }) {

            when (playerUIState) {
                PlayerUIState.PLAYING -> Icon(painter = painterResource(R.drawable.pause_icon), null)
                PlayerUIState.PAUSED -> Icon(painter = painterResource(R.drawable.play_icon), contentDescription = null)
                PlayerUIState.ERROR -> Icon(painter = painterResource(R.drawable.play_icon), contentDescription = null)
            }
        }

        // Next button
        IconButton(
            onClick = {
                onNext()
            },
            modifier = Modifier.padding(start = 20.dp)
        ) {
            Icon(Icons.Default.KeyboardArrowRight, null)
        }
    }

}


@Preview
@Composable
fun PlayerControlsBarOnPausePreview() {
    PlayerControlsBar(
        playerUIState = PlayerUIState.PAUSED,
        onPlay = { /*TODO*/ },
        onPause = { /*TODO*/ },
        onNext = { /*TODO*/ },
        onPrev = { /*TODO*/ },
        modifier = Modifier.background(color = Color.White)
    )
}

@Preview
@Composable
fun PlayerControlsBarPlayingPreview() {
    PlayerControlsBar(
        playerUIState = PlayerUIState.PLAYING,
        onPlay = { /*TODO*/ },
        onPause = { /*TODO*/ },
        onNext = { /*TODO*/ },
        onPrev = { /*TODO*/ },
        modifier = Modifier.background(color = Color.White)
    )
}