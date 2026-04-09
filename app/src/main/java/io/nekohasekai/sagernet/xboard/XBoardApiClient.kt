package io.nekohasekai.sagernet.xboard

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class XBoardApiClient(
    private val baseUrl: String = DEFAULT_BASE_URL
) {
    fun login(email: String, password: String): String {
        val body = JSONObject().apply {
            put("email", email)
            put("password", password)
        }.toString()
        val json = postJson("/api/v1/passport/auth/login", body)
        val data = json.optJSONObject("data")
        val token = data?.optString("auth_data")?.takeIf { it.isNotBlank() }
            ?: data?.optString("token")?.takeIf { it.isNotBlank() }
        return token ?: error(json.optString("message", "登录失败"))
    }

    fun getSubscribe(authHeader: String): XBoardSubscriptionInfo {
        val json = getJson("/api/v1/user/getSubscribe", authHeader)
        val data = json.optJSONObject("data") ?: error(json.optString("message", "获取订阅失败"))
        val plan = data.optJSONObject("plan")
        return XBoardSubscriptionInfo(
            token = data.optString("token"),
            subscribe_url = data.optString("subscribe_url"),
            email = data.optString("email"),
            uuid = data.optString("uuid"),
            transfer_enable = data.optLong("transfer_enable", 0),
            u = data.optLong("u", 0),
            d = data.optLong("d", 0),
            expired_at = data.optLong("expired_at", 0),
            plan_id = data.optLong("plan_id", 0),
            plan_name = plan?.optString("name")?.takeIf { it.isNotBlank() }
        )
    }

    fun getUserInfo(authHeader: String): XBoardUserInfo {
        val json = getJson("/api/v1/user/info", authHeader)
        val data = json.optJSONObject("data") ?: error(json.optString("message", "获取用户信息失败"))
        val plan = data.optJSONObject("plan")
        return XBoardUserInfo(
            email = data.optString("email"),
            transfer_enable = data.optLong("transfer_enable", 0),
            u = data.optLong("u", 0),
            d = data.optLong("d", 0),
            expired_at = data.optLong("expired_at", 0),
            uuid = data.optString("uuid"),
            plan_id = data.optLong("plan_id", 0),
            plan_name = plan?.optString("name")?.takeIf { it.isNotBlank() }
                ?: data.optString("plan_name").takeIf { it.isNotBlank() }
        )
    }

    private fun getJson(path: String, authHeader: String? = null): JSONObject {
        val conn = (URL(normalizeBaseUrl(baseUrl) + path.trimStart('/')).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 15000
            readTimeout = 15000
            setRequestProperty("Accept", "application/json")
            if (!authHeader.isNullOrBlank()) setRequestProperty("Authorization", authHeader)
        }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return stream.use {
            BufferedReader(InputStreamReader(it)).readText().let(::JSONObject)
        }
    }

    private fun postJson(path: String, body: String): JSONObject {
        val conn = (URL(normalizeBaseUrl(baseUrl) + path.trimStart('/')).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 15000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Accept", "application/json")
        }
        conn.outputStream.use { it.write(body.toByteArray()) }
        val stream = if (conn.responseCode in 200..299) conn.inputStream else conn.errorStream
        return stream.use {
            BufferedReader(InputStreamReader(it)).readText().let(::JSONObject)
        }
    }

    companion object {
        const val DEFAULT_BASE_URL = "https://ax.ty666.help/"

        fun normalizeBaseUrl(url: String): String {
            val withScheme = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
            return if (withScheme.endsWith('/')) withScheme else "$withScheme/"
        }
    }
}
