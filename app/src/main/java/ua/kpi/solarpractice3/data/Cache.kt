package ua.kpi.solarpractice3.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "solar_forecasts")
data class ForecastEntity(
    @PrimaryKey val hour: String,
    val radiation: Double,
    val powerWatts: Double,
    val savedAt: Long
)

@Dao
interface ForecastDao {
    @Query("SELECT * FROM solar_forecasts ORDER BY hour ASC")
    fun observeAll(): Flow<List<ForecastEntity>>

    @Query("SELECT * FROM solar_forecasts ORDER BY hour ASC")
    suspend fun all(): List<ForecastEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putAll(values: List<ForecastEntity>)

    @Query("DELETE FROM solar_forecasts")
    suspend fun clear()
}

@Database(entities = [ForecastEntity::class], version = 1, exportSchema = false)
abstract class SolarDatabase : RoomDatabase() {
    abstract fun forecasts(): ForecastDao
    companion object {
        @Volatile private var instance: SolarDatabase? = null
        fun get(context: Context): SolarDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(context.applicationContext, SolarDatabase::class.java, "solar-practice-3.db").build().also { instance = it }
        }
    }
}
