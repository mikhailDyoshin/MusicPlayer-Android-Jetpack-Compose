package com.example.musicplayerapp.domain.models

import com.example.musicplayerapp.config.BASE_URL
import com.example.musicplayerapp.player.PlayerState

data class TrackModel(
    val trackId: Int = 0,
    val trackName: String = "",
    val trackUrl: String = "",
    val trackImage: Int = 0,
    val artistName: String = "",
    var isSelected: Boolean = false,
    var state: PlayerState = PlayerState.STATE_IDLE
) {
    class Builder {
        private var trackId: Int = 0
        private lateinit var trackName: String
        private lateinit var trackUrl: String
        private var trackImage: Int = 0
        private lateinit var artistName: String
        private var isSelected: Boolean = false
        private var state: PlayerState = PlayerState.STATE_IDLE

        fun trackId(trackId: Int) = apply { this.trackId = trackId }
        fun trackName(trackName: String) = apply { this.trackName = trackName }
        fun trackUrl(trackUrl: String) = apply { this.trackUrl = BASE_URL + trackUrl }
        fun trackImage(trackImage: Int) = apply { this.trackImage = trackImage }
        fun artistName(artistName: String) = apply { this.artistName = artistName }

        /**
         * Builds and returns a [Track] object.
         *
         * @return A [Track] object with the set properties.
         */
        fun build(): TrackModel {
            return TrackModel(
                trackId,
                trackName,
                trackUrl,
                trackImage,
                artistName,
                isSelected,
                state
            )
        }
    }
}
