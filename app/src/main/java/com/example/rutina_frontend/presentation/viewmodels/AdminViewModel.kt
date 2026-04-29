package com.example.rutina_frontend.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutina_frontend.data.models.UserDto
import com.example.rutina_frontend.di.ServiceLocator
import com.example.rutina_frontend.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel : ViewModel() {

    private val adminRepository = ServiceLocator.getAdminRepository()

    private val _usersState = MutableStateFlow<UsersState>(UsersState.Initial)
    val usersState = _usersState.asStateFlow()

    private val _deleteUserState = MutableStateFlow<DeleteUserState>(DeleteUserState.Initial)
    val deleteUserState = _deleteUserState.asStateFlow()

    fun loadAllUsers(context: Context) {
        viewModelScope.launch {
            _usersState.value = UsersState.Loading
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getToken()

            println("🔍 AdminViewModel - Token: ${token?.take(30)}...")

            if (token.isNullOrEmpty()) {
                println("❌ AdminViewModel - No token found")
                _usersState.value = UsersState.Error("Токен не найден. Войдите заново.")
                return@launch
            }

            println("📡 AdminViewModel - Calling getAllUsers()")
            val result = adminRepository.getAllUsers()
            result.fold(
                onSuccess = { users ->
                    println("✅ AdminViewModel - Users loaded: ${users.size}")
                    _usersState.value = UsersState.Success(users)
                },
                onFailure = { error ->
                    println("❌ AdminViewModel - Failed to load users: ${error.message}")
                    _usersState.value = UsersState.Error(error.message ?: "Не удалось загрузить пользователей")
                }
            )
        }
    }

    fun deleteUser(context: Context, userId: Long, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _deleteUserState.value = DeleteUserState.Loading
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getToken()

            if (token.isNullOrEmpty()) {
                _deleteUserState.value = DeleteUserState.Error("Токен не найден")
                return@launch
            }

            println("📡 AdminViewModel - Deleting user: $userId")
            val result = adminRepository.deleteUser(userId)
            result.fold(
                onSuccess = {
                    println("✅ AdminViewModel - User deleted: $userId")
                    _deleteUserState.value = DeleteUserState.Success
                    loadAllUsers(context)
                    onSuccess()
                },
                onFailure = { error ->
                    println("❌ AdminViewModel - Failed to delete user: ${error.message}")
                    _deleteUserState.value = DeleteUserState.Error(error.message ?: "Не удалось удалить пользователя")
                }
            )
        }
    }

    fun resetDeleteState() {
        _deleteUserState.value = DeleteUserState.Initial
    }
}

sealed class UsersState {
    object Initial : UsersState()
    object Loading : UsersState()
    data class Success(val users: List<UserDto>) : UsersState()
    data class Error(val message: String) : UsersState()
}

sealed class DeleteUserState {
    object Initial : DeleteUserState()
    object Loading : DeleteUserState()
    object Success : DeleteUserState()
    data class Error(val message: String) : DeleteUserState()
}