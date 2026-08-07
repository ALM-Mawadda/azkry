package com.azkry.app.features.settings.views

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.app.AppSettingsViewModel
import com.azkry.app.app.Appearance
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.components.SectionRowCard
import com.azkry.app.core.components.RadioPickerDialog
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.settings.viewmodels.SettingsViewModel

/** Navigation the settings hub can trigger on the shell. */
data class SettingsNavigation(
    val onOpenPrayerSettings: () -> Unit,
    val onOpenNotificationSettings: () -> Unit,
    val onOpenHijriCalendar: () -> Unit,
    val onOpenExclusive: () -> Unit,
    val onOpenLicenses: () -> Unit,
)

@Composable
fun SettingsView(
    onBack: () -> Unit,
    navigation: SettingsNavigation,
    appSettingsViewModel: AppSettingsViewModel = hiltViewModel(),
    settingsViewModel: SettingsViewModel = hiltViewModel(),
) {
    val appSettings = appSettingsViewModel.settings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(settingsViewModel) {
        settingsViewModel.messages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        uri?.let(settingsViewModel::onExportBackup)
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let(settingsViewModel::onImportBackup)
    }

    var appearanceDialogOpen by rememberSaveable { mutableStateOf(false) }
    val appearance = appSettings.value?.appearance ?: Appearance.Dark

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = stringResource(R.string.settings_title),
                onBack = onBack,
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Sm),
                verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
            ) {
                Text(
                    text = stringResource(R.string.settings_section_app),
                    style = AzkryTextStyles.Title2,
                    color = AzkryTheme.colors.TextPrimary,
                )
                SectionRowCard(
                    title = stringResource(R.string.settings_prayer_times),
                    icon = Icons.Outlined.Schedule,
                    onClick = navigation.onOpenPrayerSettings,
                )
                SectionRowCard(
                    title = stringResource(R.string.settings_notifications),
                    icon = Icons.Outlined.Notifications,
                    onClick = navigation.onOpenNotificationSettings,
                )
                SectionRowCard(
                    title = stringResource(R.string.calendar_title),
                    icon = Icons.Outlined.CalendarMonth,
                    onClick = navigation.onOpenHijriCalendar,
                )
                SectionRowCard(
                    title = stringResource(R.string.exclusive_title),
                    icon = Icons.Outlined.WorkspacePremium,
                    onClick = navigation.onOpenExclusive,
                )
                SectionRowCard(
                    title = stringResource(R.string.settings_appearance),
                    subtitle = stringResource(appearance.labelRes()),
                    icon = Icons.Outlined.DarkMode,
                    onClick = { appearanceDialogOpen = true },
                )
                SectionRowCard(
                    title = stringResource(R.string.settings_language),
                    subtitle = stringResource(R.string.settings_language_value_arabic),
                    icon = Icons.Outlined.Language,
                    onClick = {},
                )
                SectionRowCard(
                    title = stringResource(R.string.settings_licenses),
                    icon = Icons.Outlined.Description,
                    onClick = navigation.onOpenLicenses,
                )

                Text(
                    text = stringResource(R.string.backup_title),
                    style = AzkryTextStyles.Title2,
                    color = AzkryTheme.colors.TextPrimary,
                    modifier = Modifier.padding(top = AzkrySpacing.Sm),
                )
                SectionRowCard(
                    title = stringResource(R.string.backup_export),
                    icon = Icons.Outlined.CloudUpload,
                    onClick = { exportLauncher.launch("azkry-backup.json") },
                )
                SectionRowCard(
                    title = stringResource(R.string.backup_import),
                    icon = Icons.Outlined.CloudDownload,
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                )
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }

    if (appearanceDialogOpen) {
        RadioPickerDialog(
            title = stringResource(R.string.settings_appearance),
            options = Appearance.entries.map { it to stringResource(it.labelRes()) },
            selected = appearance,
            onSelected = { chosen ->
                appSettingsViewModel.onAppearanceSelected(chosen)
                appearanceDialogOpen = false
            },
            onDismiss = { appearanceDialogOpen = false },
        )
    }
}

private fun Appearance.labelRes(): Int = when (this) {
    Appearance.Dark -> R.string.appearance_dark
    Appearance.Light -> R.string.appearance_light
    Appearance.System -> R.string.appearance_system
}
