package com.example.musicplayerapp

import android.os.Bundle
import android.util.Log
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.Player
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

const val PLAYBACK_STATE_TAG = "MyPlaybackState"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: PlayerViewModel by viewModels()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.playbackStateFlow.collect { state ->
                    when(state) {
                        Player.STATE_IDLE -> { Log.d(PLAYBACK_STATE_TAG, "IDLE") }
                        Player.STATE_BUFFERING -> { Log.d(PLAYBACK_STATE_TAG, "BUFFERING") }
                        Player.STATE_READY -> { Log.d(PLAYBACK_STATE_TAG, "READY") }
                        Player.STATE_ENDED -> { Log.d(PLAYBACK_STATE_TAG, "ENDED") }
                        else -> {
                             Log.d(PLAYBACK_STATE_TAG, "Null state")
                        }
                    }
                }
            }
        }


        setContent {
            MusicPlayerAppTheme {

                val selectAudioLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetMultipleContents()
                ) { uriList ->
                    viewModel.getTracks(uriList)
                }

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    PlayerScreen(
                        tracks = viewModel.tracks,
                        playbackState = viewModel.playbackState,
                        onTrackClick = { viewModel.onTrackClick(it) },
                        isBottomBarDisplayed = viewModel.isBottomBarDisplayed.value,
                        isPlaying = viewModel.isTrackPlaying.value,
                        onSeekBarPositionChanged = { currentProgress ->
                            viewModel.pullSliderFromChangingState()
                            viewModel.onSeekBarPositionChanged(
                                currentProgress
                            )
                        },
                        onSeekBarPositionChanging = {
                            viewModel.putSliderInChangingState()
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
