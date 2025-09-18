package com.example.expensetracker.utils.states

sealed class DBState {
    data class Failure(val error: String) : DBState()
    data class Success(val data: String) : DBState()
    data object Loading : DBState()
}
