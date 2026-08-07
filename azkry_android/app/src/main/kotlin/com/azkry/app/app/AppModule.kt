package com.azkry.app.app

import com.azkry.app.core.i18n.AndroidStringProvider
import com.azkry.app.core.i18n.StringProvider
import com.azkry.app.features.adhkar.services.AdhkarService
import com.azkry.app.features.adhkar.services.RoomAdhkarService
import com.azkry.app.features.counter.services.CounterService
import com.azkry.app.features.counter.services.DataStoreCounterService
import com.azkry.app.features.friday.services.FridayService
import com.azkry.app.features.friday.services.RoomFridayService
import com.azkry.app.features.mushaf.services.AssetQuranService
import com.azkry.app.features.mushaf.services.QuranService
import com.azkry.app.features.notifications.services.DataStoreNotificationSettingsService
import com.azkry.app.features.notifications.services.NotificationSettingsService
import com.azkry.app.features.pages.services.PagesService
import com.azkry.app.features.pages.services.StaticPagesService
import com.azkry.app.features.prayertimes.services.CalculatedPrayerTimesService
import com.azkry.app.features.prayertimes.services.DataStorePrayerSettingsService
import com.azkry.app.features.prayertimes.services.FusedLocationService
import com.azkry.app.features.prayertimes.services.LocationService
import com.azkry.app.features.prayertimes.services.PrayerSettingsService
import com.azkry.app.features.prayertimes.services.PrayerTimesService
import com.azkry.app.features.qibla.services.CompassService
import com.azkry.app.features.qibla.services.SensorCompassService
import com.azkry.app.features.settings.services.BackupService
import com.azkry.app.features.settings.services.JsonBackupService
import com.azkry.app.features.tracking.services.RoomWorshipTrackingService
import com.azkry.app.features.tracking.services.WorshipTrackingService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {
    @Binds
    @Singleton
    abstract fun bindAppSettingsService(service: DataStoreAppSettingsService): AppSettingsService

    @Binds
    @Singleton
    abstract fun bindStringProvider(provider: AndroidStringProvider): StringProvider

    @Binds
    @Singleton
    abstract fun bindAdhkarService(service: RoomAdhkarService): AdhkarService

    @Binds
    @Singleton
    abstract fun bindWorshipTrackingService(service: RoomWorshipTrackingService): WorshipTrackingService

    @Binds
    @Singleton
    abstract fun bindPrayerSettingsService(service: DataStorePrayerSettingsService): PrayerSettingsService

    @Binds
    @Singleton
    abstract fun bindPrayerTimesService(service: CalculatedPrayerTimesService): PrayerTimesService

    @Binds
    @Singleton
    abstract fun bindQuranService(service: AssetQuranService): QuranService

    @Binds
    @Singleton
    abstract fun bindCounterService(service: DataStoreCounterService): CounterService

    @Binds
    @Singleton
    abstract fun bindCompassService(service: SensorCompassService): CompassService

    @Binds
    @Singleton
    abstract fun bindLocationService(service: FusedLocationService): LocationService

    @Binds
    @Singleton
    abstract fun bindFridayService(service: RoomFridayService): FridayService

    @Binds
    @Singleton
    abstract fun bindNotificationSettingsService(
        service: DataStoreNotificationSettingsService,
    ): NotificationSettingsService

    @Binds
    @Singleton
    abstract fun bindBackupService(service: JsonBackupService): BackupService

    @Binds
    @Singleton
    abstract fun bindPagesService(service: StaticPagesService): PagesService

    companion object {
        /** A small seam so time-dependent screens stay unit-testable. */
        @Provides
        @Singleton
        fun provideClock(): Clock = Clock.systemDefaultZone()
    }
}
