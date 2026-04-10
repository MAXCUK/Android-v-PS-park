package com.maxcuk.xboardclient.core.proxy

import com.google.gson.GsonBuilder
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import com.maxcuk.xboardclient.core.repository.rawFields

object SingBoxConfigBuilder {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun build(node: NodeEntity): String {
        val outbound = when (node.type.lowercase()) {
            "shadowsocks" -> buildShadowsocksOutbound(node)
            else -> buildVlessOutbound(node)
        }

        val config = mapOf(
            "log" to mapOf(
                "level" to "info"
            ),
            "inbounds" to listOf(
                mapOf(
                    "type" to "tun",
                    "tag" to "tun-in",
                    "interface_name" to "sb-tun",
                    "mtu" to 1500,
                    "auto_route" to true,
                    "strict_route" to false,
                    "stack" to "system"
                )
            ),
            "outbounds" to listOf(
                outbound,
                mapOf("type" to "direct", "tag" to "direct"),
                mapOf("type" to "block", "tag" to "block")
            ),
            "route" to mapOf(
                "auto_detect_interface" to true,
                "final" to "proxy"
            )
        )

        return gson.toJson(config)
    }

    private fun buildVlessOutbound(node: NodeEntity): Map<String, Any?> {
        val raw = node.rawFields()
        val tlsEnabled = raw.security.equals("tls", true) || !raw.sni.isNullOrBlank() || !raw.publicKey.isNullOrBlank()
        val transport = when (raw.network?.lowercase()) {
            "ws", "websocket" -> mapOf(
                "type" to "ws",
                "path" to (raw.path ?: "/"),
                "headers" to mapOf(
                    "Host" to (raw.hostHeader ?: raw.sni ?: raw.host)
                )
            )
            "grpc" -> mapOf(
                "type" to "grpc",
                "service_name" to (raw.serviceName ?: "grpc")
            )
            else -> null
        }

        val tls = if (tlsEnabled) {
            buildMap<String, Any?> {
                put("enabled", true)
                put("server_name", raw.sni ?: raw.host)
                put("insecure", false)
                if (!raw.publicKey.isNullOrBlank()) {
                    put("reality", mapOf(
                        "enabled" to true,
                        "public_key" to raw.publicKey,
                        "short_id" to (raw.shortId ?: ""),
                        "spider_x" to (raw.spiderX ?: "/")
                    ))
                }
                if (!node.alpnValue().isNullOrBlank()) {
                    put("alpn", node.alpnValue()!!.split(",").map { it.trim() }.filter { it.isNotBlank() })
                }
            }
        } else null

        return buildMap {
            put("type", "vless")
            put("tag", "proxy")
            put("server", raw.host ?: node.host)
            put("server_port", raw.port ?: node.port)
            put("uuid", raw.uuid ?: node.uuid ?: "")
            put("flow", raw.flow ?: node.flow ?: "")
            put("packet_encoding", "xudp")
            if (tls != null) put("tls", tls)
            if (transport != null) put("transport", transport)
        }
    }

    private fun buildShadowsocksOutbound(node: NodeEntity): Map<String, Any?> {
        val raw = node.rawFields()
        return mapOf(
            "type" to "shadowsocks",
            "tag" to "proxy",
            "server" to (raw.host ?: node.host),
            "server_port" to (raw.port ?: node.port),
            "method" to (raw.method ?: node.method ?: "aes-128-gcm"),
            "password" to (raw.password ?: node.password ?: "")
        )
    }

    private fun NodeEntity.alpnValue(): String? = rawJson
        .let { json -> Regex("\\\"alpn\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(json)?.groupValues?.getOrNull(1) }
}
