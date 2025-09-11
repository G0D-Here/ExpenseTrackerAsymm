package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.ExpenseEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao
) {
    suspend fun insertExpense(expenseEntity: ExpenseEntity) =
        expenseDao.insertExpense(expenseEntity)

    suspend fun deleteExpense(expenseEntity: ExpenseEntity) =
        expenseDao.deleteExpense(expenseEntity)

    suspend fun updateExpense(expenseEntity: ExpenseEntity) =
        expenseDao.updateExpense(expenseEntity)

    fun getTotalAmount(): Flow<Int?> = expenseDao.getTotalAmount()

    fun getExpensesInDateRanges(startDate: Long, endDate: Long): Flow<List<ExpenseEntity>> =
        expenseDao.getExpensesInDateRanges(startDate, endDate)

    fun allExpensesOfCategory(category: String): Flow<List<ExpenseEntity>> =
        expenseDao.allExpensesOfCategory(category)

    fun getAllCategories(): Flow<List<String>> = expenseDao.getAllCategories()

    fun getTotalForCategory(category: String): Flow<Int?> =
        expenseDao.getTotalExpenseForCategory(category)

    fun getAllExpenses(): Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
}