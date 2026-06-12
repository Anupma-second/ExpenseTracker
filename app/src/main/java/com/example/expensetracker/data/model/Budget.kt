package com.example.expensetracker.data.model

data class Budget (
    val id: String = "",
    val categoryId: String = "",
    val categoryName: String = "",
    val limitAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val month: Int = 0,   // e.g. 6 for June
    val year: Int = 0,    // e.g. 2025
    val userId: String = ""
)