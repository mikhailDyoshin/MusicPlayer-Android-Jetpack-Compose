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

@Composable
fun PlayerControlsBar(
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
            .padding(vertical = 5.dp),
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
        IconButton(onClick = { if (isPlaying) onPause() else onPlay() }) {
            if (isPlaying) {
                Icon(painter = painterResource(R.drawable.pause_icon), null)
            } else {

                Icon(painter = painterResource(R.drawable.play_icon), contentDescription = null)
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
        isPlaying = false,
        onPlay = { /*TODO*/ },
        onPause = { /*TODO*/ },
        onNext = { /*TODO*/ },
        onPrev = { /*TODO*/ }
    )
}

@Preview
@Composable
fun PlayerControlsBarPlayingPreview() {
    PlayerControlsBar(
        isPlaying = true,
        onPlay = { /*TODO*/ },
        onPause = { /*TODO*/ },
        onNext = { /*TODO*/ },
        onPrev = { /*TODO*/ }
    )
}