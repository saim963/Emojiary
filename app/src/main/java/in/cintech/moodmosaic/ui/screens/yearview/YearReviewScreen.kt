package `in`.cintech.moodmosaic.ui.screens.yearreview

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.cintech.moodmosaic.domain.model.MoodEntry
import `in`.cintech.moodmosaic.utils.MosaicImageGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.Month
import java.time.Year

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearReviewScreen(
    year: Int,
    onBackClick: () -> Unit,
    isDarkMode: Boolean = true,    // ✅ NEW parameter
    viewModel: YearReviewViewModel = hiltViewModel()
) {
    val moods by viewModel.getMoodsForYear(year).collectAsStateWithLifecycle(initialValue = emptyList())
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSharing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "$year in Pixels",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isSharing = true
                                shareYearMosaic(context, moods, year, isDarkMode)  // ✅ Pass isDarkMode
                                isSharing = false
                            }
                        },
                        enabled = !isSharing
                    ) {
                        if (isSharing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share"
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Year Stats Summary
            YearSummaryCard(
                totalMoods = moods.size,
                year = year
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Month-by-month grid
            YearMonthlyGrid(
                moods = moods,
                year = year
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ✅ UPDATED: Share function with theme support
private suspend fun shareYearMosaic(
    context: Context,
    moods: List<MoodEntry>,
    year: Int,
    isDarkMode: Boolean    // ✅ NEW parameter
) = withContext(Dispatchers.IO) {
    try {
        val bitmap = MosaicImageGenerator.generateYearMosaic(
            moods = moods,
            year = year,
            context = context,
            isDarkMode = isDarkMode    // ✅ Pass theme
        )

        val cachePath = File(context.cacheDir, "images")
        cachePath.mkdirs()

        val fileName = "daymoji_year_$year.png"
        val file = File(cachePath, fileName)

        FileOutputStream(file).use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(
                Intent.EXTRA_TEXT,
                "My $year Year in Pixels 🎨\n\n${moods.size} days of feelings tracked!\n\n#Daymoji #YearInPixels #MentalHealth"
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        withContext(Dispatchers.Main) {
            val chooser = Intent.createChooser(shareIntent, "Share your Year in Pixels")

            val resInfoList = context.packageManager.queryIntentActivities(
                chooser,
                android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
            )

            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }

            context.startActivity(chooser)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
private fun YearSummaryCard(
    totalMoods: Int,
    year: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalMoods",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Days Tracked",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            HorizontalDivider(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val isLeapYear = Year.of(year).isLeap
                val totalDays = if (isLeapYear) 366 else 365
                val percentage = if (totalDays > 0) (totalMoods * 100) / totalDays else 0

                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = "Completion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun YearMonthlyGrid(
    moods: List<MoodEntry>,
    year: Int
) {
    val moodMap = remember(moods) {
        moods.associateBy { it.date }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Month.entries.forEach { month ->
            MonthSection(
                year = year,
                month = month,
                moodMap = moodMap
            )
        }
    }
}

@Composable
private fun MonthSection(
    year: Int,
    month: Month,
    moodMap: Map<LocalDate, MoodEntry>
) {
    val firstDayOfMonth = LocalDate.of(year, month, 1)
    val daysInMonth = firstDayOfMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7

    val totalCells = firstDayOfWeek + daysInMonth
    val rows = (totalCells + 6) / 7

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Month Header
        Text(
            text = month.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Week day headers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
                        fontSize = 8.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Days grid
        Column(
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            for (rowIndex in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    for (columnIndex in 0 until 7) {
                        val cellIndex = rowIndex * 7 + columnIndex
                        val dayIndex = cellIndex - firstDayOfWeek + 1

                        Box(modifier = Modifier.weight(1f)) {
                            if (cellIndex < firstDayOfWeek || dayIndex > daysInMonth) {
                                EmptyYearCell()
                            } else {
                                val date = LocalDate.of(year, month, dayIndex)
                                val mood = moodMap[date]
                                val isToday = date == LocalDate.now()

                                YearPixelCell(
                                    mood = mood,
                                    isToday = isToday
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearPixelCell(
    mood: MoodEntry?,
    isToday: Boolean
) {
    val backgroundColor = mood?.color ?: MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(backgroundColor)
            .then(
                if (isToday) {
                    Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(4.dp)
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        mood?.let {
            Text(
                text = it.emoji,
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EmptyYearCell() {
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    )
}