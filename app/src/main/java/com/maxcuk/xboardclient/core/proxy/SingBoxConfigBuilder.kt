package com.maxcuk.xboardclient.core.proxy

import com.google.gson.GsonBuilder
import com.maxcuk.xboardclient.core.database.entity.NodeEntity

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
                mapOf(
                    "type" to "direct",
                    "tag" to "direct"
                ),
                mapOf(
                    "type" to "block",
                    "tag" to "block"
                )
            ),
            "route" to mapOf(
                "auto_detect_interface" to true,
                "final" to "proxy"
            )
        )

        return gson.toJson(config)
    }

    private fun buildVlessOutbound(node: NodeEntity): Map<String, Any?> {
        val tlsEnabled = node.security.equals("tls", true) || !node.sni.isNullOrBlank() || !node.publicKey.isNullOrBlank()
        val transport = when (node.network?.lowercase()) {
            "ws", "websocket" -> mapOf(
                "type" to "ws",
                "path" to (node.path ?: "/"),
                "headers" to mapOf(
                    "Host" to (node.hostHeader ?: node.sni ?: node.host)
                )
            )
            "grpc" -> mapOf(
                "type" to "grpc",
                "service_name" to (node.serviceName ?: "grpc")
            )
            else -> null
        }

        val tls = if (tlsEnabled) {
            buildMap<String, Any?> {
                put("enabled", true)
                put("server_name", node.sni ?: node.host)
                put("insecure", false)
                if (!node.publicKey.isNullOrBlank()) {
                    put("reality", mapOf(
                        "enabled" to true,
                        "public_key" to node.publicKey,
                        "short_id" to (node.shortId ?: ""),
                        "spider_x" to (node.spiderX ?: "/")
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
            put("server", node.host)
            put("server_port", node.port)
            put("uuid", node.uuid ?: "")
            put("flow", node.flow ?: "")
            put("packet_encoding", "xudp")
            if (tls != null) put("tls", tls)
            if (transport != null) put("transport", transport)
        }
    }

    private fun buildShadowsocksOutbound(node: NodeEntity): Map<String, Any?> {
        return mapOf(
            "type" to "shadowsocks",
            "tag" to "proxy",
            "server" to node.host,
            "server_port" to node.port,
            "method" to (node.method ?: "aes-128-gcm"),
            "password" to (node.password ?: "")
        )
    }

    private fun NodeEntity.alpnValue(): String? = rawJson
        .let { json -> Regex("\"alpn\"\\s*:\\s*\"([^\"]+)\"").find(json)?.groupValues?.getOrNull(1) }
}
