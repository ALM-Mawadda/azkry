package com.azkry.app.core.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

/** A dhikr the user starred. [createdAt] (epoch millis) orders the list. */
@Entity(
    tableName = "favorite_adhkar",
    foreignKeys = [
        ForeignKey(
            entity = Dhikr::class,
            parentColumns = ["id"],
            childColumns = ["dhikrId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class FavoriteDhikr(
    @PrimaryKey val dhikrId: Long,
    val createdAt: Long,
)
