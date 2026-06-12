package com.example.expensetracker

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateReadable(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}