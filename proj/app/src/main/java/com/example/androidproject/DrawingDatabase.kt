package com.example.androidproject

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database
    (
            entities = [DrawingData::class],
            version = 1,
            exportSchema = false
            )

@TypeConverters(Converters::class)
abstract class DrawingDatabase : RoomDatabase() {
    abstract fun drawingInfoDao(): DrawingDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: DrawingDatabase? = null

        fun getDatabase(context: Context): DrawingDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DrawingDatabase::class.java,
                    "drawing_info_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }
}