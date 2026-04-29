package com.example.rutina_frontend.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutina_frontend.data.models.AdviceRequest
import com.example.rutina_frontend.data.models.HabitDto
import com.example.rutina_frontend.di.ServiceLocator
import com.example.rutina_frontend.utils.DataStoreManager
import com.example.rutina_frontend.utils.HabitNotificationScheduler
import com.example.rutina_frontend.utils.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HabitsViewModel : ViewModel() {

    private val habitRepository = ServiceLocator.getHabitRepository()
    private val aiApiService = ServiceLocator.getAiApiService()

    private val _habitsState = MutableStateFlow<HabitsState>(HabitsState.Initial)
    val habitsState = _habitsState.asStateFlow()

    private val _createHabitState = MutableStateFlow<CreateHabitState>(CreateHabitState.Initial)
    val createHabitState = _createHabitState.asStateFlow()

    fun loadHabits(context: Context) {
        viewModelScope.launch {
            _habitsState.value = HabitsState.Loading
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getToken()

            if (token.isNullOrEmpty()) {
                _habitsState.value = HabitsState.Error("No token found")
                return@launch
            }

            val result = habitRepository.getHabits()
            result.fold(
                onSuccess = { habits ->
                    _habitsState.value = HabitsState.Success(habits)
                },
                onFailure = { error ->
                    _habitsState.value = HabitsState.Error(error.message ?: "Failed to load habits")
                }
            )
        }
    }

    fun createHabit(
        context: Context,
        name: String,
        description: String,
        type: String,
        formationPeriod: Int
    ) {
        viewModelScope.launch {
            _createHabitState.value = CreateHabitState.Loading
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getToken()

            if (token.isNullOrEmpty()) {
                _createHabitState.value = CreateHabitState.Error("No token found")
                return@launch
            }

            val result = habitRepository.createHabit(name, description, type, formationPeriod)
            result.fold(
                onSuccess = { habit ->
                    // Уведомление о создании
                    NotificationHelper.showHabitCreatedNotification(context, habit.name, habit.formationPeriod)

                    // Запрашиваем совет у нейросети
                    fetchAdviceAndNotify(context, name, description)

                    // Планируем уведомление о завершении
                    HabitNotificationScheduler.scheduleHabitCompletionNotification(
                        context,
                        habit.id,
                        habit.name,
                        habit.endedAt
                    )

                    _createHabitState.value = CreateHabitState.Success(habit)
                    loadHabits(context)
                },
                onFailure = { error ->
                    _createHabitState.value = CreateHabitState.Error(error.message ?: "Failed to create habit")
                }
            )
        }
    }

    // НОВОЕ: Запрос совета у нейросети и показ уведомления
    private fun fetchAdviceAndNotify(context: Context, habitName: String, habitDescription: String) {
        viewModelScope.launch {
            try {
                // Формируем запрос: название + описание
                val query = "$habitName: $habitDescription"
                val response = aiApiService.getAdvice(AdviceRequest(query))

                if (response.isSuccessful) {
                    val advice = response.body()?.advice ?: "Продолжайте в том же духе!"
                    // Показываем уведомление с советом (с задержкой 2 секунды после создания)
                    kotlinx.coroutines.delay(2000)
                    NotificationHelper.showAdviceNotification(context, habitName, advice)
                } else {
                    // Если нейросеть недоступна, показываем стандартное сообщение
                    NotificationHelper.showAdviceNotification(
                        context,
                        habitName,
                        "Регулярность — ключ к успеху! Выполняйте привычку каждый день."
                    )
                }
            } catch (e: Exception) {
                // Если ошибка соединения с нейросетью
                println("⚠️ Не удалось получить совет от нейросети: ${e.message}")
                // Можно показать запасное уведомление или промолчать
            }
        }
    }

    fun deleteHabit(context: Context, habitId: Long) {
        viewModelScope.launch {
            val result = habitRepository.deleteHabit(habitId)
            result.fold(
                onSuccess = {
                    HabitNotificationScheduler.cancelHabitNotification(context, habitId)
                    loadHabits(context)
                },
                onFailure = { }
            )
        }
    }

    fun resetCreateHabitState() {
        _createHabitState.value = CreateHabitState.Initial
    }
}

sealed class HabitsState {
    object Initial : HabitsState()
    object Loading : HabitsState()
    data class Success(val habits: List<HabitDto>) : HabitsState()
    data class Error(val message: String) : HabitsState()
}

sealed class CreateHabitState {
    object Initial : CreateHabitState()
    object Loading : CreateHabitState()
    data class Success(val habit: HabitDto) : CreateHabitState()
    data class Error(val message: String) : CreateHabitState()
}