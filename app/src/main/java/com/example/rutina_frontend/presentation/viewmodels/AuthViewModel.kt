package com.example.rutina_frontend.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutina_frontend.data.models.AuthResponse
import com.example.rutina_frontend.di.ServiceLocator
import com.example.rutina_frontend.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val authRepository = ServiceLocator.getAuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState = _authState.asStateFlow()

    fun login(username: String, password: String, context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.login(username, password)
            result.fold(
                onSuccess = { authResponse ->
                    val dataStoreManager = DataStoreManager(context)
                    dataStoreManager.saveToken(authResponse.token)
                    dataStoreManager.saveUsername(authResponse.username)
                    _authState.value = AuthState.Success(authResponse)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Login failed")
                }
            )
        }
    }

    fun register(name: String, username: String, password: String, phone: String, context: Context) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val result = authRepository.register(name, username, password, phone)
            result.fold(
                onSuccess = { authResponse ->
                    val dataStoreManager = DataStoreManager(context)
                    dataStoreManager.saveToken(authResponse.token)
                    dataStoreManager.saveUsername(authResponse.username)
                    _authState.value = AuthState.Success(authResponse)
                },
                onFailure = { error ->
                    _authState.value = AuthState.Error(error.message ?: "Registration failed")
                }
            )
        }
    }

    fun resetState() {
        _authState.value = AuthState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
}