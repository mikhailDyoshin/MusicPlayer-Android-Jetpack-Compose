package com.example.musicplayerapp.data.contentProvider

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.musicplayerapp.data.storage.models.AudioItemStorageModel

class AudioContentProvider(private val app: Context) {

    private val displayNameProjection = MediaStore.Audio.AudioColumns.DISPLAY_NAME

    private val projection = arrayOf(
        displayNameProjection
    )

    private val selection = null

    private val selectionArgs = null

    private val sortOrder = null

    fun getContent(uri: Uri): AudioItemStorageModel? {

        val fileName = app.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->

            val index = cursor.getColumnIndex(displayNameProjection)
            cursor.moveToFirst()
            cursor.getString(index)

        }
        if (fileName == null) {
            Log.d("Audio names", "Query is null")
        }
        return fileName?.let { fullFileName ->
            val name = Uri.parse(fullFileName).lastPathSegment ?: "No name"
            Log.d(TAG, "$name -> $fullFileName\n$uri\n")
            AudioItemStorageModel(
                name = Uri.parse(fullFileName).lastPathSegment ?: "No name",
                uri = uri
            )
        }
    }

    companion object {
        private const val TAG = "AudioContentProvider"
    }

}