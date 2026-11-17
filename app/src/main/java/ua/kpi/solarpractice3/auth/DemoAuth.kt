package ua.kpi.solarpractice3.auth

import android.content.Context

// Навчальна імітація авторизації: токен не JWT, не підписаний сервером і не використовується для реального захисту.
class DemoAuth(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("demo_auth", Context.MODE_PRIVATE)

    fun login(email: String, password: String): Boolean {
        if (!email.contains('@') || password != "solar1234") return false
        val role = if (email.equals("admin@solar.test", ignoreCase = true)) "admin" else "viewer"
        val expiration = System.currentTimeMillis() + 3600_000
        prefs.edit().putString("token", "mock-$role-$expiration").putString("role", role)
            .putLong("expires", expiration).apply()
        return true
    }
    fun token(): String? = prefs.getString("token", null)?.takeIf { System.currentTimeMillis() < prefs.getLong("expires", 0) }
    fun role(): String = if (token() == null) "guest" else prefs.getString("role", "viewer") ?: "viewer"
    fun logout() = prefs.edit().clear().apply()
}
