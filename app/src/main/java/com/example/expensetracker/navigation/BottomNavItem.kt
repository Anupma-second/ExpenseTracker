package com.example.expensetracker.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.android.gms.wallet.Wallet

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Default.Home, Routes.HOME),
    BottomNavItem("Transactions", Icons.Default.List, Routes.TRANSACTIONS),
    BottomNavItem("Budget", Icons.Default.Wallet, Routes.BUDGET),
    BottomNavItem("Reports", Icons.Default.PieChart, Routes.REPORTS),
    BottomNavItem("Settings", Icons.Default.Settings, Routes.SETTINGS)
)