package com.example.rutina_frontend.presentation.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rutina_frontend.data.models.AdviceRequest
import com.example.rutina_frontend.data.models.HabitDto
import com.example.rutina_frontend.di.ServiceLocator
import com.example.rutina_frontend.utils.Constants
import com.example.rutina_frontend.utils.DataStoreManager
import com.example.rutina_frontend.utils.HabitNotificationScheduler
import com.example.rutina_frontend.utils.NotificationHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HabitsViewModel : ViewModel() {

    companion object {
        private const val TAG = "HabitsViewModel"
    }

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
        Log.d(TAG, ">>> createHabit: name='$name', period=$formationPeriod")

        viewModelScope.launch {
            _createHabitState.value = CreateHabitState.Loading
            val dataStoreManager = DataStoreManager(context)
            val token = dataStoreManager.getToken()

            if (token.isNullOrEmpty()) {
                Log.e(TAG, "No token!")
                _createHabitState.value = CreateHabitState.Error("No token found")
                return@launch
            }

            Log.d(TAG, "Creating habit on server...")
            val result = habitRepository.createHabit(name, description, type, formationPeriod)
            result.fold(
                onSuccess = { habit ->
                    Log.d(TAG, "Habit created: id=${habit.id}")

                    // Уведомление о создании
                    Log.d(TAG, "Calling showHabitCreatedNotification...")
                    NotificationHelper.showHabitCreatedNotification(context, habit.name, habit.formationPeriod)

                    // AI совет
                    Log.d(TAG, "Calling fetchAdviceAndNotify...")
                    fetchAdviceAndNotify(context, name, description)

                    // Планирование завершения
                    Log.d(TAG, "Scheduling completion notification...")
                    HabitNotificationScheduler.scheduleHabitCompletionNotification(
                        context, habit.id, habit.name, habit.endedAt
                    )

                    _createHabitState.value = CreateHabitState.Success(habit)
                    loadHabits(context)
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to create habit: ${error.message}")
                    _createHabitState.value = CreateHabitState.Error(error.message ?: "Failed to create habit")
                }
            )
        }
    }

    private suspend fun fetchAdviceAndNotify(context: Context, habitName: String, habitDescription: String) {
        Log.d(TAG, "=== fetchAdviceAndNotify START ===")
        Log.d(TAG, "AI_BASE_URL from Constants: ${Constants.AI_BASE_URL}")
        Log.d(TAG, "habitName: $habitName")
        Log.d(TAG, "habitDescription: $habitDescription")

        try {
            val query = "$habitName: $habitDescription"
            Log.d(TAG, "Query: $query")

            Log.d(TAG, "Getting AiApiService...")
            val service = ServiceLocator.getAiApiService()
            Log.d(TAG, "AiApiService: $service")

            val request = AdviceRequest(query)
            Log.d(TAG, "Request: $request")

            Log.d(TAG, "Calling aiApiService.getAdvice()...")
            val response = service.getAdvice(request)

            Log.d(TAG, "Response received!")
            Log.d(TAG, "Response code: ${response.code()}")
            Log.d(TAG, "Response isSuccessful: ${response.isSuccessful}")

            if (response.isSuccessful) {
                val adviceResponse = response.body()
                Log.d(TAG, "Response body: $adviceResponse")

                val advice = adviceResponse?.advice ?: "Продолжайте в том же духе!"
                Log.d(TAG, "Advice: $advice")

                delay(2000)
                Log.d(TAG, "Showing advice notification...")
                NotificationHelper.showAdviceNotification(context, habitName, advice)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e(TAG, "AI returned error: ${response.code()}")
                Log.e(TAG, "Error body: $errorBody")

                NotificationHelper.showAdviceNotification(
                    context, habitName,
                    "Регулярность — ключ к успеху! Выполняйте привычку каждый день."
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "EXCEPTION in fetchAdviceAndNotify!", e)
            Log.e(TAG, "Exception message: ${e.message}")
            Log.e(TAG, "Exception class: ${e.javaClass.name}")
            e.printStackTrace()

            NotificationHelper.showAdviceNotification(
                context, habitName,
                "Полезный совет: начинайте с малого и увеличивайте нагрузку постепенно."
            )
        }

        Log.d(TAG, "=== fetchAdviceAndNotify END ===")
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