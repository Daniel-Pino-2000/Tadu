package com.myapp.tadu.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.myapp.tadu.Graph
import com.myapp.tadu.data.TaskRepository
import com.myapp.tadu.data.remote.Injection
import com.myapp.tadu.data.remote.Result
import com.myapp.tadu.data.remote.User
import com.myapp.tadu.data.remote.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel(
    private val taskRepository: TaskRepository = Graph.taskRepository
) : ViewModel() {

    private val userRepository: UserRepository = UserRepository(
        FirebaseAuth.getInstance(),
        Injection.instance()
    )

    // ---------- AUTH ----------
    private val _authResult = MutableLiveData<Result<User>?>()
    val authResult: LiveData<Result<User>?> get() = _authResult

    private val _authError = MutableLiveData<String?>()
    val authError: LiveData<String?> get() = _authError

    private val _passwordResetResult = MutableLiveData<Result<Unit>?>()
    val passwordResetResult: LiveData<Result<Unit>?> get() = _passwordResetResult

    // ---------- CURRENT USER ----------
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> get() = _currentUser

    // ---------- DELETE ACCOUNT ----------
    private val _accountDeleted = MutableLiveData(false)
    val accountDeleted: LiveData<Boolean> get() = _accountDeleted

    private val _deleteAccountLoading = MutableLiveData(false)
    val deleteAccountLoading: LiveData<Boolean> get() = _deleteAccountLoading

    private val _deleteAccountError = MutableLiveData<String?>()
    val deleteAccountError: LiveData<String?> get() = _deleteAccountError

    /**
     * Sign up a new user
     */
    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            try {
                _authResult.value = userRepository.signUp(email, password, firstName, lastName)
            } catch (e: Exception) {
                _authResult.value = Result.Error(e)
            }
        }
    }

    /**
     * Log in an existing user
     */
    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authResult.value = userRepository.login(email, password)
            } catch (e: Exception) {
                _authResult.value = Result.Error(e)
            }
        }
    }

    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _passwordResetResult.value = userRepository.sendPasswordResetEmail(email)
            } catch (e: Exception) {
                _passwordResetResult.value = Result.Error(e)
            }
        }
    }

    /**
     * Clear password reset result
     */
    fun clearPasswordResetResult() {
        _passwordResetResult.value = null
    }

    /**
     * Logout the current user
     */
    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            // Clear local tasks so next user sees empty DB
            taskRepository.clearLocalTasks()

            // Sign out from Firebase
            userRepository.logout()

            // Clear previous auth result
            _authResult.postValue(null)
        }
    }

    /**
     * Check if user is currently logged in
     */
    fun isUserLoggedIn(): Boolean {
        return userRepository.currentUserId != null
    }

    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return userRepository.currentUserEmail
    }

    /**
     * Load current user data
     */
    fun loadCurrentUser() {
        viewModelScope.launch {
            val result = userRepository.getCurrentUser()
            when (result) {
                is Result.Success -> {
                    _currentUser.value = result.data
                }
                is Result.Error -> {
                    _currentUser.value = null
                }
            }
        }
    }

    /**
     * Clear authentication result
     */
    fun clearAuthResult() {
        _authResult.value = null
    }

    /**
     * Delete the user's account with password confirmation
     * Requires password for re-authentication before deletion
     */
    fun deleteUserAccount(password: String) {
        // Validate password is not empty
        if (password.isBlank()) {
            _deleteAccountError.value = "Please enter your password"
            return
        }

        viewModelScope.launch {
            _deleteAccountLoading.value = true
            _deleteAccountError.value = null

            val result = userRepository.deleteUserAccount(password)

            when (result) {
                is Result.Success -> {
                    // Clear local data first
                    taskRepository.clearLocalTasks()

                    // User is now signed out (Firebase auth deleted)
                    // Clear auth result to ensure clean state
                    _authResult.value = null

                    // Set flag to trigger navigation
                    _accountDeleted.value = true
                    _deleteAccountLoading.value = false
                }

                is Result.Error -> {
                    val exception = result.exception
                    _deleteAccountLoading.value = false

                    _deleteAccountError.value = when (exception) {
                        is FirebaseAuthInvalidCredentialsException -> {
                            "Incorrect password. Please try again."
                        }
                        is FirebaseAuthInvalidUserException -> {
                            "User account not found. Please try logging in again."
                        }
                        else -> {
                            exception.message ?: "Failed to delete account. Please try again."
                        }
                    }
                }
            }
        }
    }

    /**
     * Clear delete account error
     */
    fun clearDeleteAccountError() {
        _deleteAccountError.value = null
    }

    /**
     * Clear account deleted flag
     */
    fun clearAccountDeleted() {
        _accountDeleted.value = false
    }
}