package com.example.musicplayerapp.player

enum class PlayerState {
    /**
     * State when the player is idle, not ready to play.
     */
    STATE_IDLE,

    /**
     * State when the player has encountered an error.
     */
    STATE_ERROR,

    /**
     * State when the playback has ended.
     */
    STATE_ENDED,

    /**
     * State when the player is actively playing content.
     */
    STATE_PLAYING,

    /**
     * State when the player has paused the playback.
     */
    STATE_PAUSE,

    /**
     * State when the player has moved to the next track.
     */
    STATE_NEXT_TRACK_AUTO,

    /**
     * State when a user changes the current track.
     */
    STATE_TRACK_CHANGED_BY_USER,

    PLAYLIST_CHANGED,

    TRANSITION_REASON_REPEAT,

    POSITION_CHANGED_BY_USER
}