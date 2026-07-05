package com.vfstr.smartclass.di

import android.content.Context
import androidx.room.Room
import com.vfstr.smartclass.data.local.db.LocalDatabase
import com.vfstr.smartclass.data.local.db.SmartClassDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LocalDatabase {
        return Room.databaseBuilder(
            context,
            LocalDatabase::class.java,
            "smartclass_local_db"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    @Singleton
    fun provideDao(database: LocalDatabase): SmartClassDao {
        return database.dao()
    }
}
