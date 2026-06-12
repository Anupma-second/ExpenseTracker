package com.example.expensetracker.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.example.expensetracker.data.model.Category
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CategoryRepository {

    private val firestore = FirebaseFirestore.getInstance()

    private fun categoriesRef(userId: String) =
        firestore.collection("users")
            .document(userId)
            .collection("categories")

    // --- ADD CATEGORY ---
    suspend fun addCategory(category: Category): Result<Unit> {
        return try {
            val docRef = categoriesRef(category.userId).document()
            val withId = category.copy(id = docRef.id)
            docRef.set(withId).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- DELETE CATEGORY ---
    suspend fun deleteCategory(userId: String, categoryId: String): Result<Unit> {
        return try {
            categoriesRef(userId).document(categoryId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- GET ALL CATEGORIES (live) ---
    fun getCategories(userId: String): Flow<List<Category>> = callbackFlow {
        val listener = categoriesRef(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val categories = snapshot?.documents?.mapNotNull {
                    it.toObject(Category::class.java)
                } ?: emptyList()
                trySend(categories)
            }
        awaitClose { listener.remove() }
    }

    // --- ADD DEFAULT CATEGORIES for new users ---
    suspend fun addDefaultCategories(userId: String) {
        val defaults = listOf(
            Category(name = "Food", icon = "🍔", color = "#FF6B6B", isDefault = true, userId = userId),
            Category(name = "Transport", icon = "🚗", color = "#4ECDC4", isDefault = true, userId = userId),
            Category(name = "Shopping", icon = "🛍️", color = "#45B7D1", isDefault = true, userId = userId),
            Category(name = "Bills", icon = "💡", color = "#96CEB4", isDefault = true, userId = userId),
            Category(name = "Health", icon = "💊", color = "#FFEAA7", isDefault = true, userId = userId),
            Category(name = "Entertainment", icon = "🎬", color = "#DDA0DD", isDefault = true, userId = userId),
            Category(name = "Salary", icon = "💰", color = "#98FB98", isDefault = true, userId = userId),
            Category(name = "Other", icon = "📦", color = "#D3D3D3", isDefault = true, userId = userId)
        )
        defaults.forEach { addCategory(it) }
    }
}