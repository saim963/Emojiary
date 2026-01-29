package `in`.cintech.daymoji

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import `in`.cintech.daymoji.data.local.NotificationPreferences
import `in`.cintech.daymoji.data.local.OnboardingPreferences
import `in`.cintech.daymoji.data.local.ThemeMode
import `in`.cintech.daymoji.data.local.ThemePreferences
import `in`.cintech.daymoji.notification.NotificationHelper
import `in`.cintech.daymoji.notification.ReminderWorker
import `in`.cintech.daymoji.ui.navigation.MoodMosaicNavigation
import `in`.cintech.daymoji.ui.screens.splash.OnboardingScreen
import `in`.cintech.daymoji.ui.theme.DaymojiTheme
import `in`.cintech.daymoji.widget.MoodWidgetStateHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Install System Splash Screen (The App Logo)
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themePreferences = ThemePreferences(this)
        val onboardingPreferences = OnboardingPreferences(this)
        val notificationPreferences = NotificationPreferences(this)

        NotificationHelper.createNotificationChannel(this)

        // 2. Check Widget Launch immediately
        val isWidgetLaunch = intent?.getBooleanExtra("IS_WIDGET_LAUNCH", false) == true ||
                intent?.extras?.getBoolean("IS_WIDGET_LAUNCH") == true

        // 3. State to block UI rendering until we know where to go
        var isDataLoaded by mutableStateOf(false)
        var startDestinationIsOnboarding by mutableStateOf(false)

        // 4. Load DataStore Logic in Background
        MainScope().launch {
            val isOnboardingCompleted = onboardingPreferences.isOnboardingCompleted.first()

            // LOGIC: Show Onboarding ONLY if NOT completed AND NOT coming from widget
            startDestinationIsOnboarding = !isOnboardingCompleted && !isWidgetLaunch

            // Release the splash screen
            isDataLoaded = true
        }

        // 5. Keep System Splash visible until data is ready
        splashScreen.setKeepOnScreenCondition {
            !isDataLoaded
        }

        setContent {
            // 6. Reactive State for Settings (Theme/Notifications)
            // This ensures buttons in settings work immediately
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            val notificationsEnabled by notificationPreferences.isNotificationsEnabled.collectAsState(initial = false)

            // 7. CRITICAL FIX: Only render Compose UI when data is ready
            if (isDataLoaded) {

                // Initialize state with the PRE-CALCULATED value.
                // This prevents the "true -> false" flip that caused the flash.
                var showOnboarding by remember { mutableStateOf(startDestinationIsOnboarding) }

                val isSystemDark = isSystemInDarkTheme()
                val isDarkMode = when (themeMode) {
                    ThemeMode.SYSTEM -> isSystemDark
                    ThemeMode.LIGHT -> false
                    ThemeMode.DARK -> true
                }

                DaymojiTheme(darkTheme = isDarkMode) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (showOnboarding) {
                            OnboardingScreen(
                                onFinish = {
                                    MainScope().launch {
                                        onboardingPreferences.setOnboardingCompleted()
                                        showOnboarding = false
                                    }
                                }
                            )
                        } else {
                            MoodMosaicNavigation(
                                themeMode = themeMode,
                                isDarkMode = isDarkMode,
                                onCycleTheme = {
                                    MainScope().launch {
                                        themePreferences.cycleTheme()
                                        MoodWidgetStateHelper.updateWidget(applicationContext)
                                    }
                                },
                                notificationsEnabled = notificationsEnabled,
                                onToggleNotifications = { enabled ->
                                    MainScope().launch {
                                        notificationPreferences.setNotificationsEnabled(enabled)
                                        if (enabled) {
                                            ReminderWorker.schedule(applicationContext)
                                        } else {
                                            ReminderWorker.cancel(applicationContext)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Handle updates when app is already running
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}