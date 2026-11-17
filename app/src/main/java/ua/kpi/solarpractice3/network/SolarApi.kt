package ua.kpi.solarpractice3.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

// Публічний Open-Meteo API повертає реальні погодинні дані сонячної радіації (Вт/м²).
data class HourlySolar(
    val time: List<String> = emptyList(),
    @SerializedName("shortwave_radiation") val radiation: List<Double?> = emptyList()
)
data class SolarResponse(val hourly: HourlySolar? = null)

interface SolarApi {
    @GET("v1/forecast")
    suspend fun hourly(
        @Query("latitude") lat: Double = 50.45,
        @Query("longitude") lon: Double = 30.52,
        @Query("hourly") field: String = "shortwave_radiation",
        @Query("forecast_days") days: Int = 1,
        @Query("timezone") timezone: String = "Europe/Kyiv"
    ): SolarResponse
}

object SolarRemote {
    val api: SolarApi by lazy {
        val client = OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS).build()
        Retrofit.Builder().baseUrl("https://api.open-meteo.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client).build().create(SolarApi::class.java)
    }
}
