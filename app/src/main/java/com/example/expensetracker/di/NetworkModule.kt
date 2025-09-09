package com.example.expensetracker.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.expensetracker.data.local.ExpenseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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

    @Provides
    @Singleton
    fun provideDataBase(@ApplicationContext context: Context): ExpenseDatabase =
        Room.databaseBuilder(
            context,
            ExpenseDatabase::class.java,
            "expense_database"
        ).addMigrations(Migration_1_2)
            .build()

    @Provides
    @Singleton
    fun provideExpenseDao(expenseDatabase: ExpenseDatabase) = expenseDatabase.expenseDao()

}