package com.example.expensetracker.data.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val currency: String = "₹",
    val createdAt: Long = System.currentTimeMillis()
)