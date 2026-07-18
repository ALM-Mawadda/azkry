package com.azkry.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.azkry.app.core.database.seed.AdhkarSeed
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAzkryDatabase(
        @ApplicationContext context: Context,
        adhkarDaoProvider: Provider<AdhkarDao>,
    ): AzkryDatabase {
        // Seeding is revision-driven and runs on every open, off the main
        // thread: a no-op when the stored revision is current, a full content
        // replacement after a bundle upgrade. The callback resolves the DAO
        // lazily through the Provider so it operates on the database instance
        // being built.
        val seedScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        return Room.databaseBuilder(context, AzkryDatabase::class.java, "azkry.db")
            .addMigrations(AzkryDatabase.MIGRATION_1_2, AzkryDatabase.MIGRATION_2_3)
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onOpen(db: SupportSQLiteDatabase) {
                        super.onOpen(db)
                        seedScope.launch {
                            AdhkarSeed.seed(context, adhkarDaoProvider.get())
                        }
                    }
                },
            )
            .build()
    }

    @Provides
    fun provideAdhkarDao(database: AzkryDatabase): AdhkarDao = database.adhkarDao()

    @Provides
    fun provideTrackingDao(database: AzkryDatabase): TrackingDao = database.trackingDao()
}
