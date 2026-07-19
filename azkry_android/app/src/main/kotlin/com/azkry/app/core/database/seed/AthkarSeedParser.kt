package com.azkry.app.core.database.seed

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Bundled adhkar content (assets/adhkar/athkar_seed.json), extracted verbatim
 * from the reference iOS Athkar library. The same types describe the extra
 * Kotlin-defined categories so seeding treats both sources uniformly.
 */
@Serializable
data class SeedContent(val categories: List<SeedCategory>)

@Serializable
data class SeedCategory(
    val key: String,
    val title: String,
    val iconKey: String,
    val items: List<SeedDhikr>,
)

@Serializable
data class SeedDhikr(
    val text: String,
    val count: Int = 1,
    val source: String? = null,
    val title: String? = null,
    val virtue: String? = null,
    /** Optional immutable item key. Existing keys must never be repurposed. */
    val key: String? = null,
)

object AthkarSeedParser {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(raw: String): SeedContent = json.decodeFromString(raw)
}
