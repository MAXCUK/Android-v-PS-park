package com.maxcuk.xboardclient.core.repository

import com.maxcuk.xboardclient.core.database.dao.NodeDao
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class NodeSelectionRepository(
    nodeDao: NodeDao
) {
    val selectedNode: Flow<NodeEntity?> = nodeDao.observeNodes()
        .map { list -> list.firstOrNull { it.isSelected } }
}
