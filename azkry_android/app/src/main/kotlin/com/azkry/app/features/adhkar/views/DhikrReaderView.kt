package com.azkry.app.features.adhkar.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.preview.Samples
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.adhkar.viewmodels.DhikrCounterItem
import com.azkry.app.features.adhkar.viewmodels.DhikrReaderUiState
import com.azkry.app.features.adhkar.viewmodels.DhikrReaderViewModel
import kotlinx.coroutines.delay

@Composable
fun DhikrReaderView(
    categoryId: Long,
    onBack: () -> Unit,
    viewModel: DhikrReaderViewModel = hiltViewModel(),
) {
    LaunchedEffect(categoryId) {
        viewModel.start(categoryId)
    }
    val state = viewModel.state.collectAsStateWithLifecycle()
    DhikrReaderContent(
        state = state.value,
        onBack = onBack,
        onDhikrTapped = viewModel::onDhikrTapped,
        onCounterLongPressed = viewModel::onCounterLongPressed,
        onFavoriteToggled = viewModel::onFavoriteToggled,
        onFinishedTapped = viewModel::onFinishedTapped,
    )
}

@Composable
fun DhikrReaderContent(
    state: DhikrReaderUiState,
    onBack: () -> Unit,
    onDhikrTapped: (DhikrCounterItem) -> Unit,
    onCounterLongPressed: (DhikrCounterItem) -> Unit,
    onFavoriteToggled: (DhikrCounterItem) -> Unit,
    onFinishedTapped: () -> Unit,
) {
    var detailsDhikrId by rememberSaveable { mutableStateOf<Long?>(null) }
    var celebrationVisible by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()

    // Auto-advance to the next incomplete dhikr when one completes; when the
    // whole set completes, celebrate instead.
    var lastCompletedCount by remember { mutableIntStateOf(-1) }
    LaunchedEffect(state.completedCount, state.items.size) {
        val previous = lastCompletedCount
        lastCompletedCount = state.completedCount
        if (previous < 0 || state.completedCount <= previous) return@LaunchedEffect
        if (state.allCompleted) {
            celebrationVisible = true
            delay(2600)
            celebrationVisible = false
        } else {
            val nextIndex = state.items.indexOfFirst { !it.isComplete }
            if (nextIndex >= 0) {
                listState.animateScrollToItem(nextIndex)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = state.category?.title.orEmpty(),
                onBack = onBack,
            )

            if (state.isLoading) {
                CenteredProgress()
                return@Column
            }

            val total = state.items.size
            LinearProgressIndicator(
                progress = { if (total == 0) 0f else state.completedCount.toFloat() / total },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AzkrySpacing.Md),
                color = AzkryTheme.colors.AccentBlue,
                trackColor = AzkryTheme.colors.RingTrack,
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = AzkrySpacing.Md,
                    end = AzkrySpacing.Md,
                    top = AzkrySpacing.Md,
                    bottom = AzkrySpacing.Xl,
                ),
                verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
            ) {
                itemsIndexed(state.items, key = { _, item -> item.dhikr.id }) { index, item ->
                    DhikrCard(
                        item = item,
                        positionLabel = "${index + 1} | $total",
                        onTap = { onDhikrTapped(item) },
                        onCounterLongPress = { onCounterLongPressed(item) },
                        onFavoriteToggle = { onFavoriteToggled(item) },
                        onLongPress = { detailsDhikrId = item.dhikr.id },
                    )
                }

                item {
                    FinishedButton(
                        categoryTitle = state.category?.title.orEmpty(),
                        allCompleted = state.allCompleted,
                        onClick = onFinishedTapped,
                    )
                }

                item {
                    ReaderHints()
                }

                item {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }

        CelebrationOverlay(
            visible = celebrationVisible,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = AzkrySpacing.Xl),
        )
    }

    val detailsItem = state.items.firstOrNull { it.dhikr.id == detailsDhikrId }
    if (detailsItem != null) {
        DhikrDetailsSheet(
            item = detailsItem,
            categoryTitle = state.category?.title.orEmpty(),
            onDismiss = { detailsDhikrId = null },
        )
    }
}

@Composable
private fun CelebrationOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
    ) {
        Surface(
            shape = RoundedCornerShape(AzkryRadius.Pill),
            color = AzkryTheme.colors.AccentGreen,
            contentColor = AzkryTheme.colors.Neutral0,
        ) {
            Text(
                text = stringResource(R.string.reader_celebration),
                style = AzkryTextStyles.Title3,
                modifier = Modifier.padding(
                    horizontal = AzkrySpacing.Lg,
                    vertical = AzkrySpacing.S12,
                ),
            )
        }
    }
}

@Composable
private fun FinishedButton(
    categoryTitle: String,
    allCompleted: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AzkrySpacing.Md),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(AzkryRadius.Pill),
            color = if (allCompleted) AzkryTheme.colors.AccentGreen else AzkryTheme.colors.SurfaceCardStrong,
            contentColor = if (allCompleted) AzkryTheme.colors.Neutral0 else AzkryTheme.colors.TextPrimary,
            border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = AzkrySpacing.Lg,
                    vertical = AzkrySpacing.S12,
                ),
                horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(R.string.reader_finished_prompt, categoryTitle),
                    style = AzkryTextStyles.Headline,
                )
            }
        }
    }
}

@Composable
private fun ReaderHints() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextSecondary,
    ) {
        Column(
            modifier = Modifier.padding(AzkrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
        ) {
            Text(text = "· " + stringResource(R.string.reader_hint_tap), style = AzkryTextStyles.Footnote)
            Text(text = "· " + stringResource(R.string.reader_hint_details), style = AzkryTextStyles.Footnote)
            Text(text = "· " + stringResource(R.string.reader_hint_reset), style = AzkryTextStyles.Footnote)
        }
    }
}

@AzkryPreview
@Composable
private fun DhikrReaderContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        DhikrReaderContent(
            state = DhikrReaderUiState(
                category = Samples.morningCategory,
                items = listOf(
                    DhikrCounterItem(dhikr = Samples.ayatAlKursi, count = 1),
                    DhikrCounterItem(dhikr = Samples.tasbihDhikr, count = 40),
                    DhikrCounterItem(dhikr = Samples.longSourcelessDhikr, count = 0),
                ),
                isLoading = false,
            ),
            onBack = {},
            onDhikrTapped = {},
            onCounterLongPressed = {},
            onFavoriteToggled = {},
            onFinishedTapped = {},
        )
    }
}
