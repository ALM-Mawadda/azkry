package com.azkry.app.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AzkryDatabase::class.java,
    )

    @Test
    fun migrate1To2_preservesDataAndAddsFavorites() {
        helper.createDatabase(TEST_DB, 1).apply {
            execSQL(
                "INSERT INTO dhikr_categories (id, `key`, title, iconKey, sortOrder) " +
                    "VALUES (1, 'morning', 'أذكار الصباح', 'sun', 0)",
            )
            execSQL(
                "INSERT INTO adhkar (id, categoryId, text, repeatCount, source, sortOrder) " +
                    "VALUES (1, 1, 'سبحان الله', 33, NULL, 0)",
            )
            execSQL(
                "INSERT INTO prayer_logs (date, prayer, completed) " +
                    "VALUES ('2026-07-18', 'Fajr', 1)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 2, true, AzkryDatabase.MIGRATION_1_2)

        db.query("SELECT COUNT(*) FROM adhkar").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM prayer_logs").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
        db.execSQL("INSERT INTO favorite_adhkar (dhikrId, createdAt) VALUES (1, 123)")
        db.query("SELECT COUNT(*) FROM favorite_adhkar").use { cursor ->
            cursor.moveToFirst()
            assertEquals(1, cursor.getInt(0))
        }
    }

    @Test
    fun migrate2To3_addsTitleVirtueAndSeedInfo() {
        helper.createDatabase(TEST_DB, 2).apply {
            execSQL(
                "INSERT INTO dhikr_categories (id, `key`, title, iconKey, sortOrder) " +
                    "VALUES (1, 'morning', 'أذكار الصباح', 'sun', 0)",
            )
            execSQL(
                "INSERT INTO adhkar (id, categoryId, text, repeatCount, source, sortOrder) " +
                    "VALUES (1, 1, 'سبحان الله', 33, NULL, 0)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 3, true, AzkryDatabase.MIGRATION_2_3)

        db.query("SELECT title, virtue FROM adhkar WHERE id = 1").use { cursor ->
            cursor.moveToFirst()
            assertEquals(true, cursor.isNull(0))
            assertEquals(true, cursor.isNull(1))
        }
        db.execSQL("INSERT INTO seed_info (id, revision) VALUES (0, 2)")
        db.query("SELECT revision FROM seed_info WHERE id = 0").use { cursor ->
            cursor.moveToFirst()
            assertEquals(2, cursor.getInt(0))
        }
    }

    @Test
    fun migrate3To4_addsStableDhikrIdentity() {
        helper.createDatabase(TEST_DB, 3).apply {
            execSQL(
                "INSERT INTO dhikr_categories (id, `key`, title, iconKey, sortOrder) " +
                    "VALUES (7, 'morning', 'أذكار الصباح', 'sun', 0)",
            )
            execSQL(
                "INSERT INTO adhkar " +
                    "(id, categoryId, text, repeatCount, source, sortOrder, title, virtue) " +
                    "VALUES (42, 7, 'سبحان الله', 33, NULL, 2, NULL, NULL)",
            )
            close()
        }

        val db = helper.runMigrationsAndValidate(TEST_DB, 4, true, AzkryDatabase.MIGRATION_3_4)

        db.query("SELECT stableKey FROM adhkar WHERE id = 42").use { cursor ->
            cursor.moveToFirst()
            assertEquals("morning/item_3", cursor.getString(0))
        }
    }

    private companion object {
        const val TEST_DB = "migration-test.db"
    }
}
