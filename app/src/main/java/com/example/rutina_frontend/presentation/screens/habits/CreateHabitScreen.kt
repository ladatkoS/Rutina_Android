package com.example.rutina_frontend.presentation.screens.habits

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.rutina_frontend.presentation.viewmodels.CreateHabitState
import com.example.rutina_frontend.presentation.viewmodels.HabitsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateHabitScreen(
    navController: NavController,
    viewModel: HabitsViewModel = viewModel()
) {
    val context = LocalContext.current
    val createHabitState by viewModel.createHabitState.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var formationPeriod by remember { mutableStateOf("") }

    LaunchedEffect(createHabitState) {
        if (createHabitState is CreateHabitState.Success) {
            navController.navigateUp()
            viewModel.resetCreateHabitState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Новая привычка") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Название привычки") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            OutlinedTextField(
                value = formationPeriod,
                onValueChange = { formationPeriod = it.filter { char -> char.isDigit() } },
                label = { Text("Период формирования (в минутах)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            when (createHabitState) {
                is CreateHabitState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                }
                is CreateHabitState.Error -> {
                    Text(
                        text = (createHabitState as CreateHabitState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                else -> {}
            }

            Button(
                onClick = {
                    viewModel.createHabit(
                        context = context,
                        name = name,
                        description = description,
                        type = "DEFAULT",  // ← Значение по умолчанию
                        formationPeriod = formationPeriod.toIntOrNull() ?: 0
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = name.isNotBlank() &&
                        description.isNotBlank() &&
                        formationPeriod.isNotBlank() &&
                        createHabitState !is CreateHabitState.Loading
            ) {
                Text("Создать привычку")
            }
        }
    }
}