package com.example.rutina_frontend.presentation.screens.profile

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rutina_frontend.data.models.UserDto
import com.example.rutina_frontend.presentation.viewmodels.ProfileState
import com.example.rutina_frontend.presentation.viewmodels.ProfileViewModel
import com.example.rutina_frontend.utils.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = viewModel()
) {
    val context = LocalContext.current
    val profileState by viewModel.profileState.collectAsState()
    val scope = rememberCoroutineScope()

    var isAdmin by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val dataStoreManager = DataStoreManager(context)
        val savedRole = dataStoreManager.getUserRole()
        isAdmin = savedRole == "ADMIN"
        viewModel.loadProfile(context)
    }

    LaunchedEffect(profileState) {
        if (profileState is ProfileState.Success) {
            val user = (profileState as ProfileState.Success).user
            isAdmin = isAdmin || user.role == "ADMIN"
            val dataStoreManager = DataStoreManager(context)
            dataStoreManager.saveUserRole(user.role)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль") },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                viewModel.logout(context)
                                navController.navigate("login") {
                                    popUpTo("profile") { inclusive = true }
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Выйти")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (profileState) {
                is ProfileState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProfileState.Success -> {
                    val user = (profileState as ProfileState.Success).user
                    ProfileContent(
                        user = user,
                        isAdmin = isAdmin,
                        onRefresh = { viewModel.loadProfile(context) },
                        onAdminPanelClick = { navController.navigate("admin_users") }
                    )
                }
                is ProfileState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = (profileState as ProfileState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadProfile(context) }) {
                            Text("Повторить")
                        }
                    }
                }
                else -> {}
            }
        }
    }
}

@Composable
fun ProfileContent(
    user: UserDto,
    isAdmin: Boolean,
    onRefresh: () -> Unit,
    onAdminPanelClick: () -> Unit
) {
    val actualIsAdmin = isAdmin || user.role == "ADMIN"
    val completedHabits = user.totalScore

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Аватар
        Card(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape),
            colors = CardDefaults.cardColors(
                containerColor = if (actualIsAdmin) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.primary
                }
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (actualIsAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (actualIsAdmin) {
            Text(
                text = "👑 Администратор",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Кнопка админ-панели
        if (actualIsAdmin) {
            Button(
                onClick = onAdminPanelClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(Icons.Default.People, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Управление пользователями")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Основная информация
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Основная информация",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ProfileInfoRow("📞 Телефон", user.phone)
                ProfileInfoRow("💰 Баланс", "${user.balance}")
                ProfileInfoRow("📅 Дата регистрации", user.createdAt.substring(0, 10))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Статистика привычек
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "📊 Статистика привычек",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                ProfileInfoRow("🔄 Активных привычек", "${user.countOfHabits}")
                ProfileInfoRow("✅ Завершено привычек", "$completedHabits")
                ProfileInfoRow("🏆 Всего очков", "${user.totalScore}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Обновить данные")
        }
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}