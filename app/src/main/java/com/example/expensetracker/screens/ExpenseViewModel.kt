//@file:OptIn(ExperimentalCoroutinesApi::class)

package com.example.expensetracker.screens

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.utils.states.DBState
import com.example.expensetracker.utils.states.DataState
import com.example.expensetracker.utils.states.EntityUi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    fun syncExpenses() {
        viewModelScope.launch {
            _dataBaseOperationsState.value = DBState.Loading
            _isRefreshing.value = true
            _dataBaseOperationsState.value = repository.getAllExpensesFromRemote()
            _isRefreshing.value = false
        }
    }

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing


    //Only use Browser not X(AI)


    //Deep dive into Live data and types of live data when to use which Flow.[Done]
    //use different clicks for diff calls and item click to add expenses.[Done]
    //how to handle the screen rotation change without using vm.[Done]
    //remove the CKeyboard use system keyboard TF.[Done]
    //instead of ExpenseAddScreen use bottom pop up for that like alert dialog.[Done]
    //keep Logic utils within the screen only.[Done]
    //add drop down to select one category.[Done]


    //Why used Combine and flatMapLatest also alternates
    //add "All" always with categories list even the category is empty.


    private var _currentCategory = MutableStateFlow("All")
    val currentCategory: StateFlow<String> = _currentCategory.asStateFlow()


    @OptIn(ExperimentalCoroutinesApi::class)
    private val _totalSum: Flow<Int?> = currentCategory.flatMapLatest {
        if (it == "All") {
            repository.getTotalAmount()
        } else {
            repository.getTotalForCategory(it)
        }
    }

    val totalSum: Flow<Int?> = _totalSum

    private val _categories: StateFlow<List<String>> =
        repository.getAllCategories().stateIn(viewModelScope, SharingStarted.Eagerly, listOf("All"))

    val categories: StateFlow<List<String>> = _categories

    var currentExpense = mutableStateOf(EntityUi())
        private set

    private val _numberOfExpensesPerCategory = MutableStateFlow<Map<String, Int>>(emptyMap())

    val numberOfExpensesPerCategory: StateFlow<Map<String, Int>> =
        _numberOfExpensesPerCategory.asStateFlow()


    val ranges = MutableStateFlow(Pair(System.currentTimeMillis(), System.currentTimeMillis()))


    private val _query = MutableStateFlow("")

    private val _dataBaseOperationsState: MutableStateFlow<DBState> =
        MutableStateFlow(DBState.Success(""))
    val dataBaseOperationsState: StateFlow<DBState> = _dataBaseOperationsState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DataState> = currentCategory
        .combine(ranges) { category, range -> category to range }
        .combine(_query) { (category, range), query -> Triple(category, range, query) }
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
                        it.description.contains(query) || it.category.contains(query)
                    }
                }

                DataState.Success(filteredList.map { it.toUiEntity() }) as DataState
            }
        }.onStart {
            emit(DataState.Loading)
            delay(1000)
        }
        .flowOn(Dispatchers.IO)
        .catch { emit(DataState.Failure(it.message.orEmpty())) }
        .stateIn(viewModelScope, SharingStarted.Lazily, DataState.Loading)


    fun numberOfExpensesPerCategory() {
        viewModelScope.launch {
            repository.getAllExpenses().collect { it ->
                _numberOfExpensesPerCategory.value =
                    it.groupBy { it.category }
                        .mapValues { (_, list) ->
                            list.size
                        }
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _query.value = query

    }


    fun addCurrentExpense(expense: EntityUi) {
        currentExpense.value = expense
    }


    fun categoryChanged(
        category: String,
        startDate: Long, endDate: Long
    ) {
        viewModelScope.launch {
            ranges.value = Pair(startDate, endDate)
            _currentCategory.value = category
        }

    }

    fun addExpense(expenseEntity: ExpenseEntity) {
        viewModelScope.launch {
            _dataBaseOperationsState.value = DBState.Loading
            try {
                _dataBaseOperationsState.value = repository.insertExpense(expenseEntity)
                currentExpense.value = EntityUi()
            } catch (e: Exception) {
                _dataBaseOperationsState.value =
                    DBState.Failure(e.message ?: "SomeThing Went Wrong")
            }
        }
    }

    fun deleteExpense(expenseEntity: ExpenseEntity) {
        viewModelScope.launch {
            _dataBaseOperationsState.value = DBState.Loading
            try {
                _dataBaseOperationsState.value = repository.deleteExpense(expenseEntity)
            } catch (e: Exception) {
                _dataBaseOperationsState.value =
                    DBState.Failure(e.message ?: "SomeThing Went Wrong")
            }
        }
    }

    fun updateExpense(expense: ExpenseEntity) {
        _dataBaseOperationsState.value = DBState.Loading
        viewModelScope.launch {
            try {
                _dataBaseOperationsState.value = repository.updateExpense(expense)
                currentExpense.value = EntityUi()

            } catch (e: Exception) {
                _dataBaseOperationsState.value =
                    DBState.Failure(e.message ?: "SomeThing Went Wrong")
            }
        }
    }

}
