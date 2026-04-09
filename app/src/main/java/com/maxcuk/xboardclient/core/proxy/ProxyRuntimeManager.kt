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
    private var process: Process? = null

    fun prepare(node: NodeEntity): File {
        runtimeLogRepository.append("prepare runtime for node=${node.name} type=${node.type}")
        val config = configRepository.saveSelectedNodeConfig(node)
        val runtimeConfig = runtimeBridge.configFile()
        runtimeConfig.parentFile?.mkdirs()
        config.copyTo(runtimeConfig, overwrite = true)
        return runtimeConfig
    }

    fun runtimeInstalled(): Boolean = runtimeBridge.isRuntimeInstalled()

    fun runtimeStatus(): RuntimeStatus = runtimeBridge.status()

    fun runtimeInfo(): String {
        val status = runtimeStatus()
        return if (status.installed) "已安装" else "缺少运行时"
    }

    @Synchronized
    fun start(configFile: File = runtimeBridge.configFile()): Boolean {
        runtimeLogRepository.append("start requested config=${configFile.absolutePath}")
        if (process?.isAlive == true) {
            runtimeLogRepository.append("runtime already running")
            return true
        }

        val command = runtimeBridge.startCommand(configFile)
        if (command == null) {
            runtimeLogRepository.append("runtime missing: ${runtimeBridge.expectedBinaryHints().joinToString()}")
            return false
        }

        return runCatching {
            val logFile = runtimeBridge.logsFile().apply { parentFile?.mkdirs() }
            val builder = ProcessBuilder(command)
                .directory(runtimeBridge.workingDir())
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.appendTo(logFile))
            process = builder.start()
            runtimeLogRepository.append("runtime started cmd=${command.joinToString(" ")}")
            true
        }.getOrElse {
            runtimeLogRepository.append("runtime start failed: ${it.message}")
            false
        }
    }

    @Synchronized
    fun stop(): Boolean {
        runtimeLogRepository.append("stop requested")
        val running = process
        process = null
        if (running == null) return true
        return runCatching {
            running.destroy()
            true
        }.getOrElse {
            runtimeLogRepository.append("runtime stop failed: ${it.message}")
            false
        }
    }

    fun isRunning(): Boolean = process?.isAlive == true

    fun latestLogs(): String? = runtimeLogRepository.readOrNull()
}
