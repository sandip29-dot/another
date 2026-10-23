package com.block.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.block.app.data.db.BlockAttemptDao
import com.block.app.data.db.BlockDatabase
import com.block.app.data.repository.BlockRepository
import com.block.app.data.repository.BlockRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing app-wide singletons:
 * Room database, DAOs, and DataStore.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): BlockDatabase = Room.databaseBuilder(
        context,
        BlockDatabase::class.java,
        BlockDatabase.DATABASE_NAME
    ).build()

    @Provides
    fun provideBlockAttemptDao(
        db: BlockDatabase
    ): BlockAttemptDao = db.blockAttemptDao()

    @Provides
    @Singleton
    fun provideBlockRepository(
        impl: BlockRepositoryImpl
    ): BlockRepository = impl

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = PreferenceDataStoreFactory.create {
        context.preferencesDataStoreFile("block_settings")
    }
}
