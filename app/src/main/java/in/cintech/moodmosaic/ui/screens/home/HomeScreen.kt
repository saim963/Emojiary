package `in`.cintech.moodmosaic.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            HomeTopBar(
                currentYearMonth = uiState.currentYearMonth,
                viewMode = uiState.viewMode,
                onPreviousMonth = { viewModel.changeMonth(-1) },
                onNextMonth = { viewModel.changeMonth(1) },
                onToggleViewMode = viewModel::toggleViewMode,
                onShare = viewModel::showShareDialog
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Stats Cards
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

            Spacer(modifier = Modifier.height(16.dp))

            // Main Grid
            AnimatedContent(
                targetState = uiState.viewMode,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300))
                },
                label = "view_mode_transition"
            ) { viewMode ->
                when (viewMode) {
                    ViewMode.MONTH -> {
                        MoodGrid(
                            moods = uiState.moods,
                            yearMonth = uiState.currentYearMonth,
                            onMoodClick = viewModel::onDateSelected,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                    ViewMode.YEAR -> {
                        YearMosaicGrid(
                            moods = uiState.moods,
                            year = uiState.currentYearMonth.year,
                            onMoodClick = viewModel::onDateSelected,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
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
                onDismiss = viewModel::dismissShareDialog
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(
    currentYearMonth: YearMonth,
    viewMode: ViewMode,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToggleViewMode: () -> Unit,
    onShare: () -> Unit
) {
    TopAppBar(
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
            IconButton(onClick = onToggleViewMode) {
                Icon(
                    imageVector = when (viewMode) {
                        ViewMode.MONTH -> Icons.Default.DateRange
                        ViewMode.YEAR -> Icons.Default.CalendarMonth
                    },
                    contentDescription = "Toggle View"
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