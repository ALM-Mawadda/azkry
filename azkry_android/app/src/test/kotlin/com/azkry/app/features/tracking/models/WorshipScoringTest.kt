package com.azkry.app.features.tracking.models

import org.junit.Assert.assertEquals
import org.junit.Test

class WorshipScoringTest {
    @Test
    fun `empty day scores zero`() {
        val percent = WorshipScoring.dayPercent(
            completedPrayers = 0,
            completedAdhkarCategories = 0,
            trackedAdhkarCategories = 3,
            completedTasks = emptySet(),
        )
        assertEquals(0, percent)
    }

    @Test
    fun `full day scores at least ninety`() {
        // Prayers 50 + adhkar 10 + quran 10 + daily 10 + rawatib 10 = 90; the
        // remaining 10% ("other") has no built-in tasks yet.
        val percent = WorshipScoring.dayPercent(
            completedPrayers = 5,
            completedAdhkarCategories = 3,
            trackedAdhkarCategories = 3,
            completedTasks = WorshipTask.entries.toSet(),
        )
        assertEquals(90, percent)
    }

    @Test
    fun `prayers alone are worth half`() {
        val percent = WorshipScoring.dayPercent(
            completedPrayers = 5,
            completedAdhkarCategories = 0,
            trackedAdhkarCategories = 3,
            completedTasks = emptySet(),
        )
        assertEquals(50, percent)
    }

    @Test
    fun `one prayer is worth a tenth`() {
        val percent = WorshipScoring.dayPercent(
            completedPrayers = 1,
            completedAdhkarCategories = 0,
            trackedAdhkarCategories = 3,
            completedTasks = emptySet(),
        )
        assertEquals(10, percent)
    }

    @Test
    fun `zero tracked adhkar categories does not divide by zero`() {
        val percent = WorshipScoring.dayPercent(
            completedPrayers = 0,
            completedAdhkarCategories = 0,
            trackedAdhkarCategories = 0,
            completedTasks = emptySet(),
        )
        assertEquals(0, percent)
    }
}
