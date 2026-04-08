package com.maxcuk.xboardclient.core.network

import com.maxcuk.xboardclient.core.network.model.GuestConfigResponse
import com.maxcuk.xboardclient.core.network.model.ServerRouteResponse
import com.maxcuk.xboardclient.core.network.model.UserInfoResponse

class XBoardRemoteDataSource(
    private val api: XBoardApiService,
    private val baseUrl: String
) {
    suspend fun fetchGuestConfig(): GuestConfigResponse? {
        return api.getGuestConfig().body()?.data
    }

    suspend fun login(email: String, password: String): String {
        val body = api.login(com.maxcuk.xboardclient.core.network.model.LoginRequest(email, password)).body()
        val token = body?.data?.authData ?: body?.data?.token
        return token ?: error(body?.message ?: "登录失败：未拿到 token")
    }

    suspend fun getUserInfo(token: String): UserInfoResponse {
        val candidates = authHeaderCandidates(token)
        var lastError: String? = null
        for (header in candidates) {
            val response = api.getUserInfo(header)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) return data
            lastError = response.body()?.message ?: response.message()
        }
        error(lastError ?: "获取用户信息失败")
    }

    suspend fun fetchServers(token: String): List<ServerRouteResponse> {
        val direct = fetchServersDirect(token)
        if (direct.isNotEmpty()) return direct

        val guestConfig = runCatching { fetchGuestConfig() }.getOrNull()
        val subscribeUrl = guestConfig?.subscribe_url
        if (!subscribeUrl.isNullOrBlank()) {
            val fromSubscription = fetchServersFromSubscription(token, subscribeUrl)
            if (fromSubscription.isNotEmpty()) return fromSubscription
        }

        error("获取节点失败：面板未返回节点，且订阅兜底也为空")
    }

    private suspend fun fetchServersDirect(token: String): List<ServerRouteResponse> {
        val candidates = authHeaderCandidates(token)
        var lastError: String? = null
        for (header in candidates) {
            val response = api.fetchServers(header)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) return data
            lastError = response.body()?.message ?: response.message()
        }
        if (lastError != null) throw IllegalStateException(lastError)
        return emptyList()
    }

    private suspend fun fetchServersFromSubscription(token: String, subscribeUrl: String): List<ServerRouteResponse> {
        val absoluteUrl = normalizeSubscriptionUrl(subscribeUrl)
        val candidateHeaders = authHeaderCandidates(token) + listOf<String?>(null)
        var lastError: String? = null
        for (header in candidateHeaders.distinct()) {
            val response = api.fetchSubscriptionByUrl(absoluteUrl, header)
            if (response.isSuccessful) {
                val raw = response.body()?.string().orEmpty()
                val parsed = parseSubscription(raw)
                if (parsed.isNotEmpty()) return parsed
                lastError = "订阅已返回，但未解析出节点"
            } else {
                lastError = response.message()
            }
        }
        throw IllegalStateException(lastError ?: "订阅拉取失败")
    }

    private fun authHeaderCandidates(token: String): List<String> = listOf(token, "Bearer $token")

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
