package com.example.musicplayerapp.player.state

data class TrackState(
    val trackId: Int = 0,
    val trackName: String = "",
    val trackUrl: String = "",
    val trackImage: Int = 0,
    val artistName: String = "",
    var isSelected: Boolean = false,
)
