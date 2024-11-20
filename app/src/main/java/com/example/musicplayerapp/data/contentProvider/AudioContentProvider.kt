package com.example.musicplayerapp.data.contentProvider

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import com.example.musicplayerapp.data.storage.models.AudioItemStorageModel

class AudioContentProvider(private val app: Context) {

    private val idProjection = MediaStore.MediaColumns._ID
    private val displayNameProjection = MediaStore.Audio.AudioColumns.DISPLAY_NAME

    private val projection = arrayOf(
        idProjection,
        displayNameProjection
    )

    private val selection = null
    private val selectionArgs = null
    private val sortOrder = null

    fun getContent(uri: Uri): AudioItemStorageModel? {
        return app.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(idProjection)
            val nameIndex = cursor.getColumnIndex(displayNameProjection)

            if (cursor.moveToFirst()) {
                val id = cursor.getLong(idIndex)
                val fullFileName = cursor.getString(nameIndex)

                val name = Uri.parse(fullFileName).lastPathSegment ?: "No name"
                Log.d(TAG, "$name -> $fullFileName\n$uri\n")

                AudioItemStorageModel(
                    id = id,
                    name = name,
                    uri = uri
                )
            } else {
                Log.d(TAG, "Cursor is empty for URI: $uri")
                null
            }
        }
    }

    companion object {
        private const val TAG = "AudioContentProvider"
    }

}