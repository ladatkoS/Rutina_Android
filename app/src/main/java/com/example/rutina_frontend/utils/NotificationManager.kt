package com.example.rutina_frontend.utils

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.work.*
import com.example.rutina_frontend.workers.HabitReminderWorker
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

object HabitNotificationScheduler {

    @RequiresApi(Build.VERSION_CODES.O)
    fun scheduleHabitCompletionNotification(
        context: Context,
        habitId: Long,
        habitName: String,
        endedAt: String
    ) {
        try {
            val formatter = DateTimeFormatter.ISO_DATE_TIME
            val endTime = LocalDateTime.parse(endedAt, formatter)
            val now = LocalDateTime.now()

            val delayMinutes = Duration.between(now, endTime).toMinutes()

            if (delayMinutes > 0) {
                val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
                    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
                    .setInputData(
                        workDataOf(
                            "habit_id" to habitId,
                            "habit_name" to habitName
                        )
                    )
                    .addTag("habit_${habitId}")
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelHabitNotification(context: Context, habitId: Long) {
        WorkManager.getInstance(context).cancelAllWorkByTag("habit_${habitId}")
    }
}