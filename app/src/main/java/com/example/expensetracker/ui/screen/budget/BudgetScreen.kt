package com.example.expensetracker.ui.screen.budget

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensetracker.ui.components.BudgetCard
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.CategoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    navController: NavController,
    budgetViewModel: BudgetViewModel,
    categoryViewModel: CategoryViewModel,
    paddingValues: PaddingValues = PaddingValues(),
    fabClicked: Boolean = false,          // ← ADD THIS
    onFabHandled: () -> Unit = {}
) {
    val budgetUiState by budgetViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var dialogCategoryId by remember { mutableStateOf("") }
    var dialogCategoryName by remember { mutableStateOf("") }
    var dialogCategoryIcon by remember { mutableStateOf("") }
    var dialogAmount by remember { mutableStateOf("") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        budgetViewModel.loadBudgets()
        categoryViewModel.loadCategories()
    }
    // Open dialog when FAB is clicked from MainScreen
    LaunchedEffect(fabClicked) {
        if (fabClicked) {
            dialogCategoryId = ""
            dialogCategoryName = ""
            dialogCategoryIcon = ""
            dialogAmount = ""
            showDialog = true
            onFabHandled()
        }
    }

    Box(modifier = Modifier.padding(paddingValues)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {

            // --- Month Summary Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Budgeted",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "₹%.0f".format(
                                budgetUiState.budgets.sumOf { it.limitAmount }
                            ),
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Total Spent",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "₹%.0f".format(
                                budgetUiState.budgets.sumOf { it.spentAmount }
                            ),
                            fontSize = 22.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (budgetUiState.budgets.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "💰", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "No budgets set yet", color = Color.Gray, fontSize = 16.sp)
                    Text(
                        text = "Tap + to set a budget for a category",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(budgetUiState.budgets) { budget ->
                        val category = categoryUiState.categories
                            .find { it.name == budget.categoryName }
                        BudgetCard(
                            categoryName = budget.categoryName,
                            categoryIcon = category?.icon ?: "📦",
                            limitAmount = budget.limitAmount,
                            spentAmount = budget.spentAmount,
                            onClick = {
                                dialogCategoryId = budget.categoryId
                                dialogCategoryName = budget.categoryName
                                dialogCategoryIcon = category?.icon ?: "📦"
                                dialogAmount = budget.limitAmount.toString()
                                showDialog = true
                            }
                        )
                    }
                }
            }
        }

        // --- Set Budget Dialog ---
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Set Budget") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = if (dialogCategoryName.isNotBlank())
                                    "$dialogCategoryIcon $dialogCategoryName" else "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(
                                        expanded = categoryDropdownExpanded
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categoryUiState.categories.forEach { category ->
                                    DropdownMenuItem(
                                        text = { Text("${category.icon} ${category.name}") },
                                        onClick = {
                                            dialogCategoryId = category.id
                                            dialogCategoryName = category.name
                                            dialogCategoryIcon = category.icon
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = dialogAmount,
                            onValueChange = {
                                dialogAmount = it.filter { c -> c.isDigit() || c == '.' }
                            },
                            label = { Text("Monthly Limit (₹)") },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (dialogCategoryName.isNotBlank() && dialogAmount.isNotBlank()) {
                                budgetViewModel.setBudget(
                                    categoryId = dialogCategoryId,
                                    categoryName = dialogCategoryName,
                                    limitAmount = dialogAmount.toDoubleOrNull() ?: 0.0
                                )
                                showDialog = false
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (dialogCategoryId.isNotBlank()) {
                            TextButton(
                                onClick = {
                                    val budgetToDelete = budgetUiState.budgets
                                        .find { it.categoryId == dialogCategoryId }
                                    budgetToDelete?.let {
                                        budgetViewModel.deleteBudget(it.id)
                                    }
                                    showDialog = false
                                }
                            ) {
                                Text("Delete", color = Color(0xFFF44336))
                            }
                        }
                        TextButton(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                    }
                }
            )
        }
    }
}