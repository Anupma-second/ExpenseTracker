package com.example.expensetracker.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.expensetracker.navigation.Routes
import com.example.expensetracker.navigation.bottomNavItems
import com.example.expensetracker.ui.screen.budget.BudgetScreen
import com.example.expensetracker.ui.screen.home.HomeScreen
import com.example.expensetracker.ui.screen.reports.ReportsScreen
import com.example.expensetracker.ui.screen.settings.SettingsScreen
import com.example.expensetracker.ui.screen.transaction.TransactionListScreen
import com.example.expensetracker.viewmodel.AuthViewModel
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.CategoryViewModel
import com.example.expensetracker.viewmodel.TransactionViewModel
import androidx.compose.runtime.mutableStateOf

@Composable
fun MainScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    transactionViewModel: TransactionViewModel,
    categoryViewModel: CategoryViewModel,
    budgetViewModel: BudgetViewModel,
    startTab: Int = 0
) {
    var selectedNavIndex by remember { mutableIntStateOf(startTab) }
    var budgetFabClicked by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedNavIndex == index,
                        onClick = { selectedNavIndex = index },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label, fontSize = 10.sp) }
                    )
                }
            }
        },
        floatingActionButton = {
            when (selectedNavIndex) {
                0, 1 -> FloatingActionButton(
                    onClick = { navController.navigate(Routes.ADD_TRANSACTION) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                }
                2 -> FloatingActionButton(
                    onClick = { budgetFabClicked = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Budget", tint = Color.White)
                }
            }
        }
    ) { paddingValues ->
        when (selectedNavIndex) {
            0 -> HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                paddingValues = paddingValues
            )
            1 -> TransactionListScreen(
                navController = navController,
                transactionViewModel = transactionViewModel,
                paddingValues = paddingValues
            )
            2 -> BudgetScreen(
                navController = navController,
                budgetViewModel = budgetViewModel,
                categoryViewModel = categoryViewModel,
                paddingValues = paddingValues,
                fabClicked = budgetFabClicked,
                onFabHandled = { budgetFabClicked = false }
            )
            3 -> ReportsScreen(
                navController = navController,
                transactionViewModel = transactionViewModel,
                paddingValues = paddingValues
            )
            4 -> SettingsScreen(
                navController = navController,
                authViewModel = authViewModel,
                paddingValues = paddingValues
            )
        }
    }
}