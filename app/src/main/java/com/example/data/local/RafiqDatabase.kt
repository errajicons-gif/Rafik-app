package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AacPictogramEntity::class,
        SavedExpressionEntity::class,
        LearningActivityEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RafiqDatabase : RoomDatabase() {
    abstract fun rafiqDao(): RafiqDao

    companion object {
        @Volatile
        private var INSTANCE: RafiqDatabase? = null

        fun getInstance(context: Context): RafiqDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    RafiqDatabase::class.java,
                    "rafiq_local_db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
