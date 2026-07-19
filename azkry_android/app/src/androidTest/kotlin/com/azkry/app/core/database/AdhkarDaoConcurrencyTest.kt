package com.azkry.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.azkry.app.core.models.Dhikr
import com.azkry.app.core.models.DhikrCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AdhkarDaoConcurrencyTest {
    private lateinit var database: AzkryDatabase
    private lateinit var dao: AdhkarDao

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AzkryDatabase::class.java).build()
        dao = database.adhkarDao()
        val categoryId = dao.insertCategory(
            DhikrCategory(key = "test", title = "اختبار", iconKey = "test", sortOrder = 0),
        )
        dao.insertAdhkar(
            listOf(
                Dhikr(
                    id = DHIKR_ID,
                    categoryId = categoryId,
                    text = "سبحان الله",
                    repeatCount = REPEAT_COUNT,
                    source = null,
                    sortOrder = 0,
                    stableKey = "test/item_1",
                ),
            ),
        )
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun concurrentIncrements_areAtomicAndCapped() = runBlocking {
        coroutineScope {
            repeat(250) {
                launch(Dispatchers.Default) {
                    dao.incrementDailyCount(DHIKR_ID, DATE)
                }
            }
        }

        assertEquals(REPEAT_COUNT, dao.dailyCount(DHIKR_ID, DATE))
    }

    private companion object {
        const val DHIKR_ID = 41L
        const val REPEAT_COUNT = 37
        const val DATE = "2026-07-19"
    }
}
