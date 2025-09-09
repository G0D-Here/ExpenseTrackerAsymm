package com.example.expensetracker.data.repository

import androidx.lifecycle.LiveData
import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.ExpenseEntity
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

    fun getTotalAmount():LiveData<Int?> = expenseDao.getTotalAmount()

    fun allExpensesOfCategory(category: String): LiveData<List<ExpenseEntity>> =
        expenseDao.allExpensesOfCategory(category)

    fun getAllCategories(): LiveData<List<String>> = expenseDao.getAllCategories()

    fun getTotalForCategory(category: String): LiveData<Int?> =
        expenseDao.getTotalExpenseForCategory(category)

    fun getAllExpenses(): LiveData<List<ExpenseEntity>> = expenseDao.getAllExpenses()
}