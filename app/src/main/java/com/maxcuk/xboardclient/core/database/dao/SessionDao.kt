package com.maxcuk.xboardclient.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maxcuk.xboardclient.core.database.entity.SessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE id = 1")
    fun observeSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM session WHERE id = 1")
    suspend fun getSession(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clearSession()
}
