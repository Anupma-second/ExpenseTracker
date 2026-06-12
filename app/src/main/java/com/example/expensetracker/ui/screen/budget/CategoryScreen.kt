package com.example.expensetracker.ui.screen.budget

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.expensetracker.viewmodel.CategoryViewModel

@Composable
fun CategoryScreen(navController: NavController, categoryViewModel: CategoryViewModel) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = "Categories Screen — Coming Soon")
    }
}