package com.example.finalyearproject


import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseManager {
    private var instance: AppDatabase? = null

    val MIGRATION_1_2: Migration = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE audioRecords ADD COLUMN duration INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun getDatabase(context: Context): AppDatabase {
        if (instance == null) {
            synchronized(AppDatabase::class) {
                instance = Room.databaseBuilder(context.applicationContext,
                    AppDatabase::class.java, "audioRecords.db")
                    .addMigrations(MIGRATION_1_2)
                    .build()
            }
        }
        return instance!!
    }
}