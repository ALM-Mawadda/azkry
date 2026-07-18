package com.azkry.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.DhikrDailyCount
import com.azkry.app.core.models.FavoriteDhikr
import com.azkry.app.core.models.PrayerLog
import com.azkry.app.core.models.SeedInfo
import com.azkry.app.core.models.WorshipLog

@Database(
    entities = [
        DhikrCategory::class,
        Dhikr::class,
        DhikrDailyCount::class,
        PrayerLog::class,
        WorshipLog::class,
        FavoriteDhikr::class,
        SeedInfo::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class AzkryDatabase : RoomDatabase() {
    abstract fun adhkarDao(): AdhkarDao
    abstract fun trackingDao(): TrackingDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `favorite_adhkar` (
                        `dhikrId` INTEGER NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`dhikrId`),
                        FOREIGN KEY(`dhikrId`) REFERENCES `adhkar`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `adhkar` ADD COLUMN `title` TEXT")
                db.execSQL("ALTER TABLE `adhkar` ADD COLUMN `virtue` TEXT")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `seed_info` (
                        `id` INTEGER NOT NULL,
                        `revision` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
