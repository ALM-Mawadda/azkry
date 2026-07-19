package com.azkry.app.features.widgets

import android.content.Context
import android.util.Log
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException

/**
 * Refreshes every placed widget. Called from the reminder chain and app
 * opens so widget content follows the same freshness as notifications.
 */
@Singleton
class AzkryWidgetUpdater internal constructor(
    private val updateNextPrayer: suspend () -> Unit,
    private val updateDailyDhikr: suspend () -> Unit,
    private val logFailure: (String, Throwable) -> Unit,
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : this(
        updateNextPrayer = { NextPrayerWidget().updateAll(context) },
        updateDailyDhikr = { DailyDhikrWidget().updateAll(context) },
        logFailure = { widgetName, error ->
            Log.e(TAG, "Failed to update $widgetName", error)
        },
    )

    suspend fun updateAll() {
        updateWidget(NEXT_PRAYER_WIDGET, updateNextPrayer)
        updateWidget(DAILY_DHIKR_WIDGET, updateDailyDhikr)
    }

    private suspend fun updateWidget(
        widgetName: String,
        update: suspend () -> Unit,
    ) {
        try {
            update()
        } catch (error: CancellationException) {
            throw error
        } catch (error: Throwable) {
            logFailure(widgetName, error)
        }
    }

    private companion object {
        const val TAG = "AzkryWidgetUpdater"
        const val NEXT_PRAYER_WIDGET = "NextPrayerWidget"
        const val DAILY_DHIKR_WIDGET = "DailyDhikrWidget"
    }
}
