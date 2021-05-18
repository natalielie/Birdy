package com.example.birdyapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(
    entities = [OfflineBirdsModel::class],
    version = 1
)
abstract class BirdsDatabase : RoomDatabase() {
    abstract fun offlineBirdsInfo() : BirdsDao

    companion object{
        @Volatile
        private var instance: BirdsDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context)
        }
        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                BirdsDatabase::class.java, "birds.db"
            )
                .build()
    }
}