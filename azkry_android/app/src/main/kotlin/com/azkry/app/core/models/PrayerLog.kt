package com.azkry.app.core.models

import androidx.room.Entity

/**
 * Whether one obligatory prayer was performed on one calendar day. [prayer]
 * stores the [Prayer] enum name; [date] is a local ISO date (yyyy-MM-dd).
 */
@Entity(
    tableName = "prayer_logs",
    primaryKeys = ["date", "prayer"],
)
data class PrayerLog(
    val date: String,
    val prayer: String,
    val completed: Boolean,
)
