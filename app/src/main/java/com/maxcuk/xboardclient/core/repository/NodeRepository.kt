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
            val host = (item.host ?: item.address ?: item.server).orEmpty().trim()
            val port = item.port ?: 0
            if (host.isBlank() || port <= 0) return@mapIndexedNotNull null

            val method = item.method?.trim().orEmpty().ifBlank { item.cipher?.trim().orEmpty() }.ifBlank { null }
            val password = item.password?.trim()
            val uuid = item.uuid?.trim()
            val security = item.security?.trim().orEmpty().ifBlank { item.tls?.trim().orEmpty() }.ifBlank { null }
            val inferredType = inferType(item.type, uuid = uuid, method = method, password = password)
            val normalizedName = item.remarks ?: item.name ?: item.address ?: item.server ?: "Node $index"

            NodeEntity(
                id = stableId,
                name = normalizedName,
                type = inferredType,
                host = host,
                port = port,
                uuid = uuid,
                password = password,
                method = method,
                security = security,
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

        val effective = when {
            mapped.isEmpty() -> emptyList()
            mapped.any { it.isSelected } -> mapped
            else -> mapped.mapIndexed { index, node -> node.copy(isSelected = index == 0) }
        }

        nodeDao.clearAll()
        nodeDao.upsertAll(effective)
        effective.firstOrNull { it.isSelected }?.let { connectionPrefs?.setLastSelectedNode(it.id) }
    }

    suspend fun selectNode(nodeId: String) {
        nodeDao.selectNode(nodeId)
        connectionPrefs?.setLastSelectedNode(nodeId)
    }

    suspend fun currentSelectedNode() = nodeDao.getSelectedNode()

    private fun inferType(type: String?, uuid: String?, method: String?, password: String?): String {
        val value = type?.lowercase().orEmpty()
        return when {
            value.contains("shadowsocks") || value == "ss" -> "shadowsocks"
            value.contains("vless") -> "vless"
            !uuid.isNullOrBlank() -> "vless"
            !method.isNullOrBlank() && !password.isNullOrBlank() -> "shadowsocks"
            else -> value.ifBlank { "unknown" }
        }
    }
}
