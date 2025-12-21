package `in`.cintech.moodmosaic.data.repository

import `in`.cintech.moodmosaic.data.local.MoodDao
import `in`.cintech.moodmosaic.data.local.MoodEntity
import kotlinx.coroutines.flow.Flow
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

    suspend fun saveMood(mood: MoodEntity) = moodDao.insertMood(mood)

    suspend fun updateMood(mood: MoodEntity) = moodDao.updateMood(mood)

    suspend fun deleteMood(mood: MoodEntity) = moodDao.deleteMood(mood)

    suspend fun deleteMoodByDate(date: LocalDate) = moodDao.deleteMoodByDate(date)

    fun getMoodCount(): Flow<Int> = moodDao.getMoodCount()

    fun getMoodCountByYear(year: Int): Flow<Int> =
        moodDao.getMoodCountByYear(year.toString())
}