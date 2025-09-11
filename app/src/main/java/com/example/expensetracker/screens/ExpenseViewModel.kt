@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.expensetracker.screens

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.utils.states.DataState
import com.example.expensetracker.utils.states.EntityUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseViewModel @Inject constructor(private val repository: ExpenseRepository) :
    ViewModel() {

    //Only use Browser not X(AI)

    //Deep dive into Live data and types of live data when to use which Flow.[Done]
    //use different clicks for diff calls and item click to add expenses.[Done]
    //how to handle the screen rotation change without using vm.[Done]
    //remove the CKeyboard use system keyboard TF.[Done]
    //instead of ExpenseAddScreen use bottom pop up for that like alert dialog.[Done]
    //keep Logic utils within the screen only.[Done]
    //add drop down to select one category.


    private var _currentCategory = MutableStateFlow("All")
    val currentCategory: Flow<String> = _currentCategory


    private val _totalSum: Flow<Int?> = currentCategory.flatMapLatest {
        if (it == "All") {
            repository.getTotalAmount()
        } else {
            repository.getTotalForCategory(it)
        }
    }

    val totalSum: Flow<Int?> = _totalSum

    private val _categories: StateFlow<List<String>> =
        repository.getAllCategories().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val categories: StateFlow<List<String>> = _categories

    var currentExpense = mutableStateOf(EntityUi())
        private set
//    val currentExpense: State<EntityUi> = _currentExpense

    val ranges = MutableStateFlow(Pair(System.currentTimeMillis(), System.currentTimeMillis()))

    //refreshState: Pending until nextPhase
    private val refreshState: MutableStateFlow<Unit> = MutableStateFlow(Unit)

    private val _query = MutableStateFlow("")

    val uiState: Flow<DataState> = refreshState
        .combine(currentCategory) { _, category -> category }
        .combine(ranges) { category, range -> category to range }
        .combine(this._query) { (category, range), query -> Triple(category, range, query) }
        .flatMapLatest { (category, range, query) ->
            val flow = when (category) {
                "All" -> repository.getAllExpenses()
                "Range" -> repository.getExpensesInDateRanges(range.first, range.second)
                else -> repository.allExpensesOfCategory(category)
            }
            flow.map { list ->
                val filteredList = if (query.isEmpty()) {
                    list
                } else {
                    list.filter {
                        it.description.contains(query, ignoreCase = false)
                    }
                }
                DataState.Success(filteredList.map { it.toUiEntity() })
            }
        }
        .onStart { DataState.Loading }
        .flowOn(Dispatchers.IO)
        .catch { DataState.Failure(it.message.orEmpty()) }
        .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading)


    fun onSearchQueryChanged(query: String) {
        _query.value = query
        Log.d("SearchFunctionality", "Query: $query")
        viewModelScope.launch {
            uiState.collect {
                Log.d("SearchFunctionality", "State: $it")
            }
        }

    }


    fun addCurrentExpense(expense: EntityUi) {
        currentExpense.value = expense
    }


    fun categoryChanged(
        category: String,
        startDate: Long, endDate: Long
    ) {
        viewModelScope.launch {
//            if (category == "All")_query.value = ""
            ranges.value = Pair(startDate, endDate)
            _currentCategory.value = category

            Log.d(
                "ViewModelErrors",
                "Range: $category Start: ${startDate.toDateTimeString()} End: ${endDate.toDateTimeString()}\n\n"
            )


        }

    }

    fun addExpense(expenseEntity: ExpenseEntity) {
        viewModelScope.launch {
            repository.insertExpense(expenseEntity)
            currentExpense.value = EntityUi()

        }
    }

    fun deleteExpense(expenseEntity: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expenseEntity)
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.updateExpense(expense)
            currentExpense.value = EntityUi()
        }
    }


}
