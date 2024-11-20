package com.example.musicplayerapp.player.state

data class TrackState(
    val index: Int = 0,
    val serialNumber: Int = 1,
    val trackName: String = "",
    val trackUrl: String = "",
    var isSelected: Boolean = false,
)
