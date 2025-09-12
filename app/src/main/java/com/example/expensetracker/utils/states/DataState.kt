package com.example.expensetracker.utils.states

sealed class DataState {
    data class Failure(val error: String) : DataState()
    data class Success(val data: List<EntityUi>) : DataState()
    data object Loading : DataState()
}
