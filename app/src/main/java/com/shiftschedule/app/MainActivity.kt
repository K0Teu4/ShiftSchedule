package com.shiftschedule.app

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.shiftschedule.app.navigation.AppNavigation
import com.shiftschedule.app.ui.theme.ShiftScheduleTheme
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.LocalLang
import com.shiftschedule.app.util.Strings
import androidx.compose.runtime.CompositionLocalProvider
import com.shiftschedule.app.widget.ShiftWidgetProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        updateWidget()

        setContent {
            val viewModel: ShiftViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            val lightBased = settings.theme == "light" || settings.theme == "sepia"
            val systemLang = Strings.getSystemLanguage()

            SideEffect {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = lightBased
                controller.isAppearanceLightNavigationBars = lightBased
            }

            CompositionLocalProvider(LocalLang provides systemLang) {
                ShiftScheduleTheme(theme = settings.theme) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(viewModel)
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    private fun updateWidget() {
        val manager = AppWidgetManager.getInstance(this)
        val ids = manager.getAppWidgetIds(ComponentName(this, ShiftWidgetProvider::class.java))
        if (ids.isNotEmpty()) {
            val intent = Intent(this, ShiftWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            }
            sendBroadcast(intent)
        }
    }
}