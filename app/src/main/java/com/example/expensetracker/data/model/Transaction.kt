package com.example.expensetracker.data.model

data class Transaction(
    val id: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val type: String = "expense",  // "expense" or "income"
    val category: String = "",
    val note: String = "",
    val date: Long = System.currentTimeMillis(),
    val userId: String = ""
)