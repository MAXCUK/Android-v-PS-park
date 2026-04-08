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
        runtimeLogRepository.append("prepare runtime for node=${node.name} type=${node.type}")
        return configRepository.saveSelectedNodeConfig(node)
    }

    fun runtimeInstalled(): Boolean = runtimeBridge.isRuntimeInstalled()

    fun runtimeStatus(): RuntimeStatus = runtimeBridge.status()

    fun runtimeInfo(): String {
        val status = runtimeStatus()
        return if (status.installed) {
            "已安装"
        } else {
            "缺少运行时"
        }
    }

    fun startPlaceholder(): Boolean {
        runtimeLogRepository.append("start requested")
        val installed = runtimeInstalled()
        runtimeLogRepository.append(if (installed) "runtime detected" else "runtime missing")
        return installed
    }

    fun stopPlaceholder(): Boolean {
        runtimeLogRepository.append("stop requested")
        return true
    }

    fun latestLogs(): String? = runtimeLogRepository.readOrNull()
}
