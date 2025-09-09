package com.example.expensetracker.utils.logic

import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.utils.states.EntityUi
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun ExpenseEntity.toUiEntity(): EntityUi {
    return EntityUi(
        id = uid, amount =
        amount, description = description, category = category, date = date
    )
}

fun EntityUi.toExpenseEntity(): ExpenseEntity = ExpenseEntity(
    uid = id,
    amount = amount,
    description = description,
    category = category,
    date = date
)

fun Long.toDateTimeString(pattern: String = "dd/MM/yyyy, hh:mm a"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(
        pattern, Locale.getDefault()
    )
    return formatter.format(date)
}