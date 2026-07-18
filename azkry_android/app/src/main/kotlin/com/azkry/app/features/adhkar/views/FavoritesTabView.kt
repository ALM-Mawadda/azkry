package com.azkry.app.features.adhkar.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
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
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.preview.Samples
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.adhkar.viewmodels.DhikrCounterItem
import com.azkry.app.features.adhkar.viewmodels.FavoritesUiState
import com.azkry.app.features.adhkar.viewmodels.FavoritesViewModel

/**
 * The المفضلة home tab: every starred dhikr with its live tap counter.
 * Rendered inside the home scroll container, so it lays out as a plain
 * column rather than its own lazy list.
 */
@Composable
fun FavoritesTabView(
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    FavoritesTabContent(
        state = state.value,
        onDhikrTapped = viewModel::onDhikrTapped,
        onCounterLongPressed = viewModel::onCounterLongPressed,
        onFavoriteToggled = viewModel::onFavoriteToggled,
    )
}

@Composable
fun FavoritesTabContent(
    state: FavoritesUiState,
    onDhikrTapped: (DhikrCounterItem) -> Unit,
    onCounterLongPressed: (DhikrCounterItem) -> Unit,
    onFavoriteToggled: (DhikrCounterItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.S12),
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
    ) {
        if (!state.isLoading && state.items.isEmpty()) {
            EmptyFavorites()
            return@Column
        }

        state.items.forEach { item ->
            DhikrCard(
                item = item,
                onTap = { onDhikrTapped(item) },
                onCounterLongPress = { onCounterLongPressed(item) },
                onFavoriteToggle = { onFavoriteToggled(item) },
            )
        }
    }
}

@Composable
private fun EmptyFavorites() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AzkrySpacing.Xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
    ) {
        Icon(
            imageVector = Icons.Outlined.StarBorder,
            contentDescription = null,
            tint = AzkryTheme.colors.TextTertiary,
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = stringResource(R.string.favorites_empty),
            style = AzkryTextStyles.Body,
            color = AzkryTheme.colors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@AzkryPreview
@Composable
private fun FavoritesTabContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        FavoritesTabContent(
            state = FavoritesUiState(
                items = listOf(
                    DhikrCounterItem(dhikr = Samples.tasbihDhikr, count = 12, isFavorite = true),
                ),
                isLoading = false,
            ),
            onDhikrTapped = {},
            onCounterLongPressed = {},
            onFavoriteToggled = {},
        )
    }
}
