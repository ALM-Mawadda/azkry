package com.azkry.app.core.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Single-row bookkeeping for the bundled content seed. [revision] records the
 * content generation last written; bumping AdhkarSeed.CONTENT_REVISION makes
 * the next open replace all seeded categories with the current bundle.
 */
@Entity(tableName = "seed_info")
data class SeedInfo(
    @PrimaryKey val id: Int = 0,
    val revision: Int,
)
