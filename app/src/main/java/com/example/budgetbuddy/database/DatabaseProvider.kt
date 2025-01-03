package com.example.budgetbuddy.database

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        // Return the existing instance or create a new one if it doesn't exist
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "expense_database"  // This is the name of the database file
            ).build()
            INSTANCE = instance
            instance
        }
    }
}
