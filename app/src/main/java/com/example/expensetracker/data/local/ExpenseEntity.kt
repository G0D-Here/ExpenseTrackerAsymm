package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val uid: Int,
    val amount: Double,
    val description: String,
    val category: String,
    val date: Long,
)