package com.example.expensetracker.screens

import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.utils.logic.toUiEntity
import com.example.expensetracker.utils.states.EntityUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(private val repository: ExpenseRepository) :
    ViewModel() {

    val currentExpense = MutableLiveData(EntityUi())

    val currentCategory = MutableLiveData("All")

    private val _totalSum: LiveData<Double?> = currentCategory.switchMap { category ->
        if (category == "All") repository.getTotalAmount()
        else repository.getTotalForCategory(category)
    }
    val totalSum: LiveData<Double?> = _totalSum

    val categories: LiveData<List<String>> = repository.getAllCategories()


    val filteredExpenses: LiveData<List<EntityUi>> = currentCategory.switchMap { category ->
        if (category == "All") {
            repository.getAllExpenses().map { it -> it.map { it.toUiEntity() } }
        } else {
            repository.allExpensesOfCategory(category).map { it -> it.map { it.toUiEntity() } }
        }
    }
    val addScreen = MutableLiveData(false)


    fun addCurrentExpense(expense: EntityUi) {
        currentExpense.value = expense
    }

    fun addScreenOpener() {
        viewModelScope.launch {
            addScreen.value = !addScreen.value!!
            if (!addScreen.value!!) addCurrentExpense(
                EntityUi()
            )
        }
    }


    fun categoryChanged(category: String) {
        viewModelScope.launch {
            currentCategory.value = category

        }
    }

    fun addExpense(expenseEntity: ExpenseEntity) {
        viewModelScope.launch {
            repository.insertExpense(expenseEntity)
        }
    }

    fun deleteExpense(expenseEntity: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expenseEntity)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch { repository.updateExpense(expense) }
    }
}
