package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.Transaction
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.TransactionRepository
import com.example.expensetracker.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class TransactionUiState(
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Monthly summary
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0
)

class TransactionViewModel : ViewModel() {

    private val transactionRepository = TransactionRepository()
    private val authRepository = AuthRepository()
    private val budgetRepository = BudgetRepository()

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState

    private val userId get() = authRepository.currentUser?.uid ?: ""

    // Current month and year
    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    init {
        loadTransactions()
    }

    fun loadTransactions() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            transactionRepository
                .getTransactionsByMonth(userId, currentMonth, currentYear)
                .collect { transactions ->
                    // Calculate summary automatically
                    val income = transactions
                        .filter { it.type == "income" }
                        .sumOf { it.amount }
                    val expense = transactions
                        .filter { it.type == "expense" }
                        .sumOf { it.amount }

                    _uiState.value = _uiState.value.copy(
                        transactions = transactions,
                        isLoading = false,
                        totalIncome = income,
                        totalExpense = expense,
                        balance = income - expense
                    )
                }
        }
    }

    fun addTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val newTransaction = transaction.copy(userId = userId)
            val result = transactionRepository.addTransaction(newTransaction)

            if (result.isSuccess) {
                // If it's an expense, update the matching budget's spentAmount
                if (transaction.type == "expense") {
                    recalculateBudgetSpent(transaction.category)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Transaction added!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to add"
                )
            }
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val result = transactionRepository.updateTransaction(transaction)

            if (result.isSuccess) {
                if (transaction.type == "expense") {
                    recalculateBudgetSpent(transaction.category)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Transaction updated!"
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to update"
                )
            }
        }
    }

    fun deleteTransaction(transactionId: String) {
        viewModelScope.launch {
            // Find the transaction before deleting so we know its category
            val transaction = uiState.value.transactions.find { it.id == transactionId }

            val result = transactionRepository.deleteTransaction(userId, transactionId)

            if (result.isSuccess) {
                // Recalculate budget if it was an expense
                if (transaction?.type == "expense") {
                    recalculateBudgetSpent(transaction.category)
                }
                _uiState.value = _uiState.value.copy(successMessage = "Transaction deleted!")
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete"
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
    // Recalculates total spent for a category and updates the budget
    private suspend fun recalculateBudgetSpent(categoryName: String) {
        // Add up all expenses for this category in the current month
        val totalSpent = uiState.value.transactions
            .filter {
                it.type == "expense" && it.category == categoryName
            }
            .sumOf { it.amount }

        budgetRepository.updateSpentAmount(
            userId = userId,
            categoryName = categoryName,
            month = currentMonth,
            year = currentYear,
            newSpentAmount = totalSpent
        )
    }
}