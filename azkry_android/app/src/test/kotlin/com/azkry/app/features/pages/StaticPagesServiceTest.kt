package com.azkry.app.features.pages

import com.azkry.app.features.pages.models.PageKey
import com.azkry.app.features.pages.services.StaticPagesService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class StaticPagesServiceTest {
    private val service = StaticPagesService()

    @Test
    fun `hub lists every page key once, in the documented order`() {
        val keys = service.pages().map { it.key }
        assertEquals(PageKey.entries.toList(), keys)
    }

    @Test
    fun `every page except Friday carries content`() {
        service.pages().filter { it.key != PageKey.Friday }.forEach { page ->
            assertTrue(
                "page ${page.key} has no content",
                page.sections.isNotEmpty() || page.names.isNotEmpty(),
            )
        }
    }

    @Test
    fun `asmaul husna grid holds ninety-nine distinct names`() {
        val names = service.page(PageKey.AsmaulHusna)!!.names
        assertEquals(99, names.size)
        assertEquals(99, names.distinct().size)
        assertTrue(names.none { it.isBlank() })
    }

    @Test
    fun `sections never have blank bodies and dhikr sections cite a source`() {
        service.pages().flatMap { it.sections }.forEach { section ->
            assertTrue(section.body.isNotBlank())
        }
    }

    @Test
    fun `sayyid istighfar is a sourced dhikr`() {
        val section = service.sayyidIstighfar()
        assertTrue(section.isDhikr)
        assertTrue(section.body.startsWith("اللَّهُمَّ أَنْتَ رَبِّي"))
        assertEquals("رواه البخاري", section.source)
    }

    @Test
    fun `page lookup resolves every key`() {
        PageKey.entries.forEach { key ->
            assertEquals(key, service.page(key)?.key)
        }
    }
}
