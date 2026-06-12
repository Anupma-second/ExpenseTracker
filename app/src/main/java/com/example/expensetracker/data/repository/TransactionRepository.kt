package com.example.expensetracker.data.repository

import androidx.room.util.copy
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.expensetracker.data.model.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class TransactionRepository {

    private val firestore = FirebaseFirestore.getInstance()

    // Helper to get the transactions collection for a user
    private fun transactionsRef(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("transactions")

    // --- ADD TRANSACTION ---
    suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val docRef = transactionsRef(transaction.userId).document()
            val withId = transaction.copy(id = docRef.id)  // save the auto-generated ID
            docRef.set(withId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- UPDATE TRANSACTION ---
    suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            transactionsRef(transaction.userId)
                .document(transaction.id)
                .set(transaction)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- DELETE TRANSACTION ---
    suspend fun deleteTransaction(userId: String, transactionId: String): Result<Unit> {
        return try {
            transactionsRef(userId)
                .document(transactionId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GET ALL TRANSACTIONS (live, real-time) ---
    fun getTransactions(userId: String): Flow<List<Transaction>> = callbackFlow {
        val listener = transactionsRef(userId)
            .orderBy("date", Query.Direction.DESCENDING)  // newest first
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull {
                    it.toObject(Transaction::class.java)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }  // stop listening when screen closes
    }

    // --- GET TRANSACTIONS FOR A SPECIFIC MONTH ---
    fun getTransactionsByMonth(userId: String, month: Int, year: Int): Flow<List<Transaction>> = callbackFlow {
        // Calculate start and end timestamps for the month
        val calendar = java.util.Calendar.getInstance()
        calendar.set(year, month - 1, 1, 0, 0, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.set(year, month - 1, calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH), 23, 59, 59)
        val endOfMonth = calendar.timeInMillis

        val listener = transactionsRef(userId)
            .whereGreaterThanOrEqualTo("date", startOfMonth)
            .whereLessThanOrEqualTo("date", endOfMonth)
            .orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val transactions = snapshot?.documents?.mapNotNull {
                    it.toObject(Transaction::class.java)
                } ?: emptyList()
                trySend(transactions)
            }
        awaitClose { listener.remove() }
    }
}