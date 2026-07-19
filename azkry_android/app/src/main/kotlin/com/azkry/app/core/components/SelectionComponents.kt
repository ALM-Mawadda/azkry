package com.azkry.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.azkry.app.R
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme

@Composable
fun SelectablePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = AzkrySpacing.Md,
        vertical = AzkrySpacing.Sm,
    ),
    role: Role? = null,
) {
    val shape = RoundedCornerShape(AzkryRadius.Pill)
    Surface(
        modifier = modifier
            .clip(shape)
            .selectable(
                selected = selected,
                role = role,
                onClick = onClick,
            ),
        shape = shape,
        color = if (selected) AzkryTheme.colors.SurfaceCardStrong else AzkryTheme.colors.SurfaceCard,
        contentColor = if (selected) AzkryTheme.colors.TextPrimary else AzkryTheme.colors.TextSecondary,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.BorderDefault,
        ),
    ) {
        Text(
            text = label,
            style = AzkryTextStyles.Callout,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
fun <T> RadioPickerDialog(
    title: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelected: (T) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AzkryTheme.colors.SurfaceSheet,
        titleContentColor = AzkryTheme.colors.TextPrimary,
        textContentColor = AzkryTheme.colors.TextPrimary,
        title = { Text(text = title, style = AzkryTextStyles.Title3) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                options.forEach { (option, label) ->
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = isSelected,
                                role = Role.RadioButton,
                                onClick = { onSelected(option) },
                            )
                            .padding(vertical = AzkrySpacing.Xs),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = null,
                        )
                        Text(text = label, style = AzkryTextStyles.Body)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.action_close))
            }
        },
    )
}
