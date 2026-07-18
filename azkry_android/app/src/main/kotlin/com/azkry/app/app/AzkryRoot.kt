package com.azkry.app.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.core.components.AmbientBackground
import com.azkry.app.core.i18n.AppLocale
import com.azkry.app.core.i18n.ProvideAppLocale
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.main.MainShell
import com.azkry.app.features.notifications.models.NotificationDestination

@Composable
fun AzkryRoot(
    pendingDestination: NotificationDestination? = null,
    onDestinationConsumed: (NotificationDestination) -> Unit = {},
    onDarkThemeChanged: (Boolean) -> Unit = {},
    settingsViewModel: AppSettingsViewModel = hiltViewModel(),
) {
    val appSettings = settingsViewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val loadedSettings = appSettings.value
    val settings = loadedSettings ?: AppSettings()

    LaunchedEffect(loadedSettings?.language) {
        loadedSettings?.language?.let { loadedLanguage ->
            AppLocale.applyPlatformAppLocale(context.applicationContext, loadedLanguage)
        }
    }

    val darkTheme = when (settings.appearance) {
        Appearance.Dark -> true
        Appearance.Light -> false
        Appearance.System -> isSystemInDarkTheme()
    }

    LaunchedEffect(darkTheme) {
        onDarkThemeChanged(darkTheme)
    }

    ProvideAppLocale(language = settings.language) {
        AzkryTheme(darkTheme = darkTheme) {
            AmbientBackground {
                MainShell(
                    pendingDestination = pendingDestination,
                    onDestinationConsumed = onDestinationConsumed,
                )
            }
        }
    }
}
