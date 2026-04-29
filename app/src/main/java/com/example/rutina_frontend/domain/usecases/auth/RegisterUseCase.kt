package com.example.rutina_frontend.domain.usecases.auth

import com.example.rutina_frontend.data.repository.AuthRepository

class RegisterUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        name: String,
        username: String,
        password: String,
        phone: String
    ) = authRepository.register(name, username, password, phone)
}