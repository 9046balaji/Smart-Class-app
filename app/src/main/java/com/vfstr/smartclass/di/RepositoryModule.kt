package com.vfstr.smartclass.di

import android.content.Context
import com.vfstr.smartclass.data.local.db.LocalDatabase
import com.vfstr.smartclass.data.preferences.SecurePreferences
import com.vfstr.smartclass.data.remote.api.RetrofitApi
import com.vfstr.smartclass.data.repositories.AppRepository
import com.vfstr.smartclass.data.repositories.AttendanceRepository
import com.vfstr.smartclass.utils.geofence.WifiHelper
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideAppRepository(
        @ApplicationContext context: Context,
        api: RetrofitApi,
        db: LocalDatabase,
        securePrefs: SecurePreferences,
        wifiHelper: WifiHelper
    ): AppRepository {
        return AppRepository(context, api, db, securePrefs, wifiHelper)
    }

    @Provides
    @Singleton
    fun provideAttendanceRepository(
        api: RetrofitApi
    ): AttendanceRepository {
        return AttendanceRepository(api)
    }
}
