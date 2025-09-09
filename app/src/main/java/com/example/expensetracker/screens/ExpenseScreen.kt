package com.example.expensetracker.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.utils.logic.toDateTimeString
import com.example.expensetracker.utils.logic.toExpenseEntity
import com.example.expensetracker.utils.states.EntityUi
import java.util.Locale

@Composable
fun ExpenseScreen(modifier: Modifier = Modifier, viewModel: ExpenseViewModel = hiltViewModel()) {


    SuccessScreen(modifier = modifier,
        currentExpense = viewModel.currentExpense.observeAsState().value?: EntityUi(),
        sum = viewModel.totalSum.observeAsState().value ?: 0.0,
        addScreenState = viewModel.addScreen.observeAsState().value ?: false,
        currentCategory = viewModel.currentCategory.observeAsState().value ?: "All",
        list = viewModel.filteredExpenses.observeAsState().value ?: emptyList(),
        listCategories = viewModel.categories.observeAsState().value ?: emptyList(),
        onAddClick = {
            viewModel.addExpense(it)
            viewModel.addCurrentExpense(EntityUi(0, 0.0, "", "", 0))
        },
        onDeletePress = { expense ->
            viewModel.deleteExpense(expense)
        },
        onCategoryChange = { category ->
            viewModel.categoryChanged(category)
        }, addScreenStateChanger = { viewModel.addScreenOpener() },
        onEditClick = { viewModel.addCurrentExpense(it) },
        onUpdateClick = {
            viewModel.updateExpense(it)
        }
    )
}


@Composable
fun SuccessScreen(
    modifier: Modifier,
    currentExpense: EntityUi,
    sum: Double,
    addScreenState: Boolean,
    currentCategory: String,
    list: List<EntityUi>,
    listCategories: List<String>,
    onAddClick: (ExpenseEntity) -> Unit,
    onDeletePress: (ExpenseEntity) -> Unit,
    onCategoryChange: (String) -> Unit,
    addScreenStateChanger: () -> Unit,
    onEditClick: (expense: EntityUi) -> Unit,
    onUpdateClick: (expense: ExpenseEntity) -> Unit
) {
    Box(
        modifier
            .fillMaxSize()
            .background(Color(0xFFE5DFDF))
    ) {
        Column(
            Modifier
                .padding(10.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                "Expense Tracker",
                Modifier.padding(4.dp),
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontWeight = FontWeight.W500,
                color = Color.Black
            )

            LazyRow(
                Modifier
                    .fillMaxWidth(),
            ) {
                item {
                    Button(
                        onClick = { onCategoryChange("All") },
                        Modifier
                            .padding(2.dp)
                            .background(Color.Transparent),
                        colors = ButtonDefaults.buttonColors(CustomBlue)
                    ) { Text("All", color = Color.White) }
                }
                items(listCategories) {
                    OutlinedButton(
                        onClick = { onCategoryChange(it) },
                        Modifier.padding(2.dp),
                        colors = ButtonDefaults.textButtonColors(
                        ),
                        border = BorderStroke(
                            1.dp, if (currentCategory == it) Color.Black else
                                Color.Transparent
                        )
                    ) {
                        Text(
                            it,
                            color = Color.Black,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = MaterialTheme.typography.bodyLarge.fontSize
                        )
                    }
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    "Rs. $sum",
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    color = Color.Black,
                    fontWeight = FontWeight.W500
                )
            }

            if (list.isEmpty()) {
                Text(
                    "Save your first Expense",
                    Modifier.align(Alignment.CenterHorizontally),
                    color = Color.Black
                )
                onCategoryChange("All")
            } else {
                LazyColumn(
                    Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .background(Color.White),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)

                ) {
                    items(list) { expense ->
                        ExpenseCard(expense, onLong = {
                            onDeletePress(it.toExpenseEntity())
                        }, onClickEdit = {
                            onEditClick(it)
                            addScreenStateChanger()
                        })
                    }
                }
            }
        }

        AnimatedVisibility(
            addScreenState, Modifier
                .wrapContentSize()
                .align(Alignment.Center),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            ExpenseAddScreen(expense = currentExpense, onAddClick = { expenseEntity ->
                onAddClick(expenseEntity)
                addScreenStateChanger()
            }) {
                onUpdateClick(it)
                addScreenStateChanger()
            }
        }
        FloatingActionButton(
            onClick = {
                addScreenStateChanger()

            },
            Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 20.dp)
                .clip(CircleShape)
                .border(2.dp, CustomBlue, CircleShape),
            elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation(0.dp),
            containerColor = Color.Transparent
        ) {
            Icon(
                if (addScreenState) Icons.Filled.Close else Icons.Filled.Add,
                "",
                tint = CustomBlue
            )
        }
    }
}

@Composable
fun ExpenseAddScreen(
    expense: EntityUi = EntityUi(0, 0.0, "", "", 0),
    onAddClick: (ExpenseEntity) -> Unit, onUpdateClick: (expense: ExpenseEntity) -> Unit
) {
    val context = LocalContext.current


    var amount by remember { mutableStateOf("${expense.amount}") }
    var category by remember { mutableStateOf(expense.category) }
    var description by remember { mutableStateOf(expense.description) }
    Box(
        Modifier,
    ) {
        Column(
            Modifier
                .padding(10.dp)
                .clip(RoundedCornerShape(14.dp))
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                if (expense.id == 0)
                    "New Expense"
                else "Update Expense",
                Modifier.padding(10.dp),
                color = Color.Black,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize
            )

            CustomKeyBoard(
                if (amount == 0.0.toString()) "" else amount,
                placeHolder = "enter amount",
                keyboardType = KeyboardType.Number
            ) {
                amount = if (it.toDoubleOrNull() != null || it.isEmpty()) it else amount
            }
            CustomKeyBoard(
                category,
                placeHolder = "enter category",
                keyboardType = KeyboardType.Text
            ) {
                category = it
            }
            CustomKeyBoard(
                description,
                placeHolder = "enter description",
                keyboardType = KeyboardType.Text
            ) {
                description = it
            }

            Button(
                onClick = {
                    if (
                        expense.id != 0 && amount.isNotEmpty() && amount.toDouble() > 0.0 && category.isNotEmpty() && description.isNotEmpty()
                    ) onUpdateClick(
                        ExpenseEntity(
                            uid = expense.id,
                            amount = amount.toDouble(),
                            category = category.lowercase(Locale.ROOT).trim(),
                            description = description,
                            date = System.currentTimeMillis()
                        )
                    )
                    else if (
                        expense.id == 0 && amount.isNotEmpty() && amount.toDouble() > 0.0 && category.isNotEmpty() && description.isNotEmpty())
                        onAddClick(
                            ExpenseEntity(
                                uid = 0,
                                amount = amount.toDouble(),
                                category = category.lowercase(Locale.ROOT).trim(),
                                description = description,
                                date = System.currentTimeMillis()
                            )
                        )
                    else Toast.makeText(context, "Recheck Fields", Toast.LENGTH_SHORT)
                        .show()
                }
            ) {
                Text(
                    if (expense.id == 0)
                        "Save"
                    else "Update"
                )
            }
        }
    }
}

@Composable
fun CustomKeyBoard(
    text: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    placeHolder: String = "",

    onValueChange: (String) -> Unit
) {

    TextField(
        value = text,
        onValueChange = { onValueChange(it) },
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(14.dp))
            .fillMaxWidth(),
        placeholder = { Text(placeHolder) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        )
    )
}

@Composable
fun ExpenseCard(
    expense: EntityUi,
    onLong: (expense: EntityUi) -> Unit,
    onClickEdit: (expense: EntityUi) -> Unit = {}
) {
    Box(Modifier.padding(2.dp)) {
        Card(
            Modifier
                .fillMaxWidth()
                .clickable {
                    onClickEdit(expense)
                },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.elevatedCardColors(Color(0xFFFFFFFF))
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {

                    Text(
                        expense.category,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = Color.Black,
                        fontWeight = FontWeight.W500
                    )
                    Text(
                        "Rs. " + expense.amount.toString(),
                        fontSize = MaterialTheme.typography.titleMedium.fontSize,
                        color = Color.Black,
                        fontWeight = FontWeight.W500
                    )
                }
                Text(
                    text = expense.description,
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    color = Color.Gray,
                    fontWeight = FontWeight.W500
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        expense.date.toDateTimeString(),
                        fontSize = 11.sp,
                        color = Color(0xFF9F3E3E)
                    )
                    Icon(Icons.Filled.Delete, "Delete", Modifier.clickable {
                        onLong(expense)
                    }, tint = Color.Red)
                }
            }
        }
    }
    HorizontalDivider(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    )
}

val CustomBlue = Color(0xFF0075D2)


