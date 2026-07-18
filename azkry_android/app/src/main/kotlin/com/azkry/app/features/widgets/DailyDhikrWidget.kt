package com.azkry.app.features.widgets

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.azkry.app.MainActivity
import com.azkry.app.core.models.DhikrCategoryKeys
import java.time.LocalDate
import kotlinx.coroutines.flow.first

/** Home-screen card rotating a dhikr of the day from the morning set. */
class DailyDhikrWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val adhkarService = WidgetEntryPoint.resolve(context).adhkarService()

        val morning = adhkarService.observeCategories().first()
            .firstOrNull { it.key == DhikrCategoryKeys.MORNING }
        val adhkar = morning?.let { adhkarService.observeAdhkar(it.id).first() }.orEmpty()
        val short = adhkar.filter { it.text.length <= 120 }.ifEmpty { adhkar }
        val dhikr = short.getOrNull(LocalDate.now().dayOfYear % short.size.coerceAtLeast(1))

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(WidgetColors.background)
                        .cornerRadius(20.dp)
                        .padding(14.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = dhikr?.text ?: "سبحان الله وبحمده",
                        style = TextStyle(
                            color = ColorProvider(WidgetColors.textPrimary, WidgetColors.textPrimary),
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                        ),
                        maxLines = 5,
                    )
                    dhikr?.source?.let { source ->
                        Text(
                            text = source,
                            style = TextStyle(
                                color = ColorProvider(WidgetColors.textSecondary, WidgetColors.textSecondary),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                            ),
                            modifier = GlanceModifier.padding(top = 6.dp),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

class DailyDhikrWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyDhikrWidget()
}
