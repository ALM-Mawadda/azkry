package com.azkry.app.features.counter.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.ProgressRing
import com.azkry.app.core.components.SelectablePill
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.counter.services.COUNTER_TARGETS
import com.azkry.app.features.counter.services.CounterState
import com.azkry.app.features.counter.viewmodels.CounterViewModel

/** The العداد home tab: a persistent free tasbih counter. */
@Composable
fun CounterTabView(
    viewModel: CounterViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    CounterTabContent(
        state = state.value,
        onIncrement = viewModel::onIncrement,
        onReset = viewModel::onReset,
        onTargetSelected = viewModel::onTargetSelected,
    )
}

@Composable
fun CounterTabContent(
    state: CounterState?,
    onIncrement: () -> Unit,
    onReset: () -> Unit,
    onTargetSelected: (Int) -> Unit,
) {
    if (state == null) {
        CenteredProgress(modifier = Modifier.padding(vertical = AzkrySpacing.Xl))
        return
    }

    val haptics = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Lg),
    ) {
        Row(
            modifier = Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        ) {
            COUNTER_TARGETS.forEach { target ->
                SelectablePill(
                    label = if (target == 0) "∞" else target.toString(),
                    selected = state.target == target,
                    onClick = { onTargetSelected(target) },
                    role = Role.RadioButton,
                )
            }
        }

        Box(contentAlignment = Alignment.Center) {
            if (!state.isFree) {
                ProgressRing(
                    progress = state.countInLap.toFloat() / state.target,
                    size = 260.dp,
                    strokeWidth = 8.dp,
                    color = AzkryTheme.colors.AccentGreen,
                )
            }
            Surface(
                modifier = Modifier
                    .size(220.dp)
                    .combinedClickable(
                        onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                            onIncrement()
                        },
                        onLongClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onReset()
                        },
                    ),
                shape = CircleShape,
                color = AzkryTheme.colors.SurfaceCardStrong,
                contentColor = AzkryTheme.colors.TextPrimary,
                border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = state.countInLap.toString(),
                        style = AzkryTextStyles.Display,
                    )
                    if (!state.isFree && state.laps > 0) {
                        Text(
                            text = stringResource(R.string.counter_laps, state.laps),
                            style = AzkryTextStyles.Subhead,
                            color = AzkryTheme.colors.TextSecondary,
                        )
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(AzkryRadius.Lg),
            color = AzkryTheme.colors.SurfaceCard,
            contentColor = AzkryTheme.colors.TextSecondary,
        ) {
            Text(
                text = stringResource(R.string.counter_hint),
                style = AzkryTextStyles.Footnote,
                modifier = Modifier.padding(
                    horizontal = AzkrySpacing.Md,
                    vertical = AzkrySpacing.Sm,
                ),
            )
        }
    }
}

@AzkryPreview
@Composable
private fun CounterTabContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        CounterTabContent(
            state = CounterState(count = 45, target = 33),
            onIncrement = {},
            onReset = {},
            onTargetSelected = {},
        )
    }
}
