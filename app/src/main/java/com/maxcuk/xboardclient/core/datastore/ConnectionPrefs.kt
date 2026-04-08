package com.maxcuk.xboardclient.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "connection_prefs")

class ConnectionPrefs(private val context: Context) {
    private val lastSelectedNodeKey = stringPreferencesKey("last_selected_node")
    private val autoReconnectKey = booleanPreferencesKey("auto_reconnect")
    private val lastConnectedKey = booleanPreferencesKey("last_connected")

    val lastSelectedNode: Flow<String?> = context.dataStore.data.map { it[lastSelectedNodeKey] }
    val autoReconnect: Flow<Boolean> = context.dataStore.data.map { it[autoReconnectKey] ?: true }
    val lastConnected: Flow<Boolean> = context.dataStore.data.map { it[lastConnectedKey] ?: false }

    suspend fun setLastSelectedNode(nodeId: String) {
        context.dataStore.edit { prefs ->
            prefs[lastSelectedNodeKey] = nodeId
        }
    }

    suspend fun setAutoReconnect(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[autoReconnectKey] = enabled
        }
    }

    suspend fun setLastConnected(connected: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[lastConnectedKey] = connected
        }
    }

    suspend fun shouldAutoReconnect(): Boolean {
        return autoReconnect.firstOrNull() ?: true
    }

    suspend fun wasLastConnected(): Boolean {
        return lastConnected.firstOrNull() ?: false
    }
}
