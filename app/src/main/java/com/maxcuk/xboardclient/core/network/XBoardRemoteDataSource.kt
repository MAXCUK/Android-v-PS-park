package com.maxcuk.xboardclient.core.network

import com.maxcuk.xboardclient.core.network.model.GuestConfigResponse
import com.maxcuk.xboardclient.core.network.model.ServerRouteResponse
import com.maxcuk.xboardclient.core.network.model.SubscriptionInfoResponse
import com.maxcuk.xboardclient.core.network.model.UserInfoResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class XBoardRemoteDataSource(
    private val api: XBoardApiService,
    private val baseUrl: String,
    private val rawHttpClient: OkHttpClient = NetworkFactory.createHttpClient()
) {
    suspend fun fetchGuestConfig(): GuestConfigResponse? {
        return api.getGuestConfig().body()?.data
    }

    suspend fun login(email: String, password: String): String {
        val payload = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString().toRequestBody(NetworkFactory.jsonMediaType)

        val response = api.login(payload)
        val body = response.body()
        val token = body?.data?.authData?.takeIf { it.isNotBlank() }
            ?: body?.data?.token?.takeIf { it.isNotBlank() }

        if (response.isSuccessful && token != null) return token

        val detail = buildList {
            body?.message?.takeIf { it.isNotBlank() }?.let(::add)
            body?.error?.takeIf { it.isNotBlank() }?.let(::add)
            response.message().takeIf { it.isNotBlank() }?.let(::add)
        }.joinToString(" | ")

        error(detail.ifBlank { "登录失败：未拿到 token" })
    }

    suspend fun getUserInfo(token: String): UserInfoResponse {
        val candidates = authHeaderCandidates(token)
        var lastError: String? = null
        for (header in candidates) {
            val response = api.getUserInfo(header)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) return data
            lastError = response.body()?.message ?: response.body()?.error ?: response.message()
        }
        error(lastError ?: "获取用户信息失败")
    }

    suspend fun getSubscriptionInfo(token: String): SubscriptionInfoResponse {
        val candidates = authHeaderCandidates(token)
        var lastError: String? = null
        for (header in candidates) {
            val response = api.getSubscribe(header)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) return data
            lastError = response.body()?.message ?: response.body()?.error ?: response.message()
        }
        error(lastError ?: "获取订阅信息失败")
    }

    suspend fun fetchServers(token: String): List<ServerRouteResponse> {
        val subscriptionInfo = runCatching { getSubscriptionInfo(token) }.getOrNull()
        val subscriptionUrl = subscriptionInfo?.subscribe_url
        if (!subscriptionUrl.isNullOrBlank()) {
            val fromSubscription = fetchServersFromSubscription(token, subscriptionUrl)
            if (fromSubscription.isNotEmpty()) return fromSubscription
        }

        val direct = fetchServersDirect(token)
        if (direct.isNotEmpty()) return direct

        val guestConfig = runCatching { fetchGuestConfig() }.getOrNull()
        val guestSubscriptionUrl = guestConfig?.subscribe_url
        if (!guestSubscriptionUrl.isNullOrBlank()) {
            val fromGuestSubscription = fetchServersFromSubscription(token, guestSubscriptionUrl)
            if (fromGuestSubscription.isNotEmpty()) return fromGuestSubscription
        }

        error("获取节点失败：订阅与摘要接口都未返回可用节点")
    }

    private suspend fun fetchServersDirect(token: String): List<ServerRouteResponse> {
        val candidates = authHeaderCandidates(token)
        var lastError: String? = null
        for (header in candidates) {
            val response = api.fetchServers(header)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) return data
            lastError = response.body()?.message ?: response.body()?.error ?: response.message()
        }
        if (lastError != null) throw IllegalStateException(lastError)
        return emptyList()
    }

    private suspend fun fetchServersFromSubscription(token: String, subscribeUrl: String): List<ServerRouteResponse> {
        val absoluteUrl = normalizeSubscriptionUrl(subscribeUrl)
        val authHeader = authHeaderCandidates(token).firstOrNull { it.startsWith("Bearer ") } ?: token
        val request = Request.Builder()
            .url(absoluteUrl)
            .header("User-Agent", "v2rayNG/1.8.0")
            .header("Accept", "text/plain,*/*")
            .header("Authorization", authHeader)
            .build()

        rawHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("订阅拉取失败: HTTP ${response.code}")
            }
            val raw = response.body?.string().orEmpty()
            val parsed = parseSubscription(raw)
            if (parsed.isNotEmpty()) return parsed
        }
        return emptyList()
    }

    private fun authHeaderCandidates(token: String): List<String> =
        if (token.startsWith("Bearer ")) listOf(token, token.removePrefix("Bearer ").trim())
        else listOf(token, "Bearer $token")

    private fun normalizeSubscriptionUrl(url: String): String {
        return when {
            url.startsWith("http://") || url.startsWith("https://") -> url
            url.startsWith("/") -> baseUrl.trimEnd('/') + url
            else -> baseUrl.trimEnd('/') + "/" + url.trimStart('/')
        }
    }

    fun parseSubscription(raw: String): List<ServerRouteResponse> {
        return SubscriptionParser.parse(raw)
    }
}
