package com.example.expensetracker.data.remote.remotedto

data class ExpensesResponsesItem(
    val amount: Int,
    val category: String,
    val date: Long,
    val description: String,
    val id: String? = null
)