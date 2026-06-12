package com.example.expensetracker.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensetracker.ui.components.SummaryCard
import com.example.expensetracker.ui.components.TransactionCard
import com.example.expensetracker.viewmodel.AuthViewModel
import com.example.expensetracker.viewmodel.TransactionViewModel

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    paddingValues: PaddingValues = PaddingValues()
) {
    val uiState by transactionViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        transactionViewModel.loadTransactions()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Text(text = "This Month", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryCard(
                    title = "Balance",
                    amount = uiState.balance,
                    backgroundColor = MaterialTheme.colorScheme.primary
                )
                SummaryCard(
                    title = "Income",
                    amount = uiState.totalIncome,
                    backgroundColor = Color(0xFF4CAF50)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryCard(
                    title = "Expenses",
                    amount = uiState.totalExpense,
                    backgroundColor = Color(0xFFF44336)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Transactions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (uiState.transactions.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🧾", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "No transactions yet", color = Color.Gray)
                    Text(
                        text = "Tap + to add your first one!",
                        color = Color.Gray,
                        fontSize = 13.sp
                    )
                }
            }
        } else {
            items(uiState.transactions.take(10)) { transaction ->
                TransactionCard(
                    transaction = transaction,
                    onClick = {
                        navController.navigate("edit_transaction/${transaction.id}")
                    }
                )
            }
        }
    }
}