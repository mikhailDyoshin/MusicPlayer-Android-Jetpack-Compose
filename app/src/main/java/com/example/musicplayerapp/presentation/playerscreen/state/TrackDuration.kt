package com.example.musicplayerapp.presentation.playerscreen.state

import com.example.musicplayerapp.utils.millisToMinutesSeconds

data class TrackDuration(val milliseconds: Long) {
    override fun toString(): String {
        return milliseconds.millisToMinutesSeconds()
    }
}
