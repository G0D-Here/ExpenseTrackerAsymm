package com.example.expensetracker.utils.states

data class EntityUi(
    val id: Int = 0,
    val amount: Double = 0.0,
    val description: String = "",
    val category: String = "",
    val date: Long = 0,
)