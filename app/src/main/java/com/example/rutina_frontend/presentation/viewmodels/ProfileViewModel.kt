package com.example.rutina_frontend.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutina_frontend.data.models.HabitDto
import com.example.rutina_frontend.data.models.UserDto
import com.example.rutina_frontend.di.ServiceLocator
import com.example.rutina_frontend.utils.DataStoreManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ProfileViewModel : ViewModel() {

    private val apiService = ServiceLocator.getApiService()
    private val habitRepository = ServiceLocator.getHabitRepository()

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState = _profileState.asStateFlow()

    private val _statsState = MutableStateFlow<StatsState>(StatsState.Initial)
    val statsState = _statsState.asStateFlow()

    fun loadProfile(context: Context) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getToken()

            println("🔍 ProfileViewModel - Token: ${token?.take(30)}...")

            if (token.isNullOrEmpty()) {
                println("❌ ProfileViewModel - No token found")
                _profileState.value = ProfileState.Error("Токен не найден. Войдите заново.")
                return@launch
            }

            try {
                println("📡 ProfileViewModel - Calling validateToken()")
                val response = apiService.validateToken()
                println("📡 ProfileViewModel - Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val user = response.body()!!
                    println("✅ ProfileViewModel - User loaded: ${user.name}, role: ${user.role}")
                    _profileState.value = ProfileState.Success(user)

                    // Загружаем статистику привычек
                    loadHabitsStats()

                    // Сохраняем роль пользователя
                    saveUserRole(context, user.role)
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("❌ ProfileViewModel - Failed with code ${response.code()}: $errorBody")
                    _profileState.value = ProfileState.Error("Ошибка загрузки профиля (${response.code()})")
                }
            } catch (e: Exception) {
                println("❌ ProfileViewModel - Exception: ${e.message}")
                e.printStackTrace()
                _profileState.value = ProfileState.Error(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    private fun loadHabitsStats() {
        viewModelScope.launch {
            _statsState.value = StatsState.Loading

            println("📊 ProfileViewModel - Loading habits stats...")
            val result = habitRepository.getHabits()
            result.fold(
                onSuccess = { habits ->
                    println("✅ ProfileViewModel - Habits loaded: ${habits.size}")
                    val stats = calculateStats(habits)
                    _statsState.value = StatsState.Success(stats)
                },
                onFailure = { error ->
                    println("❌ ProfileViewModel - Failed to load habits: ${error.message}")
                    _statsState.value = StatsState.Error(error.message ?: "Не удалось загрузить статистику")
                }
            )
        }
    }

    private fun calculateStats(habits: List<HabitDto>): HabitStats {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ISO_DATE_TIME

        val completedHabits = habits.filter { habit ->
            try {
                val endedAt = LocalDateTime.parse(habit.endedAt, formatter)
                endedAt.isBefore(now)
            } catch (e: Exception) {
                false
            }
        }

        val activeHabits = habits.filter { habit ->
            try {
                val endedAt = LocalDateTime.parse(habit.endedAt, formatter)
                endedAt.isAfter(now)
            } catch (e: Exception) {
                true
            }
        }

        return HabitStats(
            totalHabits = habits.size,
            activeHabits = activeHabits.size,
            completedHabits = completedHabits.size,
            completionRate = if (habits.isNotEmpty()) {
                (completedHabits.size.toFloat() / habits.size) * 100
            } else 0f
        )
    }

    private fun saveUserRole(context: Context, role: String) {
        viewModelScope.launch {
            val dataStoreManager = DataStoreManager(context)
            dataStoreManager.saveUserRole(role)
            println("💾 ProfileViewModel - Role saved: $role")
        }
    }

    fun logout(context: Context) {
        viewModelScope.launch {
            val dataStoreManager = DataStoreManager(context)
            dataStoreManager.clearToken()
            _profileState.value = ProfileState.Initial
            _statsState.value = StatsState.Initial
            println("👋 ProfileViewModel - Logged out")
        }
    }
}

data class HabitStats(
    val totalHabits: Int,
    val activeHabits: Int,
    val completedHabits: Int,
    val completionRate: Float
)

sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: UserDto) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

sealed class StatsState {
    object Initial : StatsState()
    object Loading : StatsState()
    data class Success(val stats: HabitStats) : StatsState()
    data class Error(val message: String) : StatsState()
}