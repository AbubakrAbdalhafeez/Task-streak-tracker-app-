package com.abubakr.taskstreak

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.ui.MainApp
import com.abubakr.taskstreak.ui.theme.TaskStreakTheme
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel
import com.abubakr.taskstreak.util.NotificationHelper
import java.util.Locale

class MainActivity : ComponentActivity() {
    private val viewModel: StreakViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize notification channel
        NotificationHelper.createNotificationChannel(this)

        // Request POST_NOTIFICATIONS on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val themeMode by viewModel.preferences.themeMode.collectAsStateWithLifecycle()
            val colorPalette by viewModel.preferences.colorPalette.collectAsStateWithLifecycle()
            val appLanguage by viewModel.preferences.appLanguage.collectAsStateWithLifecycle()

            val targetLocale = remember(appLanguage) {
                when (appLanguage) {
                    "ar" -> Locale("ar")
                    "en" -> Locale("en")
                    else -> Locale.getDefault()
                }
            }

            val currentConfig = LocalConfiguration.current
            val localizedConfig = remember(currentConfig, targetLocale) {
                Configuration(currentConfig).apply {
                    setLocale(targetLocale)
                    setLayoutDirection(targetLocale)
                }
            }

            val currentContext = LocalContext.current
            val localizedContext = remember(currentContext, targetLocale) {
                val conf = Configuration(currentContext.resources.configuration)
                conf.setLocale(targetLocale)
                conf.setLayoutDirection(targetLocale)
                currentContext.createConfigurationContext(conf)
            }

            val isRtl = targetLocale.language == "ar"

            CompositionLocalProvider(
                LocalConfiguration provides localizedConfig,
                LocalContext provides localizedContext,
                LocalLayoutDirection provides if (isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr
            ) {
                TaskStreakTheme(themePreference = themeMode, colorPalette = colorPalette) {
                    MainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    TaskStreakTheme { Greeting("Android") }
}
