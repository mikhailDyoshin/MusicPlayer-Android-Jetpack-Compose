package com.example.musicplayerapp.player

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.media3.common.PlaybackException
import com.example.musicplayerapp.player.state.TrackPosition
import com.example.musicplayerapp.utils.StateUpdater
import javax.inject.Inject

class MusicPlayer @Inject constructor(private val player: ExoPlayer) : Player.Listener {


    private val _playerState = MutableStateFlow(PlayerState.STATE_IDLE)
    val playerState: StateFlow<PlayerState> get() = _playerState

    private val _positionState = MutableStateFlow(TrackPosition(0L))
    val positionState: StateFlow<TrackPosition> get() = _positionState

    private val stateUpdater = StateUpdater(
        callBack = {
            val position = if (player.currentPosition > 0L) player.currentPosition else 0L
            _positionState.tryEmit(TrackPosition(position))
        },
        updatePeriodMillis = UPDATE_POSITION_DELAY_MILLIS
    )

    init {
        initPlayer()
    }

    private fun initPlayer() {
        player.addListener(this)
        player.prepare()
        trackPosition()
    }

    fun releasePlayer() {
        player.release()
        releasePositionTracking()
    }

    fun getCurrentTrackIndex(): Int {
        return player.currentMediaItemIndex
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        createPlayerStateLog("-> STATE_ERROR")
        emitPlayerState(PlayerState.STATE_ERROR)
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        createPlayerStateLog("PLAY_WHEN_READY_CHANGED")
        if (player.playbackState == Player.STATE_READY) {
            reactOnPlayWhenReady(
                onTrue = {
                    createPlayerStateLog("\t-> STATE_PLAYING")
                    emitPlayerState(PlayerState.STATE_PLAYING)
                },
                onFalse = {
                    createPlayerStateLog("\t-> STATE_PAUSE")
                    emitPlayerState(PlayerState.STATE_PAUSE)
                }
            )
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        when (reason) {
            Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
                createPlayerStateLog("-> STATE_NEXT_TRACK_AUTO")
                emitPlayerState(PlayerState.STATE_NEXT_TRACK_AUTO)
            }

            Player.MEDIA_ITEM_TRANSITION_REASON_SEEK -> {
                createPlayerStateLog("-> STATE_TRACK_CHANGED_BY_USER")
                emitPlayerState(PlayerState.STATE_TRACK_CHANGED_BY_USER)
            }

            Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
                createPlayerStateLog("-> PLAYLIST_CHANGED")
                emitPlayerState(PlayerState.PLAYLIST_CHANGED)
            }

            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> {
                createPlayerStateLog("-> TRANSITION_REASON_REPEAT")
                emitPlayerState(PlayerState.TRANSITION_REASON_REPEAT)
            }
        }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        when (playbackState) {
            Player.STATE_IDLE -> {
                createPlayerStateLog("-> STATE_IDLE")
                emitPlayerState(PlayerState.STATE_PAUSE)
            }

            Player.STATE_BUFFERING -> {
                createPlayerStateLog("STATE_BUFFERING")
                reactOnPlayWhenReady(
                    onTrue = {
                        emitPlayerState(PlayerState.STATE_PLAYING)
                        createPlayerStateLog("\t-> STATE_PLAYING")
                    },
                    onFalse = {
                        emitPlayerState(PlayerState.STATE_PAUSE)
                        createPlayerStateLog("\t-> STATE_PAUSE")
                    }
                )
            }

            Player.STATE_READY -> {
                createPlayerStateLog("STATE_READY")
                reactOnPlayWhenReady(
                    onTrue = {
                        createPlayerStateLog("\t-> STATE_PLAYING")
                        emitPlayerState(PlayerState.STATE_PLAYING)
                    },
                    onFalse = {
                        createPlayerStateLog("\t-> STATE_PAUSE")
                        emitPlayerState(PlayerState.STATE_PAUSE)
                    }
                )
            }

            Player.STATE_ENDED -> {
                createPlayerStateLog("-> STATE_ENDED")
                emitPlayerState(PlayerState.STATE_ENDED)
            }
        }
    }

    private fun createPlayerStateLog(message: String) {
        Log.d(PLAYER_STATE_TAG, message)
    }

    private fun reactOnPlayWhenReady(onTrue: () -> Unit, onFalse: () -> Unit) {
        if (player.playWhenReady) {
            onTrue()
        } else {
            onFalse()
        }
    }

    private fun trackPosition() {
        stateUpdater.start()
    }

    private fun releasePositionTracking() {
        stateUpdater.stop()
    }

    private fun emitPlayerState(state: PlayerState) {
        _playerState.tryEmit(state)
    }

    companion object {
        private const val PLAYER_STATE_TAG = "ExoPlayerListener"
        private const val UPDATE_POSITION_DELAY_MILLIS = 20L
    }
}
