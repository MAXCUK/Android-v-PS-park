package com.maxcuk.xboardclient.core.network

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkFactory {
    val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun normalizeBaseUrl(baseUrl: String): String {
        return when {
            baseUrl.startsWith("http://") || baseUrl.startsWith("https://") -> baseUrl
            else -> "https://$baseUrl"
        }.trim().let { if (it.endsWith('/')) it else "$it/" }
    }

    fun createHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()
    }

    fun create(baseUrl: String): XBoardApiService {
        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(createHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XBoardApiService::class.java)
    }
}
