package com.maxcuk.xboardclient.core.network

import android.util.Base64
import com.maxcuk.xboardclient.core.network.model.ServerRouteResponse

object SubscriptionParser {
    fun parse(raw: String): List<ServerRouteResponse> {
        val text = decodeIfBase64(raw)
        return text.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .mapIndexedNotNull { index, line -> parseLine(index, line) }
            .toList()
    }

    private fun decodeIfBase64(raw: String): String {
        return runCatching {
            val bytes = Base64.decode(raw, Base64.DEFAULT)
            val decoded = bytes.toString(Charsets.UTF_8)
            if (decoded.contains("://")) decoded else raw
        }.getOrDefault(raw)
    }

    private fun parseLine(index: Int, line: String): ServerRouteResponse? {
        return when {
            line.startsWith("ss://", true) -> ServerRouteResponse(
                id = index.toLong(),
                remarks = "SS-$index",
                type = "shadowsocks",
                server = extractHost(line),
                address = extractHost(line),
                port = extractPort(line),
                method = "aes-128-gcm"
            )
            line.startsWith("vless://", true) -> ServerRouteResponse(
                id = index.toLong(),
                remarks = "VLESS-$index",
                type = "vless",
                server = extractHost(line),
                address = extractHost(line),
                port = extractPort(line),
                uuid = extractUser(line),
                security = if (line.contains("security=tls")) "tls" else null,
                sni = extractQuery(line, "sni"),
                path = extractQuery(line, "path"),
                host_header = extractQuery(line, "host"),
                network = extractQuery(line, "type")
            )
            else -> null
        }
    }

    private fun extractUser(url: String): String? = url.substringAfter("://").substringBefore("@").ifBlank { null }
    private fun extractHost(url: String): String? = url.substringAfter("@").substringBefore(":").substringBefore("?").ifBlank { null }
    private fun extractPort(url: String): Int? = url.substringAfterLast(":", "0").substringBefore("?").toIntOrNull()
    private fun extractQuery(url: String, key: String): String? = url.substringAfter("?", "")
        .split("&")
        .mapNotNull {
            val parts = it.split("=", limit = 2)
            if (parts.size == 2 && parts[0] == key) parts[1] else null
        }.firstOrNull()
}
