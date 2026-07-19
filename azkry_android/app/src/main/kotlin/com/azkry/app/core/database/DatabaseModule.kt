package com.azkry.app.core.database

import android.content.Context
import androidx.room.Room
import com.azkry.app.core.database.seed.AdhkarSeed
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAzkryDatabase(
        @ApplicationContext context: Context,
        readiness: DatabaseReadiness,
    ): AzkryDatabase {
        val database = Room.databaseBuilder(context, AzkryDatabase::class.java, "azkry.db")
            .addMigrations(
                AzkryDatabase.MIGRATION_1_2,
                AzkryDatabase.MIGRATION_2_3,
                AzkryDatabase.MIGRATION_3_4,
            )
            .build()
        readiness.initialize {
            // Use the raw DAO here. Hilt consumers receive the readiness-gated
            // DAO below, which would wait on this initializer and deadlock.
            AdhkarSeed.seed(context, database.adhkarDao())
        }
        return database
    }

    @Provides
    @Singleton
    fun provideAdhkarDao(
        database: AzkryDatabase,
        readiness: DatabaseReadiness,
    ): AdhkarDao = ReadyAdhkarDao(database.adhkarDao(), readiness)

    @Provides
    fun provideTrackingDao(database: AzkryDatabase): TrackingDao = database.trackingDao()
}
