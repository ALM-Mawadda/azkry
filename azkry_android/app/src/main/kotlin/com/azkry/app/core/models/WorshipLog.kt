package com.azkry.app.core.models

import androidx.room.Entity

/**
 * Completion of one non-prayer worship task (Quran reading, rawatib, Duha,
 * qiyam, witr…) on one calendar day. [taskKey] is the feature-defined task
 * key; [date] is a local ISO date (yyyy-MM-dd).
 */
@Entity(
    tableName = "worship_logs",
    primaryKeys = ["date", "taskKey"],
)
data class WorshipLog(
    val date: String,
    val taskKey: String,
    val completed: Boolean,
)
