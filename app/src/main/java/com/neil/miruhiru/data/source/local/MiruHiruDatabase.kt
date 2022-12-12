package com.neil.miruhiru.data.source.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Task

/**
 * Created by Neil Tsai in Dec. 2022.
 */
@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class MiruHiruDatabase : RoomDatabase() {

    abstract val miruHiruDatabaseDao: MiruHiruDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: MiruHiruDatabase? = null

        fun getInstance(context: Context): MiruHiruDatabase {

            synchronized(this) {

                var instance = INSTANCE
                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MiruHiruDatabase::class.java,
                        "miruHiru_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }

                return instance
            }
        }
    }
}