package com.example.expensetracker.data.repository

import android.util.Log
import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.remote.MockApi
import com.example.expensetracker.data.remote.remotedto.ExpensesResponsesItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val mockApi: MockApi
) {
    suspend fun insertExpense(expenseEntity: ExpenseEntity) {
        val dbExpenseId = expenseDao.insertExpense(expenseEntity)
        try {
            val remoteResponse = mockApi.postExpense(expenseEntity.toDto())
            remoteResponse.id?.let { remoteId ->
                expenseDao.updateRemoteId(
                    dbExpenseId.toInt(),
                    remoteId
                )
                Log.d("ErrorInDeletingExpense", "Remote saved with id=$remoteId $dbExpenseId")
            }
        } catch (e: Exception) {
            Log.d("ErrorInDeletingExpense", e.message.toString())

        }
    }


    suspend fun getAllExpensesFromRemote() {
        try {
            val remoteExpenses = mockApi.getExpenses()
            val entities = remoteExpenses.map { it.toExpenseEntity() }
            expenseDao.clearAll()
            expenseDao.insertAll(entities)
        } catch (e: Exception) {
            Log.e("ExpenseRepo", "Sync failed: ${e.message}")
        }
    }


    suspend fun deleteExpense(expenseEntity: ExpenseEntity) {
        expenseDao.deleteExpense(expenseEntity)
        Log.d("ErrorInDeletingExpense", "(DELETE) remoteId = ${expenseEntity.remoteId}")
        Log.d("ErrorInDeletingExpense", "(DELETE) remoteId = $expenseEntity")

        expenseEntity.remoteId?.let {
            try {
                mockApi.deleteExpense(it)
                Log.d("ErrorInDeletingExpense", "(DELETE) Deleted from remote")
            } catch (e: Exception) {
                Log.d("ErrorInDeletingExpense", e.message.toString())
            }
        }
    }

    suspend fun updateExpense(expenseEntity: ExpenseEntity) =
        expenseEntity.remoteId?.let {
            try {
                expenseDao.updateExpense(expenseEntity)
                mockApi.updateExpense(
                    expenseEntity.remoteId,
                    expenseEntity.toDto()
                )
            } catch (e: Exception) {
                Log.d("ErrorInDeletingExpense", "$e")


            }

        }

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

fun ExpensesResponsesItem.toExpenseEntity(): ExpenseEntity = ExpenseEntity(
    remoteId = id,
    amount = amount,
    description = description,
    category = category,
    date = date
)

fun ExpenseEntity.toDto(): ExpensesResponsesItem = ExpensesResponsesItem(
    amount = amount,
    category = category,
    date = date,
    description = description,
)

