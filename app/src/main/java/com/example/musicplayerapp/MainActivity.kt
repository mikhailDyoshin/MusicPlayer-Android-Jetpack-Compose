package com.example.musicplayerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.musicplayerapp.presentation.playerscreen.PlayerScreen
import com.example.musicplayerapp.presentation.playerscreen.viewmodel.PlayerViewModel
import com.example.musicplayerapp.ui.theme.MusicPlayerAppTheme
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import com.example.musicplayerapp.presentation.playerscreen.state.PlayerBarState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusicPlayerAppTheme {

                val selectAudioLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetMultipleContents()
                ) { uriList ->
                    viewModel.addTracks(uriList)
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    PlayerScreen(
                        playlistState = viewModel.playlistState.collectAsState(initial = emptyList()).value,
                        sliderProgressState = viewModel.sliderProgressState,
                        playerBarState = viewModel.playerBarState.collectAsState(initial = PlayerBarState()).value,
                        onTrackClick = { viewModel.onTrackClick(it) },
                        onSeekBarPositionChanged = { currentProgress ->
                            viewModel.setSliderToAutoState()
                            viewModel.onSeekBarPositionChanged(
                                currentProgress
                            )
                        },
                        onSeekBarPositionChanging = {
                            viewModel.setSliderToManualState()
                        },
                        onPlay = { viewModel.onPlayClick() },
                        onPause = { viewModel.onPauseClick() },
                        onNext = { viewModel.onNextClick() },
                        onPrev = { viewModel.onPreviousClick() },
                        launchActivity = { selectAudioLauncher.launch(it) }
                    )
                }
            }
        }
    }
}
