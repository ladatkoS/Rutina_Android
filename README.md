
# Rutina — Android Client

**Rutina** — Android-приложение для формирования полезных привычек с персональными AI-советами от нейросети.

Проект является мобильным клиентом для микросервисной экосистемы **Rutina**, которая включает:
- [Rutina Gateway](https://github.com/ladatkoS/Rutina_Gateway_Service.git) — API Gateway
- [Rutina Auth](https://github.com/ladatkoS/Rutina_Auth_Service.git) — аутентификация и пользователи
- [Rutina Main](https://github.com/ladatkoS/Rutina_Main_Service.git) — управление привычками
- [Rutina Neural Network](https://github.com/AntonSlon/Rutina-neural-network.git) — NLP-сервис советов

---

## Основные функции

### Для пользователя

1. **Создание привычек** — название, описание, период формирования
2. **AI-советы** — при создании привычки нейросеть анализирует описание и присылает персональную push-рекомендацию
3. **Push-уведомления**:
   - 🎯 О создании привычки
   - 🤖 AI-совет по привычке (задержка 2 секунды)
   - ✅ О завершении привычки (по истечении времени)
4. **Профиль** — информация о пользователе и детальная статистика:
   - Количество активных привычек
   - Количество завершённых привычек
   - Общий счёт (очки)
   - Дата регистрации
5. **Удаление привычек** — с отменой запланированных уведомлений

### Для администратора

1. **Админ-панель** — список всех пользователей
2. **Статистика пользователей** — активные/завершённые привычки
3. **Удаление пользователей** — с подтверждением

---

## Технический стек

### Язык и платформа
- **Kotlin** — основной язык
- **Android SDK** (minSdk: 24, targetSdk: 34)

### UI
- **Jetpack Compose** — декларативный UI
- **Material 3 (Material You)** — дизайн-система с динамическими цветами
- **Navigation Compose** — навигация между экранами

### Архитектура
- **MVVM** (Model-View-ViewModel) — разделение ответственности
- **Ручной DI** (ServiceLocator) — без Hilt для упрощения и совместимости с JDK 21

### Сеть
- **Retrofit 2** — HTTP клиент
- **OkHttp** — перехватчики запросов
- **Gson** — JSON сериализация

### Локальное хранение
- **DataStore Preferences** — сохранение JWT токенов и роли пользователя

### Фоновые задачи
- **WorkManager** — отложенные уведомления о завершении привычек

### Уведомления
- **NotificationCompat** — построение уведомлений
- **NotificationChannel** — каналы уведомлений (Android 8+)

---

## Структура проекта

```
app/src/main/java/com/example/rutina_frontend/
├── data/
│   ├── api/
│   │   ├── ApiService.kt              # REST API (основной бэкенд)
│   │   ├── AiApiService.kt            # REST API (нейросеть)
│   │   └── AuthInterceptor.kt         # Interceptor JWT токенов
│   ├── models/
│   │   ├── AuthModels.kt              # Модели авторизации
│   │   ├── Habit.kt                   # Модели привычек
│   │   ├── User.kt                    # Модель пользователя
│   │   └── AdviceModels.kt            # Модели AI-советов
│   └── repository/
│       ├── AuthRepository.kt          # Авторизация
│       ├── HabitRepository.kt         # Привычки
│       └── AdminRepository.kt         # Админ-панель
├── di/
│   └── ServiceLocator.kt              # Ручной DI контейнер
├── domain/usecases/
│   ├── auth/
│   │   ├── LoginUseCase.kt
│   │   └── RegisterUseCase.kt
│   └── habits/
│       ├── CreateHabitUseCase.kt
│       ├── GetHabitsUseCase.kt
│       └── DeleteHabitUseCase.kt
├── presentation/
│   ├── navigation/
│   │   └── Navigation.kt              # NavHost
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt         # Экран входа
│   │   │   └── RegisterScreen.kt      # Экран регистрации
│   │   ├── habits/
│   │   │   ├── HabitsListScreen.kt    # Список привычек
│   │   │   ├── CreateHabitScreen.kt   # Создание привычки
│   │   │   └── HabitDetailScreen.kt   # Детали привычки
│   │   ├── admin/
│   │   │   └── AdminUsersScreen.kt    # Управление пользователями
│   │   └── profile/
│   │       └── ProfileScreen.kt       # Профиль и статистика
│   ├── theme/
│   │   ├── Color.kt                   # Цвета
│   │   ├── Theme.kt                   # Тема Material 3
│   │   └── Type.kt                    # Типографика
│   └── viewmodels/
│       ├── AuthViewModel.kt           # Логика авторизации
│       ├── HabitsViewModel.kt         # Логика привычек
│       ├── ProfileViewModel.kt        # Логика профиля
│       └── AdminViewModel.kt          # Логика админ-панели
├── workers/
│   └── HabitReminderWorker.kt         # WorkManager Worker
└── utils/
    ├── Constants.kt                    # URL конфигурация
    ├── DataStoreManager.kt             # Менеджер токенов и ролей
    ├── Extensions.kt                   # Extension-функции
    ├── HabitNotificationScheduler.kt   # Планировщик уведомлений
    └── NotificationHelper.kt           # Утилиты уведомлений
```

---

## Архитектура MVVM

```
┌─────────────────────────────────────────────────────┐
│  Screen (Composable)                                │
│  - Отображает UI                                    │
│  - Отправляет действия во ViewModel                 │
│  - Наблюдает State через StateFlow                  │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│  ViewModel                                          │
│  - Управляет состоянием экрана (StateFlow)          │
│  - Вызывает Repository                              │
│  - Запускает корутины (viewModelScope)              │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│  Repository                                         │
│  - Вызывает ApiService (Retrofit)                   │
│  - Обрабатывает ответы (Result<T>)                  │
└────────────────────┬────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│  ApiService / AiApiService (Retrofit)               │
│  - HTTP запросы к бэкенду и нейросети               │
│  - Interceptor добавляет JWT токен                  │
└─────────────────────────────────────────────────────┘


---

## Детали реализации

### JWT авторизация

Токен сохраняется в **DataStore** (зашифрованное локальное хранилище) и автоматически добавляется ко всем запросам через `AuthInterceptor`:

```kotlin
val newRequest = request.newBuilder()
    .addHeader("Authorization", "Bearer $cleanToken")
    .build()
```

**Особенность:** Interceptor сам очищает токен от дублирующегося `Bearer ` и добавляет правильный заголовок. ViewModel и Repository не занимаются форматированием токена.

### Push-уведомления

**Канал уведомлений** создаётся при запуске приложения:

```kotlin
val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance)
notificationManager.createNotificationChannel(channel)
```

**Три типа уведомлений:**
| Тип | Когда | Содержание |
|-----|-------|------------|
| Создание | Сразу при создании привычки | "Привычка создана! Будет формироваться X мин." |
| AI-совет | Через 2 сек после создания | Ответ нейросети на основе описания |
| Завершение | По истечении периода | "Привычка завершена! Поздравляем!" |

**WorkManager** планирует уведомление о завершении на точное время:

```kotlin
val workRequest = OneTimeWorkRequestBuilder<HabitReminderWorker>()
    .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
    .setInputData(workDataOf("habit_name" to habitName))
    .build()
```

При удалении привычки запланированное уведомление **отменяется**.

### Android 13+ (TIRAMISU)

Приложение запрашивает разрешение `POST_NOTIFICATIONS` при запуске:

```kotlin
registerForActivityResult(
    ActivityResultContracts.RequestPermission()
) { isGranted -> /* обработка */ }
```

### AI интеграция

При создании привычки:
1. Название и описание отправляются в нейросеть: `POST http://IP:8000/advice`
2. Ответ парсится и показывается в уведомлении
3. Если нейросеть недоступна — **fallback** сообщение

```kotlin
try {
    val response = aiApiService.getAdvice(AdviceRequest(query))
    if (response.isSuccessful) {
        NotificationHelper.showAdviceNotification(context, habitName, advice)
    }
} catch (e: Exception) {
    // Fallback
    NotificationHelper.showAdviceNotification(context, habitName, fallbackAdvice)
}
```

---

## Экраны приложения

### Вход
- Поля: Email, Пароль
- Кнопка: "Войти"
- Ссылка: "Нет аккаунта? Зарегистрироваться"
- Обработка ошибок (неверный пароль, пользователь не найден)

### Регистрация
- Поля: Имя, Email, Телефон, Пароль
- Кнопка: "Зарегистрироваться"
- Ссылка: "Уже есть аккаунт? Войти"

### Мои привычки
- Список активных привычек
- Карточка: название, описание, период
- Кнопка удаления с подтверждением
- FAB (+) для создания новой
- Кнопка выхода и перехода в профиль

### Новая привычка
- Поля: Название, Описание, Период (минуты)
- Валидация: все поля обязательны
- Кнопка: "Создать привычку"

### Профиль
- Аватар (цвет зависит от роли)
- Имя, Email, Роль
- Для ADMIN: кнопка "Управление пользователями"
- Основная информация: телефон, баланс, дата регистрации
- Статистика: активные/завершённые привычки, очки
- Кнопка "Обновить данные"
- Кнопка выхода

### Пользователи (ADMIN)
- Список всех пользователей
- Карточка: имя, email, роль, счётчики привычек
- Кнопка удаления (только для USER, не для ADMIN)
- Подтверждение удаления

---

## Настройка и запуск

### 1. Клонировать репозиторий

```bash
git clone https://github.com/ladatkoS/Rutina_Android.git
```

### 2. Открыть в Android Studio

- **File → Open** → выбрать папку проекта
- Дождаться синхронизации Gradle

### 3. Настроить URL бэкенда

В файле `app/src/main/java/com/example/rutina_frontend/utils/Constants.kt`:

```kotlin
object Constants {
    // Для эмулятора Android:
    const val BASE_URL = "http://10.0.2.2:8081/"
    const val AI_BASE_URL = "http://10.0.2.2:8000/"
    
    // Для реального устройства (узнайте IP компьютера через ipconfig):
    // const val BASE_URL = "http://192.168.1.9:8081/"
    // const val AI_BASE_URL = "http://192.168.1.9:8000/"
}
```

### 4. Запустить все микросервисы

Убедитесь, что запущены:

| Сервис | Порт |
|--------|------|
| Gateway | 8081 |
| Auth Service | 8082 |
| Main Service | 8083 |
| Neural Network | 8000 |

### 5. Запустить приложение

- Подключить устройство или эмулятор
- Нажать **Run 'app'** (Shift+F10)

---

## Учётные данные по умолчанию

| Роль | Email | Пароль |
|------|-------|--------|
| ADMIN | `admin@example.com` | `Admin123!` |

---

## Системные требования

- **Android 7.0** (API 24) и выше
- **Для динамических цветов:** Android 12+ (API 31)
- **Для уведомлений на Android 13+:** разрешение пользователя
- **Сетевое соединение** с Gateway и нейросетью

---
