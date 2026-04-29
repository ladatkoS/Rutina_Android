package com.example.rutina_frontend.domain.usecases.habits

import com.example.rutina_frontend.data.repository.HabitRepository

class DeleteHabitUseCase(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(habitId: Long) = habitRepository.deleteHabit(habitId)
}