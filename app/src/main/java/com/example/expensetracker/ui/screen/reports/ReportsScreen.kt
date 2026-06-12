package com.example.expensetracker.ui.screen.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import co.yml.charts.common.model.PlotType
import co.yml.charts.ui.piechart.charts.PieChart
import co.yml.charts.ui.piechart.models.PieChartConfig
import co.yml.charts.ui.piechart.models.PieChartData
import com.example.expensetracker.viewmodel.TransactionViewModel

val chartColors = listOf(
    Color(0xFFF44336), Color(0xFF2196F3), Color(0xFF4CAF50),
    Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4),
    Color(0xFFFFEB3B), Color(0xFF795548)
)

@Composable
fun ReportsScreen(
    navController: NavController,
    transactionViewModel: TransactionViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by transactionViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        transactionViewModel.loadTransactions()
    }

    val expensesByCategory = uiState.transactions
        .filter { it.type == "expense" }
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val pieSlices = expensesByCategory.mapIndexed { index, (category, amount) ->
        PieChartData.Slice(
            label = category,
            value = amount.toFloat(),
            color = chartColors[index % chartColors.size]
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "This Month Summary",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryStatItem("Income", uiState.totalIncome, Color(0xFF81C784))
                        SummaryStatItem("Expense", uiState.totalExpense, Color(0xFFE57373))
                        SummaryStatItem("Balance", uiState.balance, Color.White)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Spending by Category",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (pieSlices.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No expense data yet", color = Color.Gray)
                        }
                    } else {
                        PieChart(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(300.dp),
                            pieChartData = PieChartData(
                                slices = pieSlices,
                                plotType = PlotType.Pie
                            ),
                            pieChartConfig = PieChartConfig(
                                isAnimationEnable = true,
                                showSliceLabels = true,
                                animationDuration = 800,
                                labelVisible = true,
                                strokeWidth = 120f,
                                activeSliceAlpha = 0.9f,
                                isEllipsizeEnabled = true,
                                labelColor = Color.White,
                                backgroundColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            expensesByCategory.forEachIndexed { index, (category, amount) ->
                                LegendItem(
                                    color = chartColors[index % chartColors.size],
                                    label = category,
                                    amount = amount,
                                    total = uiState.totalExpense
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        text = "Top Categories",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (expensesByCategory.isEmpty()) {
                        Text(text = "No expenses recorded yet", color = Color.Gray)
                    } else {
                        expensesByCategory.take(5).forEachIndexed { index, (category, amount) ->
                            TopCategoryRow(
                                rank = index + 1,
                                category = category,
                                amount = amount,
                                total = uiState.totalExpense,
                                color = chartColors[index % chartColors.size]
                            )
                            if (index < expensesByCategory.size - 1) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatBox(uiState.transactions.size.toString(), "Total Transactions")
                    StatBox(
                        uiState.transactions.count { it.type == "income" }.toString(),
                        "Income Entries"
                    )
                    StatBox(
                        uiState.transactions.count { it.type == "expense" }.toString(),
                        "Expense Entries"
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = color.copy(alpha = 0.8f), fontSize = 12.sp)
        Text(
            text = "₹%.0f".format(amount),
            color = color,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun LegendItem(color: Color, label: String, amount: Double, total: Double) {
    val percentage = if (total > 0) (amount / total * 100).toInt() else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 14.sp)
        }
        Row {
            Text(text = "₹%.0f".format(amount), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "($percentage%)", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
fun TopCategoryRow(rank: Int, category: String, amount: Double, total: Double, color: Color) {
    val percentage = if (total > 0) (amount / total * 100).toInt() else 0
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "#$rank",
                fontSize = 14.sp,
                color = color,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(30.dp)
            )
            Text(text = category, fontSize = 14.sp)
        }
        Text(
            text = "₹%.0f ($percentage%)".format(amount),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@Composable
fun StatBox(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}