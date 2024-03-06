package com.example.musicplayerapp.presentation.playerscreen.state

import com.example.musicplayerapp.player.PlayerState

data class TrackState(
    val trackId: Int = 0,
    val trackName: String = "",
    val trackUrl: String = "",
    val trackImage: Int = 0,
    val artistName: String = "",
    var isSelected: Boolean = false,
    var state: PlayerState = PlayerState.STATE_IDLE
)
