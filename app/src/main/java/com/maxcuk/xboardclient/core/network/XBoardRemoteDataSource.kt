package com.maxcuk.xboardclient.core.network

import com.maxcuk.xboardclient.core.network.model.GuestConfigResponse
import com.maxcuk.xboardclient.core.network.model.ServerRouteResponse
import com.maxcuk.xboardclient.core.network.model.UserInfoResponse

class XBoardRemoteDataSource(
    private val api: XBoardApiService
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
        val candidates = listOf(token, "Bearer $token")
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
        val candidates = listOf(token, "Bearer $token")
        var lastError: String? = null
        for (header in candidates) {
            val response = api.fetchServers(header)
            val data = response.body()?.data
            if (response.isSuccessful && data != null) return data
            lastError = response.body()?.message ?: response.message()
        }
        error(lastError ?: "获取节点失败")
    }

    fun parseSubscription(raw: String): List<ServerRouteResponse> {
        return SubscriptionParser.parse(raw)
    }
}
