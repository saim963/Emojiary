package `in`.cintech.moodmosaic.widget

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import `in`.cintech.moodmosaic.MainActivity
import `in`.cintech.moodmosaic.data.local.MoodDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as DateTextStyle
import java.util.*
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.action.ActionParameters
import `in`.cintech.moodmosaic.data.local.dataStore

class MoodWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val widgetData = loadWidgetData(context)
        val isDarkMode = getAppTheme(context)  // ✅ Get app's theme preference

        provideContent {
            MoodWidgetContent(data = widgetData, isDarkMode = isDarkMode)
        }
    }

    // ✅ Get theme from app's DataStore preference
    private suspend fun getAppTheme(context: Context): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val preferences = context.dataStore.data.first()
                val themeMode = preferences[intPreferencesKey("theme_mode")] ?: 0

                when (themeMode) {
                    1 -> false  // Light mode
                    2 -> true   // Dark mode
                    else -> {   // System mode - check system setting
                        (context.resources.configuration.uiMode and
                                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
                    }
                }
            } catch (e: Exception) {
                // Fallback to system theme
                (context.resources.configuration.uiMode and
                        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
            }
        }
    }

    private suspend fun loadWidgetData(context: Context): WidgetData {
        return withContext(Dispatchers.IO) {
            try {
                val database = MoodDatabase.getDatabase(context)
                val dao = database.moodDao()

                val today = LocalDate.now()
                val todayMood = dao.getMoodByDate(today)

                val pastDays = (1..5).map { daysAgo ->
                    val date = today.minusDays(daysAgo.toLong())
                    val mood = dao.getMoodByDate(date)
                    DayMood(
                        date = date,
                        emoji = mood?.emoji,
                        hasEntry = mood != null
                    )
                }.reversed()

                var streak = 0
                var checkDate = today
                while (true) {
                    val mood = dao.getMoodByDate(checkDate)
                    if (mood != null) {
                        streak++
                        checkDate = checkDate.minusDays(1)
                    } else break
                }

                WidgetData(
                    todayEmoji = todayMood?.emoji,
                    todayDate = today.format(DateTimeFormatter.ofPattern("EEE, MMM d")),
                    pastDays = pastDays,
                    currentStreak = streak,
                    hasTodayEntry = todayMood != null
                )
            } catch (e: Exception) {
                WidgetData()
            }
        }
    }
}

data class WidgetData(
    val todayEmoji: String? = null,
    val todayDate: String = "",
    val pastDays: List<DayMood> = emptyList(),
    val currentStreak: Int = 0,
    val hasTodayEntry: Boolean = false
)

data class DayMood(
    val date: LocalDate,
    val emoji: String? = null,
    val hasEntry: Boolean = false
)

object WidgetTheme {
    val darkBackground = Color(0xCC1A1A1A)
    val darkCellBackground = Color(0x40FFFFFF)
    val darkCellEmpty = Color(0x20FFFFFF)
    val darkTextPrimary = Color.White
    val darkTextSecondary = Color(0xFFAAAAAA)
    val darkTextMuted = Color(0xFF666666)

    val lightBackground = Color(0xCCFFFFFF)
    val lightCellBackground = Color(0x40000000)
    val lightCellEmpty = Color(0x15000000)
    val lightTextPrimary = Color(0xFF1A1A1A)
    val lightTextSecondary = Color(0xFF666666)
    val lightTextMuted = Color(0xFF999999)

    val todayBackground = Color(0xFF4ECDC4)
    val todayEmpty = Color(0x504ECDC4)
    val streakColor = Color(0xFFFF6B6B)
    val streakBackground = Color(0x40FF6B6B)
}

@Composable
fun MoodWidgetContent(data: WidgetData, isDarkMode: Boolean = false) {
    val backgroundColor = if (isDarkMode) WidgetTheme.darkBackground else WidgetTheme.lightBackground
    val textPrimary = if (isDarkMode) WidgetTheme.darkTextPrimary else WidgetTheme.lightTextPrimary
    val textSecondary = if (isDarkMode) WidgetTheme.darkTextSecondary else WidgetTheme.lightTextSecondary
    val textMuted = if (isDarkMode) WidgetTheme.darkTextMuted else WidgetTheme.lightTextMuted
    val cellBackground = if (isDarkMode) WidgetTheme.darkCellBackground else WidgetTheme.lightCellBackground
    val cellEmpty = if (isDarkMode) WidgetTheme.darkCellEmpty else WidgetTheme.lightCellEmpty

    Box(
        modifier = GlanceModifier
            .wrapContentSize()
            .cornerRadius(20.dp)
            .background(backgroundColor)
            .clickable(
                actionStartActivity<MainActivity>(
                    actionParametersOf(
                        ActionParameters.Key<Boolean>("IS_WIDGET_LAUNCH") to true
                    )
                )
            )
            .padding(10.dp)
    ) {
        Column(
            modifier = GlanceModifier.wrapContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = GlanceModifier.wrapContentSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (data.hasTodayEntry) "Today" else "How are you?",
                    style = TextStyle(
                        color = ColorProvider(textPrimary),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                if (data.currentStreak > 0) {
                    Spacer(modifier = GlanceModifier.width(6.dp))
                    Box(
                        modifier = GlanceModifier
                            .cornerRadius(8.dp)
                            .background(WidgetTheme.streakBackground)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = "🔥${data.currentStreak}",
                            style = TextStyle(
                                color = ColorProvider(WidgetTheme.streakColor),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Today - Main Focus
            Box(
                modifier = GlanceModifier
                    .size(50.dp)
                    .cornerRadius(25.dp)
                    .background(
                        if (data.hasTodayEntry) WidgetTheme.todayBackground else WidgetTheme.todayEmpty
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = data.todayEmoji ?: "➕",
                    style = TextStyle(fontSize = 26.sp)
                )
            }

            Spacer(modifier = GlanceModifier.height(3.dp))

            // Date
            Text(
                text = data.todayDate,
                style = TextStyle(
                    color = ColorProvider(textSecondary),
                    fontSize = 9.sp
                )
            )

            Spacer(modifier = GlanceModifier.height(6.dp))

            // Past 5 days
            Row(
                modifier = GlanceModifier.wrapContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                data.pastDays.forEachIndexed { index, day ->
                    PastDayCell(
                        dayMood = day,
                        cellBackground = cellBackground,
                        cellEmpty = cellEmpty,
                        textMuted = textMuted
                    )
                    if (index < data.pastDays.size - 1) {
                        Spacer(modifier = GlanceModifier.width(3.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PastDayCell(
    dayMood: DayMood,
    cellBackground: Color,
    cellEmpty: Color,
    textMuted: Color
) {
    Box(
        modifier = GlanceModifier
            .size(22.dp)
            .cornerRadius(6.dp)
            .background(if (dayMood.hasEntry) cellBackground else cellEmpty),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = dayMood.emoji ?: dayMood.date.dayOfMonth.toString(),
            style = TextStyle(
                fontSize = if (dayMood.emoji != null) 12.sp else 8.sp,
                color = if (dayMood.emoji == null)
                    ColorProvider(textMuted)
                else
                    ColorProvider(Color.White)
            )
        )
    }
}