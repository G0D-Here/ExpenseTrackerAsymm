package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.local.ExpenseDatabase
import com.example.expensetracker.data.remote.MockApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private val Migration_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE new_expenses(
                uid INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                amount INTEGER NOT NULL,
                description TEXT NOT NULL,
                category TEXT NOT NULL,
                date INTEGER NOT NULL
                )
            """.trimIndent()
            )

            db.execSQL(
                """
                    INSERT INTO new_expenses (uid, amount, description, category, date)
                    SELECT uid, CAST(amount AS INTEGER) AS amount, description, category, date FROM expenses
                """.trimIndent()
            )

            db.execSQL("DROP TABLE expenses")
            db.execSQL("ALTER TABLE new_expenses RENAME TO expenses")
        }
    }

    private val Migration_2_3 = object : Migration(2, 3) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE expenses ADD COLUMN remoteId TEXT")
        }
    }

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit =
        Retrofit.Builder().baseUrl("https://68c58825a712aaca2b6907e2.mockapi.io/expenses/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideMockApi(retrofit: Retrofit): MockApi = retrofit.create(MockApi::class.java)

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context): ExpenseDatabase =
        Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            "expense_database"
        ).addMigrations(Migration_1_2, Migration_2_3).build()

    @Provides
    @Singleton
    fun provideExpenseDao(expenseDatabase: ExpenseDatabase) = expenseDatabase.expenseDao()


}