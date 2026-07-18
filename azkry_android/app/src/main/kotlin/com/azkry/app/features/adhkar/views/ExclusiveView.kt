package com.azkry.app.features.adhkar.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.components.SectionRowCard
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.adhkar.viewmodels.ExclusiveViewModel

/** النسخة الحصرية: the extra sections (Umrah, Hajj, kids, loved ones…). */
@Composable
fun ExclusiveView(
    onBack: () -> Unit,
    onOpenCategory: (DhikrCategory) -> Unit,
    onOpenHijriCalendar: () -> Unit,
    viewModel: ExclusiveViewModel = hiltViewModel(),
) {
    val categories = viewModel.categories.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.exclusive_title),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Sm),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            SectionRowCard(
                title = stringResource(R.string.calendar_title),
                icon = Icons.Outlined.CalendarMonth,
                onClick = onOpenHijriCalendar,
            )
            categories.value.forEach { category ->
                SectionRowCard(
                    title = category.title,
                    icon = categoryIcon(category.iconKey),
                    onClick = { onOpenCategory(category) },
                )
            }
            if (categories.value.isEmpty()) {
                Text(
                    text = stringResource(R.string.coming_soon),
                    style = AzkryTextStyles.Body,
                    color = AzkryTheme.colors.TextSecondary,
                )
            }
        }
    }
}
