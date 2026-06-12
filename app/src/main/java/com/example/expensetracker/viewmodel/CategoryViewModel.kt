package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.model.Category
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class CategoryViewModel : ViewModel() {

    private val categoryRepository = CategoryRepository()
    private val authRepository = AuthRepository()

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState

    private val userId get() = authRepository.currentUser?.uid ?: ""

    init {
        loadCategories()
    }

    fun loadCategories() {
        if (userId.isBlank()) return
        viewModelScope.launch {
            categoryRepository.getCategories(userId)
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false
                    )
                }
        }
    }

    fun addCategory(name: String, icon: String, color: String) {
        viewModelScope.launch {
            val category = Category(
                name = name,
                icon = icon,
                color = color,
                userId = userId
            )
            val result = categoryRepository.addCategory(category)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to add category"
                )
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            val result = categoryRepository.deleteCategory(userId, categoryId)

            if (result.isFailure) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = result.exceptionOrNull()?.message ?: "Failed to delete"
                )
            }
        }
    }
}