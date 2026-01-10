package `in`.cintech.moodmosaic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import `in`.cintech.moodmosaic.domain.model.MoodEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

private fun isDateEditable(date: LocalDate): Boolean {
    val today = LocalDate.now()
    val threeDaysAgo = today.minusDays(3)
    return !date.isAfter(today) && !date.isBefore(threeDaysAgo)
}

@Composable
fun MoodGrid(
    modifier: Modifier = Modifier,
    moods: List<MoodEntry>,
    yearMonth: YearMonth,
    pixelSize: Dp = 44.dp,
    onMoodClick: (LocalDate) -> Unit,
    onNonEditableClick: ((LocalDate) -> Unit)? = null  // ✅ NEW: Optional callback
) {
    val today = LocalDate.now()
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val moodMap = remember(moods) {
        moods.associateBy { it.date }
    }

    Column(modifier = modifier) {
        // Month Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = yearMonth.year.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        // Day of Week Headers
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Box(
                    modifier = Modifier.size(pixelSize),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Calendar Grid
        val totalCells = firstDayOfWeek + daysInMonth
        val rows = (totalCells + 6) / 7

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            for (rowIndex in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (columnIndex in 0 until 7) {
                        val cellIndex = rowIndex * 7 + columnIndex
                        val dayIndex = cellIndex - firstDayOfWeek

                        Box(modifier = Modifier.weight(1f)) {
                            if (cellIndex < firstDayOfWeek || dayIndex >= daysInMonth) {
                                EmptyPixel(size = pixelSize)
                            } else {
                                val date = yearMonth.atDay(dayIndex + 1)
                                val mood = moodMap[date]
                                val isToday = date == today
                                val isEditable = isDateEditable(date)
                                val animationDelay = cellIndex * 30

                                MoodPixel(
                                    mood = mood,
                                    date = date,
                                    size = pixelSize,
                                    isToday = isToday,
                                    isEditable = isEditable,
                                    animationDelay = animationDelay,
                                    onClick = {
                                        if (isEditable) {
                                            onMoodClick(date)
                                        } else {
                                            onNonEditableClick?.invoke(date)  // ✅ Safe call
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}