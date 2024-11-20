package com.example.musicplayerapp.utils

import java.util.Locale

/**
 * Formats a long duration value (in milliseconds) into a time string in the format "MM:SS".
 *
 * @return The formatted time string.
 */
fun Long.millisToMinutesSeconds(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val remainingSeconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, remainingSeconds)
}