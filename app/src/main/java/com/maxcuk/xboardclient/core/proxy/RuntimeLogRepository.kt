package com.maxcuk.xboardclient.core.proxy

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RuntimeLogRepository(
    private val context: Context,
    private val runtimeBridge: SingBoxRuntimeBridge = SingBoxRuntimeBridge(context)
) {
    private val timeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun append(line: String) {
        val file = runtimeBridge.logsFile()
        file.parentFile?.mkdirs()
        file.appendText("[${timeFormat.format(Date())}] $line\n")
    }

    fun readOrNull(): String? {
        val file = runtimeBridge.logsFile()
        return if (file.exists()) file.readText() else null
    }

    fun clear() {
        val file = runtimeBridge.logsFile()
        if (file.exists()) file.delete()
    }
}
