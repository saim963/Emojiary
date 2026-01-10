package `in`.cintech.moodmosaic.ui.screens.home

import `in`.cintech.moodmosaic.data.local.ThemeMode
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import `in`.cintech.moodmosaic.ui.components.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToYearReview: (Int) -> Unit,
    themeMode: ThemeMode = ThemeMode.SYSTEM,  // ✅ Theme mode
    isDarkMode: Boolean = false,               // ✅ No default to dark
    onCycleTheme: () -> Unit = {},             // ✅ Cycle theme
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            HomeTopBar(
                currentYearMonth = uiState.currentYearMonth,
                onPreviousMonth = { viewModel.changeMonth(-1) },
                onNextMonth = { viewModel.changeMonth(1) },
                onYearReviewClick = { onNavigateToYearReview(uiState.currentYearMonth.year) },
                onShare = viewModel::showShareDialog,
                themeMode = themeMode,
                onCycleTheme = onCycleTheme
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onDateSelected(LocalDate.now()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Today's Mood"
                )
            }
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Stats Cards
            item {
                AnimatedVisibility(
                    visible = !uiState.isLoading,
                    enter = fadeIn() + slideInVertically()
                ) {
                    StatsRow(
                        totalMoods = uiState.totalMoodCount,
                        currentStreak = uiState.currentStreak,
                        longestStreak = uiState.longestStreak,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ✅ SIMPLIFIED: Only Month Grid (no ViewMode switching)
            // Month Grid with non-editable callback
            item {
                MoodGrid(
                    moods = uiState.moods,
                    yearMonth = uiState.currentYearMonth,
                    onMoodClick = viewModel::onDateSelected,
                    onNonEditableClick = { date ->  // ✅ Handle non-editable click
                        Toast.makeText(
                            context,
                            "You can only edit moods from the last 3 days",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // ✅ NEW: Year Review Button
            item {
                Spacer(modifier = Modifier.height(24.dp))

                YearReviewCard(
                    year = uiState.currentYearMonth.year,
                    onClick = { onNavigateToYearReview(uiState.currentYearMonth.year) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        // Add/Edit Mood Sheet
        if (uiState.showAddMoodSheet && uiState.selectedDate != null) {
            AddMoodBottomSheet(
                date = uiState.selectedDate!!,
                existingMood = uiState.selectedMood,
                onSave = viewModel::saveMood,
                onDelete = uiState.selectedMood?.let {
                    { viewModel.deleteMood(uiState.selectedDate!!) }
                },
                onDismiss = viewModel::dismissAddMoodSheet
            )
        }

        // Share Dialog
        if (uiState.showShareDialog) {
            ShareMosaicDialog(
                moods = uiState.moods,
                yearMonth = uiState.currentYearMonth,
                isDarkMode = isDarkMode,
                onDismiss = viewModel::dismissShareDialog
            )
        }
    }
}

// ✅ NEW: Year Review Card Component
@Composable
private fun YearReviewCard(
    year: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = "$year Year in Pixels",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "View your entire year's mood journey",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    currentYearMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onYearReviewClick: () -> Unit,
    onShare: () -> Unit,
    themeMode: ThemeMode,    // ✅ Theme mode
    onCycleTheme: () -> Unit // ✅ Cycle theme
) {TopAppBar(
    title = {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous"
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = currentYearMonth.month.getDisplayName(
                        TextStyle.FULL,
                        Locale.getDefault()
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currentYearMonth.year.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onNextMonth) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next"
                )
            }
        }
    },
    actions = {
        // ✅ Theme Toggle Button with correct icon
        IconButton(onClick = onCycleTheme) {
            Icon(
                imageVector = when (themeMode) {
                    ThemeMode.SYSTEM -> Icons.Default.Brightness6  // Auto icon
                    ThemeMode.LIGHT -> Icons.Default.LightMode     // Sun icon
                    ThemeMode.DARK -> Icons.Default.DarkMode       // Moon icon
                },
                contentDescription = when (themeMode) {
                    ThemeMode.SYSTEM -> "System Theme (tap to change)"
                    ThemeMode.LIGHT -> "Light Theme (tap to change)"
                    ThemeMode.DARK -> "Dark Theme (tap to change)"
                }
            )
        }

        IconButton(onClick = onYearReviewClick) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Year Review"
            )
        }
        IconButton(onClick = onShare) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = "Share"
            )
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background
    )
)
}

@Composable
private fun StatsRow(
    totalMoods: Int,
    currentStreak: Int,
    longestStreak: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Total Days",
            value = totalMoods.toString(),
            icon = Icons.Default.Favorite,
            gradient = listOf(Color(0xFF6BCB77), Color(0xFF4ECDC4)),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Current Streak",
            value = "$currentStreak 🔥",
            icon = Icons.Default.LocalFireDepartment,
            gradient = listOf(Color(0xFFFF6B6B), Color(0xFFFF8E53)),
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Best Streak",
            value = longestStreak.toString(),
            icon = Icons.Default.EmojiEvents,
            gradient = listOf(Color(0xFFE056FD), Color(0xFF9B59B6)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(gradient),
                    alpha = 0.15f
                )
                .padding(16.dp)
        ) {
            Column {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = gradient[0],
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}