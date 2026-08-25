package com.shiftschedule.app

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
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
        updateWidget()

        setContent {
            val viewModel: ShiftViewModel = viewModel()
            val settings by viewModel.settings.collectAsState()
            val lightBased = when (settings.theme) {
                "light" -> true
                "system" -> (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_NO
                else -> false
            }
            val lang = when (settings.lang) {
                "ru" -> "ru"
                "en" -> "en"
                else -> Strings.getSystemLanguage()
            }

            SideEffect {
                val controller = WindowCompat.getInsetsController(window, window.decorView)
                controller.isAppearanceLightStatusBars = lightBased
                controller.isAppearanceLightNavigationBars = lightBased
            }

            CompositionLocalProvider(LocalLang provides lang) {
                ShiftScheduleTheme(theme = settings.theme) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        AppNavigation(viewModel)
                    }
                }
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

