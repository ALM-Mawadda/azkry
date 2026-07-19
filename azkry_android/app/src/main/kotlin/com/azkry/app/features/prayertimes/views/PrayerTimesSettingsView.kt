package com.azkry.app.features.prayertimes.views

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.RadioPickerDialog
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.prayertimes.AsrMadhab
import com.azkry.app.core.prayertimes.CalculationMethod
import com.azkry.app.core.prayertimes.HighLatitudeRule
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.prayertimes.services.PrayerSettings
import com.azkry.app.features.prayertimes.viewmodels.PrayerTimesSettingsViewModel
import java.time.ZoneId
import java.util.Locale

private enum class OpenPicker { None, Method, AsrMadhab, HighLatitude, Location }

@Composable
fun PrayerTimesSettingsView(
    onBack: () -> Unit,
    viewModel: PrayerTimesSettingsViewModel = hiltViewModel(),
) {
    val settings = viewModel.settings.collectAsStateWithLifecycle()
    val isLocating = viewModel.isLocating.collectAsStateWithLifecycle()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.onUseCurrentLocation()
    }

    PrayerTimesSettingsContent(
        settings = settings.value,
        isLocating = isLocating.value,
        onBack = onBack,
        onMethodSelected = viewModel::onMethodSelected,
        onAsrMadhabSelected = viewModel::onAsrMadhabSelected,
        onHighLatitudeRuleSelected = viewModel::onHighLatitudeRuleSelected,
        onManualLocationEntered = viewModel::onManualLocationEntered,
        onUseCurrentLocation = {
            if (viewModel.hasLocationPermission) {
                viewModel.onUseCurrentLocation()
            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
            }
        },
    )
}

@Composable
fun PrayerTimesSettingsContent(
    settings: PrayerSettings?,
    isLocating: Boolean,
    onBack: () -> Unit,
    onMethodSelected: (CalculationMethod) -> Unit,
    onAsrMadhabSelected: (AsrMadhab) -> Unit,
    onHighLatitudeRuleSelected: (HighLatitudeRule) -> Unit,
    onManualLocationEntered: (String, Double, Double, ZoneId) -> Unit,
    onUseCurrentLocation: () -> Unit,
) {
    var openPicker by rememberSaveable { mutableStateOf(OpenPicker.None) }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.settings_prayer_times),
            onBack = onBack,
        )

        if (settings == null) {
            CenteredProgress()
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            SettingsCard(
                title = stringResource(R.string.prayer_settings_location),
                value = settings.cityName,
                secondary = stringResource(
                    R.string.prayer_settings_location_details,
                    stringResource(
                        R.string.prayer_settings_coordinates,
                        formatCoordinate(settings.location.longitude),
                        formatCoordinate(settings.location.latitude),
                    ),
                    settings.zoneId.id,
                ),
                isBusy = isLocating,
                onClick = { openPicker = OpenPicker.Location },
            )
            SettingsCard(
                title = stringResource(R.string.prayer_settings_method),
                value = stringResource(settings.method.labelRes()),
                onClick = { openPicker = OpenPicker.Method },
            )
            SettingsCard(
                title = stringResource(R.string.prayer_settings_asr),
                value = stringResource(settings.asrMadhab.labelRes()),
                onClick = { openPicker = OpenPicker.AsrMadhab },
            )
            SettingsCard(
                title = stringResource(R.string.prayer_settings_highlat),
                value = stringResource(settings.highLatitudeRule.labelRes()),
                onClick = { openPicker = OpenPicker.HighLatitude },
            )

            Text(
                text = stringResource(R.string.prayer_settings_note),
                style = AzkryTextStyles.Footnote,
                color = AzkryTheme.colors.TextSecondary,
                modifier = Modifier.padding(
                    horizontal = AzkrySpacing.Sm,
                    vertical = AzkrySpacing.Md,
                ),
            )
        }
    }

    when (openPicker) {
        OpenPicker.Method -> RadioPickerDialog(
            title = stringResource(R.string.prayer_settings_method),
            options = CalculationMethod.entries.map { it to stringResource(it.labelRes()) },
            selected = settings?.method,
            onSelected = {
                onMethodSelected(it)
                openPicker = OpenPicker.None
            },
            onDismiss = { openPicker = OpenPicker.None },
        )

        OpenPicker.AsrMadhab -> RadioPickerDialog(
            title = stringResource(R.string.prayer_settings_asr),
            options = AsrMadhab.entries.map { it to stringResource(it.labelRes()) },
            selected = settings?.asrMadhab,
            onSelected = {
                onAsrMadhabSelected(it)
                openPicker = OpenPicker.None
            },
            onDismiss = { openPicker = OpenPicker.None },
        )

        OpenPicker.HighLatitude -> RadioPickerDialog(
            title = stringResource(R.string.prayer_settings_highlat),
            options = HighLatitudeRule.entries.map { it to stringResource(it.labelRes()) },
            selected = settings?.highLatitudeRule,
            onSelected = {
                onHighLatitudeRuleSelected(it)
                openPicker = OpenPicker.None
            },
            onDismiss = { openPicker = OpenPicker.None },
        )

        OpenPicker.Location -> if (settings != null) {
            LocationDialog(
                settings = settings,
                onManualLocationEntered = { city, lat, lng, zoneId ->
                    onManualLocationEntered(city, lat, lng, zoneId)
                    openPicker = OpenPicker.None
                },
                onUseCurrentLocation = {
                    onUseCurrentLocation()
                    openPicker = OpenPicker.None
                },
                onDismiss = { openPicker = OpenPicker.None },
            )
        }

        OpenPicker.None -> Unit
    }
}

@Composable
private fun LocationDialog(
    settings: PrayerSettings,
    onManualLocationEntered: (String, Double, Double, ZoneId) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onDismiss: () -> Unit,
) {
    var city by rememberSaveable { mutableStateOf(settings.cityName) }
    var latitude by rememberSaveable { mutableStateOf(settings.location.latitude.toString()) }
    var longitude by rememberSaveable { mutableStateOf(settings.location.longitude.toString()) }
    var zoneIdText by rememberSaveable { mutableStateOf(settings.zoneId.id) }

    val parsedLatitude = latitude.toDoubleOrNull()?.takeIf { it in -90.0..90.0 }
    val parsedLongitude = longitude.toDoubleOrNull()?.takeIf { it in -180.0..180.0 }
    val parsedZoneId = runCatching { ZoneId.of(zoneIdText.trim()) }.getOrNull()
    val isValid = city.isNotBlank() && parsedLatitude != null &&
        parsedLongitude != null && parsedZoneId != null

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AzkryTheme.colors.SurfaceSheet,
        titleContentColor = AzkryTheme.colors.TextPrimary,
        textContentColor = AzkryTheme.colors.TextPrimary,
        title = {
            Text(
                text = stringResource(R.string.prayer_settings_location),
                style = AzkryTextStyles.Title3,
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
            ) {
                TextButton(onClick = onUseCurrentLocation) {
                    Text(text = stringResource(R.string.prayer_settings_use_current))
                }
                OutlinedTextField(
                    value = city,
                    onValueChange = { city = it },
                    label = { Text(stringResource(R.string.prayer_settings_city)) },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(stringResource(R.string.prayer_settings_latitude)) },
                    singleLine = true,
                    isError = parsedLatitude == null,
                )
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(stringResource(R.string.prayer_settings_longitude)) },
                    singleLine = true,
                    isError = parsedLongitude == null,
                )
                OutlinedTextField(
                    value = zoneIdText,
                    onValueChange = { zoneIdText = it },
                    label = { Text(stringResource(R.string.prayer_settings_timezone)) },
                    supportingText = { Text(stringResource(R.string.prayer_settings_timezone_hint)) },
                    singleLine = true,
                    isError = parsedZoneId == null,
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = {
                    onManualLocationEntered(
                        city.trim(),
                        parsedLatitude!!,
                        parsedLongitude!!,
                        parsedZoneId!!,
                    )
                },
            ) {
                Text(text = stringResource(R.string.action_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_close))
            }
        },
    )
}

@Composable
private fun SettingsCard(
    title: String,
    value: String,
    onClick: () -> Unit,
    secondary: String? = null,
    isBusy: Boolean = false,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Row(
            modifier = Modifier.padding(AzkrySpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
            ) {
                Text(text = title, style = AzkryTextStyles.Headline)
                Text(
                    text = value,
                    style = AzkryTextStyles.Body,
                    color = AzkryTheme.colors.AccentBlue,
                )
                if (secondary != null) {
                    Text(
                        text = secondary,
                        style = AzkryTextStyles.Footnote,
                        color = AzkryTheme.colors.TextSecondary,
                    )
                }
            }
            if (isBusy) {
                CircularProgressIndicator(modifier = Modifier.padding(AzkrySpacing.Xs))
            }
        }
    }
}

@StringRes
fun CalculationMethod.labelRes(): Int = when (this) {
    CalculationMethod.MuslimWorldLeague -> R.string.method_mwl
    CalculationMethod.Egyptian -> R.string.method_egyptian
    CalculationMethod.UmmAlQura -> R.string.method_umm_al_qura
    CalculationMethod.Karachi -> R.string.method_karachi
    CalculationMethod.NorthAmerica -> R.string.method_north_america
    CalculationMethod.France15 -> R.string.method_france15
    CalculationMethod.France12 -> R.string.method_france12
}

@StringRes
fun AsrMadhab.labelRes(): Int = when (this) {
    AsrMadhab.Shafii -> R.string.asr_shafii
    AsrMadhab.Hanafi -> R.string.asr_hanafi
}

@StringRes
fun HighLatitudeRule.labelRes(): Int = when (this) {
    HighLatitudeRule.None -> R.string.highlat_none
    HighLatitudeRule.MiddleOfTheNight -> R.string.highlat_middle
    HighLatitudeRule.SeventhOfTheNight -> R.string.highlat_seventh
    HighLatitudeRule.AngleBased -> R.string.highlat_angle_based
}

private fun formatCoordinate(value: Double): String =
    String.format(Locale.ENGLISH, "%.4f", value)

@AzkryPreview
@Composable
private fun PrayerTimesSettingsContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        PrayerTimesSettingsContent(
            settings = PrayerSettings(),
            isLocating = false,
            onBack = {},
            onMethodSelected = {},
            onAsrMadhabSelected = {},
            onHighLatitudeRuleSelected = {},
            onManualLocationEntered = { _, _, _, _ -> },
            onUseCurrentLocation = {},
        )
    }
}
