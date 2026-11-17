package ua.kpi.solarpractice3.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import ua.kpi.solarpractice3.auth.DemoAuth
import ua.kpi.solarpractice3.data.*
import ua.kpi.solarpractice3.network.SolarRemote
import java.io.IOException
import java.net.SocketTimeoutException

data class ScreenState(val loading: Boolean = false, val message: String = "", val filter: String = "", val descending: Boolean = false)

class SolarViewModel(app: Application): AndroidViewModel(app) {
    private val repo = ForecastRepository(SolarDatabase.get(app).forecasts(), SolarRemote.api)
    private val auth = DemoAuth(app)
    private val mutable = MutableStateFlow(ScreenState())
    val ui = mutable.asStateFlow()
    val forecasts = repo.observed.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val roleState = MutableStateFlow(auth.role())
    val role = roleState.asStateFlow()
    fun setFilter(s: String) { mutable.update { it.copy(filter = s) } }
    fun toggleSort() { mutable.update { it.copy(descending = !it.descending) } }
    val visible = combine(forecasts, ui) { values, state ->
        val filtered = values.filter { it.hour.contains(state.filter, ignoreCase = true) }
        if (state.descending) filtered.sortedByDescending { it.powerWatts } else filtered.sortedBy { it.hour }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ViewModel ізолює UI від Coroutine, Retrofit і Room та передає реактивний стан.
    fun refresh() = viewModelScope.launch {
        mutable.update { it.copy(loading = true, message = "Завантаження прогнозу…") }
        try {
            val count = repo.refreshWithRetry()
            mutable.update { it.copy(loading = false, message = "Отримано $count значень; кеш Room оновлено") }
        } catch (e: Exception) {
            val explanation = when(e) {
                is SocketTimeoutException -> "Час очікування вичерпано. Спробуйте ще раз."
                is IOException -> "Немає доступу до мережі. Відображено збережені дані."
                is HttpException -> when(e.code()) {
                    401, 403 -> "Доступ відхилено (HTTP ${e.code()})"
                    404 -> "Ресурс не знайдено (HTTP 404)"
                    in 500..599 -> "Помилка сервера (HTTP ${e.code()})"
                    else -> "HTTP ${e.code()}"
                }
                else -> e.message ?: "Некоректна відповідь сервера"
            }
            mutable.update { it.copy(loading = false, message = explanation) }
        }
    }
    fun clearCache() = viewModelScope.launch { repo.clear(); mutable.update { it.copy(message = "Кеш очищено") } }
    fun login(email: String, password: String) : Boolean {
        val ok = auth.login(email, password); roleState.value = auth.role()
        mutable.update { it.copy(message = if(ok) "Демонстраційний вхід виконано" else "Неправильні облікові дані") }
        return ok
    }
    fun logout() { auth.logout(); roleState.value = auth.role(); mutable.update { it.copy(message = "Вихід виконано") } }
}
