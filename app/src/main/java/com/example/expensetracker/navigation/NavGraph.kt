package com.example.expensetracker.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.expensetracker.ui.screen.MainScreen
import com.example.expensetracker.ui.screen.auth.LoginScreen
import com.example.expensetracker.ui.screen.auth.RegisterScreen
import com.example.expensetracker.ui.screen.auth.SplashScreen
import com.example.expensetracker.ui.screen.transaction.AddEditTransactionScreen
import com.example.expensetracker.viewmodel.AuthViewModel
import com.example.expensetracker.viewmodel.BudgetViewModel
import com.example.expensetracker.viewmodel.CategoryViewModel
import com.example.expensetracker.viewmodel.TransactionViewModel

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {

    val authViewModel: AuthViewModel = viewModel()
    val transactionViewModel: TransactionViewModel = viewModel()
    val categoryViewModel: CategoryViewModel = viewModel()
    val budgetViewModel: BudgetViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        composable(Routes.SPLASH) {
            SplashScreen(navController, authViewModel)
        }

        composable(Routes.LOGIN) {
            LoginScreen(navController, authViewModel)
        }

        composable(Routes.REGISTER) {
            RegisterScreen(navController, authViewModel)
        }

        // All main tabs live inside MainScreen
        composable(Routes.HOME) {
            MainScreen(
                navController = navController,
                authViewModel = authViewModel,
                transactionViewModel = transactionViewModel,
                categoryViewModel = categoryViewModel,
                budgetViewModel = budgetViewModel
            )
        }

        composable(Routes.ADD_TRANSACTION) {
            AddEditTransactionScreen(
                navController = navController,
                transactionViewModel = transactionViewModel,
                categoryViewModel = categoryViewModel
            )
        }

        composable(
            route = Routes.EDIT_TRANSACTION,
            arguments = listOf(
                navArgument("transactionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            AddEditTransactionScreen(
                navController = navController,
                transactionViewModel = transactionViewModel,
                categoryViewModel = categoryViewModel,
                transactionId = transactionId
            )
        }
    }
}