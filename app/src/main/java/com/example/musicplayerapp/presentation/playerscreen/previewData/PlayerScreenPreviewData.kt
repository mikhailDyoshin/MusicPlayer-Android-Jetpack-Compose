package com.example.musicplayerapp.presentation.playerscreen.previewData

import com.example.musicplayerapp.presentation.playerscreen.state.SliderProgressState
import com.example.musicplayerapp.presentation.playerscreen.state.TrackUIState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object PlayerScreenPreviewData {

    val track = TrackUIState(trackName = "Test track", artistName = "Android", isSelected = false)

    val activeTrack =
        TrackUIState(trackName = "Test track", artistName = "Android", isSelected = true)

    private val longNameTrack = TrackUIState(
        trackName = "Track with enormously long and boring name that I created to test my UI",
        artistName = "Android",
        isSelected = false
    )

    val listOfTracks = listOf(
        TrackUIState(trackName = "Track 1", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 2", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 3", artistName = "Android", isSelected = true),
        TrackUIState(trackName = "Track 4", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 5", artistName = "Android", isSelected = false),
        longNameTrack,
        TrackUIState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 5", artistName = "Android", isSelected = false),
        TrackUIState(trackName = "Track 5", artistName = "Android", isSelected = false),
    )

    val sliderProgressStateFlow = createSliderProgressStateFlow()

    private fun createSliderProgressStateFlow(): StateFlow<SliderProgressState> {
        val currentPosition = 50000L
        val trackDuration = 500000L

        val mutableFlow =
            MutableStateFlow(SliderProgressState(currentPosition, trackDuration))
        val flow: StateFlow<SliderProgressState> = mutableFlow

        return flow
    }

}