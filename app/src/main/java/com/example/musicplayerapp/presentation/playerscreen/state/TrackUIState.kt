package com.example.musicplayerapp.presentation.playerscreen.state

data class TrackUIState(
    val trackId: Int = 0,
    val trackName: String = "",
    val trackUrl: String = "",
    val trackImage: Int = 0,
    val artistName: String = "",
    var isSelected: Boolean = false,
)
