package com.example.rutina_frontend.domain.usecases.auth

import com.example.rutina_frontend.data.repository.AuthRepository

class LoginUseCase(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String) =
        authRepository.login(username, password)
}