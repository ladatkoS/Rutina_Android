package com.example.rutina_frontend.domain.usecases.habits

import com.example.rutina_frontend.data.repository.HabitRepository

class CreateHabitUseCase(
    private val habitRepository: HabitRepository
) {
    suspend operator fun invoke(
        name: String,
        description: String,
        type: String,
        formationPeriod: Int
    ) = habitRepository.createHabit(name, description, type, formationPeriod)
}