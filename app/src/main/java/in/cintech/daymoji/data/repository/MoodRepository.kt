package `in`.cintech.daymoji.data.repository

import `in`.cintech.daymoji.data.local.MoodDao
import `in`.cintech.daymoji.data.local.MoodEntity
import `in`.cintech.daymoji.domain.model.MoodEntry
import `in`.cintech.daymoji.domain.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MoodRepository @Inject constructor(
    private val moodDao: MoodDao
) {
    fun getAllMoods(): Flow<List<MoodEntity>> = moodDao.getAllMoods()

    suspend fun getMoodByDate(date: LocalDate): MoodEntity? =
        moodDao.getMoodByDate(date)

    fun getMoodByDateFlow(date: LocalDate): Flow<MoodEntity?> =
        moodDao.getMoodByDateFlow(date)

    fun getMoodsInRange(startDate: LocalDate, endDate: LocalDate): Flow<List<MoodEntity>> =
        moodDao.getMoodsInRange(startDate, endDate)

    fun getMoodsByYear(year: Int): Flow<List<MoodEntity>> =
        moodDao.getMoodsByYear(year.toString())

    fun getMoodsByMonth(yearMonth: YearMonth): Flow<List<MoodEntity>> =
        moodDao.getMoodsByMonth(yearMonth.toString())

    // ✅ NEW: Get moods between dates (for Year Review)
    fun getMoodsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<MoodEntry>> =
        moodDao.getMoodsInRange(startDate, endDate).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveMood(mood: MoodEntity) = moodDao.insertMood(mood)

    suspend fun updateMood(mood: MoodEntity) = moodDao.updateMood(mood)

    suspend fun deleteMood(mood: MoodEntity) = moodDao.deleteMood(mood)

    suspend fun deleteMoodByDate(date: LocalDate) = moodDao.deleteMoodByDate(date)

    fun getMoodCount(): Flow<Int> = moodDao.getMoodCount()

    fun getMoodCountByYear(year: Int): Flow<Int> =
        moodDao.getMoodCountByYear(year.toString())
}