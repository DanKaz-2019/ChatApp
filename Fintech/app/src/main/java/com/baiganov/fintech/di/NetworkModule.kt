package com.baiganov.fintech.di

import com.baiganov.fintech.data.network.AuthInterceptor
import com.baiganov.fintech.data.network.ChatApi
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
class NetworkModule {

    @Singleton
    @Provides
    fun provideOkHttpClient(authInterceptor: AuthInterceptor, loggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .readTimeout(READ_TIMEOUT_MILLIS, TimeUnit.SECONDS)
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthInterceptor(): AuthInterceptor {
        return AuthInterceptor()
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
    }

    @ExperimentalSerializationApi
    @Singleton
    @Provides
    fun provideApi(client: OkHttpClient): ChatApi =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .addConverterFactory(json.asConverterFactory(contentType))
            .client(client)
            .build().create(ChatApi::class.java)

    companion object {

        private val json = Json { ignoreUnknownKeys = true }
        private val contentType = "application/json".toMediaType()
        private const val READ_TIMEOUT_MILLIS = 20L
        const val BASE_URL = "https://tinkoff-android-fall21.zulipchat.com/api/v1/"
    }
}