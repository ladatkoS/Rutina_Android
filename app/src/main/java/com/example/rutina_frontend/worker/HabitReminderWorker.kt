package com.example.rutina_frontend.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.rutina_frontend.utils.NotificationHelper

class HabitReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val habitName = inputData.getString("habit_name") ?: "Привычка"
        val habitId = inputData.getLong("habit_id", 0)

        // Показываем уведомление о завершении привычки
        NotificationHelper.showHabitCompletedNotification(applicationContext, habitName)

        return Result.success()
    }
}