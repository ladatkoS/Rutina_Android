package com.example.rutina_frontend.domain.usecases.habits

import com.example.rutina_frontend.data.repository.HabitRepository

class GetHabitsUseCase(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke() = habitRepository.getHabits()
}