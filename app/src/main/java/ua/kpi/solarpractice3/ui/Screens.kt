package ua.kpi.solarpractice3.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ua.kpi.solarpractice3.viewmodel.SolarViewModel
import ua.kpi.solarpractice3.data.ForecastEntity
import java.util.Locale

// Окремі Composable демонструють навігацію та повторне використання інтерфейсу.
@Composable fun SolarApp(vm: SolarViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    val titles = listOf("ПЗ 9 · Repository", "ПЗ 10 · REST API", "ПЗ 11 · Токен", "ПЗ 12 · Помилки")
    Scaffold(topBar = { Surface(tonalElevation = 2.dp) { Text("SolarPractice · ПР №3", Modifier.fillMaxWidth().padding(20.dp), style = MaterialTheme.typography.titleLarge) } }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
            Text("Сонячна електростанція · прогноз генерації", style = MaterialTheme.typography.bodyMedium)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                titles.forEachIndexed { idx, _ ->
                    FilterChip(selected = tab == idx, onClick = { tab = idx }, label = { Text("ПЗ ${idx+9}") }, modifier = Modifier.weight(1f))
                }
            }
            Text(titles[tab], style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            when(tab) {
                0 -> RepositoryScreen(vm)
                1 -> ApiScreen(vm)
                2 -> AuthScreen(vm)
                else -> ErrorScreen(vm)
            }
        }
    }
}

@Composable private fun ForecastCards(vm: SolarViewModel) {
    val list by vm.visible.collectAsStateWithLifecycle()
    val state by vm.ui.collectAsStateWithLifecycle()
    OutlinedTextField(value = state.filter, onValueChange = vm::setFilter, label = { Text("Фільтр за годиною") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
    TextButton(onClick = vm::toggleSort) { Text(if (state.descending) "Сортування: потужність ↓" else "Сортування: час ↑") }
    Text("Записів у списку: ${list.size}")
    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items(list, key={it.hour}) { ForecastCard(it) }
    }
}
@Composable private fun ForecastCard(item: ForecastEntity) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp)) {
            Text(item.hour, fontWeight = FontWeight.Bold)
            Text("Радіація: ${item.radiation} Вт/м²")
            Text(String.format(Locale.US, "Прогноз: %.0f Вт", item.powerWatts))
        }
    }
}
@Composable private fun Status(vm: SolarViewModel) {
    val state by vm.ui.collectAsStateWithLifecycle()
    if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    if (state.message.isNotEmpty()) Text(state.message, color = MaterialTheme.colorScheme.primary)
}
@Composable private fun RepositoryScreen(vm: SolarViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Repository поєднує мережевий Data Source і кеш Room; UI отримує StateFlow.")
        Button(onClick=vm::refresh) { Text("Синхронізувати з REST API") }
        Status(vm)
        ForecastCards(vm)
    }
}
@Composable private fun ApiScreen(vm: SolarViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Retrofit + Gson отримують погодинну сонячну радіацію з публічного Open-Meteo API.")
        Text("P = G × 10 м² × 20% — демонстраційний розрахунок потужності.")
        Button(onClick=vm::refresh) { Text("GET /v1/forecast") }
        Status(vm)
        ForecastCards(vm)
    }
}
@Composable private fun AuthScreen(vm: SolarViewModel) {
    val role by vm.role.collectAsStateWithLifecycle()
    var email by remember { mutableStateOf("admin@solar.test") }
    var password by remember { mutableStateOf("") }
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Навчальна імітація токена. Не JWT і не реальна серверна авторизація.")
        OutlinedTextField(email, {email=it}, label={Text("Email")}, modifier=Modifier.fillMaxWidth())
        OutlinedTextField(password, {password=it}, label={Text("Пароль")}, modifier=Modifier.fillMaxWidth())
        Text("Демо пароль: solar1234; admin@solar.test — адміністратор, інший email — перегляд.")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick={vm.login(email,password)}) { Text("Увійти") }
            OutlinedButton(onClick=vm::logout) { Text("Вийти") }
        }
        Text("Поточна роль: $role", fontWeight=FontWeight.Bold)
        if (role == "admin") Text("Доступний розділ адміністратора (лише демонстрація UI)")
        if (role == "viewer") Text("Доступні функції перегляду")
        Status(vm)
    }
}
@Composable private fun ErrorScreen(vm: SolarViewModel) {
    val state by vm.ui.collectAsStateWithLifecycle()
    Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Повторні спроби: до 3 разів для збоїв мережі, HTTP 429 і HTTP 5xx.")
        Text("HTTP 401/403, 404 та інші постійні помилки не повторюються автоматично.")
        Button(onClick=vm::refresh, enabled=!state.loading) { Text("Завантажити / повторити") }
        if (state.loading) CircularProgressIndicator()
        Status(vm)
        OutlinedButton(onClick=vm::clearCache) { Text("Очистити локальний кеш") }
        Text("У разі помилки кешовані дані залишаються доступними у вкладці ПЗ 9.")
    }
}
