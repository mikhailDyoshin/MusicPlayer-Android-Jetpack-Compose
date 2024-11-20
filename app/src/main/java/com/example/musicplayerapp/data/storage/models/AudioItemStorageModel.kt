package com.example.musicplayerapp.data.storage.models

import android.net.Uri

data class AudioItemStorageModel(
    val id: Long,
    val name: String,
    val uri: Uri
)
