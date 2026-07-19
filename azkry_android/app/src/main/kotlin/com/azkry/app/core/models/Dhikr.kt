package com.azkry.app.core.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A single dhikr text with its recommended repeat count and narration source
 * (e.g. رواه مسلم). Belongs to exactly one [DhikrCategory].
 */
@Entity(
    tableName = "adhkar",
    foreignKeys = [
        ForeignKey(
            entity = DhikrCategory::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["stableKey"], unique = true),
    ],
)
data class Dhikr(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val text: String,
    val repeatCount: Int,
    val source: String?,
    val sortOrder: Int,
    /** Display heading, e.g. آية الكرسي or دعاء الكرب. */
    val title: String? = null,
    /** Reported virtue/fadl of the dhikr, shown under the text. */
    val virtue: String? = null,
    /** Stable seed identity used by backups; unlike [id], it survives reseeding. */
    @ColumnInfo(defaultValue = "''") val stableKey: String = "",
)
