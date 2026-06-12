package com.example.expensetracker.data.model

data class Category(
    val id: String = "",
    val name: String = "",
    val icon: String = "📦",
    val color: String = "#FF6B6B",
    val isDefault: Boolean = false,
    val userId: String = ""
)