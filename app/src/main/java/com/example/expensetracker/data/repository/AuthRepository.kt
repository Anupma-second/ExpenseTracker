package com.example.expensetracker.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.example.expensetracker.data.model.User
import kotlinx.coroutines.tasks.await

class AuthRepository {

    // These are our Firebase tools
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Returns the currently logged-in user (or null if not logged in)
    val currentUser: FirebaseUser? get() = auth.currentUser

    // Check if someone is logged in
    val isLoggedIn: Boolean get() = auth.currentUser != null

    // --- REGISTER ---
    suspend fun register(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            // Create account in Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!

            // Also save user info in Firestore database
            val user = User(
                uid = firebaseUser.uid,
                name = name,
                email = email
            )
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOGIN ---
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- LOGOUT ---
    fun logout() {
        auth.signOut()
    }
}