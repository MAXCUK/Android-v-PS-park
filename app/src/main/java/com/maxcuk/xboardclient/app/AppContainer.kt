package com.maxcuk.xboardclient.app

import android.content.Context
import com.maxcuk.xboardclient.core.database.DatabaseFactory
import com.maxcuk.xboardclient.core.datastore.ConnectionPrefs
import com.maxcuk.xboardclient.core.proxy.ProxyRuntimeManager
import com.maxcuk.xboardclient.core.repository.AuthRepository
import com.maxcuk.xboardclient.core.repository.NodeRepository
import com.maxcuk.xboardclient.core.vpn.VpnController

class AppContainer(context: Context) {
    private val database = DatabaseFactory.create(context)
    private val connectionPrefs = ConnectionPrefs(context)
    val authRepository = AuthRepository(database.sessionDao())
    val nodeRepository = NodeRepository(database.nodeDao(), connectionPrefs)
    private val proxyRuntimeManager = ProxyRuntimeManager(context)
    val vpnController = VpnController(context, nodeRepository, proxyRuntimeManager, connectionPrefs)
}
