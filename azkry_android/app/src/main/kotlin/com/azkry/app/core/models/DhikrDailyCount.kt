package com.azkry.app.core.models

import androidx.room.Entity

/**
 * The tap counter of one dhikr for one calendar day. [date] is a local ISO
 * date string (yyyy-MM-dd) so a new day naturally starts every counter at
 * zero without a reset job.
 */
@Entity(
    tableName = "dhikr_daily_counts",
    primaryKeys = ["date", "dhikrId"],
)
data class DhikrDailyCount(
    val date: String,
    val dhikrId: Long,
    val count: Int,
)
