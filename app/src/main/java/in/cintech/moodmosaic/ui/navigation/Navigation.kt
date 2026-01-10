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
import `in`.cintech.moodmosaic.ui.screens.home.HomeScreen
import `in`.cintech.moodmosaic.ui.screens.yearreview.YearReviewScreen
import java.time.Year

sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object YearReview : Screen("year_review/{year}") {
        fun createRoute(year: Int) = "year_review/$year"
    }

    // Keep for future use if needed
    data object MoodDetail : Screen("mood/{date}") {
        fun createRoute(date: String) = "mood/$date"
    }
}

@Composable
fun MoodMosaicNavigation(
    navController: NavHostController = rememberNavController(),
    isDarkMode: Boolean = true,           // ✅ NEW
    onToggleTheme: () -> Unit = {}        // ✅ NEW
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
        // Home Screen (Month View Only)
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToYearReview = { year ->
                    navController.navigate(Screen.YearReview.createRoute(year))
                },
                isDarkMode = isDarkMode,         // ✅ Pass to HomeScreen
                onToggleTheme = onToggleTheme    // ✅ Pass to HomeScreen
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
                isDarkMode = isDarkMode          // ✅ Pass to YearReviewScreen
            )
        }
    }
}