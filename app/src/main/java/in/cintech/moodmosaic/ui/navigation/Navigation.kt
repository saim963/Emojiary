package `in`.cintech.moodmosaic.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `in`.cintech.moodmosaic.data.local.ThemeMode
import `in`.cintech.moodmosaic.ui.screens.analysis.AnalysisScreen
import `in`.cintech.moodmosaic.ui.screens.home.HomeScreen
import `in`.cintech.moodmosaic.ui.screens.settings.SettingsScreen
import `in`.cintech.moodmosaic.ui.screens.yearreview.YearReviewScreen
import java.time.Year

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Settings : Screen("settings")
    data object Analysis : Screen("analysis")
    data object YearReview : Screen("year_review/{year}") {
        fun createRoute(year: Int) = "year_review/$year"
    }
}

@Composable
fun MoodMosaicNavigation(
    navController: NavHostController = rememberNavController(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isDarkMode: Boolean = false,
    onCycleTheme: () -> Unit = {},
    notificationsEnabled: Boolean = false,
    onToggleNotifications: (Boolean) -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left)
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left)
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right)
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) +
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right)
        }
    ) {
        // Home Screen
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToYearReview = { year ->
                    navController.navigate(Screen.YearReview.createRoute(year))
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                },
                onNavigateToAnalysis = {
                    navController.navigate(Screen.Analysis.route)
                },
                themeMode = themeMode,
                isDarkMode = isDarkMode,
                onCycleTheme = onCycleTheme
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                themeMode = themeMode,
                onCycleTheme = onCycleTheme,
                notificationsEnabled = notificationsEnabled,
                onToggleNotifications = onToggleNotifications
            )
        }

        // Analysis Screen
        composable(Screen.Analysis.route) {
            AnalysisScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // Year Review Screen
        composable(
            route = Screen.YearReview.route,
            arguments = listOf(
                navArgument("year") {
                    type = NavType.IntType
                    defaultValue = Year.now().value
                }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: Year.now().value
            YearReviewScreen(
                year = year,
                onBackClick = { navController.popBackStack() },
                isDarkMode = isDarkMode
            )
        }
    }
}