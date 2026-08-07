package com.azkry.app.features.settings.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.azkry.app.R
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.settings.models.APP_LICENSES
import com.azkry.app.features.settings.models.AppLicense

/**
 * Attribution for every third-party work the app ships. The CC BY-SA adhan
 * recording and the OFL fonts require their notices to reach the user, so this
 * screen is a licence obligation rather than an about page.
 */
@Composable
fun LicensesView(onBack: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = stringResource(R.string.settings_licenses), onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.S12)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            Text(
                text = stringResource(R.string.licenses_intro),
                style = AzkryTextStyles.Subhead,
                color = AzkryTheme.colors.TextSecondary,
            )

            APP_LICENSES.forEach { license -> LicenseCard(license) }

            Text(
                text = stringResource(R.string.licenses_ofl_note),
                style = AzkryTextStyles.Footnote,
                color = AzkryTheme.colors.TextTertiary,
            )
        }
    }
}

@Composable
private fun LicenseCard(license: AppLicense) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Column(
            modifier = Modifier.padding(AzkrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
        ) {
            Text(
                text = stringResource(license.headingRes),
                style = AzkryTextStyles.Headline,
                color = AzkryTheme.colors.TextPrimary,
            )
            LegalText(license.work, AzkryTheme.colors.TextSecondary)
            LegalText(license.copyright, AzkryTheme.colors.TextTertiary)
            LegalText(license.license, AzkryTheme.colors.AccentYellow)
            LegalText(license.url, AzkryTheme.colors.TextTertiary)
        }
    }
}

/**
 * Latin legal text inside an RTL layout: forced LTR so licence names and URIs
 * stay readable and copyable exactly as published.
 */
@Composable
private fun LegalText(text: String, color: Color) {
    Text(
        text = text,
        style = AzkryTextStyles.Footnote.copy(textDirection = TextDirection.Ltr),
        color = color,
    )
}

@AzkryPreview
@Composable
private fun LicensesViewPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        LicensesView(onBack = {})
    }
}
