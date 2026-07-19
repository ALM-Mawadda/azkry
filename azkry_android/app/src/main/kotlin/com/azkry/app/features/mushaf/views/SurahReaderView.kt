package com.azkry.app.features.mushaf.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.CircleIconButton
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryFonts
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.QuranTextStyle
import com.azkry.app.features.mushaf.models.QuranAyah
import com.azkry.app.features.mushaf.models.SurahContent
import com.azkry.app.core.utilities.toArabicIndicDigits
import com.azkry.app.features.mushaf.viewmodels.ReaderPage
import com.azkry.app.features.mushaf.viewmodels.SurahReaderUiState
import com.azkry.app.features.mushaf.viewmodels.SurahReaderViewModel
import kotlinx.coroutines.flow.distinctUntilChanged

private const val BASMALA = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

@Composable
fun SurahReaderView(
    surahNumber: Int,
    startAyah: Int?,
    startPage: Int?,
    onBack: () -> Unit,
    viewModel: SurahReaderViewModel = hiltViewModel(),
) {
    LaunchedEffect(surahNumber, startAyah, startPage) {
        viewModel.start(surahNumber, startAyah, startPage)
    }
    val state = viewModel.state.collectAsStateWithLifecycle()
    SurahReaderContent(
        state = state.value,
        onBack = onBack,
        onPageViewed = viewModel::onPageViewed,
        onBookmarkToggled = viewModel::onBookmarkToggled,
    )
}

@Composable
fun SurahReaderContent(
    state: SurahReaderUiState,
    onBack: () -> Unit,
    onPageViewed: (ReaderPage) -> Unit,
    onBookmarkToggled: () -> Unit,
) {
    val navigationBarPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    // The mushaf reads on a near-black backdrop, deliberately darker than
    // the slate app chrome (matching the design's Quran screens).
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzkryTheme.colors.MushafBackground),
    ) {
        ScreenHeader(
            title = state.surah?.name.orEmpty(),
            onBack = onBack,
            actions = {
                CircleIconButton(
                    icon = if (state.isCurrentPageBookmarked) {
                        Icons.Outlined.Bookmark
                    } else {
                        Icons.Outlined.BookmarkBorder
                    },
                    contentDescription = stringResource(R.string.mushaf_bookmark_add),
                    onClick = onBookmarkToggled,
                    tint = if (state.isCurrentPageBookmarked) {
                        AzkryTheme.colors.AccentYellow
                    } else {
                        AzkryTheme.colors.TextPrimary
                    },
                )
            },
        )

        if (state.isLoading || state.surah == null) {
            CenteredProgress()
            return@Column
        }

        val listState = rememberLazyListState(
            // +1 for the decorative surah header item that precedes the pages.
            initialFirstVisibleItemIndex = (state.initialPageIndex + 1).coerceAtMost(state.pages.size),
        )

        LaunchedEffect(listState, state.surah.number, state.initialPageIndex) {
            val requestedItem = (state.initialPageIndex + 1).coerceAtMost(state.pages.size)
            if (listState.firstVisibleItemIndex != requestedItem) {
                listState.scrollToItem(requestedItem)
            }
        }

        LaunchedEffect(listState, state.pages) {
            snapshotFlow { listState.firstVisibleItemIndex }
                .distinctUntilChanged()
                .collect { index ->
                    state.pages.getOrNull(index - 1)?.let(onPageViewed)
                }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AzkrySpacing.Md,
                end = AzkrySpacing.Md,
                bottom = AzkrySpacing.Xl + navigationBarPadding,
            ),
        ) {
            item(key = "surah-header") {
                SurahHeaderBand(surah = state.surah)
            }

            itemsIndexed(state.pages, key = { _, page -> "page-${page.page}" }) { index, page ->
                Column {
                    if (index == 0 && state.surah.number != 1 && state.surah.number != 9) {
                        Text(
                            text = BASMALA,
                            style = QuranTextStyle,
                            color = AzkryTheme.colors.TextPrimary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = AzkrySpacing.Sm),
                        )
                    }
                    PageText(page = page)
                    PageFooter(page = page)
                }
            }
        }
    }
}

@Composable
private fun SurahHeaderBand(surah: SurahContent) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AzkrySpacing.Sm),
        shape = RoundedCornerShape(AzkryRadius.Md),
        color = AzkryTheme.colors.SurfaceCardStrong,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.AccentYellow.copy(alpha = 0.5f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AzkrySpacing.S12),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AzkrySpacing.Md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrnamentLine(modifier = Modifier.weight(1f))
                Text(
                    text = surah.name,
                    style = AzkryTextStyles.Title2.copy(fontFamily = AzkryFonts.Amiri),
                    modifier = Modifier.padding(horizontal = AzkrySpacing.Md),
                )
                OrnamentLine(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun OrnamentLine(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(2.dp)
            .background(
                AzkryTheme.colors.AccentYellow.copy(alpha = 0.35f),
                RoundedCornerShape(1.dp),
            ),
    )
}

@Composable
private fun PageText(page: ReaderPage) {
    val markerColor = AzkryTheme.colors.AccentYellow
    val text = remember(page, markerColor) {
        buildAnnotatedString {
            page.ayahs.forEachIndexed { index, ayah ->
                append(ayah.text)
                append(" ")
                pushStyle(SpanStyle(color = markerColor))
                append("﴿${ayah.numberInSurah.toArabicIndicDigits()}﴾")
                pop()
                if (index != page.ayahs.lastIndex) append(" ")
            }
        }
    }
    Text(
        text = text,
        style = QuranTextStyle,
        color = AzkryTheme.colors.TextPrimary,
        textAlign = TextAlign.Justify,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AzkrySpacing.Sm, bottom = AzkrySpacing.Md),
    )
}

@Composable
private fun PageFooter(page: ReaderPage) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AzkrySpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = AzkryTheme.colors.Divider,
        )
        Text(
            text = stringResource(R.string.mushaf_page_n, page.page) +
                " · " + stringResource(R.string.mushaf_juz_n, page.juz),
            style = AzkryTextStyles.Caption,
            color = AzkryTheme.colors.TextTertiary,
            modifier = Modifier.padding(horizontal = AzkrySpacing.S12),
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = AzkryTheme.colors.Divider,
        )
    }
}

@AzkryPreview
@Composable
private fun SurahReaderContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        SurahReaderContent(
            state = SurahReaderUiState(
                surah = SurahContent(
                    number = 112,
                    name = "سُورَةُ الإِخۡلَاصِ",
                    ayahs = emptyList(),
                ),
                pages = listOf(
                    ReaderPage(
                        page = 604,
                        juz = 30,
                        ayahs = listOf(
                            QuranAyah(1, "قُلۡ هُوَ ٱللَّهُ أَحَدٌ", 604, 30),
                            QuranAyah(2, "ٱللَّهُ ٱلصَّمَدُ", 604, 30),
                            QuranAyah(3, "لَمۡ يَلِدۡ وَلَمۡ يُولَدۡ", 604, 30),
                            QuranAyah(4, "وَلَمۡ يَكُن لَّهُۥ كُفُوًا أَحَدُۢ", 604, 30),
                        ),
                    ),
                ),
                isLoading = false,
            ),
            onBack = {},
            onPageViewed = {},
            onBookmarkToggled = {},
        )
    }
}
