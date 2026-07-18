package com.azkry.app.features.adhkar.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.components.SectionRowCard
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.preview.Samples
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.features.adhkar.viewmodels.AdhkarCategoriesUiState
import com.azkry.app.features.adhkar.viewmodels.AdhkarCategoriesViewModel

@Composable
fun AdhkarCategoriesView(
    onBack: () -> Unit,
    onOpenCategory: (DhikrCategory) -> Unit,
    viewModel: AdhkarCategoriesViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    AdhkarCategoriesContent(
        state = state.value,
        onBack = onBack,
        onOpenCategory = onOpenCategory,
    )
}

@Composable
fun AdhkarCategoriesContent(
    state: AdhkarCategoriesUiState,
    onBack: () -> Unit,
    onOpenCategory: (DhikrCategory) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.home_adhkar_duas),
            onBack = onBack,
        )

        if (state.isLoading) {
            CenteredProgress()
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                start = AzkrySpacing.Md,
                end = AzkrySpacing.Md,
                top = AzkrySpacing.Sm,
                bottom = AzkrySpacing.Xl,
            ),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            items(state.categories, key = DhikrCategory::id) { category ->
                SectionRowCard(
                    title = category.title,
                    icon = categoryIcon(category.iconKey),
                    onClick = { onOpenCategory(category) },
                )
            }
            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@AzkryPreview
@Composable
private fun AdhkarCategoriesContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        AdhkarCategoriesContent(
            state = AdhkarCategoriesUiState(
                categories = Samples.categories,
                isLoading = false,
            ),
            onBack = {},
            onOpenCategory = {},
        )
    }
}
