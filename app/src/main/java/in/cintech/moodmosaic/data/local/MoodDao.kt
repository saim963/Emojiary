package `in`.cintech.moodmosaic.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MoodDao {

    @Query("SELECT * FROM moods ORDER BY date DESC")
    fun getAllMoods(): Flow<List<MoodEntity>>

    @Query("SELECT * FROM moods WHERE date = :date")
    suspend fun getMoodByDate(date: LocalDate): MoodEntity?

    @Query("SELECT * FROM moods WHERE date = :date")
    fun getMoodByDateFlow(date: LocalDate): Flow<MoodEntity?>

    @Query("""
        SELECT * FROM moods 
        WHERE date >= :startDate AND date <= :endDate 
        ORDER BY date ASC
    """)
    fun getMoodsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<MoodEntity>>

    @Query("""
        SELECT * FROM moods 
        WHERE strftime('%Y', date) = :year 
        ORDER BY date ASC
    """)
    fun getMoodsByYear(year: String): Flow<List<MoodEntity>>

    @Query("""
        SELECT * FROM moods 
        WHERE strftime('%Y-%m', date) = :yearMonth 
        ORDER BY date ASC
    """)
    fun getMoodsByMonth(yearMonth: String): Flow<List<MoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMood(mood: MoodEntity)

    @Update
    suspend fun updateMood(mood: MoodEntity)

    @Delete
    suspend fun deleteMood(mood: MoodEntity)

    @Query("DELETE FROM moods WHERE date = :date")
    suspend fun deleteMoodByDate(date: LocalDate)

    @Query("SELECT COUNT(*) FROM moods")
    fun getMoodCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM moods WHERE strftime('%Y', date) = :year")
    fun getMoodCountByYear(year: String): Flow<Int>
}