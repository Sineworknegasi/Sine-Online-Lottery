package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        User::class,
        LotteryDraw::class,
        Ticket::class,
        Transaction::class,
        SavedNotification::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LotteryDatabase : RoomDatabase() {
    abstract val lotteryDao: LotteryDao

    companion object {
        @Volatile
        private var INSTANCE: LotteryDatabase? = null

        fun getDatabase(context: Context): LotteryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LotteryDatabase::class.java,
                    "lottery_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
