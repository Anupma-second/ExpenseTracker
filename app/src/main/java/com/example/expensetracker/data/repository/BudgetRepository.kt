package com.example.expensetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.expensetracker.data.model.Budget
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BudgetRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun budgetsRef(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("budgets")

    // --- SET BUDGET (add or overwrite) ---
    suspend fun setBudget(budget: Budget): Result<Unit> {
        return try {
            val docRef = budgetsRef(budget.userId).document()
            val withId = budget.copy(id = docRef.id)
            docRef.set(withId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GET BUDGETS FOR A MONTH (live) ---
    fun getBudgetsByMonth(userId: String, month: Int, year: Int): Flow<List<Budget>> = callbackFlow {
        val listener = budgetsRef(userId)
            .whereEqualTo("month", month)
            .whereEqualTo("year", year)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val budgets = snapshot?.documents?.mapNotNull {
                    it.toObject(Budget::class.java)
                } ?: emptyList()
                trySend(budgets)
            }
        awaitClose { listener.remove() }
    }

    // --- DELETE BUDGET ---
    suspend fun deleteBudget(userId: String, budgetId: String): Result<Unit> {
        return try {
            budgetsRef(userId).document(budgetId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // --- UPDATE SPENT AMOUNT when a transaction is added/deleted ---
    suspend fun updateSpentAmount(
        userId: String,
        categoryName: String,
        month: Int,
        year: Int,
        newSpentAmount: Double
    ): Result<Unit> {
        return try {
            // Find the budget document for this category + month + year
            val snapshot = budgetsRef(userId)
                .whereEqualTo("categoryName", categoryName)
                .whereEqualTo("month", month)
                .whereEqualTo("year", year)
                .get()
                .await()

            // If a budget exists for this category, update its spentAmount
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents[0]
                doc.reference.update("spentAmount", newSpentAmount).await()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}