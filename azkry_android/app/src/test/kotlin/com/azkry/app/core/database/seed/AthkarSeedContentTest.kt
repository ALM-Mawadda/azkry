package com.azkry.app.core.database.seed

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates the bundled adhkar library (extracted from the reference iOS
 * Athkar database) without an Android runtime: the JSON is read straight
 * from the assets directory on disk.
 */
class AthkarSeedContentTest {
    private val content by lazy {
        AthkarSeedParser.parse(
            File("src/main/assets/adhkar/athkar_seed.json").readText(),
        )
    }

    @Test
    fun `bundle carries the full imported library`() {
        assertEquals(11, content.categories.size)
        assertEquals(339, content.categories.sumOf { it.items.size })
    }

    @Test
    fun `category keys are unique and ordered like the reference app`() {
        val keys = content.categories.map { it.key }
        assertEquals(keys.distinct(), keys)
        assertEquals(
            listOf(
                "morning", "evening", "prayer", "after_prayer", "sleep", "waking",
                "quran_duas", "prophet_duas", "ruqyah_quran", "ruqyah_sunnah", "misc",
            ),
            keys,
        )
    }

    @Test
    fun `every item has text and a sane repeat count`() {
        content.categories.flatMap { it.items }.forEach { item ->
            assertTrue(item.text.isNotBlank())
            assertTrue("count ${item.count}", item.count in 1..100)
        }
    }

    @Test
    fun `generated backup identities are unique and stable`() {
        val keys = content.categories.flatMap { category ->
            category.items.mapIndexed { index, item ->
                stableDhikrKey(category.key, item, index)
            }
        }

        assertEquals(keys.distinct(), keys)
        assertEquals("morning/item_1", keys.first())
    }

    @Test
    fun `titled categories keep their titles`() {
        val misc = content.categories.first { it.key == "misc" }
        assertEquals(90, misc.items.size)
        assertTrue(misc.items.all { !it.title.isNullOrBlank() })
    }

    @Test
    fun `no html leaks into the extracted texts`() {
        content.categories.flatMap { it.items }.forEach { item ->
            listOfNotNull(item.text, item.virtue, item.source, item.title).forEach { field ->
                assertTrue("html in: ${field.take(60)}", '<' !in field && '>' !in field)
                assertTrue("entity in: ${field.take(60)}", "&nbsp" !in field && "&amp" !in field)
            }
        }
    }
}
