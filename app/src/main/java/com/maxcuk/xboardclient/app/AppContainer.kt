package com.maxcuk.xboardclient.app

import android.content.Context
import com.maxcuk.xboardclient.core.database.DatabaseFactory
import com.maxcuk.xboardclient.core.datastore.ConnectionPrefs
import com.maxcuk.xboardclient.core.proxy.ProxyRuntimeManager
import com.maxcuk.xboardclient.core.repository.AuthRepository
import com.maxcuk.xboardclient.core.repository.NodeRepository
import com.maxcuk.xboardclient.core.vpn.VpnController
import com.maxcuk.xboardclient.core.work.RefreshScheduler

class AppContainer(private val context: Context) {
    private val database = DatabaseFactory.create(context)
    val connectionPrefs = ConnectionPrefs(context)
    val authRepository = AuthRepository(database.sessionDao())
    val nodeRepository = NodeRepository(database.nodeDao(), connectionPrefs)
    private val proxyRuntimeManager = ProxyRuntimeManager(context)
    val vpnController = VpnController(context, nodeRepository, proxyRuntimeManager, connectionPrefs)

    fun setRefreshEnabled(enabled: Boolean) {
        if (enabled) RefreshScheduler.schedule(context) else RefreshScheduler.cancel(context)
    }
}
