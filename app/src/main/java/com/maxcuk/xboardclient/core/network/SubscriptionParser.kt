package com.maxcuk.xboardclient.core.network

import android.net.Uri
import android.util.Base64
import com.maxcuk.xboardclient.core.network.model.ServerRouteResponse
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

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
            val cleaned = raw.trim().replace("\n", "").replace("\r", "")
            val bytes = Base64.decode(cleaned, Base64.DEFAULT)
            val decoded = bytes.toString(Charsets.UTF_8)
            if (decoded.contains("://")) decoded else raw
        }.getOrDefault(raw)
    }

    private fun parseLine(index: Int, line: String): ServerRouteResponse? {
        return when {
            line.startsWith("ss://", true) -> parseShadowsocks(index, line)
            line.startsWith("vless://", true) -> parseVless(index, line)
            else -> null
        }
    }

    private fun parseShadowsocks(index: Int, line: String): ServerRouteResponse? {
        val raw = line.removePrefix("ss://")
        val mainPart = raw.substringBefore("#")
        val name = decodeComponent(raw.substringAfter("#", "SS-$index")).ifBlank { "SS-$index" }

        val userAndHost = if (mainPart.contains("@")) {
            mainPart
        } else {
            val decoded = decodeBase64Segment(mainPart.substringBefore("?")) ?: return null
            val suffix = mainPart.substringAfter("?", "").let { if (it.isBlank()) "" else "?$it" }
            decoded + suffix
        }

        val credentials = userAndHost.substringBefore("@")
        val hostPart = userAndHost.substringAfter("@", "")
        val methodPassword = if (credentials.contains(":")) credentials else decodeBase64Segment(credentials).orEmpty()
        val method = methodPassword.substringBefore(":", "")
        val password = methodPassword.substringAfter(":", "")
        val host = hostPart.substringBefore(":").substringBefore("?")
        val port = hostPart.substringAfter(":", "0").substringBefore("?").toIntOrNull() ?: 0

        if (host.isBlank() || method.isBlank()) return null
        return ServerRouteResponse(
            id = index.toLong(),
            remarks = name,
            type = "shadowsocks",
            server = host,
            address = host,
            port = port,
            method = method,
            password = password
        )
    }

    private fun parseVless(index: Int, line: String): ServerRouteResponse? {
        val uri = Uri.parse(line)
        val host = uri.host ?: return null
        val name = decodeComponent(uri.fragment ?: "VLESS-$index").ifBlank { "VLESS-$index" }
        return ServerRouteResponse(
            id = index.toLong(),
            remarks = name,
            type = "vless",
            server = host,
            address = host,
            port = uri.port.takeIf { it > 0 },
            uuid = uri.userInfo,
            security = uri.getQueryParameter("security") ?: if (line.contains("security=tls")) "tls" else null,
            sni = uri.getQueryParameter("sni"),
            path = decodeComponent(uri.getQueryParameter("path") ?: ""),
            host_header = uri.getQueryParameter("host"),
            network = uri.getQueryParameter("type"),
            flow = uri.getQueryParameter("flow"),
            public_key = uri.getQueryParameter("pbk"),
            short_id = uri.getQueryParameter("sid")
        )
    }

    private fun decodeBase64Segment(value: String): String? {
        return runCatching {
            val normalized = value.replace('-', '+').replace('_', '/')
            val padded = normalized.padEnd(((normalized.length + 3) / 4) * 4, '=')
            Base64.decode(padded, Base64.DEFAULT).toString(Charsets.UTF_8)
        }.getOrNull()
    }

    private fun decodeComponent(value: String): String {
        return runCatching { URLDecoder.decode(value, StandardCharsets.UTF_8.name()) }.getOrDefault(value)
    }
}
