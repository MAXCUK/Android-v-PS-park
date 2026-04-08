package com.maxcuk.xboardclient.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val baseUrl: String,
    val email: String,
    val authToken: String,
    val loggedInAt: Long = System.currentTimeMillis()
)
