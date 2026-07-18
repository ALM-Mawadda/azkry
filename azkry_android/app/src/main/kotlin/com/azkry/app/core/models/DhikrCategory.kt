package com.azkry.app.core.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A named group of adhkar (e.g. أذكار الصباح). Titles are stored in Arabic —
 * the content itself is Arabic-only; UI chrome strings live in resources.
 */
@Entity(
    tableName = "dhikr_categories",
    indices = [Index(value = ["key"], unique = true)],
)
data class DhikrCategory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** Stable machine key, e.g. "morning". Never shown to users. */
    val key: String,
    val title: String,
    /** Maps to an icon in the UI layer; unknown keys fall back to a default. */
    val iconKey: String,
    val sortOrder: Int,
)

object DhikrCategoryKeys {
    const val MORNING = "morning"
    const val EVENING = "evening"
    const val SLEEP = "sleep"
    const val WAKING = "waking"
    const val PRAYER = "prayer"
    const val AFTER_PRAYER = "after_prayer"
    const val TASBIH = "tasbih"
    const val ISTIGHFAR = "istighfar"
    const val HAMD = "hamd"
    const val QURAN_DUAS = "quran_duas"
    const val PROPHET_DUAS = "prophet_duas"
    const val RUQYAH_SUNNAH = "ruqyah_sunnah"
    const val RUQYAH_QURAN = "ruqyah_quran"
    const val MISC = "misc"

    // Exclusive-section categories: hidden from the main adhkar list.
    const val UMRAH = "x_umrah"
    const val HAJJ = "x_hajj"
    const val LOVED_ONES = "x_loved"
    const val KIDS = "x_kids"

    const val EXCLUSIVE_PREFIX = "x_"
}

val DhikrCategory.isExclusive: Boolean
    get() = key.startsWith(DhikrCategoryKeys.EXCLUSIVE_PREFIX)
