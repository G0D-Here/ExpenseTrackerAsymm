package com.example.expensetracker

import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.remote.MockApi
import com.example.expensetracker.data.remote.remotedto.ExpensesResponsesItem
import com.example.expensetracker.data.repository.ExpenseRepository
import com.example.expensetracker.utils.states.DBState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }
}


class ExpenseRepositoryTest {

    private lateinit var repository: ExpenseRepository
    private val mockDao: ExpenseDao = mock()
    private val mockApi: MockApi = mock()

    @Before
    fun setup() {
        repository = ExpenseRepository(mockDao, mockApi)
    }

    @Test
    fun `insertExpense returns Success when API and DB succeed`(): Unit = runTest {
        val expense = ExpenseEntity(1, "String", 100, "Lunch", "12345L", 4L)
        val response = ExpensesResponsesItem(1, "100", 123456L, "Lunch", "1")

        whenever(mockDao.insertExpense(expense)).thenReturn(1)

        whenever(mockApi.postExpense(any())).thenReturn(response)

        val result = repository.insertExpense(expense)
        assertTrue(result is DBState.Success)
        verify(mockDao).updateRemoteId(1, "1")
    }

    @Test
    fun `insertExpense returns Failure when API throws exception`() = runTest {
        val expense = ExpenseEntity(0, "String", 100, "Lunch", "12645L", 4L)
        whenever(mockDao.insertExpense(expense)).thenReturn(1L)
        whenever(mockApi.postExpense(any())).thenThrow(RuntimeException("API error"))

        val result = repository.insertExpense(expense)

        assertTrue(result is DBState.Failure)
        assertEquals("API error", (result as DBState.Failure).error)
    }
}
