package com.example.musicplayerapp.player.state

data class TrackState(
    val trackName: String = "",
    val trackUrl: String = "",
    var isSelected: Boolean = false,
)
