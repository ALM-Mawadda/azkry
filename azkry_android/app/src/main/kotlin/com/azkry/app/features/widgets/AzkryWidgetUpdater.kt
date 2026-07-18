package com.azkry.app.features.widgets

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Refreshes every placed widget. Called from the reminder chain and app
 * opens so widget content follows the same freshness as notifications.
 */
@Singleton
class AzkryWidgetUpdater @Inject constructor(
    @param:ApplicationContext private val context: Context,
) {
    suspend fun updateAll() {
        runCatching { NextPrayerWidget().updateAll(context) }
        runCatching { DailyDhikrWidget().updateAll(context) }
    }
}
