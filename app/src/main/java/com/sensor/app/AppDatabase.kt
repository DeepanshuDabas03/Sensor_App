package com.sensor.app

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [OrientationData::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun orientationDao(): OrientationDao
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase? {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "OrientationData"
                    ).build()
                    INSTANCE = instance
                    instance
                } catch (e: Exception) {
                    // Handle the exception
                    INSTANCE = null // Reset INSTANCE to force retry
                    null // Return null to indicate failure
                }
            }
        }
    }

}