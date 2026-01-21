package com.myapp.tadu.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.myapp.tadu.data.remote.Injection
import com.myapp.tadu.data.remote.UserRepository
import kotlinx.coroutines.launch
import com.myapp.tadu.data.remote.Result
import com.myapp.tadu.data.remote.User

class AuthViewModel : ViewModel() {
    private val userRepository: UserRepository

    private val _authResult = MutableLiveData<Result<User>?>()
    val authResult: LiveData<Result<User>?> get() = _authResult


    init {
        userRepository = UserRepository(
            FirebaseAuth.getInstance(),
            Injection.instance()
        )
    }

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
        viewModelScope.launch {
            userRepository.logout()
            _authResult.value = null           // clear any previous auth result
        }
    }

    // Optional helper to check if user is logged in
    fun isUserLoggedIn(): Boolean {
        return userRepository.currentUserId != null
    }
}