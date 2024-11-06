package com.example.musicplayerapp.presentation.playerscreen.state

/**
 * Data class that represents the current playback state of a media item.
 *
 * @property currentPlaybackPosition Playback position of a currently playing track, in ms.
 * @property currentTrackDuration Duration of playing track, in ms.
 */
data class SliderProgressState(
    val currentPlaybackPosition: Long = 0L,
    val currentTrackDuration: Long = 0L,
)
