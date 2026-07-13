package com.vfstr.smartclass.di

import com.vfstr.smartclass.data.remote.api.RetrofitApi
import com.vfstr.smartclass.data.remote.interceptors.AuthInterceptor
import com.vfstr.smartclass.data.remote.interceptors.TokenAuthenticator
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCoroutineExceptionHandler(): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, exception ->
            Log.e("SmartClass_CrashShield", "Intercepted uncaught thread exception: ${exception.localizedMessage}")
        }
    }

    @Provides
    @Singleton
    fun provideNetworkScope(exceptionHandler: CoroutineExceptionHandler): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO + exceptionHandler)
    }

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        tokenAuthenticator: TokenAuthenticator
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (com.vfstr.smartclass.BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        val pinnerBuilder = okhttp3.CertificatePinner.Builder()
        if (!com.vfstr.smartclass.BuildConfig.DEBUG) {
            // Pin the production server domain to prevent MITM attacks
            pinnerBuilder.add("smartclass.vignan.ac.in", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        }
        val pinner = pinnerBuilder.build()

        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(logging)
            .certificatePinner(pinner)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, moshi: Moshi): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://localhost/api/v1/") // Removed trailing slash fix inside Api interface
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofitApi(retrofit: Retrofit): RetrofitApi {
        return retrofit.create(RetrofitApi::class.java)
    }

    @Provides
    @Singleton
    fun provideEnrollmentApiService(retrofit: Retrofit): com.vfstr.smartclass.data.remote.api.EnrollmentApiService {
        return retrofit.create(com.vfstr.smartclass.data.remote.api.EnrollmentApiService::class.java)
    }
}
