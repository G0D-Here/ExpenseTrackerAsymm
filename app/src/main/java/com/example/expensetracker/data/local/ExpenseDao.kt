package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Query("SELECT SUM(amount) FROM expenses")
    fun getTotalAmount(): Flow<Int?>

    @Query("SELECT * FROM expenses WHERE date BETWEEN:startDate AND :endDate ORDER BY date DESC")
    fun getExpensesInDateRanges(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE category = :category")
    fun allExpensesOfCategory(category: String): Flow<List<ExpenseEntity>>

    @Query("SELECT DISTINCT category FROM expenses")
    fun getAllCategories(): Flow<List<String>>

    @Query("SELECT SUM(amount) FROM expenses WHERE category = :category")
    fun getTotalExpenseForCategory(category: String): Flow<Int?>

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>
}