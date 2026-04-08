package com.maxcuk.xboardclient.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkFactory {
    fun normalizeBaseUrl(baseUrl: String): String {
        return when {
            baseUrl.startsWith("http://") || baseUrl.startsWith("https://") -> baseUrl
            else -> "https://$baseUrl"
        }.trim().let { if (it.endsWith('/')) it else "$it/" }
    }

    fun create(baseUrl: String): XBoardApiService {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val normalizedBaseUrl = normalizeBaseUrl(baseUrl)

        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(XBoardApiService::class.java)
    }
}
