package com.example.expensetracker.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.utils.states.DataState
import com.example.expensetracker.utils.states.EntityUi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Data(val name: String, val age: Int)

@Composable
fun ExpenseScreen(modifier: Modifier = Modifier, viewModel: ExpenseViewModel = hiltViewModel()) {

    val currentExpense by viewModel.currentExpense
    val sum by viewModel.totalSum.collectAsState(0)
    val currentCategory by viewModel.currentCategory.collectAsState("All")
    val filteredExpenses = viewModel.uiState.collectAsState(emptyList<DataState.Loading>()).value
    val categories by viewModel.categories.collectAsState(emptyList())

    when (filteredExpenses) {
        is DataState.Failure -> {
            Toast.makeText(LocalContext.current, filteredExpenses.error, Toast.LENGTH_SHORT)
                .show()
        }

        DataState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        is DataState.Success -> {
            SuccessScreen(modifier = modifier,
                currentExpense = currentExpense,
                sum = sum ?: 0,
                currentCategory = currentCategory,
                list = filteredExpenses.data,
                listCategories = categories,
                onAddClick = {
                    viewModel.addExpense(it)
                },
                onDeletePress = { expense ->
                    viewModel.deleteExpense(expense)
                },
                onCategoryChange = { category, s, e ->
                    viewModel.categoryChanged(category, s, e)
                },
                onEditClick = { viewModel.addCurrentExpense(it) },
                rangeSet = viewModel.ranges.collectAsState().value,
                resetCurrentExpense = { viewModel.addCurrentExpense(EntityUi()) },
                onSearch = { viewModel.onSearchQueryChanged(it) },
                onUpdateClick = {
                    viewModel.updateExpense(it)
                }
            )
        }
    }

}

//Only for Learning
@Composable
fun BarChart(
    data: List<Data>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF0075D2),
    textColor: Color = Color.Black,
    axisColor: Color = Color.Gray
) {
    val textMeasurer = rememberTextMeasurer()
    val maxValue = data.maxOf { it.age }.toFloat()
    val padding = 40f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(400.dp)
    ) {
        val canvasWidth = size.width - padding * 2
        val canvasHeight = size.height - padding * 2
        val barWidth = (canvasWidth / data.size) * 0.7f
        val barSpacing = (canvasWidth / data.size) * 0.3f

        drawLine(
            color = axisColor,
            start = Offset(padding, 0f),
            end = Offset(padding, size.height - padding),
            strokeWidth = 2f
        )

        drawLine(
            color = axisColor,
            start = Offset(padding, size.height - padding),
            end = Offset(size.width, size.height - padding),
            strokeWidth = 2f
        )

        val yStep = maxValue / 5
        for (i in 0..5) {
            val value = i * yStep
            val yPos = size.height - padding - (value / maxValue * canvasHeight)

            drawText(
                textMeasurer = textMeasurer,
                text = value.toInt().toString(),
                style = TextStyle(color = textColor, fontSize = 12.sp),
                topLeft = Offset(0f, yPos - 8f)
            )

            drawLine(
                color = axisColor.copy(alpha = 0.3f),
                start = Offset(padding, yPos),
                end = Offset(size.width, yPos),
                strokeWidth = 1f
            )
        }

        data.forEachIndexed { index, item ->
            val barHeight = (item.age.toFloat() / maxValue) * canvasHeight
            val xPos = padding + index * (barWidth + barSpacing) + barSpacing / 2
            val yPos = size.height - padding - barHeight

            drawRect(
                color = barColor,
                topLeft = Offset(xPos, yPos),
                size = Size(barWidth, barHeight),
            )

            drawText(
                textMeasurer = textMeasurer,
                text = item.age.toString(),
                style = TextStyle(color = Color.White, fontSize = 12.sp),
                topLeft = Offset(xPos + barWidth / 2 - 20f, yPos - 0f)
            )

            drawText(
                textMeasurer = textMeasurer,
                text = item.name,
                style = TextStyle(color = textColor, fontSize = 12.sp),
                topLeft = Offset(xPos + barWidth / 2 - 20f, size.height - padding + 5f)
            )
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessScreen(
    modifier: Modifier,
    currentExpense: EntityUi,
    sum: Int,
    currentCategory: String,
    list: List<EntityUi>,
    listCategories: List<String>,
    onAddClick: (ExpenseEntity) -> Unit,
    onDeletePress: (ExpenseEntity) -> Unit,
    onCategoryChange: (String, Long, Long) -> Unit,
    onEditClick: (expense: EntityUi) -> Unit,
    rangeSet: (Pair<Long, Long>),
    resetCurrentExpense: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onUpdateClick: (expense: ExpenseEntity) -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var ranges by remember { mutableStateOf(false) }
    var addScreen by rememberSaveable { mutableStateOf(false) }
    var searchView by rememberSaveable { mutableStateOf(false) }
    val dateRangePickerState = rememberDateRangePickerState()
    val snackBar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


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
                        onClick = {
                            onCategoryChange("All", 0, 0)
                            onSearch("")
                            searchView = false
                        },
                        Modifier
                            .padding(2.dp)
                            .background(Color.Transparent),
                        colors = ButtonDefaults.buttonColors(CustomBlue)
                    ) { Text("All", color = Color.White) }
                }
                items(listCategories) {
                    OutlinedButton(
                        onClick = {
                            onCategoryChange(it, 0, 0)
                            onSearch("")
                            searchView = false
                        },
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
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.Center) {
                    IconButton(onClick = {
                        searchView = !searchView
                    }) {
                        Icon(Icons.Filled.Search, "")
                    }
                    if (!searchView)
                        OutlinedButton({
                            ranges = !ranges
                        }, contentPadding = PaddingValues(horizontal = 8.dp)) {
                            Text(
                                text = "Set range ",
                                fontSize = MaterialTheme.typography.titleSmall.fontSize,
                                color = Color.Black,
                                fontWeight = FontWeight.W500
                            )
                        }
                    else
                        TextField(
                            query,
                            onValueChange = { query = it },
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            singleLine = true,
                            trailingIcon = {
                                TextButton(onClick = { onSearch(query) }) {
                                    Text("Search")
                                }
                            }, colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            )
                        )
                }
                if (!searchView)
                    Text(
                        "Rs. $sum",
                        fontSize = MaterialTheme.typography.titleLarge.fontSize,
                        color = Color.Black,
                        fontWeight = FontWeight.W500
                    )

            }

            if (currentCategory == "Range")
                Text(
                    "${rangeSet.first.toDateTimeString("dd-MM-YYYY")} To ${
                        rangeSet.second.toDateTimeString(
                            "dd-MM-YYYY"
                        )
                    }",
                    Modifier.padding(4.dp),
                    fontSize = MaterialTheme.typography.titleSmall.fontSize,
                    color = Color.Black,
                    fontWeight = FontWeight.W500
                )

            if (ranges) {
                DatePickerDialog(
                    onDismissRequest = {
                        ranges = false
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onCategoryChange(
                                    "Range",
                                    dateRangePickerState.selectedStartDateMillis ?: 0,
                                    dateRangePickerState.selectedEndDateMillis ?: 0
                                )
                                ranges = false
                            }
                        ) {
                            Text("OK")
                        }

                    },
                    dismissButton = {
                        TextButton(onClick = {
                            ranges = false
                        }
                        ) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DateRangePicker(
                        state = dateRangePickerState,
                        title = {
                            Text(
                                text = "Select date range"
                            )
                        },
                        showModeToggle = false,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .padding(16.dp)
                    )
                }
            }
            if (list.isEmpty()) {
                if (currentCategory != "Range") onCategoryChange("All", 0, 0)
                Text(
                    "Save your first Expense",
                    Modifier.align(Alignment.CenterHorizontally),
                    color = Color.Black
                )

            } else {
                LazyColumn(
                    Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .fillMaxWidth()
                        .background(Color.White),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 10.dp)

                ) {
                    items(list, key = { expense -> expense.id }) { expense ->
                        SwipeToDelete(
                            onDelete = {
                                onDeletePress(expense.toExpenseEntity())
                                scope.launch {
                                    snackBar.showSnackbar("Expense Deleted")
                                }
                            }, scope = scope
                        ) {
                            ExpenseCard(expense, onLong = {
                                onDeletePress(it.toExpenseEntity())
                            }, onClickEdit = {
                                onEditClick(it)
                                addScreen = !addScreen
                            })
                        }
                    }
                }
            }
        }


        if (addScreen) {
            Dialog(onDismissRequest = { addScreen = !addScreen }) {
                ExpenseAddScreen(expense = currentExpense, onAddClick = { expenseEntity ->
                    onAddClick(expenseEntity)
                    addScreen = !addScreen
                }) {
                    onUpdateClick(it)
                    addScreen = !addScreen
                }
            }
        }
        FloatingActionButton(
            onClick = {
                resetCurrentExpense()
                addScreen = !addScreen
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
                Icons.Filled.Add,
                "",
                tint = CustomBlue
            )
        }

        SnackbarHost(
            hostState = snackBar, Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp)
        )

    }
}

@Composable
fun SwipeToDelete(
    scope: CoroutineScope,
    onDelete: () -> Unit,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()

    var isRemoved by remember { mutableStateOf(true) }
    AnimatedVisibility(isRemoved) {
        SwipeToDismissBox(
            state = state,
            backgroundContent = {
                ConfirmDelete(
                    onDelete = {
                        onDelete()
                        isRemoved = false
                    },
                    onCancel = {
                        scope.launch {
                            state.reset()
                        }
                    }
                )
            },
            enableDismissFromStartToEnd = false,
        ) {
            content()
        }
    }
}


@Composable
fun ConfirmDelete(
    onDelete: () -> Unit = {},
    onCancel: () -> Unit = {}
) {
    Column(
        Modifier
            .padding(10.dp)
            .fillMaxSize()
            .background(CustomBlue)
            .padding(end = 20.dp),
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Are you sure you want to delete?", color = Color.White)
        Row {
            TextButton({ onDelete() }) {
                Text("Delete", color = Color.White)
            }
            TextButton({ onCancel() }) {
                Text("Cancel", color = Color.White)
            }
        }
    }
}

@Composable
fun ExpenseAddScreen(
    expense: EntityUi = EntityUi(),
    onAddClick: (ExpenseEntity) -> Unit, onUpdateClick: (expense: ExpenseEntity) -> Unit
) {
    val context = LocalContext.current

    var amount by rememberSaveable { mutableStateOf("${expense.amount}") }
    var category by rememberSaveable { mutableStateOf(expense.category) }
    var description by rememberSaveable { mutableStateOf(expense.description) }

    Box(
        Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White),
    ) {
        Column(
            Modifier
                .padding(vertical = 20.dp, horizontal = 10.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = if (expense.id == 0) "Add Expense" else "Update Expense",
                fontSize = MaterialTheme.typography.titleLarge.fontSize,
                fontWeight = FontWeight.W500
            )
            Column(Modifier.padding(vertical = 10.dp)) {
                TextField(
                    value = if (amount == 0.toString()) "" else amount,
                    onValueChange = {
                        amount = if (it.toDoubleOrNull() != null || it.isEmpty()) it else amount
                    },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .fillMaxWidth(),
                    placeholder = { Text("enter amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                TextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .fillMaxWidth(),
                    placeholder = { Text("enter category") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )

                TextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .fillMaxWidth(),
                    placeholder = { Text("enter description") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    )
                )
            }
            Button(
                onClick = {
                    if (
                        expense.id != 0 && amount.isNotEmpty() && amount.toInt() > 0 && category.isNotEmpty() && description.isNotEmpty()
                    ) onUpdateClick(
                        ExpenseEntity(
                            uid = expense.id,
                            amount = amount.toInt(),
                            category = category.lowercase(Locale.ROOT).trim(),
                            description = description,
                            date = System.currentTimeMillis()
                        )
                    )
                    else if (
                        expense.id == 0 && amount.isNotEmpty() && amount.toInt() > 0 && category.isNotEmpty() && description.isNotEmpty())
                        onAddClick(
                            ExpenseEntity(
                                uid = 0,
                                amount = amount.toInt(),
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


fun ExpenseEntity.toUiEntity(): EntityUi {
    return EntityUi(
        id = uid, amount =
        amount, description = description, category = category, date = date
    )
}

fun EntityUi.toExpenseEntity(): ExpenseEntity = ExpenseEntity(
    uid = id,
    amount = amount,
    description = description,
    category = category,
    date = date
)

fun Long.toDateTimeString(pattern: String = "dd/MM/yyyy, hh:mm a"): String {
    val date = Date(this)
    val formatter = SimpleDateFormat(
        pattern, Locale.getDefault()
    )
    return formatter.format(date)
}