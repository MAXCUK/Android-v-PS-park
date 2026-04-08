package com.maxcuk.xboardclient.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.maxcuk.xboardclient.core.database.dao.NodeDao
import com.maxcuk.xboardclient.core.database.dao.SessionDao
import com.maxcuk.xboardclient.core.database.entity.NodeEntity
import com.maxcuk.xboardclient.core.database.entity.SessionEntity

@Database(
    entities = [NodeEntity::class, SessionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun nodeDao(): NodeDao
    abstract fun sessionDao(): SessionDao
}
