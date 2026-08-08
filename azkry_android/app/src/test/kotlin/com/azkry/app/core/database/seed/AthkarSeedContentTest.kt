package com.azkry.app.core.database.seed

import java.io.File
import java.text.Normalizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Validates the bundled adhkar library without an Android runtime: the JSON is
 * read straight from the assets directory on disk.
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
    fun `category keys are unique and ordered as the app presents them`() {
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
    fun `a title is a label, never a repeat of the dhikr body`() {
        // The imported data used its title field as a list preview, so about
        // half the items carried the dua text twice — once lightly vocalised
        // as the title, once fully vocalised as the body. The card renders
        // both, so any overlap shows up as duplicated text on screen.
        val offenders = content.categories.flatMap { category ->
            category.items.map { category.key to it }
        }.filter { (_, item) ->
            val title = arabicOnly(item.title)
            title.length >= 12 && title in arabicOnly(item.text)
        }.map { (key, item) -> "$key: ${item.title?.take(40)}" }

        assertEquals(emptyList<String>(), offenders)
    }

    @Test
    fun `a body never opens by repeating its own title`() {
        val offenders = content.categories.flatMap { it.items }
            .filter { item ->
                val first = item.text.lineSequence().firstOrNull { it.isNotBlank() }
                arabicOnly(item.title).isNotEmpty() &&
                    arabicOnly(first) == arabicOnly(item.title)
            }
            .map { it.title?.take(40).orEmpty() }

        assertEquals(emptyList<String>(), offenders)
    }

    /**
     * Fold to bare Arabic letters so the same words match however they were
     * typed. The imported corpus mixes vocalisation levels, Unicode
     * presentation forms (U+FE8E and friends) and stray tatweel inside words
     * — "الكُـ__فرِ" and "الْكُفْرِ" are the same word and must compare equal.
     */
    private fun arabicOnly(value: String?): String =
        Normalizer.normalize(value ?: "", Normalizer.Form.NFKC)
            .replace(Regex("\\p{Mn}"), "")
            .replace("ـ", "")
            .replace(Regex("[أإآٱ]"), "ا")
            .replace('ة', 'ه').replace('ى', 'ي').replace('ؤ', 'و').replace('ئ', 'ي')
            .filter { it in 'ء'..'ي' }

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
