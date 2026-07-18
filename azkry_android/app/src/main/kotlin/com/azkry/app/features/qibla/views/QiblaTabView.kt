package com.azkry.app.features.qibla.views

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.qibla.viewmodels.QiblaUiState
import com.azkry.app.features.qibla.viewmodels.QiblaViewModel
import kotlin.math.roundToInt

/** The القبلة home tab: a compass dial whose needle points at the Kaaba. */
@Composable
fun QiblaTabView(
    viewModel: QiblaViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    QiblaTabContent(state = state.value)
}

@Composable
fun QiblaTabContent(state: QiblaUiState?) {
    if (state == null) {
        CenteredProgress(modifier = Modifier.padding(vertical = AzkrySpacing.Xl))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Lg),
    ) {
        Box(
            modifier = Modifier.size(260.dp),
            contentAlignment = Alignment.Center,
        ) {
            // The dial counter-rotates so its red tick tracks true north
            // while the needle independently tracks the qibla.
            CompassDial(modifier = Modifier.rotate(-state.azimuthDegrees.toFloat()))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .rotate(state.needleRotationDegrees.toFloat()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "🕋",
                    style = AzkryTextStyles.Title1,
                    modifier = Modifier.padding(top = AzkrySpacing.S12),
                )
                NeedleLine(modifier = Modifier.weight(1f))
            }

            Text(
                text = "${state.bearingDegrees.roundToInt()}°",
                style = AzkryTextStyles.Title1,
                color = AzkryTheme.colors.TextPrimary,
            )
        }

        Text(
            text = stringResource(R.string.qibla_bearing_label, state.bearingDegrees.roundToInt()),
            style = AzkryTextStyles.Headline,
            color = AzkryTheme.colors.TextPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = state.cityName,
            style = AzkryTextStyles.Subhead,
            color = AzkryTheme.colors.TextSecondary,
        )
        if (!state.hasCompass) {
            Text(
                text = stringResource(R.string.qibla_no_compass),
                style = AzkryTextStyles.Footnote,
                color = AzkryTheme.colors.Warning,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun CompassDial(modifier: Modifier = Modifier) {
    val tickColor = AzkryTheme.colors.RingTrack
    val majorTickColor = AzkryTheme.colors.TextSecondary
    val ringColor = AzkryTheme.colors.BorderDefault
    val northColor = AzkryTheme.colors.Error
    Canvas(modifier = modifier.size(260.dp)) {
        val radius = size.minDimension / 2f
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCircle(color = ringColor, radius = radius - 2.dp.toPx(), style = Stroke(2.dp.toPx()))
        for (degree in 0 until 360 step 15) {
            val isMajor = degree % 90 == 0
            val isNorth = degree == 0
            val angle = Math.toRadians(degree.toDouble())
            val outer = radius - 6.dp.toPx()
            val inner = outer - (if (isMajor) 16.dp.toPx() else 8.dp.toPx())
            val direction = Offset(
                kotlin.math.sin(angle).toFloat(),
                -kotlin.math.cos(angle).toFloat(),
            )
            drawLine(
                color = when {
                    isNorth -> northColor
                    isMajor -> majorTickColor
                    else -> tickColor
                },
                start = center + direction * inner,
                end = center + direction * outer,
                strokeWidth = if (isMajor) 3.dp.toPx() else 1.5f.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun NeedleLine(modifier: Modifier = Modifier) {
    val needleColor = AzkryTheme.colors.AccentYellow
    Canvas(modifier = modifier.fillMaxWidth()) {
        drawLine(
            color = needleColor,
            start = Offset(size.width / 2f, 0f),
            end = Offset(size.width / 2f, size.height * 0.32f),
            strokeWidth = 3.dp.toPx(),
            cap = StrokeCap.Round,
        )
    }
}

@AzkryPreview
@Composable
private fun QiblaTabContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        QiblaTabContent(
            state = QiblaUiState(
                cityName = "مكة المكرمة",
                bearingDegrees = 119.0,
                azimuthDegrees = 20.0,
                hasCompass = true,
            ),
        )
    }
}
