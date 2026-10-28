package com.example.metrosathi.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.metrosathi.data.model.*

@Database(
    entities = [
        City::class,
        Line::class,
        Station::class,
        StationLine::class,
        Connection::class,
        Interchange::class,
        Gate::class,
        Parking::class,
        Amenity::class,
        NearbyPlace::class,
        Timing::class,
        FareSlab::class,
        Favourite::class,
        TripHistory::class,
        RecentSearch::class,
        CardBalance::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun metroDao(): MetroDao
    abstract fun userDataDao(): UserDataDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "metrosathi_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
