package com.example.musicplayerapp.player.state

data class TrackState(
    val index: Int = 0,
    val serialNumber: Int = 1,
    val trackName: String = "",
    val trackUrl: String = "",
    val isSelected: Boolean = false,
    val duration: Long = 0L,
)
