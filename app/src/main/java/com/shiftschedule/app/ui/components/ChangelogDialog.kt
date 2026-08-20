package com.shiftschedule.app.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.tr

@Composable
fun ChangelogDialog(viewModel: ShiftViewModel) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val version = remember {
        try { context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0" } catch (e: Exception) { "1.0" }
    }
    val show = settings.lastSeenVersion.isNotEmpty() && settings.lastSeenVersion != version
    if (show) {
        AlertDialog(
            onDismissRequest = { viewModel.updateSettings(settings.copy(lastSeenVersion = version)) },
            title = { Text(tr("changelog_title") + " " + version) },
            text = { Text(tr("changelog_text")) },
            confirmButton = {
                TextButton(onClick = { viewModel.updateSettings(settings.copy(lastSeenVersion = version)) }) { Text(tr("close")) }
            }
        )
    }
}
