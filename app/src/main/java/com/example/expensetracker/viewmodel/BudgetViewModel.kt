package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.Budget
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.BudgetRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class BudgetUiState(
    val budgets: List<Budget> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class BudgetViewModel : ViewModel() {

    private val budgetRepository = BudgetRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(BudgetUiState())
    val uiState: StateFlow<BudgetUiState> = _uiState

    private val userId get() = authRepository.currentUser?.uid ?: ""

    private val currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            budgetRepository.getBudgetsByMonth(userId, currentMonth, currentYear)
                .collect { budgets ->
                    _uiState.value = _uiState.value.copy(
                        budgets = budgets,
                        isLoading = false
                    )
                }
        }
    }

    fun setBudget(categoryId: String, categoryName: String, limitAmount: Double) {
        viewModelScope.launch {
            val budget = Budget(
                categoryId = categoryId,
                categoryName = categoryName,
                limitAmount = limitAmount,
                month = currentMonth,
                year = currentYear,
                userId = userId
            )
            val result = budgetRepository.setBudget(budget)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(successMessage = "Budget set!")
            } else {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to set budget"
                )
            }
        }
    }

    fun deleteBudget(budgetId: String) {
        viewModelScope.launch {
            budgetRepository.deleteBudget(userId, budgetId)
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}