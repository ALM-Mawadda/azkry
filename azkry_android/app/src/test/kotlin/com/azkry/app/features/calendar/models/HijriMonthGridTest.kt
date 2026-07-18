package com.azkry.app.features.calendar.models

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HijriMonthGridTest {
    private val today = LocalDate.of(2026, 7, 18) // 4 Safar 1448

    @Test
    fun `current month contains today at the right hijri day`() {
        val month = HijriMonthGrid.monthAtOffset(0, today)

        val todayCell = month.days.single { it.isToday }
        assertEquals(4, todayCell.hijriDay)
        assertEquals(today, todayCell.gregorianDate)
    }

    @Test
    fun `month starts on the first hijri day and is a lunar length`() {
        val month = HijriMonthGrid.monthAtOffset(0, today)

        assertEquals(1, month.days.first().hijriDay)
        assertTrue(month.days.size == 29 || month.days.size == 30)
        // 1 Safar 1448 = 15 July 2026.
        assertEquals(LocalDate.of(2026, 7, 15), month.days.first().gregorianDate)
    }

    @Test
    fun `leading blanks align day one to a saturday-first grid`() {
        val month = HijriMonthGrid.monthAtOffset(0, today)

        // 15 July 2026 is a Wednesday: Sat(0) Sun(1) Mon(2) Tue(3) Wed(4).
        assertEquals(4, month.leadingBlanks)
    }

    @Test
    fun `offset months chain consecutively`() {
        val current = HijriMonthGrid.monthAtOffset(0, today)
        val next = HijriMonthGrid.monthAtOffset(1, today)

        val expectedNextStart = current.days.first().gregorianDate.plusDays(current.days.size.toLong())
        assertEquals(expectedNextStart, next.days.first().gregorianDate)
        assertTrue(next.days.none { it.isToday })
    }
}
