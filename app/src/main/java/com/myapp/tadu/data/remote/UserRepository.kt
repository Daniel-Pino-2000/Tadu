package com.myapp.tadu.data.remote

import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UserRepository(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {

    // Sign up a new user and save to Firestore
    suspend fun signUp(email: String, password: String, firstName: String, lastName: String): Result<User> {
        return try {
            // Create user in Firebase Auth
            auth.createUserWithEmailAndPassword(email, password).await()

            // Get UID from the created Firebase user
            val firebaseUser = auth.currentUser
                ?: return Result.Error(Exception("User creation failed"))

            // Create User object including UID
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                firstName = firstName,
                lastName = lastName
            )

            // Save user to Firestore using UID as document ID
            saveUserToFirestore(user)

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Login function
    suspend fun login(email: String, password: String): Result<User> {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = auth.currentUser
                ?: return Result.Error(Exception("Login failed"))

            // Get user info from Firestore
            val snapshot = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            val user = snapshot.toObject(User::class.java)
                ?: return Result.Error(Exception("User not found in Firestore"))

            Result.Success(user)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    // Logout
    fun logout() {
        auth.signOut()
    }

    // Current user ID for other repos (like TaskRepository)
    val currentUserId: String?
        get() = auth.currentUser?.uid

    // Helper to save user to Firestore
    private suspend fun saveUserToFirestore(user: User) {
        firestore.collection("users")
            .document(user.uid) // Use UID as doc ID, not email
            .set(user)
            .await()
    }

    // Send password reset email
    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                auth.sendPasswordResetEmail(email).await()
                Result.Success(Unit)
            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }

    /**
     * Delete user account with password re-authentication
     * This ensures the user has recently logged in before performing the destructive action
     */
    suspend fun deleteUserAccount(password: String): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val user = auth.currentUser
                    ?: return@withContext Result.Error(Exception("User not authenticated"))

                val email = user.email
                    ?: return@withContext Result.Error(Exception("User email not found"))

                val uid = user.uid

                // Step 1: Re-authenticate user with password for security
                val credential = EmailAuthProvider.getCredential(email, password)
                user.reauthenticate(credential).await()

                // Step 2: Delete user tasks from Firestore in batches
                val tasksRef = firestore.collection("users")
                    .document(uid)
                    .collection("tasks")

                val taskSnapshots = tasksRef.get().await()

                // Delete in batches of 500 (Firestore limit)
                val batchSize = 500
                taskSnapshots.documents.chunked(batchSize).forEach { chunk ->
                    val batch = firestore.batch()
                    chunk.forEach { doc ->
                        batch.delete(doc.reference)
                    }
                    batch.commit().await()
                }

                // Step 3: Delete user document from Firestore
                firestore.collection("users")
                    .document(uid)
                    .delete()
                    .await()

                // Step 4: Delete Firebase Authentication user (LAST step)
                user.delete().await()

                Result.Success(Unit)

            } catch (e: Exception) {
                Result.Error(e)
            }
        }
    }
}