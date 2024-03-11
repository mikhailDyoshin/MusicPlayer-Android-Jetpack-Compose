package com.example.musicplayerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.musicplayerapp.presentation.playerscreen.PlayerScreen
import com.example.musicplayerapp.presentation.playerscreen.viewmodel.PlayerViewModel
import com.example.musicplayerapp.ui.theme.MusicPlayerAppTheme
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MusicPlayerAppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    PlayerScreen(
                        tracks = viewModel.tracks,
                        playbackState = viewModel.playbackState,
                        playerInterface = viewModel,
                        onSeekBarPositionChanged = { currentProgress ->
                            viewModel.pullSliderFromChangingState()
                            viewModel.onSeekBarPositionChanged(
                                currentProgress
                            )
                        },
                        onSeekBarPositionChanging = {
                            viewModel.putSliderInChangingState()
                        }
                        )
                }
            }
        }
    }
}
