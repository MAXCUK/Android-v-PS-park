package com.maxcuk.xboardclient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "nodes")
data class NodeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val type: String,
    val host: String,
    val port: Int,
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
    val spiderX: String? = null,
    val rawJson: String,
    val lastLatencyMs: Int? = null,
    val isSelected: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)
