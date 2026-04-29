package com.example.rutina_frontend.presentation.screens.habits

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import com.example.rutina_frontend.data.models.HabitDto
import com.example.rutina_frontend.presentation.viewmodels.HabitsState
import com.example.rutina_frontend.presentation.viewmodels.HabitsViewModel
import com.example.rutina_frontend.utils.DataStoreManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsListScreen(
    navController: NavController,
    viewModel: HabitsViewModel = viewModel()
) {
    val context = LocalContext.current
    val habitsState by viewModel.habitsState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<HabitDto?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadHabits(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои привычки") },
                actions = {
                    IconButton(onClick = {
                        val dataStoreManager = DataStoreManager(context)
                        kotlinx.coroutines.GlobalScope.launch {
                            dataStoreManager.clearToken()
                        }
                        navController.navigate("login") {
                            popUpTo("habits") { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Выйти")
                    }
                }
            )
            TopAppBar(
                title = { Text("Мои привычки") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate("profile") }) {
                        Icon(Icons.Default.Person, contentDescription = "Профиль")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            val dataStoreManager = DataStoreManager(context)
                            dataStoreManager.clearToken()
                            navController.navigate("login") {
                                popUpTo("habits") { inclusive = true }
                            }
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Выйти")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_habit") }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Добавить привычку")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (habitsState) {
                is HabitsState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is HabitsState.Success -> {
                    val habits = (habitsState as HabitsState.Success).habits
                    if (habits.isEmpty()) {
                        Text(
                            text = "У вас пока нет привычек. Добавьте первую!",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(habits) { habit ->
                                HabitCard(
                                    habit = habit,
                                    onDelete = { showDeleteDialog = habit }
                                )
                            }
                        }
                    }
                }
                is HabitsState.Error -> {
                    Text(
                        text = (habitsState as HabitsState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {}
            }
        }
    }

    showDeleteDialog?.let { habit ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Удалить привычку?") },
            text = { Text("Вы уверены, что хотите удалить привычку '${habit.name}'?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHabit(context, habit.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Удалить")
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
fun HabitCard(
    habit: HabitDto,
    onDelete: () -> Unit
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
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = habit.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Период: ${habit.formationPeriod} мин",
                    style = MaterialTheme.typography.bodySmall
                )
            }
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