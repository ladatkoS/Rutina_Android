package com.example.rutina_frontend.presentation.screens.admin

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rutina_frontend.data.models.UserDto
import com.example.rutina_frontend.presentation.viewmodels.AdminViewModel
import com.example.rutina_frontend.presentation.viewmodels.UsersState
import com.example.rutina_frontend.utils.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUsersScreen(
    navController: NavController,
    viewModel: AdminViewModel = viewModel()
) {
    val context = LocalContext.current
    val usersState by viewModel.usersState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<UserDto?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadAllUsers(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление пользователями") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val dataStoreManager = DataStoreManager(context)
                            dataStoreManager.clearToken()
                            navController.navigate("login") {
                                popUpTo("admin_users") { inclusive = true }
                            }
                        }
                    }){
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
            when (usersState) {
                is UsersState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is UsersState.Success -> {
                    val users = (usersState as UsersState.Success).users
                    if (users.isEmpty()) {
                        Text(
                            text = "Нет зарегистрированных пользователей",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(users) { user ->
                                AdminUserCard(
                                    user = user,
                                    onDelete = { showDeleteDialog = user },
                                    onViewDetails = {
                                        // Можно добавить детальный просмотр
                                    }
                                )
                            }
                        }
                    }
                }
                is UsersState.Error -> {
                    Text(
                        text = (usersState as UsersState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {}
            }
        }
    }

    showDeleteDialog?.let { user ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить пользователя?") },
            text = {
                Text("Вы уверены, что хотите удалить пользователя '${user.name}' (${user.email})? Это действие нельзя отменить.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(context, user.id) {
                            showDeleteDialog = null
                        }
                    }
                ) {
                    Text("Удалить", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
fun AdminUserCard(
    user: UserDto,
    onDelete: () -> Unit,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = user.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row {
                        // Активные привычки
                        Text(
                            text = "Активных: ${user.countOfHabits}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Завершённые = очки
                        Text(
                            text = "Завершено: ${user.totalScore}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(
                        text = "Роль: ${user.role}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (user.role == "ADMIN") {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (user.role != "ADMIN") {
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Удалить",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}