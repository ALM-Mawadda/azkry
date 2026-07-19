package com.azkry.app.features.tracking.models

import com.azkry.app.core.models.Prayer
import com.azkry.app.core.models.WorshipSection
import com.azkry.app.core.models.WorshipTask

/**
 * Day-completion percentage with the design's default weights: prayers 50%,
 * adhkar 10%, Quran 10%, rawatib (nawafil) 10%, daily worships 10%, other
 * 10%. Custom weights are a later feature; the shape stays a pure function
 * so it is trivially testable.
 */
object WorshipScoring {
    const val PRAYERS_WEIGHT = 50
    const val ADHKAR_WEIGHT = 10
    const val QURAN_WEIGHT = 10
    const val RAWATIB_WEIGHT = 10
    const val DAILY_WEIGHT = 10
    const val OTHER_WEIGHT = 10

    fun dayPercent(
        completedPrayers: Int,
        completedAdhkarCategories: Int,
        trackedAdhkarCategories: Int,
        completedTasks: Set<WorshipTask>,
    ): Int {
        val prayersScore = ratio(completedPrayers, Prayer.obligatory.size) * PRAYERS_WEIGHT
        val adhkarScore = ratio(completedAdhkarCategories, trackedAdhkarCategories) * ADHKAR_WEIGHT

        val quranTasks = WorshipTask.entries.filter { it.section == WorshipSection.Quran }
        val dailyTasks = WorshipTask.entries.filter { it.section == WorshipSection.Daily }
        val rawatibTasks = WorshipTask.entries.filter { it.section == WorshipSection.Rawatib }

        val quranScore = ratio(completedTasks.count { it in quranTasks }, quranTasks.size) * QURAN_WEIGHT
        val dailyScore = ratio(completedTasks.count { it in dailyTasks }, dailyTasks.size) * DAILY_WEIGHT
        val rawatibScore = ratio(completedTasks.count { it in rawatibTasks }, rawatibTasks.size) * RAWATIB_WEIGHT

        val total = prayersScore + adhkarScore + quranScore + dailyScore + rawatibScore
        return total.toInt().coerceIn(0, 100)
    }

    private fun ratio(completed: Int, total: Int): Double =
        if (total <= 0) 0.0 else completed.toDouble() / total
}
