package com.example.musicplayerapp.presentation.playerscreen

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.musicplayerapp.player.MusicPlayerInterface
import com.example.musicplayerapp.presentation.playerscreen.components.TrackList
import com.example.musicplayerapp.presentation.playerscreen.state.TrackState
import com.example.musicplayerapp.presentation.playerscreen.viewmodel.PlayerViewModel

@Composable
fun PlayerScreen() {

    val viewModel: PlayerViewModel = hiltViewModel()

    val trackList: List<TrackState> = viewModel.tracks

    TrackList(tracks = trackList)

}
