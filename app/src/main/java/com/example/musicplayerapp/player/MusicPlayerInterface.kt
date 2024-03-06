package com.example.musicplayerapp.player

import com.example.musicplayerapp.presentation.playerscreen.state.TrackState

interface MusicPlayerInterface {

    /**
     * Invoked when the play or pause button is clicked.
     */
    fun onPlayPauseClick()

    /**
     * Invoked when the previous button is clicked.
     */
    fun onPreviousClick()

    /**
     * Invoked when the next button is clicked.
     */
    fun onNextClick()

    /**
     * Invoked when a track is clicked. The clicked [TrackState] is provided as a parameter.
     *
     * @param track The track that was clicked.
     */
    fun onTrackClick(track: TrackState)

    /**
     * Invoked when the position of the seek bar has changed. The new position is provided as a parameter.
     *
     * @param position The new position of the seek bar.
     */
    fun onSeekBarPositionChanged(position: Long)

}
