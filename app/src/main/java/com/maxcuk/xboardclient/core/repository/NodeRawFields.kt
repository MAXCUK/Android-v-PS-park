package com.maxcuk.xboardclient.core.repository

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.maxcuk.xboardclient.core.database.entity.NodeEntity

data class NodeRawFields(
    val host: String? = null,
    val port: Int? = null,
    val uuid: String? = null,
    val password: String? = null,
    val method: String? = null,
    val security: String? = null,
    val flow: String? = null,
    val sni: String? = null,
    val network: String? = null,
    val path: String? = null,
    val hostHeader: String? = null,
    val serviceName: String? = null,
    val publicKey: String? = null,
    val shortId: String? = null,
    val spiderX: String? = null
)

private val gson by lazy { Gson() }

fun NodeEntity.rawFields(): NodeRawFields {
    val json = runCatching { gson.fromJson(rawJson, JsonObject::class.java) }.getOrNull()
    fun str(vararg keys: String): String? {
        for (key in keys) {
            val v = json?.get(key)
            if (v != null && !v.isJsonNull) {
                val s = v.asString
                if (s.isNotBlank()) return s
            }
        }
        return null
    }
    fun int(vararg keys: String): Int? {
        for (key in keys) {
            val v = json?.get(key)
            if (v != null && !v.isJsonNull) {
                runCatching { return v.asInt }.getOrNull()
                runCatching { return v.asString.toInt() }.getOrNull()
            }
        }
        return null
    }
    return NodeRawFields(
        host = str("host", "address", "server") ?: host.takeIf { it.isNotBlank() },
        port = int("port") ?: port.takeIf { it > 0 },
        uuid = str("uuid") ?: uuid,
        password = str("password") ?: password,
        method = str("method", "cipher") ?: method,
        security = str("security") ?: security,
        flow = str("flow") ?: flow,
        sni = str("sni") ?: sni,
        network = str("network", "transport") ?: network,
        path = str("path") ?: path,
        hostHeader = str("host_header", "host") ?: hostHeader,
        serviceName = str("service_name", "serviceName") ?: serviceName,
        publicKey = str("public_key", "publicKey") ?: publicKey,
        shortId = str("short_id", "shortId") ?: shortId,
        spiderX = str("spider_x", "spiderX") ?: spiderX
    )
}
