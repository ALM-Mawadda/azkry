package com.azkry.app.features.home.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
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
import com.azkry.app.features.prayertimes.views.PrayerDayTabView
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
    // the compact chrome (actions + wordmark), matching the reference app.
    val collapsed by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AzkrySpacing.Xl),
    ) {
        item(key = "sky-header") {
            SkyHeaderSection(state = state, navigation = navigation)
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

            HomeTab.Prayer -> item { PrayerDayTabView() }
            HomeTab.Qibla -> item { QiblaTabView() }
            HomeTab.Favorites -> item { FavoritesTabView() }
            HomeTab.Counter -> item { CounterTabView() }
        }

        item {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }
}

/** The expanded sky header: actions, rotating verse, and the prayer strip. */
@Composable
private fun SkyHeaderSection(
    state: HomeUiState?,
    navigation: HomeNavigation,
) {
    val phase = state?.headerPhase ?: HeaderPhase.Night
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .sky(phase = phase, pageColor = AzkryTheme.colors.Slate900)
            .statusBarsPadding()
            .padding(horizontal = AzkrySpacing.Md)
            .padding(top = AzkrySpacing.Sm, bottom = AzkrySpacing.Md),
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Lg),
    ) {
        HeaderActionsRow(navigation = navigation, showLogo = false)

        Text(
            text = state?.verse.orEmpty(),
            style = AzkryTextStyles.Title2,
            color = SkyText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AzkrySpacing.Md),
        )

        if (state != null) {
            PrayerStrip(state)
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
                    HeaderActionsRow(navigation = navigation, showLogo = true)
                }
            }
            HomeTabsRow(selectedTab = selectedTab, onTabSelected = onTabSelected)
        }
    }
}

@Composable
private fun HeaderActionsRow(
    navigation: HomeNavigation,
    showLogo: Boolean,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        ) {
            CircleIconButton(
                icon = Icons.Outlined.Settings,
                contentDescription = stringResource(R.string.action_settings),
                onClick = navigation.onOpenSettings,
            )
            CircleIconButton(
                icon = Icons.Outlined.Search,
                contentDescription = stringResource(R.string.action_search),
                onClick = navigation.onOpenSearch,
            )
            CircleIconButton(
                icon = Icons.Outlined.Bookmark,
                contentDescription = stringResource(R.string.action_bookmarks),
                onClick = navigation.onOpenMushaf,
            )
        }

        if (showLogo) {
            Text(
                text = stringResource(R.string.app_name),
                style = AzkryTextStyles.Title2.copy(fontFamily = AzkryFonts.Amiri),
                color = AzkryTheme.colors.TextPrimary,
                modifier = Modifier.align(Alignment.Center),
            )
        }

        CircleIconButton(
            icon = Icons.Outlined.PlayArrow,
            contentDescription = stringResource(R.string.action_play),
            onClick = navigation.onOpenMushaf,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun PrayerStrip(state: HomeUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PrayerStripTime(
            label = stringResource(state.previousPrayer.labelRes()),
            time = state.previousTime,
        )

        // Two-line hijri chip (day above month), matching the design.
        Surface(
            shape = RoundedCornerShape(AzkryRadius.Md),
            color = Color(0x33101828),
            contentColor = SkyText,
            border = BorderStroke(1.dp, Color(0x2EFFFFFF)),
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = AzkrySpacing.S20,
                    vertical = AzkrySpacing.Xs,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = state.hijriDay.toString(),
                    style = AzkryTextStyles.Headline,
                )
                Text(
                    text = state.hijriDayMonth,
                    style = AzkryTextStyles.Caption,
                    color = SkyTextSecondary,
                )
            }
        }

        PrayerStripTime(
            label = stringResource(state.upcomingPrayer.labelRes()),
            time = state.upcomingTime,
        )
    }
}

@Composable
private fun PrayerStripTime(label: String, time: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, style = AzkryTextStyles.Headline, color = SkyText)
        Text(text = time, style = AzkryTextStyles.Headline, color = SkyTextSecondary)
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
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.S12),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        HomeTab.entries.forEach { tab ->
            Text(
                text = stringResource(tab.titleRes),
                style = AzkryTextStyles.Headline,
                color = if (tab == selectedTab) {
                    AzkryTheme.colors.TextPrimary
                } else {
                    AzkryTheme.colors.TextSecondary
                },
                modifier = Modifier.clickable { onTabSelected(tab) },
            )
        }
    }
}

@Composable
private fun AdhanCountdownRow(state: HomeUiState) {
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
                R.string.countdown_line,
                stringResource(state.countdownPrayer.labelRes()),
                state.countdown,
            ),
            style = AzkryTextStyles.Headline,
            color = AzkryTheme.colors.AccentYellow,
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
        // like the reference (أذكار الصباح at midday, المساء in the evening).
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

        // صفحات gains a الجمعة shortcut on Fridays, like the reference.
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
            imageVector = Icons.Outlined.KeyboardArrowLeft,
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
                countdown = "1:14:42",
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
            ),
        )
    }
}
