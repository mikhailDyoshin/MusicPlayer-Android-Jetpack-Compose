package com.example.musicplayerapp.player.service

import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.media3.common.Player
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSessionService

@UnstableApi
@RequiresApi(Build.VERSION_CODES.O)
class FocusListener(private val player: Player, private val audioManager: AudioManager, private val mediaSessionService: MediaSessionService) {

    private val afChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                // Permanent loss of audio focus
                // Pause playback immediately
                player.pause()
                mediaSessionService.stopSelf()
                Log.d(FOCUS_LISTENER_TAG, "Audio focus lost permanently, stopping player")
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                player.pause()
                Log.d(FOCUS_LISTENER_TAG, "Audio focus lost transiently")
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(FOCUS_LISTENER_TAG, "Audio focus lost transiently, ducking playback")
            }

            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!player.isPlaying) {
                    player.play()
                }
                Log.d(FOCUS_LISTENER_TAG, "Audio focus gained")
            }
        }
    }

    private val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
        setAudioAttributes(AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        })
        setAcceptsDelayedFocusGain(true)
        setOnAudioFocusChangeListener(afChangeListener)
        build()
    }



    fun requestAudioFocus(onRequestDenied: () -> Unit) {
        Log.d(FOCUS_LISTENER_TAG, "Making audio focus request")

        // Request audio focus
        val result = audioManager.requestAudioFocus(
            focusRequest
        )

       if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
           Log.d(FOCUS_LISTENER_TAG, "Audio-focus request granted")
           player.play()
       }

        if (result != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            // Handle failure to gain audio focus
            Log.d(FOCUS_LISTENER_TAG, "Failed to gain audio focus")
            onRequestDenied()
        }
    }

    companion object {
        const val FOCUS_LISTENER_TAG = "PlayerFocusListener"
    }

}