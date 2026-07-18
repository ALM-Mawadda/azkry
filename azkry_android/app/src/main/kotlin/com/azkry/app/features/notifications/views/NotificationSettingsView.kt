package com.azkry.app.features.notifications.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.notifications.models.ReminderKind
import com.azkry.app.features.notifications.models.titleRes
import com.azkry.app.features.notifications.services.NotificationSettings
import com.azkry.app.features.notifications.viewmodels.NotificationSettingsViewModel

@Composable
fun NotificationSettingsView(
    onBack: () -> Unit,
    viewModel: NotificationSettingsViewModel = hiltViewModel(),
) {
    val settings = viewModel.settings.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var pendingToggle by remember { mutableStateOf<ReminderKind?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // The toggle is stored regardless: notifications simply stay silent
        // until the permission is granted from system settings.
        pendingToggle?.let { kind -> viewModel.onKindToggled(kind, true) }
        pendingToggle = null
    }

    NotificationSettingsContent(
        settings = settings.value,
        onBack = onBack,
        onPreAdhanToggled = viewModel::onPreAdhanToggled,
        onNextPrayerOngoingToggled = viewModel::onNextPrayerOngoingToggled,
        onKindToggled = { kind, enabled ->
            val needsPermission = enabled &&
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) != PackageManager.PERMISSION_GRANTED
            if (needsPermission) {
                pendingToggle = kind
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.onKindToggled(kind, enabled)
            }
        },
    )
}

@Composable
fun NotificationSettingsContent(
    settings: NotificationSettings?,
    onBack: () -> Unit,
    onKindToggled: (ReminderKind, Boolean) -> Unit,
    onPreAdhanToggled: (Boolean) -> Unit = {},
    onNextPrayerOngoingToggled: (Boolean) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.settings_notifications),
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
            Text(
                text = stringResource(R.string.notif_section_adhan),
                style = AzkryTextStyles.Title2,
                color = AzkryTheme.colors.TextPrimary,
            )
            ReminderKind.adhanKinds.forEach { kind ->
                ReminderToggleRow(
                    title = stringResource(R.string.notif_adhan_row, stringResource(kind.titleRes())),
                    checked = kind in settings.enabledKinds,
                    onCheckedChange = { onKindToggled(kind, it) },
                )
            }
            ReminderToggleRow(
                title = stringResource(R.string.notif_pre_adhan_toggle),
                checked = settings.preAdhanEnabled,
                onCheckedChange = onPreAdhanToggled,
            )
            ReminderToggleRow(
                title = stringResource(R.string.notif_ongoing_toggle),
                checked = settings.nextPrayerOngoing,
                onCheckedChange = onNextPrayerOngoingToggled,
            )

            Text(
                text = stringResource(R.string.notif_section_adhkar),
                style = AzkryTextStyles.Title2,
                color = AzkryTheme.colors.TextPrimary,
                modifier = Modifier.padding(top = AzkrySpacing.Sm),
            )
            listOf(ReminderKind.MorningAdhkar, ReminderKind.EveningAdhkar).forEach { kind ->
                ReminderToggleRow(
                    title = stringResource(kind.titleRes()),
                    checked = kind in settings.enabledKinds,
                    onCheckedChange = { onKindToggled(kind, it) },
                )
            }

            Text(
                text = stringResource(R.string.notif_footer_note),
                style = AzkryTextStyles.Footnote,
                color = AzkryTheme.colors.TextSecondary,
                modifier = Modifier.padding(
                    horizontal = AzkrySpacing.Sm,
                    vertical = AzkrySpacing.Md,
                ),
            )
        }
    }
}

@Composable
private fun ReminderToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AzkrySpacing.Md,
                vertical = AzkrySpacing.Sm,
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                style = AzkryTextStyles.Headline,
                modifier = Modifier.weight(1f),
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedTrackColor = AzkryTheme.colors.AccentGreen,
                ),
            )
        }
    }
}

@AzkryPreview
@Composable
private fun NotificationSettingsContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        NotificationSettingsContent(
            settings = NotificationSettings(
                enabledKinds = setOf(ReminderKind.FajrAdhan, ReminderKind.MorningAdhkar),
            ),
            onBack = {},
            onKindToggled = { _, _ -> },
        )
    }
}
