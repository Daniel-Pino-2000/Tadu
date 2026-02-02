package com.myapp.tadu.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
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

    private val _authResult = MutableLiveData<Result<User>?>()
    val authResult: LiveData<Result<User>?> get() = _authResult



    fun signUp(email: String, password: String, firstName: String, lastName: String) {
        viewModelScope.launch {
            _authResult.value = userRepository.signUp(email, password, firstName, lastName)
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authResult.value = userRepository.login(email, password)
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            // 1Clear local tasks so next user sees empty DB
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
}


