package com.abubakr.taskstreak

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.ui.MainApp
import com.abubakr.taskstreak.ui.theme.TaskStreakTheme
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel
import com.abubakr.taskstreak.util.NotificationHelper

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
            TaskStreakTheme(themePreference = themeMode, colorPalette = colorPalette) {
                MainApp(viewModel = viewModel)
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
