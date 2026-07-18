package com.azkry.app.features.notifications.models

import com.azkry.app.core.models.Prayer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderPlannerPreAdhanTest {
    private val times = mapOf(
        Prayer.Fajr to LocalTime.of(4, 15),
        Prayer.Dhuhr to LocalTime.of(12, 30),
        Prayer.Asr to LocalTime.of(16, 5),
        Prayer.Maghrib to LocalTime.of(19, 10),
        Prayer.Isha to LocalTime.of(20, 40),
    )

    private fun timesFor(@Suppress("UNUSED_PARAMETER") date: LocalDate) = times

    @Test
    fun `pre-adhan fires fifteen minutes before the adhan`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 12, 0),
            enabled = setOf(ReminderKind.DhuhrAdhan),
            preAdhanEnabled = true,
            timesForDate = ::timesFor,
        )

        assertEquals(ReminderKind.DhuhrAdhan, next?.kind)
        assertTrue(next!!.isPreReminder)
        assertEquals(LocalDateTime.of(2026, 7, 18, 12, 15), next.at)
    }

    @Test
    fun `after the pre-adhan the chain plans the adhan itself`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 12, 15),
            enabled = setOf(ReminderKind.DhuhrAdhan),
            preAdhanEnabled = true,
            timesForDate = ::timesFor,
        )

        assertEquals(ReminderKind.DhuhrAdhan, next?.kind)
        assertFalse(next!!.isPreReminder)
        assertEquals(LocalDateTime.of(2026, 7, 18, 12, 30), next.at)
    }

    @Test
    fun `pre-adhan is not planned when disabled`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 12, 0),
            enabled = setOf(ReminderKind.DhuhrAdhan),
            preAdhanEnabled = false,
            timesForDate = ::timesFor,
        )

        assertFalse(next!!.isPreReminder)
        assertEquals(LocalDateTime.of(2026, 7, 18, 12, 30), next.at)
    }

    @Test
    fun `pre-adhan does not apply to adhkar reminders`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 4, 20),
            enabled = setOf(ReminderKind.MorningAdhkar),
            preAdhanEnabled = true,
            timesForDate = ::timesFor,
        )

        assertFalse(next!!.isPreReminder)
        assertEquals(LocalDateTime.of(2026, 7, 18, 4, 45), next.at)
    }
}
