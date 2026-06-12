package com.example.expensetracker.ui.screen.transaction

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensetracker.data.model.Transaction
import com.example.expensetracker.formatDateReadable
import com.example.expensetracker.viewmodel.CategoryViewModel
import com.example.expensetracker.viewmodel.TransactionViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionScreen(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    transactionId: String? = null
) {
    val transactionUiState by transactionViewModel.uiState.collectAsState()
    val categoryUiState by categoryViewModel.uiState.collectAsState()

    // Find existing transaction if editing
    val existingTransaction = remember(transactionId) {
        if (transactionId != null) {
            transactionUiState.transactions.find { it.id == transactionId }
        } else null
    }

    val isEditing = existingTransaction != null

    // Form fields — pre-filled if editing
    var title by remember { mutableStateOf(existingTransaction?.title ?: "") }
    var amount by remember { mutableStateOf(existingTransaction?.amount?.toString() ?: "") }
    var type by remember { mutableStateOf(existingTransaction?.type ?: "expense") }
    var selectedCategory by remember { mutableStateOf(existingTransaction?.category ?: "") }
    var note by remember { mutableStateOf(existingTransaction?.note ?: "") }
    var selectedDate by remember { mutableLongStateOf(existingTransaction?.date ?: System.currentTimeMillis()) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Load categories when screen opens
    LaunchedEffect(Unit) {
        categoryViewModel.loadCategories()
    }

    // Show success message and go back
    LaunchedEffect(transactionUiState.successMessage) {
        if (transactionUiState.successMessage != null) {
            snackbarHostState.showSnackbar(transactionUiState.successMessage!!)
            transactionViewModel.clearMessages()
            navController.popBackStack()
        }
    }

    // Date picker setup
    val calendar = Calendar.getInstance().apply {
        timeInMillis = selectedDate
    }
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth)
            selectedDate = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Transaction" else "Add Transaction",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Show delete button only when editing
                    if (isEditing) {
                        IconButton(onClick = {
                            existingTransaction?.let {
                                transactionViewModel.deleteTransaction(it.id)
                                navController.popBackStack()
                            }
                        }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // --- Income / Expense Toggle ---
            Text(text = "Type", fontWeight = FontWeight.Medium)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = type == "expense",
                    onClick = { type = "expense" },
                    label = { Text("Expense") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFF44336),
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = type == "income",
                    onClick = { type = "income" },
                    label = { Text("Income") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF4CAF50),
                        selectedLabelColor = Color.White
                    )
                )
            }

            // --- Amount Field ---
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("Amount (₹)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                ),
                leadingIcon = { Text("₹", modifier = Modifier.padding(start = 4.dp)) }
            )

            // --- Title Field ---
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                placeholder = { Text("e.g. Lunch, Salary, Rent") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // --- Category Dropdown ---
            Text(text = "Category", fontWeight = FontWeight.Medium)
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select Category") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded)
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
                                selectedCategory = category.name
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // --- Date Picker ---
            OutlinedTextField(
                value = formatDateReadable(selectedDate),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { datePickerDialog.show() }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick Date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // --- Note Field ---
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                placeholder = { Text("Any extra details...") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            // --- Error Message ---
            if (localError != null) {
                Text(
                    text = localError!!,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // --- Save Button ---
            Button(
                onClick = {
                    when {
                        amount.isBlank() -> localError = "Please enter an amount"
                        title.isBlank() -> localError = "Please enter a title"
                        selectedCategory.isBlank() -> localError = "Please select a category"
                        else -> {
                            localError = null
                            val transaction = Transaction(
                                id = existingTransaction?.id ?: "",
                                title = title,
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                type = type,
                                category = selectedCategory,
                                note = note,
                                date = selectedDate
                            )
                            if (isEditing) {
                                transactionViewModel.updateTransaction(transaction)
                            } else {
                                transactionViewModel.addTransaction(transaction)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !transactionUiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (type == "income") Color(0xFF4CAF50) else Color(0xFFF44336)
                )
            ) {
                if (transactionUiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        text = if (isEditing) "Update Transaction" else "Save Transaction",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}