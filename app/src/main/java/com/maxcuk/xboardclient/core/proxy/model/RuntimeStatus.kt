package com.maxcuk.xboardclient.core.proxy.model

data class RuntimeStatus(
    val installed: Boolean,
    val binaryPath: String,
    val configPath: String,
    val logPath: String,
    val message: String
)
