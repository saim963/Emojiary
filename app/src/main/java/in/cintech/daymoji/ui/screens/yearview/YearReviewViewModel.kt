package `in`.cintech.daymoji.ui.screens.yearreview

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import `in`.cintech.daymoji.data.repository.MoodRepository
import `in`.cintech.daymoji.domain.model.MoodEntry
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class YearReviewViewModel @Inject constructor(
    private val repository: MoodRepository
) : ViewModel() {

    fun getMoodsForYear(year: Int): Flow<List<MoodEntry>> {
        val startDate = LocalDate.of(year, 1, 1)
        val endDate = LocalDate.of(year, 12, 31)
        return repository.getMoodsBetweenDates(startDate, endDate)
    }
}