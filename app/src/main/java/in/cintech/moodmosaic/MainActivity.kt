package `in`.cintech.moodmosaic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import `in`.cintech.moodmosaic.data.local.ThemeMode
import `in`.cintech.moodmosaic.data.local.ThemePreferences
import `in`.cintech.moodmosaic.ui.navigation.MoodMosaicNavigation
import `in`.cintech.moodmosaic.ui.theme.MoodMosaicTheme
import dagger.hilt.android.AndroidEntryPoint
import `in`.cintech.moodmosaic.widget.MoodWidgetStateHelper
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val themePreferences = ThemePreferences(this)

        setContent {
            // ✅ Get theme mode
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            // ✅ Get system dark mode
            val isSystemDark = isSystemInDarkTheme()

            // ✅ Determine actual dark mode based on preference
            val isDarkMode = when (themeMode) {
                ThemeMode.SYSTEM -> isSystemDark  // Follow system
                ThemeMode.LIGHT -> false          // Force light
                ThemeMode.DARK -> true            // Force dark
            }

            MoodMosaicTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoodMosaicNavigation(
                        themeMode = themeMode,
                        isDarkMode = isDarkMode,
                        // The widget updates automatically when mood is saved/deleted
                        // But for theme changes, update in MainActivity or wherever theme is cycled
                        onCycleTheme = {
                            MainScope().launch {
                                themePreferences.cycleTheme()
                                // ✅ Update widget to reflect new theme
                                MoodWidgetStateHelper.updateWidget(applicationContext)
                            }
                        }
                    )
                }
            }
        }
    }
}