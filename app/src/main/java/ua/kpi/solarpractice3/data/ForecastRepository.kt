package ua.kpi.solarpractice3.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import retrofit2.HttpException
import ua.kpi.solarpractice3.network.SolarApi
import java.io.IOException

// Repository поєднує мережевий Data Source та локальний кеш Room.
class ForecastRepository(private val dao: ForecastDao, private val api: SolarApi) {
    val observed: Flow<List<ForecastEntity>> = dao.observeAll()

    suspend fun cached(): List<ForecastEntity> = dao.all()

    suspend fun refresh(area: Double = 10.0, efficiencyPercent: Double = 20.0): Int {
        require(area > 0 && efficiencyPercent in 0.0..100.0)
        val answer = api.hourly()
        val hours = answer.hourly ?: error("У відповіді немає погодинних даних")
        val records = hours.time.zip(hours.radiation).mapNotNull { (t, radiation) ->
            radiation?.takeIf { it.isFinite() && it >= 0.0 }?.let { g ->
                ForecastEntity(t, g, g * area * efficiencyPercent / 100.0, System.currentTimeMillis())
            }
        }
        require(records.isNotEmpty()) { "Сервер повернув порожній прогноз" }
        dao.putAll(records)
        return records.size
    }

    // Повторюємо тільки тимчасові відмови, а не HTTP 4xx (крім 429).
    suspend fun refreshWithRetry(): Int {
        repeat(3) { attempt ->
            try { return refresh() }
            catch (e: IOException) {
                if (attempt == 2) throw e
            }
            catch (e: HttpException) {
                if (attempt == 2 || (e.code() < 500 && e.code() != 429)) throw e
            }
            delay(500L * (attempt + 1))
        }
        error("Невідомий стан запиту")
    }

    suspend fun clear() = dao.clear()
}
