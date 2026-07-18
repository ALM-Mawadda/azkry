package com.azkry.app.features.adhkar.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.azkry.app.R
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.adhkar.viewmodels.DhikrCounterItem

/** Long-press details: the full text plus source, counts, and category. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DhikrDetailsSheet(
    item: DhikrCounterItem,
    categoryTitle: String,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AzkryTheme.colors.SurfaceSheet,
        contentColor = AzkryTheme.colors.TextPrimary,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AzkrySpacing.Lg)
                .padding(bottom = AzkrySpacing.Xl)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Md),
        ) {
            Text(
                text = item.dhikr.title ?: stringResource(R.string.details_title),
                style = AzkryTextStyles.Title2,
            )
            Text(
                text = item.dhikr.text,
                style = AzkryTextStyles.DhikrBody,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            item.dhikr.virtue?.let { virtue ->
                Text(
                    text = virtue,
                    style = AzkryTextStyles.Subhead,
                    color = AzkryTheme.colors.TextSecondary,
                )
            }
            item.dhikr.source?.let { source ->
                Text(
                    text = source,
                    style = AzkryTextStyles.Callout,
                    color = AzkryTheme.colors.AccentYellow,
                )
            }
            Text(
                text = stringResource(R.string.details_repeat, item.dhikr.repeatCount),
                style = AzkryTextStyles.Body,
                color = AzkryTheme.colors.TextSecondary,
            )
            Text(
                text = stringResource(R.string.details_today_count, item.count),
                style = AzkryTextStyles.Body,
                color = AzkryTheme.colors.TextSecondary,
            )
            if (categoryTitle.isNotBlank()) {
                Text(
                    text = stringResource(R.string.details_category, categoryTitle),
                    style = AzkryTextStyles.Body,
                    color = AzkryTheme.colors.TextSecondary,
                )
            }
        }
    }
}
