package com.maxcuk.xboardclient.core.proxy

import android.content.Context
import android.os.Build
import com.maxcuk.xboardclient.core.proxy.model.RuntimeStatus
import java.io.File
import java.util.zip.ZipFile

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
        findBundledNativeBinary()?.let { return it }

        val extractedDir = runtimeDir()
        candidateBinaryNames().forEach { name ->
            val file = File(extractedDir, name)
            if (file.exists()) return file
        }

        return null
    }

    private fun findBundledNativeBinary(): File? {
        val nativeDir = File(context.applicationInfo.nativeLibraryDir.orEmpty())
        if (nativeDir.exists()) {
            candidateBinaryNames().forEach { name ->
                val file = File(nativeDir, name)
                if (file.exists()) return file
            }
        }
        return null
    }

    private fun extractBinaryFromAssets(): File? {
        val assetManager = context.assets
        val targetDir = runtimeDir()
        candidateBinaryNames().forEach { name ->
            runCatching {
                assetManager.open("${assetRuntimeDir()}/$name").use { input ->
                    val out = File(targetDir, name)
                    out.outputStream().use { output -> input.copyTo(output) }
                    out.setExecutable(true, false)
                    return out
                }
            }
        }
        return null
    }

    private fun extractBinaryFromApkLibs(): File? {
        val apkPaths = buildList {
            context.applicationInfo.sourceDir?.takeIf { it.isNotBlank() }?.let(::add)
            context.applicationInfo.splitSourceDirs?.filter { !it.isNullOrBlank() }?.let { addAll(it) }
        }

        val targetDir = runtimeDir()
        apkPaths.forEach { apkPath ->
            runCatching {
                ZipFile(apkPath).use { zip ->
                    supportedAbis().forEach { abi ->
                        candidateBinaryNames().forEach { name ->
                            val entry = zip.getEntry("lib/$abi/$name") ?: return@forEach
                            val out = File(targetDir, name)
                            zip.getInputStream(entry).use { input ->
                                out.outputStream().use { output -> input.copyTo(output) }
                            }
                            out.setExecutable(true, false)
                            return out
                        }
                    }
                }
            }
        }
        return null
    }

    fun ensureRuntimeExecutable(): File? {
        installedBinaryFile()?.let { existing ->
            existing.setExecutable(true, false)
            return existing
        }

        extractBinaryFromAssets()?.let { return it }
        extractBinaryFromApkLibs()?.let { return it }

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
        val binary = installedBinaryFile() ?: ensureRuntimeExecutable()
        val installed = binary != null
        val nativeDir = File(context.applicationInfo.nativeLibraryDir.orEmpty())
        val nativeEntries = nativeDir.takeIf { it.exists() }?.list()?.sorted()?.joinToString() ?: "(empty)"
        return RuntimeStatus(
            installed = installed,
            binaryPath = binary?.absolutePath.orEmpty(),
            configPath = configFile().absolutePath,
            logPath = logsFile().absolutePath,
            message = if (installed) {
                "sing-box runtime ready"
            } else {
                "sing-box runtime missing; nativeDir=${nativeDir.absolutePath}; entries=$nativeEntries; expected=${expectedBinaryHints().joinToString()}"
            }
        )
    }
}
