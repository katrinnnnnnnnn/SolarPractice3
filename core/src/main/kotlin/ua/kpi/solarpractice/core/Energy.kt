package ua.kpi.solarpractice.core

/** Потужність панелей у Вт за G (Вт/м²), η (%) та A (м²). */
object EnergyCalculator {
    fun watts(irradiance: Double, efficiencyPercent: Double, area: Double): Double {
        require(irradiance in 0.0..1500.0 && efficiencyPercent in 0.0..100.0 && area > 0.0)
        return irradiance * efficiencyPercent / 100.0 * area
    }
}
