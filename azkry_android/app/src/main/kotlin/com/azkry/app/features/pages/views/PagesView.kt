package com.azkry.app.features.pages.views

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.AlarmOff
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventRepeat
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.NightsStay
import androidx.compose.material.icons.outlined.Spa
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.azkry.app.R
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.components.SectionRowCard
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.features.pages.models.AzkryPage
import com.azkry.app.features.pages.models.PageKey
import com.azkry.app.features.pages.models.PageSection
import com.azkry.app.features.pages.viewmodels.PagesViewModel

/** The صفحات hub: topic page list plus the inline سيد الاستغفار card. */
@Composable
fun PagesView(
    onBack: () -> Unit,
    onOpenPage: (PageKey) -> Unit,
    onOpenFriday: () -> Unit,
    viewModel: PagesViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    PagesContent(
        pages = viewModel.pages,
        sayyidIstighfar = viewModel.sayyidIstighfar,
        onBack = onBack,
        onOpenPage = { key ->
            if (key == PageKey.Friday) onOpenFriday() else onOpenPage(key)
        },
        onShare = { section ->
            val text = listOfNotNull(section.heading, section.body, section.source)
                .joinToString(separator = "\n\n")
            val intent = Intent(Intent.ACTION_SEND)
                .setType("text/plain")
                .putExtra(Intent.EXTRA_TEXT, text)
            context.startActivity(Intent.createChooser(intent, null))
        },
    )
}

@Composable
fun PagesContent(
    pages: List<AzkryPage>,
    sayyidIstighfar: PageSection,
    onBack: () -> Unit,
    onOpenPage: (PageKey) -> Unit,
    onShare: (PageSection) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.home_pages),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Sm)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            pages.forEach { page ->
                SectionRowCard(
                    title = page.title,
                    icon = pageIcon(page.key),
                    onClick = { onOpenPage(page.key) },
                )
            }

            PageSectionCard(section = sayyidIstighfar, onShare = onShare)
        }
    }
}

/** Icons chosen to mirror the reference screenshots row by row. */
fun pageIcon(key: PageKey): ImageVector = when (key) {
    PageKey.AsmaulHusna -> Icons.Outlined.AutoStories
    PageKey.Friday -> Icons.Outlined.CalendarMonth
    PageKey.Rawatib -> Icons.Outlined.NightsStay
    PageKey.Duha -> Icons.Outlined.WbSunny
    PageKey.DuaEtiquette -> Icons.AutoMirrored.Outlined.MenuBook
    PageKey.DhikrAndDua -> Icons.Outlined.AutoAwesome
    PageKey.ForbiddenTimes -> Icons.Outlined.AlarmOff
    PageKey.RamadanQada -> Icons.Outlined.EventRepeat
    PageKey.DeceasedDuas -> Icons.Outlined.Spa
    PageKey.AyahTafsir -> Icons.Outlined.ManageSearch
}

@AzkryPreview
@Composable
private fun PagesContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        PagesContent(
            pages = listOf(
                AzkryPage(key = PageKey.AsmaulHusna, title = "أسماء الله الحسنى"),
                AzkryPage(key = PageKey.Friday, title = "الجمعة"),
                AzkryPage(key = PageKey.Rawatib, title = "السنن الرواتب"),
            ),
            sayyidIstighfar = PageSection(
                heading = "سيد الاستغفار",
                body = "اللَّهُمَّ أَنْتَ رَبِّي لَا إِلَهَ إِلَّا أَنْتَ…",
                source = "رواه البخاري",
                isDhikr = true,
            ),
            onBack = {},
            onOpenPage = {},
            onShare = {},
        )
    }
}
