package com.azkry.app.features.widgets

import android.content.Context
import com.azkry.app.features.adhkar.services.AdhkarService
import com.azkry.app.features.prayertimes.services.PrayerTimesService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

/** Glance widgets run outside Hilt's graph; this is their bridge into it. */
@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetEntryPoint {
    fun prayerTimesService(): PrayerTimesService
    fun adhkarService(): AdhkarService

    companion object {
        fun resolve(context: Context): WidgetEntryPoint =
            EntryPointAccessors.fromApplication(
                context.applicationContext,
                WidgetEntryPoint::class.java,
            )
    }
}
