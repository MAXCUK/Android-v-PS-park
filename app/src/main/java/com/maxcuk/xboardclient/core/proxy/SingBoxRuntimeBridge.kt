package com.maxcuk.xboardclient.core.proxy

import android.content.Context
import android.os.Build
import com.maxcuk.xboardclient.core.proxy.model.RuntimeStatus
import java.io.File

class SingBoxRuntimeBridge(
    private val context: Context
) {
    fun runtimeDir(): File = File(context.filesDir, "singbox-runtime").apply { mkdirs() }

    fun configFile(): File = File(runtimeDir(), "config.json")

    fun logsFile(): File = File(runtimeDir(), "singbox.log")

    fun workingDir(): File = File(runtimeDir(), "work").apply { mkdirs() }

    fun supportedAbis(): List<String> = Build.SUPPORTED_ABIS?.toList().orEmpty()

    fun assetRuntimeDir(): String = "singbox"

    fun candidateBinaryNames(): List<String> = listOf(
        "sing-box",
        "singbox",
        "libsing-box.so",
        "libbox.so"
    )

    fun expectedBinaryHints(): List<String> = buildList {
        supportedAbis().forEach { abi ->
            candidateBinaryNames().forEach { name ->
                add("jniLibs/$abi/$name")
            }
        }
        candidateBinaryNames().forEach { name ->
            add("assets/${assetRuntimeDir()}/$name")
        }
    }

    fun installedBinaryFile(): File? {
        val nativeDir = File(context.applicationInfo.nativeLibraryDir.orEmpty())
        if (nativeDir.exists()) {
            candidateBinaryNames().forEach { name ->
                val file = File(nativeDir, name)
                if (file.exists()) return file
            }
        }

        val extractedDir = runtimeDir()
        candidateBinaryNames().forEach { name ->
            val file = File(extractedDir, name)
            if (file.exists()) return file
        }

        return null
    }

    fun ensureRuntimeExecutable(): File? {
        installedBinaryFile()?.let { existing ->
            existing.setExecutable(true, false)
            return existing
        }

        val assetManager = context.assets
        val runtimeDir = runtimeDir()
        candidateBinaryNames().forEach { name ->
            runCatching {
                assetManager.open("${assetRuntimeDir()}/$name").use { input ->
                    val out = File(runtimeDir, name)
                    out.outputStream().use { output -> input.copyTo(output) }
                    out.setExecutable(true, false)
                    return out
                }
            }
        }
        return null
    }

    fun isRuntimeInstalled(): Boolean = ensureRuntimeExecutable() != null

    fun startCommand(config: File = configFile()): List<String>? {
        val binary = ensureRuntimeExecutable() ?: return null
        return listOf(
            binary.absolutePath,
            "run",
            "-c",
            config.absolutePath
        )
    }

    fun status(): RuntimeStatus {
        val binary = installedBinaryFile()
        val installed = binary != null
        return RuntimeStatus(
            installed = installed,
            binaryPath = binary?.absolutePath.orEmpty(),
            configPath = configFile().absolutePath,
            logPath = logsFile().absolutePath,
            message = if (installed) {
                "sing-box runtime ready"
            } else {
                "sing-box runtime missing; place one of ${expectedBinaryHints().joinToString()}"
            }
        )
    }
}
