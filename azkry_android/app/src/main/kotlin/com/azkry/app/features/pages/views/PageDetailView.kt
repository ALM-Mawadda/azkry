package com.azkry.app.features.pages.views

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.mushaf.models.toArabicIndicDigits
import com.azkry.app.features.pages.models.AzkryPage
import com.azkry.app.features.pages.models.PageKey
import com.azkry.app.features.pages.models.PageSection
import com.azkry.app.features.pages.viewmodels.PagesViewModel

private const val NAMES_PER_ROW = 3

/** A single topic page: section cards, plus the names grid for الأسماء الحسنى. */
@Composable
fun PageDetailView(
    pageKey: PageKey,
    onBack: () -> Unit,
    viewModel: PagesViewModel = hiltViewModel(),
) {
    val page = viewModel.page(pageKey) ?: return
    val context = LocalContext.current
    PageDetailContent(
        page = page,
        onBack = onBack,
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
fun PageDetailContent(
    page: AzkryPage,
    onBack: () -> Unit,
    onShare: (PageSection) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(title = page.title, onBack = onBack)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AzkrySpacing.Md,
                end = AzkrySpacing.Md,
                top = AzkrySpacing.Sm,
                bottom = AzkrySpacing.Xl,
            ),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            items(page.sections.size) { index ->
                PageSectionCard(section = page.sections[index], onShare = onShare)
            }

            if (page.names.isNotEmpty()) {
                val rows = page.names.chunked(NAMES_PER_ROW)
                items(rows.size) { rowIndex ->
                    NameRow(
                        names = rows[rowIndex],
                        firstNumber = rowIndex * NAMES_PER_ROW + 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun NameRow(names: List<String>, firstNumber: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.S12)) {
        names.forEachIndexed { index, name ->
            NameChip(
                name = name,
                number = firstNumber + index,
                modifier = Modifier.weight(1f),
            )
        }
        repeat(NAMES_PER_ROW - names.size) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun NameChip(name: String, number: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(
                modifier = Modifier.padding(
                    horizontal = AzkrySpacing.Xs,
                    vertical = AzkrySpacing.S12,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
            ) {
                Text(
                    text = number.toArabicIndicDigits(),
                    style = AzkryTextStyles.Caption,
                    color = AzkryTheme.colors.TextTertiary,
                )
                Text(
                    text = name,
                    style = AzkryTextStyles.DhikrBody.copy(fontSize = 16.sp, lineHeight = 24.sp),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@AzkryPreview
@Composable
private fun PageDetailContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        PageDetailContent(
            page = AzkryPage(
                key = PageKey.Rawatib,
                title = "السنن الرواتب",
                sections = listOf(
                    PageSection(
                        heading = "فضلها",
                        body = "مَا مِنْ عَبْدٍ مُسْلِمٍ يُصَلِّي لِلَّهِ كُلَّ يَوْمٍ ثِنْتَيْ عَشْرَةَ رَكْعَةً تَطَوُّعًا…",
                        source = "رواه مسلم",
                        isDhikr = true,
                    ),
                    PageSection(
                        heading = "توزيع الركعات",
                        body = "ركعتان قبل الفجر، وأربع قبل الظهر وركعتان بعدها…",
                    ),
                ),
            ),
            onBack = {},
            onShare = {},
        )
    }
}
