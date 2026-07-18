package com.azkry.app.features.widgets

import android.content.Context
import androidx.compose.ui.graphics.Color
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
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.azkry.app.MainActivity
import com.azkry.app.core.models.labelRes
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.flow.first

/** Home-screen card showing the next prayer name, time, and city. */
class NextPrayerWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val entryPoint = WidgetEntryPoint.resolve(context)
        val prayerTimesService = entryPoint.prayerTimesService()

        val now = LocalDateTime.now()
        val dayTimes = prayerTimesService.observeTimes(LocalDate.now()).first()
        val next = prayerTimesService.nextPrayer(now, dayTimes)
        val timeFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)

        val prayerName = context.getString(next.prayer.labelRes())
        val time = next.at.toLocalTime().format(timeFormatter)
        val city = dayTimes.cityName

        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(WidgetColors.background)
                        .cornerRadius(20.dp)
                        .padding(12.dp)
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = prayerName,
                        style = TextStyle(
                            color = ColorProvider(WidgetColors.textPrimary, WidgetColors.textPrimary),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = time,
                        style = TextStyle(
                            color = ColorProvider(WidgetColors.accent, WidgetColors.accent),
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                        ),
                    )
                    Text(
                        text = city,
                        style = TextStyle(
                            color = ColorProvider(WidgetColors.textSecondary, WidgetColors.textSecondary),
                            fontSize = 12.sp,
                        ),
                    )
                }
            }
        }
    }
}

class NextPrayerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NextPrayerWidget()
}

/** Widgets keep the app's night identity regardless of the system theme. */
internal object WidgetColors {
    val background = Color(0xFF141B3D)
    val textPrimary = Color(0xFFF2F5F8)
    val textSecondary = Color(0xFF9FACBA)
    val accent = Color(0xFFF5C84C)
}
