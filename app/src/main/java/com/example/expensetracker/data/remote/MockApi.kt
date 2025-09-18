package com.example.expensetracker.data.remote

import com.example.expensetracker.data.remote.remotedto.ExpensesResponses
import com.example.expensetracker.data.remote.remotedto.ExpensesResponsesItem
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface MockApi {
    @GET("expenses")
    suspend fun getExpenses(): ExpensesResponses

    @POST("expenses")
    suspend fun postExpense(@Body expense: ExpensesResponsesItem): ExpensesResponsesItem

    @DELETE("expenses/{id}")
    suspend fun deleteExpense(@Path("id") id: String)

    @PUT("expenses/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body expense: ExpensesResponsesItem
    )
}