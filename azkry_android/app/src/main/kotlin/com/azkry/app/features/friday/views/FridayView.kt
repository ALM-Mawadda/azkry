package com.azkry.app.features.friday.views

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
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
import com.azkry.app.features.friday.models.FridaySunnah
import com.azkry.app.features.friday.viewmodels.FridayUiState
import com.azkry.app.features.friday.viewmodels.FridayViewModel

@Composable
fun FridayView(
    onBack: () -> Unit,
    onOpenKahf: () -> Unit,
    viewModel: FridayViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    FridayContent(
        state = state.value,
        onBack = onBack,
        onOpenKahf = onOpenKahf,
        onSunnahToggled = viewModel::onSunnahToggled,
    )
}

@Composable
fun FridayContent(
    state: FridayUiState?,
    onBack: () -> Unit,
    onOpenKahf: () -> Unit,
    onSunnahToggled: (FridaySunnah, Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.home_friday),
            onBack = onBack,
        )

        if (state == null) {
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
            SunanCard(checks = state.checks, onSunnahToggled = onSunnahToggled)
            SalawatCard()
            KahfShortcutCard(onOpenKahf = onOpenKahf)
            FadailSection()
        }
    }
}

@Composable
private fun SunanCard(
    checks: Set<FridaySunnah>,
    onSunnahToggled: (FridaySunnah, Boolean) -> Unit,
) {
    SectionCard(title = stringResource(R.string.friday_sunan_title)) {
        FridaySunnah.entries.chunked(2).forEach { rowSunan ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AzkrySpacing.Sm),
                horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
            ) {
                rowSunan.forEach { sunnah ->
                    val checked = sunnah in checks
                    SunnahChip(
                        label = stringResource(sunnah.titleRes),
                        checked = checked,
                        onClick = { onSunnahToggled(sunnah, checked) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (rowSunan.size == 1) {
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SunnahChip(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = if (checked) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.TextPrimary,
        border = BorderStroke(
            1.dp,
            if (checked) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.BorderDefault,
        ),
    ) {
        Text(
            text = label,
            style = AzkryTextStyles.Callout,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                horizontal = AzkrySpacing.Sm,
                vertical = AzkrySpacing.S12,
            ),
        )
    }
}

@Composable
private fun SalawatCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Text(
            text = stringResource(R.string.friday_salawat_text),
            style = AzkryTextStyles.DhikrBody,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(AzkrySpacing.S20),
        )
    }
}

@Composable
private fun KahfShortcutCard(onOpenKahf: () -> Unit) {
    Surface(
        onClick = onOpenKahf,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.AccentGreen.copy(alpha = 0.15f),
        contentColor = AzkryTheme.colors.AccentGreen,
        border = BorderStroke(1.dp, AzkryTheme.colors.AccentGreen.copy(alpha = 0.5f)),
    ) {
        Row(
            modifier = Modifier.padding(AzkrySpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
            Text(
                text = stringResource(R.string.friday_open_kahf),
                style = AzkryTextStyles.Headline,
            )
        }
    }
}

@Composable
private fun FadailSection() {
    SectionCard(title = stringResource(R.string.friday_fadail_title)) {
        Column(verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Md)) {
            Text(
                text = stringResource(R.string.friday_fadail_1),
                style = AzkryTextStyles.DhikrBody,
            )
            Text(
                text = stringResource(R.string.friday_fadail_2),
                style = AzkryTextStyles.Body,
                color = AzkryTheme.colors.TextSecondary,
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Column(modifier = Modifier.padding(AzkrySpacing.Md)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = AzkrySpacing.Md),
                shape = RoundedCornerShape(AzkryRadius.Md),
                color = AzkryTheme.colors.Slate900,
                contentColor = AzkryTheme.colors.TextPrimary,
            ) {
                Text(
                    text = title,
                    style = AzkryTextStyles.Title3,
                    modifier = Modifier.padding(AzkrySpacing.S12),
                )
            }
            content()
        }
    }
}

@AzkryPreview
@Composable
private fun FridayContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        FridayContent(
            state = FridayUiState(checks = setOf(FridaySunnah.Dua, FridaySunnah.Kahf)),
            onBack = {},
            onOpenKahf = {},
            onSunnahToggled = { _, _ -> },
        )
    }
}
