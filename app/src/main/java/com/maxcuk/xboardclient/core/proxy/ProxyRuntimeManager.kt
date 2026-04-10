package com.maxcuk.xboardclient.core.proxy

import android.content.Context
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import com.maxcuk.xboardclient.core.proxy.model.RuntimeStatus
import java.io.File

class ProxyRuntimeManager(
    private val context: Context,
    private val configRepository: ProxyConfigRepository = ProxyConfigRepository(context),
    private val runtimeBridge: SingBoxRuntimeBridge = SingBoxRuntimeBridge(context),
    private val runtimeLogRepository: RuntimeLogRepository = RuntimeLogRepository(context)
) {
    fun prepare(node: NodeEntity): File {
        runtimeLogRepository.clear()
        runtimeLogRepository.append("prepare runtime for node=${node.name} type=${node.type}")
        val config = configRepository.saveSelectedNodeConfig(node)
        val runtimeConfig = runtimeBridge.configFile()
        runtimeConfig.parentFile?.mkdirs()
        config.copyTo(runtimeConfig, overwrite = true)
        return runtimeConfig
    }

    fun runtimeInstalled(): Boolean = true

    fun runtimeStatus(): RuntimeStatus = runtimeBridge.status().copy(message = "libbox service ready")

    fun runtimeInfo(): String = "已内置"

    fun start(configFile: File = runtimeBridge.configFile()): Boolean {
        runtimeLogRepository.append("service mode start requested config=${configFile.absolutePath}")
        return configFile.exists()
    }

    fun stop(): Boolean {
        runtimeLogRepository.append("service mode stop requested")
        return true
    }

    fun isRunning(): Boolean = false

    fun latestLogs(): String? = runtimeLogRepository.readOrNull()
}
