package com.example.musicplayerapp.data.storage.models

import android.net.Uri

data class AudioStorageModel(
    val id: Long,
    val name: String,
    val artist: String,
    val uri: Uri
)
