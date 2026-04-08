package com.maxcuk.xboardclient.core.model

data class NodeProfile(
    val id: String,
    val name: String,
    val type: String,
    val host: String,
    val port: Int
)
