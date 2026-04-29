package com.example.rutina_frontend.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.rutina_frontend.presentation.screens.admin.AdminUsersScreen
import com.example.rutina_frontend.presentation.screens.auth.LoginScreen
import com.example.rutina_frontend.presentation.screens.auth.RegisterScreen
import com.example.rutina_frontend.presentation.screens.habits.CreateHabitScreen
import com.example.rutina_frontend.presentation.screens.habits.HabitsListScreen
import com.example.rutina_frontend.presentation.screens.profile.ProfileScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("habits") {
            HabitsListScreen(navController = navController)
        }

        composable("create_habit") {
            CreateHabitScreen(navController = navController)
        }

        composable("profile") {
            ProfileScreen(navController = navController)
        }

        composable("admin_users") {
            AdminUsersScreen(navController = navController)
        }
    }
}