package com.example.expensetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BudgetCard(
    categoryName: String,
    categoryIcon: String,
    limitAmount: Double,
    spentAmount: Double,
    onClick: () -> Unit
) {
    // Calculate progress 0.0 to 1.0
    val progress = if (limitAmount > 0) (spentAmount / limitAmount).toFloat() else 0f
    val isOverBudget = spentAmount > limitAmount
    val progressColor = when {
        isOverBudget -> Color(0xFFF44336)           // red
        progress > 0.75f -> Color(0xFFFF9800)        // orange warning
        else -> Color(0xFF4CAF50)                    // green safe
    }
    val remaining = limitAmount - spentAmount

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top row — category name and amounts
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$categoryIcon $categoryName",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )
                Text(
                    text = "₹%.0f / ₹%.0f".format(spentAmount, limitAmount),
                    fontSize = 14.sp,
                    color = if (isOverBudget) Color(0xFFF44336) else Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth(),
                color = progressColor,
                trackColor = Color.LightGray
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Bottom row — remaining or over budget message
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isOverBudget)
                        "⚠️ Over budget by ₹%.0f".format(-remaining)
                    else
                        "₹%.0f remaining".format(remaining),
                    fontSize = 12.sp,
                    color = if (isOverBudget) Color(0xFFF44336) else Color.Gray
                )
                Text(
                    text = "Tap to edit",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}