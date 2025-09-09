package com.example.expensetracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ExpenseEntity::class], version = 2)
abstract class ExpenseDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
}
