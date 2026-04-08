package com.maxcuk.xboardclient.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NodeDao {
    @Query("SELECT * FROM nodes ORDER BY isSelected DESC, name ASC")
    fun observeNodes(): Flow<List<NodeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(nodes: List<NodeEntity>)

    @Query("UPDATE nodes SET isSelected = CASE WHEN id = :nodeId THEN 1 ELSE 0 END")
    suspend fun selectNode(nodeId: String)

    @Query("SELECT * FROM nodes WHERE isSelected = 1 LIMIT 1")
    suspend fun getSelectedNode(): NodeEntity?

    @Query("DELETE FROM nodes")
    suspend fun clearAll()
}
