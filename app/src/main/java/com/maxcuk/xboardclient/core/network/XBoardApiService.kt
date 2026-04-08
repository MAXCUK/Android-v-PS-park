package com.maxcuk.xboardclient.core.network

import com.maxcuk.xboardclient.core.network.model.ApiResponse
import com.maxcuk.xboardclient.core.network.model.AuthData
import com.maxcuk.xboardclient.core.network.model.GuestConfigResponse
import com.maxcuk.xboardclient.core.network.model.LoginRequest
import com.maxcuk.xboardclient.core.network.model.ServerRouteResponse
import com.maxcuk.xboardclient.core.network.model.UserInfoResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface XBoardApiService {
    @GET("api/v1/guest/comm/config")
    suspend fun getGuestConfig(): Response<ApiResponse<GuestConfigResponse>>

    @POST("api/v1/passport/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<AuthData>>

    @GET("api/v1/user/info")
    suspend fun getUserInfo(
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<UserInfoResponse>>

    @GET("api/v1/user/server/fetch")
    suspend fun fetchServers(
        @Header("Authorization") authorization: String
    ): Response<ApiResponse<List<ServerRouteResponse>>>

    @GET
    suspend fun fetchSubscriptionByUrl(
        @Url url: String,
        @Header("Authorization") authorization: String? = null
    ): Response<ResponseBody>
}
