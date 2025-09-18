package com.example.expensetracker.data.repository

import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.remote.MockApi
import com.example.expensetracker.data.remote.remotedto.ExpensesResponsesItem
import com.example.expensetracker.utils.states.DBState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val expenseDao: ExpenseDao,
    private val mockApi: MockApi
) {
    suspend fun insertExpense(expenseEntity: ExpenseEntity): DBState = withContext(Dispatchers.IO) {
        try {
            val dbExpenseId = expenseDao.insertExpense(expenseEntity)
            val remoteResponse = mockApi.postExpense(expenseEntity.toDto())
            remoteResponse.id?.let { remoteId ->
                expenseDao.updateRemoteId(
                    dbExpenseId.toInt(),
                    remoteId
                )
                DBState.Success("Added")
            } ?: DBState.Failure("No Remote Id")
        } catch (e: Exception) {
            DBState.Failure(e.message.toString())
        }
    }


    suspend fun getAllExpensesFromRemote(): DBState = withContext(Dispatchers.IO) {
        try {
            val remoteExpenses = mockApi.getExpenses()
            val entities = remoteExpenses.map { it.toExpenseEntity() }
            expenseDao.clearAll() //checking purpose onlyyy
            expenseDao.insertAll(entities)
            DBState.Success("Synced")
        } catch (e: Exception) {
            DBState.Failure(e.message.toString())
        }

    }


    suspend fun deleteExpense(expenseEntity: ExpenseEntity): DBState = withContext(Dispatchers.IO) {
        expenseDao.deleteExpense(expenseEntity)
        try {
            expenseEntity.remoteId?.let {
                mockApi.deleteExpense(it)
                DBState.Success("Expense Deleted")
            } ?: DBState.Failure("No Remote Id")
        } catch (e: Exception) {
            DBState.Failure(e.message.toString())

        }
    }

    suspend fun updateExpense(expenseEntity: ExpenseEntity): DBState = withContext(Dispatchers.IO) {
        try {
            expenseEntity.remoteId?.let {
                expenseDao.updateExpense(expenseEntity)
                mockApi.updateExpense(
                    expenseEntity.remoteId,
                    expenseEntity.toDto()
                )
                DBState.Success("Updated")
            } ?: DBState.Failure("No Remote Id")

        } catch (e: Exception) {
            DBState.Failure(e.message.toString())
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
//
//fun List<ExpenseEntity>.toListOfResponses(): List<ExpensesResponsesItem> {
//    return this.forEach { expenseEntity ->
//        ExpensesResponsesItem(
//            amount = expenseEntity.amount,
//            category = expenseEntity.category,
//            date = expenseEntity.date,
//            description = expenseEntity.description,
//            id = expenseEntity.remoteId,
//
//        )
//    }
//}

