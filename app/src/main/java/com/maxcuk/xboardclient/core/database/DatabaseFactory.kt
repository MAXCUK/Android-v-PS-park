package com.maxcuk.xboardclient.core.database

import android.content.Context
import androidx.room.Room

object DatabaseFactory {
    fun create(context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "xboard_client.db"
        ).fallbackToDestructiveMigration().build()
    }
}
