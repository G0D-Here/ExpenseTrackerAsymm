package com.example.expensetracker.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalAmount(): LiveData<Double?>

    @Query("SELECT * FROM expenses WHERE category = :category")
    fun allExpensesOfCategory(category: String): LiveData<List<ExpenseEntity>>

    @Query("SELECT DISTINCT category FROM expenses")
    fun getAllCategories(): LiveData<List<String>>

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    fun getTotalExpenseForCategory(category: String): LiveData<Double?>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): LiveData<List<ExpenseEntity>>
}