package com.maxcuk.xboardclient.core.repository

import com.maxcuk.xboardclient.core.database.dao.SessionDao
import com.maxcuk.xboardclient.core.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

class AuthRepository(
    private val sessionDao: SessionDao
) {
    fun observeSession(): Flow<SessionEntity?> = sessionDao.observeSession()

    suspend fun saveSession(baseUrl: String, email: String, authToken: String) {
        sessionDao.saveSession(
            SessionEntity(
                baseUrl = baseUrl,
                email = email,
                authToken = authToken
            )
        )
    }

    suspend fun currentSession(): SessionEntity? = sessionDao.getSession()

    suspend fun clearSession() {
        sessionDao.clearSession()
    }
}
