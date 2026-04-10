package com.maxcuk.xboardclient.core.repository

import com.google.gson.Gson
import com.maxcuk.xboardclient.core.database.dao.NodeDao
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import com.maxcuk.xboardclient.core.datastore.ConnectionPrefs
import com.maxcuk.xboardclient.core.network.model.ServerRouteResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class NodeRepository(
    private val nodeDao: NodeDao,
    private val connectionPrefs: ConnectionPrefs? = null,
    private val gson: Gson = Gson()
) {
    fun observeNodes(): Flow<List<NodeEntity>> = nodeDao.observeNodes()

    suspend fun replaceNodes(servers: List<ServerRouteResponse>) {
        val selectedId = nodeDao.getSelectedNode()?.id ?: connectionPrefs?.lastSelectedNode?.firstOrNull()
        val mapped = servers.mapIndexedNotNull { index, item ->
            val stableId = item.route_id ?: item.id?.toString() ?: "node_$index"
            val type = normalizeType(item.type)
            val host = (item.host ?: item.address ?: item.server).orEmpty().trim()
            val port = item.port ?: 0
            val method = item.method ?: item.cipher
            val hasRequiredFields = when (type) {
                "shadowsocks" -> host.isNotBlank() && port > 0 && !method.isNullOrBlank() && !item.password.isNullOrBlank()
                "vless" -> host.isNotBlank() && port > 0 && !item.uuid.isNullOrBlank()
                else -> host.isNotBlank() && port > 0
            }
            if (!hasRequiredFields) return@mapIndexedNotNull null
            NodeEntity(
                id = stableId,
                name = item.remarks ?: item.name ?: item.address ?: item.server ?: "Node $index",
                type = type,
                host = host,
                port = port,
                uuid = item.uuid,
                password = item.password,
                method = method,
                security = item.security,
                flow = item.flow,
                sni = item.sni,
                network = item.network ?: item.transport,
                path = item.path,
                hostHeader = item.host_header,
                serviceName = item.service_name ?: item.serviceName,
                publicKey = item.public_key ?: item.publicKey,
                shortId = item.short_id ?: item.shortId,
                spiderX = item.spider_x ?: item.spiderX,
                rawJson = gson.toJson(item),
                isSelected = stableId == selectedId
            )
        }
        nodeDao.clearAll()
        nodeDao.upsertAll(mapped)
    }

    suspend fun selectNode(nodeId: String) {
        nodeDao.selectNode(nodeId)
        connectionPrefs?.setLastSelectedNode(nodeId)
    }

    suspend fun currentSelectedNode() = nodeDao.getSelectedNode()

    private fun normalizeType(type: String?): String {
        val value = type?.lowercase().orEmpty()
        return when {
            value.contains("shadowsocks") || value == "ss" -> "shadowsocks"
            value.contains("vless") -> "vless"
            else -> value.ifBlank { "unknown" }
        }
    }
}
