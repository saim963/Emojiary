package `in`.cintech.moodmosaic.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import `in`.cintech.moodmosaic.data.repository.MoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.cintech.moodmosaic.domain.model.MoodEntry
import `in`.cintech.moodmosaic.domain.model.toDomain
import `in`.cintech.moodmosaic.domain.model.toEntity
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

data class HomeUiState(
    val currentYearMonth: YearMonth = YearMonth.now(),
    val moods: List<MoodEntry> = emptyList(),
    val totalMoodCount: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isLoading: Boolean = true,
    val selectedDate: LocalDate? = null,
    val selectedMood: MoodEntry? = null,
    val showAddMoodSheet: Boolean = false,
    val showShareDialog: Boolean = false,
    val viewMode: ViewMode = ViewMode.MONTH
)

enum class ViewMode {
    MONTH, YEAR
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MoodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadMoods()
        calculateStats()
    }

    private fun loadMoods() {
        viewModelScope.launch {
            combine(
                repository.getMoodsByMonth(_uiState.value.currentYearMonth),
                repository.getMoodsByYear(_uiState.value.currentYearMonth.year)
            ) { monthMoods, yearMoods ->
                val moods = when (_uiState.value.viewMode) {
                    ViewMode.MONTH -> monthMoods
                    ViewMode.YEAR -> yearMoods
                }
                moods.map { it.toDomain() }
            }.collect { moods ->
                _uiState.update {
                    it.copy(
                        moods = moods,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun calculateStats() {
        viewModelScope.launch {
            repository.getAllMoods().collect { allMoods ->
                val sortedMoods = allMoods.sortedByDescending { it.date }

                // Calculate current streak
                var currentStreak = 0
                var checkDate = LocalDate.now()
                for (mood in sortedMoods) {
                    if (mood.date == checkDate) {
                        currentStreak++
                        checkDate = checkDate.minusDays(1)
                    } else if (mood.date == checkDate.minusDays(1)) {
                        // Allow checking yesterday if today not logged yet
                        if (currentStreak == 0) {
                            checkDate = mood.date
                            currentStreak++
                            checkDate = checkDate.minusDays(1)
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }

                // Calculate longest streak
                var longestStreak = 0
                var tempStreak = 0
                var prevDate: LocalDate? = null

                for (mood in allMoods.sortedBy { it.date }) {
                    if (prevDate == null || mood.date == prevDate.plusDays(1)) {
                        tempStreak++
                        longestStreak = maxOf(longestStreak, tempStreak)
                    } else {
                        tempStreak = 1
                    }
                    prevDate = mood.date
                }

                _uiState.update {
                    it.copy(
                        totalMoodCount = allMoods.size,
                        currentStreak = currentStreak,
                        longestStreak = longestStreak
                    )
                }
            }
        }
    }

    fun changeMonth(delta: Int) {
        _uiState.update {
            it.copy(currentYearMonth = it.currentYearMonth.plusMonths(delta.toLong()))
        }
        loadMoods()
    }

    fun goToToday() {
        _uiState.update {
            it.copy(currentYearMonth = YearMonth.now())
        }
        loadMoods()
    }

    fun toggleViewMode() {
        _uiState.update {
            it.copy(
                viewMode = when (it.viewMode) {
                    ViewMode.MONTH -> ViewMode.YEAR
                    ViewMode.YEAR -> ViewMode.MONTH
                }
            )
        }
        loadMoods()
    }

    fun onDateSelected(date: LocalDate) {
        viewModelScope.launch {
            val existingMood = repository.getMoodByDate(date)
            _uiState.update {
                it.copy(
                    selectedDate = date,
                    selectedMood = existingMood?.toDomain(),
                    showAddMoodSheet = true
                )
            }
        }
    }

    fun saveMood(mood: MoodEntry) {
        viewModelScope.launch {
            repository.saveMood(mood.toEntity())
            _uiState.update {
                it.copy(
                    showAddMoodSheet = false,
                    selectedDate = null,
                    selectedMood = null
                )
            }
            loadMoods()
            calculateStats()
        }
    }

    fun deleteMood(date: LocalDate) {
        viewModelScope.launch {
            repository.deleteMoodByDate(date)
            _uiState.update {
                it.copy(
                    showAddMoodSheet = false,
                    selectedDate = null,
                    selectedMood = null
                )
            }
            loadMoods()
            calculateStats()
        }
    }

    fun dismissAddMoodSheet() {
        _uiState.update {
            it.copy(
                showAddMoodSheet = false,
                selectedDate = null,
                selectedMood = null
            )
        }
    }

    fun showShareDialog() {
        _uiState.update { it.copy(showShareDialog = true) }
    }

    fun dismissShareDialog() {
        _uiState.update { it.copy(showShareDialog = false) }
    }
}