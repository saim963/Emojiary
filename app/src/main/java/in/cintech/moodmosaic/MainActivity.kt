package `in`.cintech.moodmosaic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import `in`.cintech.moodmosaic.data.local.ThemePreferences
import `in`.cintech.moodmosaic.ui.navigation.MoodMosaicNavigation
import `in`.cintech.moodmosaic.ui.theme.MoodMosaicTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ FIX: Initialize BEFORE setContent
        val themePreferences = ThemePreferences(this)

        setContent {
            val isDarkMode by themePreferences.isDarkMode.collectAsState(initial = true)

            MoodMosaicTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MoodMosaicNavigation(
                        isDarkMode = isDarkMode,
                        onToggleTheme = {
                            MainScope().launch {
                                themePreferences.toggleTheme()
                            }
                        }
                    )
                }
            }
        }
    }
}