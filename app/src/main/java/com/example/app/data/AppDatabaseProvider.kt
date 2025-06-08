package com.example.app.data

import android.content.Context
import androidx.room.Room

object AppDatabaseProvider {
    @Volatile
    private var db: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return db ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "reiks_db"
            ).build()
            db = instance
            instance
        }
    }
}
