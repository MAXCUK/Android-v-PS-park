package com.maxcuk.xboardclient.core.proxy

import android.content.Context
import com.maxcuk.xboardclient.core.proxy.model.RuntimeStatus
import java.io.File

class SingBoxRuntimeBridge(
    private val context: Context
) {
    fun runtimeDir(): File {
        val dir = File(context.filesDir, "singbox-runtime")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun configFile(): File = File(runtimeDir(), "config.json")

    fun logsFile(): File = File(runtimeDir(), "singbox.log")

    fun binaryFileName(): String {
        val abi = android.os.Build.SUPPORTED_ABIS.firstOrNull().orEmpty()
        return when {
            abi.contains("arm64") -> "libsing-box.so"
            abi.contains("armeabi") || abi.contains("arm") -> "libsing-box.so"
            abi.contains("x86_64") -> "libsing-box.so"
            else -> "libsing-box.so"
        }
    }

    fun expectedBinaryPath(): String = "jniLibs/${android.os.Build.SUPPORTED_ABIS.firstOrNull().orEmpty()}/${binaryFileName()}"

    fun installedBinaryFile(): File = File(context.applicationInfo.nativeLibraryDir, binaryFileName())

    fun isRuntimeInstalled(): Boolean = installedBinaryFile().exists()

    fun status(): RuntimeStatus {
        val installed = isRuntimeInstalled()
        return RuntimeStatus(
            installed = installed,
            binaryPath = installedBinaryFile().absolutePath,
            configPath = configFile().absolutePath,
            logPath = logsFile().absolutePath,
            message = if (installed) "sing-box runtime detected" else "sing-box runtime missing"
        )
    }
}
