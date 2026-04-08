package com.maxcuk.xboardclient.core.network.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val data: T? = null,
    val message: String? = null
)

data class LoginRequest(
    val email: String,
    val password: String
)

data class AuthData(
    @SerializedName("auth_data")
    val authData: String? = null,
    val token: String? = null
)

data class GuestConfigResponse(
    val app_name: String? = null,
    val app_description: String? = null,
    val app_url: String? = null,
    val subscribe_url: String? = null,
    val telegram_discuss_link: String? = null,
    val telegram_bot_username: String? = null,
    val tos_url: String? = null,
    val safe_mode_enable: Int? = null,
    val api_base: String? = null
)

data class UserInfoResponse(
    val email: String? = null,
    val transfer_enable: Long? = null,
    val u: Long? = null,
    val d: Long? = null,
    val expired_at: Long? = null,
    val plan_id: Long? = null,
    val uuid: String? = null
)

data class ServerRouteResponse(
    val id: Long? = null,
    val name: String? = null,
    val route_id: String? = null,
    val remarks: String? = null,
    val address: String? = null,
    val port: Int? = null,
    val type: String? = null,
    val host: String? = null,
    val server: String? = null,
    val cipher: String? = null,
    val method: String? = null,
    val uuid: String? = null,
    val password: String? = null,
    val security: String? = null,
    val flow: String? = null,
    val sni: String? = null,
    val network: String? = null,
    val tls: String? = null,
    val transport: String? = null,
    val path: String? = null,
    val host_header: String? = null,
    val serviceName: String? = null,
    val service_name: String? = null,
    val publicKey: String? = null,
    val public_key: String? = null,
    val shortId: String? = null,
    val short_id: String? = null,
    val spiderX: String? = null,
    val spider_x: String? = null,
    val alpn: String? = null
)
