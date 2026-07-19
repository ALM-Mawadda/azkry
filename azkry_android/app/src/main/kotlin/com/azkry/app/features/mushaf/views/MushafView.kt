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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.components.SelectablePill
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryFonts
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.mushaf.models.JuzStart
import com.azkry.app.features.mushaf.models.LastRead
import com.azkry.app.features.mushaf.models.QuranBookmark
import com.azkry.app.features.mushaf.models.SurahInfo
import com.azkry.app.features.mushaf.viewmodels.KhatmahProgress
import com.azkry.app.features.mushaf.viewmodels.MushafTab
import com.azkry.app.features.mushaf.viewmodels.MushafUiState
import com.azkry.app.features.mushaf.viewmodels.MushafViewModel
import com.azkry.app.features.mushaf.viewmodels.PageEntry

/** Opening target for the surah reader. */
data class SurahOpenRequest(
    val surahNumber: Int,
    val startAyah: Int? = null,
    val startPage: Int? = null,
)

@Composable
fun MushafView(
    onBack: () -> Unit,
    onOpenSurah: (SurahOpenRequest) -> Unit,
    viewModel: MushafViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    MushafContent(
        state = state.value,
        onBack = onBack,
        onTabSelected = viewModel::onTabSelected,
        onOpenSurah = onOpenSurah,
        onStartKhatmah = viewModel::onStartKhatmah,
        onFinishKhatmah = viewModel::onFinishKhatmah,
    )
}

@Composable
fun MushafContent(
    state: MushafUiState,
    onBack: () -> Unit,
    onTabSelected: (MushafTab) -> Unit,
    onOpenSurah: (SurahOpenRequest) -> Unit,
    onStartKhatmah: (Int) -> Unit,
    onFinishKhatmah: () -> Unit,
) {
    val navigationBarPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    // Matches the design's darker Quran screens.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AzkryTheme.colors.MushafBackground),
    ) {
        ScreenHeader(
            title = stringResource(R.string.mushaf_title),
            onBack = onBack,
        )

        if (state.isLoading) {
            CenteredProgress()
            return@Column
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AzkrySpacing.Md,
                end = AzkrySpacing.Md,
                top = AzkrySpacing.Sm,
                bottom = AzkrySpacing.Xl + navigationBarPadding,
            ),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            item(span = { GridItemSpan(2) }) {
                LastReadRow(state = state, onOpenSurah = onOpenSurah)
            }

            item(span = { GridItemSpan(2) }) {
                KhatmahCard(
                    khatmah = state.khatmah,
                    onStartKhatmah = onStartKhatmah,
                    onFinishKhatmah = onFinishKhatmah,
                )
            }

            if (state.bookmarks.isNotEmpty()) {
                item(span = { GridItemSpan(2) }) {
                    BookmarksRow(bookmarks = state.bookmarks, onOpenSurah = onOpenSurah)
                }
            }

            item(span = { GridItemSpan(2) }) {
                MushafTabsRow(selected = state.selectedTab, onTabSelected = onTabSelected)
            }

            when (state.selectedTab) {
                MushafTab.Surahs -> items(state.surahs, key = SurahInfo::number) { surah ->
                    SurahCard(
                        surah = surah,
                        onClick = { onOpenSurah(SurahOpenRequest(surah.number)) },
                    )
                }

                MushafTab.Juzs -> items(
                    state.juzs,
                    key = { "juz-${it.number}" },
                    span = { GridItemSpan(2) },
                ) { juz ->
                    JuzRow(
                        juz = juz,
                        surahName = state.surahInfo(juz.surah)?.name.orEmpty(),
                        onClick = {
                            onOpenSurah(SurahOpenRequest(juz.surah, startAyah = juz.ayah))
                        },
                    )
                }

                MushafTab.Pages -> items(
                    state.pages,
                    key = { "page-${it.page}" },
                    span = { GridItemSpan(2) },
                ) { entry ->
                    PageRow(
                        entry = entry,
                        onClick = {
                            state.surahForPage(entry.page)?.let { surah ->
                                onOpenSurah(
                                    SurahOpenRequest(surah.number, startPage = entry.page),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LastReadRow(
    state: MushafUiState,
    onOpenSurah: (SurahOpenRequest) -> Unit,
) {
    val lastRead = state.lastRead
    val resume = {
        if (lastRead != null) {
            onOpenSurah(SurahOpenRequest(lastRead.surahNumber, startAyah = lastRead.ayahNumber))
        } else {
            onOpenSurah(SurahOpenRequest(1))
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.S12)) {
        LastReadCard(
            title = stringResource(R.string.mushaf_last_page),
            value = lastRead?.surahName ?: stringResource(R.string.mushaf_no_last_read),
            subtitle = lastRead?.let { stringResource(R.string.mushaf_page_n, it.page) },
            modifier = Modifier.weight(1f),
            onClick = resume,
        )
        LastReadCard(
            title = stringResource(R.string.mushaf_last_ayah),
            value = lastRead?.surahName ?: stringResource(R.string.mushaf_no_last_read),
            subtitle = lastRead?.let { stringResource(R.string.mushaf_ayah_n, it.ayahNumber) },
            modifier = Modifier.weight(1f),
            onClick = resume,
        )
    }
}

@Composable
private fun KhatmahCard(
    khatmah: KhatmahProgress?,
    onStartKhatmah: (Int) -> Unit,
    onFinishKhatmah: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Column(
            modifier = Modifier.padding(AzkrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        ) {
            Text(
                text = stringResource(R.string.khatmah_title),
                style = AzkryTextStyles.Title3,
            )
            if (khatmah == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm)) {
                    TextButton(onClick = { onStartKhatmah(30) }) {
                        Text(stringResource(R.string.khatmah_start_30))
                    }
                    TextButton(onClick = { onStartKhatmah(60) }) {
                        Text(stringResource(R.string.khatmah_start_60))
                    }
                }
            } else {
                Text(
                    text = stringResource(
                        R.string.khatmah_progress,
                        khatmah.dayNumber,
                        khatmah.totalDays,
                        khatmah.targetPage,
                    ),
                    style = AzkryTextStyles.Body,
                    color = AzkryTheme.colors.TextSecondary,
                )
                LinearProgressIndicator(
                    progress = {
                        khatmah.currentPage.toFloat() /
                            com.azkry.app.features.mushaf.models.Khatmah.TOTAL_PAGES
                    },
                    modifier = Modifier.fillMaxWidth(),
                    color = AzkryTheme.colors.AccentGreen,
                    trackColor = AzkryTheme.colors.RingTrack,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.khatmah_position, khatmah.currentPage),
                        style = AzkryTextStyles.Footnote,
                        color = AzkryTheme.colors.TextTertiary,
                    )
                    TextButton(onClick = onFinishKhatmah) {
                        Text(stringResource(R.string.khatmah_finish))
                    }
                }
            }
        }
    }
}

@Composable
private fun BookmarksRow(
    bookmarks: List<QuranBookmark>,
    onOpenSurah: (SurahOpenRequest) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm)) {
        Text(
            text = stringResource(R.string.mushaf_bookmarks),
            style = AzkryTextStyles.Title3,
            color = AzkryTheme.colors.TextPrimary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        ) {
            bookmarks.forEach { bookmark ->
                Surface(
                    onClick = {
                        onOpenSurah(
                            SurahOpenRequest(bookmark.surahNumber, startPage = bookmark.page),
                        )
                    },
                    shape = RoundedCornerShape(AzkryRadius.Pill),
                    color = AzkryTheme.colors.SurfaceCardStrong,
                    contentColor = AzkryTheme.colors.TextPrimary,
                    border = BorderStroke(1.dp, AzkryTheme.colors.AccentYellow.copy(alpha = 0.5f)),
                ) {
                    Text(
                        text = bookmark.surahName + " · " +
                            stringResource(R.string.mushaf_page_n, bookmark.page),
                        style = AzkryTextStyles.Callout,
                        modifier = Modifier.padding(
                            horizontal = AzkrySpacing.Md,
                            vertical = AzkrySpacing.Sm,
                        ),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun LastReadCard(
    title: String,
    value: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Column(
            modifier = Modifier.padding(AzkrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
        ) {
            Text(
                text = title,
                style = AzkryTextStyles.Headline,
            )
            Text(
                text = value,
                style = AzkryTextStyles.Body,
                color = AzkryTheme.colors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = AzkryTextStyles.Footnote,
                    color = AzkryTheme.colors.TextTertiary,
                )
            }
        }
    }
}

@Composable
private fun MushafTabsRow(
    selected: MushafTab,
    onTabSelected: (MushafTab) -> Unit,
) {
    Row(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
    ) {
        SelectablePill(
            label = stringResource(R.string.mushaf_tab_surahs),
            selected = selected == MushafTab.Surahs,
            onClick = { onTabSelected(MushafTab.Surahs) },
            contentPadding = PaddingValues(
                horizontal = AzkrySpacing.S20,
                vertical = AzkrySpacing.Sm,
            ),
            role = Role.Tab,
        )
        SelectablePill(
            label = stringResource(R.string.mushaf_tab_juzs),
            selected = selected == MushafTab.Juzs,
            onClick = { onTabSelected(MushafTab.Juzs) },
            contentPadding = PaddingValues(
                horizontal = AzkrySpacing.S20,
                vertical = AzkrySpacing.Sm,
            ),
            role = Role.Tab,
        )
        SelectablePill(
            label = stringResource(R.string.mushaf_tab_pages),
            selected = selected == MushafTab.Pages,
            onClick = { onTabSelected(MushafTab.Pages) },
            contentPadding = PaddingValues(
                horizontal = AzkrySpacing.S20,
                vertical = AzkrySpacing.Sm,
            ),
            role = Role.Tab,
        )
    }
}

@Composable
private fun SurahCard(
    surah: SurahInfo,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Box(modifier = Modifier.padding(AzkrySpacing.Md)) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(30.dp),
                shape = CircleShape,
                color = AzkryTheme.colors.SurfaceCardStrong,
                contentColor = AzkryTheme.colors.TextSecondary,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = surah.number.toString(), style = AzkryTextStyles.Label)
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AzkrySpacing.Xs),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
            ) {
                Text(
                    text = surah.name,
                    style = AzkryTextStyles.Title3.copy(fontFamily = AzkryFonts.Amiri),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = stringResource(
                        if (surah.isMeccan) R.string.mushaf_meccan else R.string.mushaf_medinan,
                    ) + "، " + stringResource(R.string.mushaf_ayat_count, surah.ayahCount),
                    style = AzkryTextStyles.Footnote,
                    color = AzkryTheme.colors.TextSecondary,
                )
                Text(
                    text = stringResource(R.string.mushaf_page_n, surah.page),
                    style = AzkryTextStyles.Footnote,
                    color = AzkryTheme.colors.TextTertiary,
                )
            }
        }
    }
}

@Composable
private fun JuzRow(
    juz: JuzStart,
    surahName: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AzkrySpacing.Md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.mushaf_juz_n, juz.number),
                style = AzkryTextStyles.Headline,
            )
            Text(
                text = "$surahName · " + stringResource(R.string.mushaf_page_n, juz.page),
                style = AzkryTextStyles.Subhead,
                color = AzkryTheme.colors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun PageRow(
    entry: PageEntry,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AzkrySpacing.Md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.mushaf_page_n, entry.page),
                style = AzkryTextStyles.Headline,
            )
            Text(
                text = entry.surahName,
                style = AzkryTextStyles.Subhead.copy(fontFamily = AzkryFonts.Amiri),
                color = AzkryTheme.colors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@AzkryPreview
@Composable
private fun MushafContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        MushafContent(
            state = MushafUiState(
                surahs = listOf(
                    SurahInfo(1, "سُورَةُ الفَاتِحَةِ", "Al-Faatiha", "Meccan", 7, 1, 1),
                    SurahInfo(2, "سُورَةُ البَقَرَةِ", "Al-Baqara", "Medinan", 286, 2, 1),
                ),
                juzs = listOf(JuzStart(1, 1, 1, 1)),
                lastRead = LastRead(7, "سُورَةُ الأَعۡرَافِ", 1, 151, 0L),
                bookmarks = listOf(QuranBookmark(18, "سُورَةُ الكَهۡفِ", 293)),
                khatmah = KhatmahProgress(dayNumber = 3, totalDays = 30, targetPage = 61, currentPage = 45),
                isLoading = false,
            ),
            onBack = {},
            onTabSelected = {},
            onOpenSurah = {},
            onStartKhatmah = {},
            onFinishKhatmah = {},
        )
    }
}
