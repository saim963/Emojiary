package `in`.cintech.moodmosaic.ui.screens.analysis

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.cintech.moodmosaic.data.repository.MoodRepository
import `in`.cintech.moodmosaic.domain.model.MoodEntry
import `in`.cintech.moodmosaic.domain.model.toDomain
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class MoodStat(
    val emoji: String,
    val color: Color,
    val count: Int,
    val percentage: Float
)

data class DayOfWeekStat(
    val day: DayOfWeek,
    val averageMoodCount: Int,
    val mostCommonEmoji: String?
)

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val totalDays: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val moodDistribution: List<MoodStat> = emptyList(),
    val dayOfWeekStats: List<DayOfWeekStat> = emptyList(),
    val mostFrequentMood: String? = null,
    val averageMoodsPerWeek: Float = 0f
)

@HiltViewModel
class AnalysisViewModel @Inject constructor(
    private val repository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalysisUiState())
    val uiState: StateFlow<AnalysisUiState> = _uiState.asStateFlow()

    init {
        loadAnalysis()
    }

    private fun loadAnalysis() {
        viewModelScope.launch {
            repository.getAllMoods().collect { entities ->
                val moods = entities.map { it.toDomain() }

                if (moods.isEmpty()) {
                    _uiState.update { it.copy(isLoading = false) }
                    return@collect
                }

                // Calculate mood distribution
                val moodGroups = moods.groupBy { it.emoji }
                val totalMoods = moods.size
                val moodDistribution = moodGroups.map { (emoji, entries) ->
                    MoodStat(
                        emoji = emoji,
                        color = entries.first().color,
                        count = entries.size,
                        percentage = entries.size.toFloat() / totalMoods
                    )
                }.sortedByDescending { it.count }

                // Calculate streaks
                val sortedMoods = moods.sortedByDescending { it.date }
                var currentStreak = 0
                var checkDate = LocalDate.now()
                for (mood in sortedMoods) {
                    if (mood.date == checkDate) {
                        currentStreak++
                        checkDate = checkDate.minusDays(1)
                    } else if (mood.date == checkDate.minusDays(1) && currentStreak == 0) {
                        checkDate = mood.date
                        currentStreak++
                        checkDate = checkDate.minusDays(1)
                    } else {
                        break
                    }
                }

                var longestStreak = 0
                var tempStreak = 0
                var prevDate: LocalDate? = null
                for (mood in moods.sortedBy { it.date }) {
                    if (prevDate == null || mood.date == prevDate.plusDays(1)) {
                        tempStreak++
                        longestStreak = maxOf(longestStreak, tempStreak)
                    } else {
                        tempStreak = 1
                    }
                    prevDate = mood.date
                }

                // Day of week analysis
                val dayOfWeekStats = DayOfWeek.entries.map { day ->
                    val moodsOnDay = moods.filter { it.date.dayOfWeek == day }
                    val mostCommon = moodsOnDay
                        .groupBy { it.emoji }
                        .maxByOrNull { it.value.size }
                        ?.key

                    DayOfWeekStat(
                        day = day,
                        averageMoodCount = moodsOnDay.size,
                        mostCommonEmoji = mostCommon
                    )
                }

                // Average moods per week
                val firstMood = moods.minByOrNull { it.date }
                val weeks = if (firstMood != null) {
                    val daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                        firstMood.date, LocalDate.now()
                    )
                    (daysBetween / 7.0).coerceAtLeast(1.0)
                } else 1.0

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        totalDays = moods.size,
                        currentStreak = currentStreak,
                        longestStreak = longestStreak,
                        moodDistribution = moodDistribution,
                        dayOfWeekStats = dayOfWeekStats,
                        mostFrequentMood = moodDistribution.firstOrNull()?.emoji,
                        averageMoodsPerWeek = (moods.size / weeks).toFloat()
                    )
                }
            }
        }
    }
}