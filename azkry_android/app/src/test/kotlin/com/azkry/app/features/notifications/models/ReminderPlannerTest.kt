package com.azkry.app.features.notifications.models

import com.azkry.app.core.models.Prayer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReminderPlannerTest {
    private val times = mapOf(
        Prayer.Fajr to LocalTime.of(4, 15),
        Prayer.Sunrise to LocalTime.of(5, 50),
        Prayer.Dhuhr to LocalTime.of(12, 30),
        Prayer.Asr to LocalTime.of(16, 5),
        Prayer.Maghrib to LocalTime.of(19, 10),
        Prayer.Isha to LocalTime.of(20, 40),
    )

    private fun timesFor(@Suppress("UNUSED_PARAMETER") date: LocalDate) = times

    @Test
    fun `no enabled kinds plans nothing`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 10, 0),
            enabled = emptySet(),
            timesForDate = ::timesFor,
        )
        assertNull(next)
    }

    @Test
    fun `picks the earliest upcoming reminder today`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 10, 0),
            enabled = setOf(ReminderKind.DhuhrAdhan, ReminderKind.IshaAdhan),
            timesForDate = ::timesFor,
        )

        assertEquals(ReminderKind.DhuhrAdhan, next?.kind)
        assertEquals(LocalDateTime.of(2026, 7, 18, 12, 30), next?.at)
    }

    @Test
    fun `after isha the next adhan is tomorrow's fajr`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 23, 0),
            enabled = setOf(ReminderKind.FajrAdhan, ReminderKind.IshaAdhan),
            timesForDate = ::timesFor,
        )

        assertEquals(ReminderKind.FajrAdhan, next?.kind)
        assertEquals(LocalDateTime.of(2026, 7, 19, 4, 15), next?.at)
    }

    @Test
    fun `morning adhkar reminder fires thirty minutes after fajr`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 4, 20),
            enabled = setOf(ReminderKind.MorningAdhkar),
            timesForDate = ::timesFor,
        )

        assertEquals(ReminderKind.MorningAdhkar, next?.kind)
        assertEquals(LocalDateTime.of(2026, 7, 18, 4, 45), next?.at)
    }

    @Test
    fun `evening adhkar reminder anchors to asr`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 16, 10),
            enabled = setOf(ReminderKind.EveningAdhkar),
            timesForDate = ::timesFor,
        )

        assertEquals(ReminderKind.EveningAdhkar, next?.kind)
        assertEquals(LocalDateTime.of(2026, 7, 18, 16, 35), next?.at)
    }

    @Test
    fun `a reminder exactly at now is not replanned`() {
        val next = ReminderPlanner.nextReminder(
            now = LocalDateTime.of(2026, 7, 18, 12, 30),
            enabled = setOf(ReminderKind.DhuhrAdhan),
            timesForDate = ::timesFor,
        )

        // Dhuhr today is not after now, so the plan moves to tomorrow.
        assertEquals(LocalDateTime.of(2026, 7, 19, 12, 30), next?.at)
    }
}
