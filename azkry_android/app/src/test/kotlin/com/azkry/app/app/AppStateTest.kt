package com.azkry.app.app

import org.junit.Assert.assertEquals
import org.junit.Test

class AppStateTest {
    @Test
    fun `fromCode resolves known codes`() {
        assertEquals(AppLanguage.Arabic, AppLanguage.fromCode("ar"))
        assertEquals(AppLanguage.Arabic, AppLanguage.fromCode("AR"))
        assertEquals(AppLanguage.Arabic, AppLanguage.fromCode("arabic"))
    }

    @Test
    fun `fromCode falls back to default`() {
        assertEquals(AppLanguage.Default, AppLanguage.fromCode(null))
        assertEquals(AppLanguage.Default, AppLanguage.fromCode(""))
        assertEquals(AppLanguage.Default, AppLanguage.fromCode("  "))
        assertEquals(AppLanguage.Default, AppLanguage.fromCode("system"))
        assertEquals(AppLanguage.Default, AppLanguage.fromCode("default"))
        assertEquals(AppLanguage.Default, AppLanguage.fromCode("zz"))
    }
}
