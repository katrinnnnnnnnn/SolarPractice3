package ua.kpi.solarpractice.core
import org.junit.Assert.assertEquals
import org.junit.Test
class EnergyTest {
 @Test fun nominalPower() { assertEquals(1600.0, EnergyCalculator.watts(800.0,20.0,10.0),0.001) }
 @Test(expected=IllegalArgumentException::class) fun rejectsNegativeArea() { EnergyCalculator.watts(800.0,20.0,-1.0) }
}
