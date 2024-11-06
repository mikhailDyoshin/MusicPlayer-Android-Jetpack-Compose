package com.example.musicplayerapp.presentation.playerscreen.state

data class PlayerBarState(
    val playerState: PlayerUIState = PlayerUIState.PAUSED,
    val sliderControlState: SliderControlState = SliderControlState.AUTO,
    val barVisibility: PlayerBarVisibility = PlayerBarVisibility.INVISIBLE
)
