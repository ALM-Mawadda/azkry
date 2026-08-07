package com.azkry.app.features.home.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CircleIconButton
import com.azkry.app.core.components.ProgressRing
import com.azkry.app.core.components.SectionRowCard
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.models.labelRes
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.preview.Samples
import com.azkry.app.core.theme.AzkryIcons
import com.azkry.app.core.theme.AzkryFonts
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.adhkar.views.FavoritesTabView
import com.azkry.app.features.adhkar.views.categoryIcon
import com.azkry.app.features.counter.views.CounterTabView
import com.azkry.app.features.home.models.HeaderPhase
import com.azkry.app.features.home.viewmodels.HomeUiState
import com.azkry.app.features.home.viewmodels.HomeViewModel
import com.azkry.app.features.qibla.views.QiblaTabView

enum class HomeTab(val titleRes: Int) {
    Misc(R.string.tab_misc),
    Prayer(R.string.tab_prayer),
    Qibla(R.string.tab_qibla),
    Favorites(R.string.tab_favorites),
    Counter(R.string.tab_counter),
}

/** Navigation callbacks the home hub can trigger on the shell. */
data class HomeNavigation(
    val onOpenTracking: () -> Unit,
    val onOpenAdhkar: () -> Unit,
    val onOpenMushaf: () -> Unit,
    val onOpenPages: () -> Unit,
    val onOpenFriday: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenExclusive: () -> Unit,
    val onOpenCategoryReader: (Long) -> Unit,
    val onOpenPrayerDay: () -> Unit,
)

/** Header text stays light in every phase — it sits on the sky, not the page. */
private val SkyText = Color(0xFFF2F5F8)
private val SkyTextSecondary = Color(0xFFC9D4E2)

@Composable
fun HomeView(
    navigation: HomeNavigation,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(HomeTab.Misc) }
    HomeContent(
        state = state.value,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        navigation = navigation,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeContent(
    state: HomeUiState?,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    navigation: HomeNavigation,
) {
    val listState = rememberLazyListState()
    // The sky header is item 0: once it scrolls away, the pinned bar grows
    // the compact chrome (actions + wordmark), matching the design.
    val collapsed by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }
    // Content of the sky header fades progressively with the scroll offset,
    // like the design — the strip dims as it slides under the pinned bar.
    val collapseRangePx = with(LocalDensity.current) { 180.dp.toPx() }
    val headerAlpha by remember(collapseRangePx) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                0f
            } else {
                (1f - listState.firstVisibleItemScrollOffset / collapseRangePx)
                    .coerceIn(0f, 1f)
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AzkrySpacing.Xl),
    ) {
        item(key = "sky-header") {
            SkyHeaderSection(
                state = state,
                navigation = navigation,
                contentAlpha = { headerAlpha },
            )
        }

        stickyHeader(key = "pinned-bar") {
            PinnedBar(
                collapsed = collapsed,
                selectedTab = selectedTab,
                onTabSelected = onTabSelected,
                navigation = navigation,
            )
        }

        when (selectedTab) {
            HomeTab.Misc -> {
                if (state != null) {
                    item { AdhanCountdownRow(state) }
                }
                item { HomeSections(state = state, navigation = navigation) }
                if (state != null) {
                    item {
                        LocationFooter(state = state, onOpenSettings = navigation.onOpenSettings)
                    }
                }
            }

            // الصلاة is a destination, not an inline tab: tapping it opens
            // the full-screen prayer board, so nothing renders here.
            HomeTab.Prayer -> Unit
            HomeTab.Qibla -> item { QiblaTabView() }
            HomeTab.Favorites -> item { FavoritesTabView() }
            HomeTab.Counter -> item { CounterTabView() }
        }

        item {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

/** Translucent band the prayer strip sits on, like the design. */
private val StripBand = Color(0x21C7D4FF)

/**
 * The expanded sky header: actions, rotating verse, and the prayer strip.
 * [contentAlpha] fades the content (not the sky) as the list scrolls, so the
 * header melts away gradually instead of jumping.
 */
@Composable
private fun SkyHeaderSection(
    state: HomeUiState?,
    navigation: HomeNavigation,
    contentAlpha: () -> Float,
) {
    val phase = state?.headerPhase ?: HeaderPhase.Night
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sky(phase = phase, pageColor = AzkryTheme.colors.Slate900)
            .graphicsLayer { alpha = contentAlpha() }
            .statusBarsPadding()
            .padding(top = AzkrySpacing.Sm),
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Lg),
    ) {
        Box(modifier = Modifier.padding(horizontal = AzkrySpacing.Md)) {
            HeaderActionsRow(navigation = navigation, showLogo = false, onSky = true)
        }

        Text(
            text = state?.verse.orEmpty(),
            style = AzkryTextStyles.Title2,
            color = SkyText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Md),
        )

        if (state != null) {
            PrayerStripBand(state)
        }
    }
}

/**
 * The pinned bar under the header: always the tab strip; once the sky has
 * scrolled away it also carries the compact chrome (actions + wordmark).
 */
@Composable
private fun PinnedBar(
    collapsed: Boolean,
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
    navigation: HomeNavigation,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AzkryTheme.colors.Slate900,
        contentColor = AzkryTheme.colors.TextPrimary,
    ) {
        Column(
            modifier = if (collapsed) Modifier.statusBarsPadding() else Modifier,
        ) {
            AnimatedVisibility(
                visible = collapsed,
                enter = expandVertically(),
                exit = shrinkVertically(),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Sm),
                ) {
                    HeaderActionsRow(navigation = navigation, showLogo = true, onSky = false)
                }
            }
            HomeTabsRow(
                selectedTab = selectedTab,
                onTabSelected = { tab ->
                    if (tab == HomeTab.Prayer) {
                        navigation.onOpenPrayerDay()
                    } else {
                        onTabSelected(tab)
                    }
                },
            )
        }
    }
}

/** Indigo-ringed circles on the sky, like the design header buttons. */
private val SkyButtonFill = Color(0x59202A5C)
private val SkyButtonRing = Color(0x805E6BB8)

@Composable
private fun HeaderActionsRow(
    navigation: HomeNavigation,
    showLogo: Boolean,
    onSky: Boolean,
) {
    val fill = if (onSky) SkyButtonFill else AzkryTheme.colors.SurfaceCard
    val ring = if (onSky) SkyButtonRing else AzkryTheme.colors.BorderDefault
    val tint = if (onSky) SkyText else AzkryTheme.colors.TextPrimary

    Box(modifier = Modifier.fillMaxWidth()) {
        // Reading-start side (visual right in RTL): the lone play button,
        // matching the design layout exactly.
        CircleIconButton(
            icon = Icons.Outlined.PlayArrow,
            contentDescription = stringResource(R.string.action_play),
            onClick = navigation.onOpenMushaf,
            modifier = Modifier.align(Alignment.CenterStart),
            tint = tint,
            containerColor = fill,
            borderColor = ring,
        )

        if (showLogo) {
            Text(
                text = stringResource(R.string.app_name),
                style = AzkryTextStyles.Title2.copy(fontFamily = AzkryFonts.Amiri),
                color = AzkryTheme.colors.TextPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        // Opposite side (visual left): settings, search, bookmark reading
        // left-to-right, as in the design.
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        ) {
            CircleIconButton(
                icon = Icons.Outlined.Bookmark,
                contentDescription = stringResource(R.string.action_bookmarks),
                onClick = navigation.onOpenMushaf,
                tint = tint,
                containerColor = fill,
                borderColor = ring,
            )
            CircleIconButton(
                icon = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.action_search),
                onClick = navigation.onOpenSearch,
                tint = tint,
                containerColor = fill,
                borderColor = ring,
            )
            CircleIconButton(
                icon = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.action_settings),
                onClick = navigation.onOpenSettings,
                tint = tint,
                containerColor = fill,
                borderColor = ring,
            )
        }
    }
}

/** The pill rises this much above the band's top edge, into the sky. */
private val ChipOverflowTop = 22.dp

/** The pill sinks this much past the bottom edge — clipped, tucked under. */
private val ChipTuckBottom = 10.dp

private val ChipCorner = 18.dp

/**
 * The prayer strip as in the design: a lighter full-width panel whose top
 * line runs behind the taller hijri pill; the pill is the SAME fill as the
 * panel — both are drawn as one path so the overlap is seamless — and its
 * bottom is clipped, tucked under the tabs bar below. Prayer name and time
 * are both bold white, centered on the panel.
 */
@Composable
private fun PrayerStripBand(state: HomeUiState) {
    val density = LocalDensity.current
    val bandTopPx = with(density) { ChipOverflowTop.toPx() }
    val chipCornerPx = with(density) { ChipCorner.toPx() }
    var containerCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    var chipRect by remember { mutableStateOf<Rect?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clipToBounds()
            .onGloballyPositioned { containerCoords = it }
            .drawBehind {
                // One fill for panel + pill: overlap stays uniform, so the
                // pill reads as part of the panel rather than a box on it.
                val path = Path().apply {
                    addRect(Rect(0f, bandTopPx, size.width, size.height))
                    chipRect?.let { rect ->
                        addRoundRect(
                            RoundRect(rect, CornerRadius(chipCornerPx, chipCornerPx)),
                        )
                    }
                }
                drawPath(path, StripBand)
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AzkrySpacing.Md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Equal-weight side columns keep the pill dead-centre no matter how
            // wide either prayer label is — the text adapts, the pill never moves.
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = ChipOverflowTop),
                contentAlignment = Alignment.Center,
            ) {
                PrayerStripTime(
                    label = stringResource(state.previousPrayer.labelRes()),
                    time = state.previousTime,
                )
            }

            HijriChip(
                state = state,
                modifier = Modifier
                    .offset(y = ChipTuckBottom)
                    .onGloballyPositioned { coords ->
                        chipRect = containerCoords?.localBoundingBoxOf(coords, false)
                    },
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(top = ChipOverflowTop),
                contentAlignment = Alignment.Center,
            ) {
                PrayerStripTime(
                    label = stringResource(state.upcomingPrayer.labelRes()),
                    time = state.upcomingTime,
                )
            }
        }

        HorizontalDivider(
            modifier = Modifier.align(Alignment.BottomCenter),
            thickness = 1.dp,
            color = Color(0x2EFFFFFF),
        )
    }
}

/**
 * Two-line hijri pill (day above month). Outline only — its fill is drawn
 * by [PrayerStripBand] as one shape with the panel.
 */
@Composable
private fun HijriChip(state: HomeUiState, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .border(1.dp, Color(0x47FFFFFF), RoundedCornerShape(ChipCorner))
            .padding(horizontal = AzkrySpacing.S20, vertical = AzkrySpacing.S12),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = state.hijriDay.toString(),
            style = AzkryTextStyles.Title2,
            color = SkyText,
        )
        Text(
            text = state.hijriDayMonth,
            style = AzkryTextStyles.Subhead,
            color = SkyTextSecondary,
        )
    }
}

@Composable
private fun PrayerStripTime(
    label: String,
    time: String,
    modifier: Modifier = Modifier,
) {
    // Name and time are equally bold and white in the design.
    Row(
        modifier = modifier.padding(vertical = AzkrySpacing.S12),
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = AzkryTextStyles.Title2, color = SkyText)
        Text(text = time, style = AzkryTextStyles.Title2, color = SkyText)
    }
}

@Composable
private fun HomeTabsRow(
    selectedTab: HomeTab,
    onTabSelected: (HomeTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectableGroup()
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.S12),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HomeTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .selectable(
                        selected = isSelected,
                        role = Role.Tab,
                        onClick = { onTabSelected(tab) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(tab.titleRes),
                    style = AzkryTextStyles.Headline,
                    color = if (isSelected) {
                        AzkryTheme.colors.TextPrimary
                    } else {
                        AzkryTheme.colors.TextSecondary
                    },
                )
            }
        }
    }
}

@Composable
private fun AdhanCountdownRow(state: HomeUiState) {
    // Reference layout: yellow accent bar + white phrase on the reading-start
    // side ("أذان العشاء بعد 4 دقائق"), gray precise ticker opposite ("3:08").
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.S12),
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 20.dp)
                .background(AzkryTheme.colors.AccentYellow, RoundedCornerShape(2.dp)),
        )
        Text(
            text = stringResource(
                R.string.adhan_countdown_line,
                stringResource(state.countdownPrayer.labelRes()),
                state.countdownPhrase,
            ),
            style = AzkryTextStyles.Headline,
            color = AzkryTheme.colors.TextPrimary,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = state.countdownClock,
            style = AzkryTextStyles.Headline,
            color = AzkryTheme.colors.TextSecondary,
        )
    }
}

@Composable
private fun HomeSections(
    state: HomeUiState?,
    navigation: HomeNavigation,
) {
    Column(
        modifier = Modifier.padding(
            horizontal = AzkrySpacing.Md,
            vertical = AzkrySpacing.S12,
        ),
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
    ) {
        SectionRowCard(
            title = stringResource(R.string.home_worship_tracking),
            onClick = navigation.onOpenTracking,
            trailing = {
                ProgressRing(
                    progress = (state?.trackingPercent ?: 0) / 100f,
                    size = 40.dp,
                    strokeWidth = 3.dp,
                    label = (state?.trackingPercent ?: 0).toString(),
                    labelStyle = AzkryTextStyles.Label,
                )
            },
        )
        SectionRowCard(
            title = stringResource(R.string.home_mushaf),
            icon = Icons.AutoMirrored.Outlined.MenuBook,
            onClick = navigation.onOpenMushaf,
            trailing = {
                val bookmarkCount = state?.quranBookmarkCount ?: 0
                if (bookmarkCount > 0) {
                    Text(
                        text = bookmarkCount.toString(),
                        style = AzkryTextStyles.Callout,
                        color = AzkryTheme.colors.TextSecondary,
                    )
                }
            },
        )

        // الأذكار والأدعية with the time-appropriate set nested inside,
        // like the design (أذكار الصباح at midday, المساء in the evening).
        NestedCard(
            mainTitle = stringResource(R.string.home_adhkar_duas),
            mainIcon = Icons.Outlined.AutoAwesome,
            onMainClick = navigation.onOpenAdhkar,
            subTitle = state?.suggestedCategory?.title,
            subIcon = state?.suggestedCategory?.let { categoryIcon(it.iconKey) },
            subIconTint = AzkryTheme.colors.AccentBlue,
            onSubClick = {
                state?.suggestedCategory?.let { navigation.onOpenCategoryReader(it.id) }
            },
        )

        SectionRowCard(
            title = stringResource(R.string.home_comprehensive_duas),
            subtitle = if (state?.isFriday == true) {
                stringResource(R.string.home_friday)
            } else {
                stringResource(R.string.home_comprehensive_duas_subtitle)
            },
            icon = Icons.Outlined.Description,
            onClick = navigation.onOpenAdhkar,
        )

        // صفحات gains a الجمعة shortcut on Fridays, like the design.
        NestedCard(
            mainTitle = stringResource(R.string.home_pages),
            mainIcon = Icons.Outlined.Dashboard,
            onMainClick = navigation.onOpenPages,
            subTitle = if (state?.isFriday == true) stringResource(R.string.home_friday) else null,
            subIcon = if (state?.isFriday == true) Icons.Outlined.CalendarMonth else null,
            subIconTint = AzkryTheme.colors.AccentGreen,
            onSubClick = navigation.onOpenFriday,
        )

        SectionRowCard(
            title = stringResource(R.string.home_exclusive),
            icon = Icons.Outlined.WorkspacePremium,
            onClick = navigation.onOpenExclusive,
        )
        SectionRowCard(
            title = stringResource(R.string.home_all_sections),
            icon = Icons.Outlined.GridView,
            onClick = navigation.onOpenAdhkar,
        )
    }
}

/** A card with a main row and an optional highlighted nested shortcut row. */
@Composable
private fun NestedCard(
    mainTitle: String,
    mainIcon: ImageVector,
    onMainClick: () -> Unit,
    subTitle: String?,
    subIcon: ImageVector?,
    subIconTint: Color,
    onSubClick: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Column {
            InnerRow(
                title = mainTitle,
                icon = mainIcon,
                iconTint = AzkryTheme.colors.TextPrimary,
                iconContainer = AzkryTheme.colors.Slate900,
                titleStyle = AzkryTextStyles.Headline,
                onClick = onMainClick,
            )
            if (subTitle != null && subIcon != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = AzkrySpacing.Md),
                    color = AzkryTheme.colors.Divider,
                )
                InnerRow(
                    title = subTitle,
                    icon = subIcon,
                    iconTint = AzkryTheme.colors.Neutral0,
                    iconContainer = subIconTint,
                    titleStyle = AzkryTextStyles.Body,
                    titleColor = AzkryTheme.colors.TextSecondary,
                    onClick = onSubClick,
                )
            }
        }
    }
}

@Composable
private fun InnerRow(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    iconContainer: Color,
    titleStyle: TextStyle,
    onClick: () -> Unit,
    titleColor: Color = AzkryTheme.colors.TextPrimary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            // One row, one TalkBack stop: the title announces it, so the icon
            // and chevron stay decorative with no description.
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Md),
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = RoundedCornerShape(AzkryRadius.Md),
            color = iconContainer,
            contentColor = iconTint,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            }
        }
        Text(
            text = title,
            style = titleStyle,
            color = titleColor,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = AzkryIcons.Forward,
            contentDescription = null,
            tint = AzkryTheme.colors.TextTertiary,
        )
    }
}

@Composable
private fun LocationFooter(
    state: HomeUiState,
    onOpenSettings: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(horizontal = AzkrySpacing.Md),
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AzkryRadius.Lg),
            color = AzkryTheme.colors.SurfaceCard,
            contentColor = AzkryTheme.colors.TextSecondary,
        ) {
            Row(
                modifier = Modifier.padding(AzkrySpacing.Md),
                horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(text = state.cityName, style = AzkryTextStyles.Callout)
            }
        }

        Surface(
            onClick = onOpenSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(AzkryRadius.Lg),
            color = AzkryTheme.colors.SurfaceCard,
            contentColor = AzkryTheme.colors.TextPrimary,
        ) {
            Row(
                modifier = Modifier.padding(AzkrySpacing.Md),
                horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Outlined.Settings,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text(
                    text = stringResource(R.string.settings_title),
                    style = AzkryTextStyles.Callout,
                )
            }
        }
    }
}

@AzkryPreview
@Composable
private fun HomeContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        HomeContent(
            state = HomeUiState(
                verse = "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
                hijriDay = 4,
                hijriDayMonth = "صفر",
                cityName = "مكة المكرمة",
                headerPhase = HeaderPhase.Day,
                previousPrayer = Prayer.Sunrise,
                previousTime = "06:07",
                upcomingPrayer = Prayer.Dhuhr,
                upcomingTime = "01:45",
                countdownPhrase = "ساعة و15 دقيقة",
                countdownClock = "1:14:42",
                countdownPrayer = Prayer.Dhuhr,
                trackingPercent = 10,
                isFriday = false,
                suggestedCategory = Samples.morningCategory,
                quranBookmarkCount = 1,
            ),
            selectedTab = HomeTab.Misc,
            onTabSelected = {},
            navigation = HomeNavigation(
                onOpenTracking = {},
                onOpenAdhkar = {},
                onOpenMushaf = {},
                onOpenPages = {},
                onOpenFriday = {},
                onOpenSettings = {},
                onOpenSearch = {},
                onOpenExclusive = {},
                onOpenCategoryReader = {},
                onOpenPrayerDay = {},
            ),
        )
    }
}
