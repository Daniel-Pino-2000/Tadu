package com.myapp.tadu.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.FirebaseNetworkException
import com.myapp.tadu.Graph
import com.myapp.tadu.data.TaskRepository
import com.myapp.tadu.data.remote.Injection
import com.myapp.tadu.data.remote.Result
import com.myapp.tadu.data.remote.User
import com.myapp.tadu.data.remote.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.UnknownHostException

class AuthViewModel(
    private val taskRepository: TaskRepository = Graph.taskRepository
) : ViewModel() {

    private val userRepository: UserRepository = UserRepository(
        FirebaseAuth.getInstance(),
        Injection.instance()
    )

    private val _authResult = MutableLiveData<Result<User>?>()
    val authResult: LiveData<Result<User>?> get() = _authResult

    private val _passwordResetResult = MutableLiveData<Result<Unit>?>()
    val passwordResetResult: LiveData<Result<Unit>?> get() = _passwordResetResult

    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            try {
                _authResult.value = userRepository.signUp(email, password, firstName, lastName)
            } catch (e: Exception) {
                _authResult.value = Result.Error(e)
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authResult.value = userRepository.login(email, password)
            } catch (e: Exception) {
                _authResult.value = Result.Error(e)
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _passwordResetResult.value = userRepository.sendPasswordResetEmail(email)
            } catch (e: Exception) {
                _passwordResetResult.value = Result.Error(e)
            }
        }
    }

    fun clearPasswordResetResult() {
        _passwordResetResult.value = null
    }

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

    fun isUserLoggedIn(): Boolean {
        return userRepository.currentUserId != null
    }

    fun clearAuthResult() {
        _authResult.value = null
    }
}