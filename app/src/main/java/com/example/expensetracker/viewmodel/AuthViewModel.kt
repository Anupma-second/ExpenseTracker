package com.example.expensetracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.expensetracker.data.repository.AuthRepository
import com.example.expensetracker.data.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// This holds everything the Auth screen needs to display
data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val authRepository = AuthRepository()
    private val categoryRepository = CategoryRepository()

    // _uiState is private — only this ViewModel can change it
    private val _uiState = MutableStateFlow(AuthUiState())
    // uiState is public — the screen reads from this
    val uiState: StateFlow<AuthUiState> = _uiState

    init {
        // Check if user is already logged in when app starts
        if (authRepository.isLoggedIn) {
            _uiState.value = _uiState.value.copy(isLoggedIn = true)
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            // Show loading spinner
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.register(name, email, password)

            if (result.isSuccess) {
                // Add default categories for new user
                val userId = authRepository.currentUser?.uid ?: ""
                categoryRepository.addDefaultCategories(userId)
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
            } else {
                // Show error message
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Registration failed"
                )
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            val result = authRepository.login(email, password)

            if (result.isSuccess) {
                _uiState.value = _uiState.value.copy(isLoading = false, isLoggedIn = true)
            } else {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun logout() {
        authRepository.logout()
        _uiState.value = AuthUiState() // reset everything
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}