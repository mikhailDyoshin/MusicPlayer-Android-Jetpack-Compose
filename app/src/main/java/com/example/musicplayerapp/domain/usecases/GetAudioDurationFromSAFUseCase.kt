package com.example.musicplayerapp.domain.usecases

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import javax.inject.Inject

class GetAudioDurationFromSAFUseCase @Inject constructor(private val context: Context) {

    operator fun invoke(uri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        return try {
            val descriptor = context.contentResolver.openFileDescriptor(uri, "r") ?: return 0L
            retriever.setDataSource(descriptor.fileDescriptor)
            descriptor.close()

            // Retrieve duration in milliseconds
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            0L
        } finally {
            retriever.release()
        }
    }

}