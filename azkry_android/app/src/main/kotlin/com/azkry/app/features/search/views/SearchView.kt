package com.azkry.app.features.search.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.preview.Samples
import com.azkry.app.core.theme.AzkryFonts
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.mushaf.models.SurahInfo
import com.azkry.app.features.search.viewmodels.DhikrSearchHit
import com.azkry.app.features.search.viewmodels.SearchUiState
import com.azkry.app.features.search.viewmodels.SearchViewModel

@Composable
fun SearchView(
    onBack: () -> Unit,
    onOpenDhikrCategory: (categoryId: Long) -> Unit,
    onOpenSurah: (surahNumber: Int) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    SearchContent(
        state = state.value,
        onBack = onBack,
        onQueryChanged = viewModel::onQueryChanged,
        onOpenDhikrCategory = onOpenDhikrCategory,
        onOpenSurah = onOpenSurah,
    )
}

@Composable
fun SearchContent(
    state: SearchUiState,
    onBack: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onOpenDhikrCategory: (Long) -> Unit,
    onOpenSurah: (Int) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val navigationBarPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding(),
    ) {
        ScreenHeader(
            title = stringResource(R.string.search_title),
            onBack = onBack,
        )

        OutlinedTextField(
            value = state.query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AzkrySpacing.Md),
            placeholder = { Text(stringResource(R.string.search_hint)) },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
            },
            singleLine = true,
            shape = RoundedCornerShape(AzkryRadius.Lg),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                },
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AzkryTheme.colors.AccentYellow,
                unfocusedBorderColor = AzkryTheme.colors.BorderDefault,
            ),
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AzkrySpacing.Md,
                end = AzkrySpacing.Md,
                top = AzkrySpacing.Md,
                bottom = AzkrySpacing.Xl + navigationBarPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        ) {
            if (state.isEmptyResult) {
                item {
                    Text(
                        text = stringResource(R.string.search_empty),
                        style = AzkryTextStyles.Body,
                        color = AzkryTheme.colors.TextSecondary,
                        modifier = Modifier.padding(AzkrySpacing.Md),
                    )
                }
            }

            if (state.surahHits.isNotEmpty()) {
                item {
                    SectionLabel(stringResource(R.string.search_section_surahs))
                }
                items(state.surahHits, key = { "surah-${it.number}" }) { surah ->
                    SurahHitRow(surah = surah, onClick = { onOpenSurah(surah.number) })
                }
            }

            if (state.adhkarHits.isNotEmpty()) {
                item {
                    SectionLabel(stringResource(R.string.search_section_adhkar))
                }
                items(state.adhkarHits, key = { "dhikr-${it.dhikr.id}" }) { hit ->
                    DhikrHitRow(
                        hit = hit,
                        onClick = { onOpenDhikrCategory(hit.dhikr.categoryId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = AzkryTextStyles.Title3,
        color = AzkryTheme.colors.TextPrimary,
        modifier = Modifier.padding(vertical = AzkrySpacing.Xs),
    )
}

@Composable
private fun SurahHitRow(surah: SurahInfo, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Row(
            modifier = Modifier.padding(AzkrySpacing.Md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = surah.name,
                style = AzkryTextStyles.Headline.copy(fontFamily = AzkryFonts.Amiri),
            )
            Text(
                text = stringResource(R.string.mushaf_page_n, surah.page),
                style = AzkryTextStyles.Footnote,
                color = AzkryTheme.colors.TextSecondary,
            )
        }
    }
}

@Composable
private fun DhikrHitRow(hit: DhikrSearchHit, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Column(
            modifier = Modifier.padding(AzkrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
        ) {
            Text(
                text = hit.dhikr.text,
                style = AzkryTextStyles.Body.copy(fontFamily = AzkryFonts.Amiri),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = hit.categoryTitle,
                style = AzkryTextStyles.Footnote,
                color = AzkryTheme.colors.TextSecondary,
            )
        }
    }
}

@AzkryPreview
@Composable
private fun SearchContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        SearchContent(
            state = SearchUiState(
                query = "سبحان",
                adhkarHits = listOf(
                    DhikrSearchHit(
                        dhikr = Samples.tasbihDhikr,
                        categoryTitle = "أذكار الصباح",
                    ),
                ),
                surahHits = listOf(
                    SurahInfo(1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", "Meccan", 7, 1, 1),
                ),
            ),
            onBack = {},
            onQueryChanged = {},
            onOpenDhikrCategory = {},
            onOpenSurah = {},
        )
    }
}
