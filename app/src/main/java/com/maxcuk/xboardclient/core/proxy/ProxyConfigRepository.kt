package com.maxcuk.xboardclient.core.proxy

import android.content.Context
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import java.io.File

class ProxyConfigRepository(
    private val context: Context
) {
    fun saveSelectedNodeConfig(node: NodeEntity): File {
        val dir = File(context.filesDir, "singbox")
        if (!dir.exists()) dir.mkdirs()
        val file = File(dir, "config.json")
        file.writeText(SingBoxConfigBuilder.build(node))
        return file
    }
}
