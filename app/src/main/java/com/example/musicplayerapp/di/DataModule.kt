package com.example.musicplayerapp.di

import com.example.musicplayerapp.data.MusicPlayerRepositoryImpl
import com.example.musicplayerapp.domain.repository.MusicPlayerRepository
import com.example.musicplayerapp.domain.usecases.GetTracksUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DataModule {

    /**
     * Provides a singleton instance of [MusicPlayerRepository].
     *
     * @param trackRepository An instance of [MusicPlayerRepositoryImpl] which is a concrete implementation of [MusicPlayerRepository].
     * @return An instance of [MusicPlayerRepository].
     */
    @Provides
    @Singleton
    fun provideMusicPlayerRepository(trackRepository: MusicPlayerRepositoryImpl): MusicPlayerRepository {
        return trackRepository
    }

    @Provides
    fun provideGetTracksUseCase(
        repository: MusicPlayerRepository
    ): GetTracksUseCase {
        return GetTracksUseCase(repository = repository)
    }
}