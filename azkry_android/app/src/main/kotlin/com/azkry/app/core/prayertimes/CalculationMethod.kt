package com.azkry.app.core.prayertimes

/**
 * Twilight-angle conventions for Fajr and Isha. [ishaMinutesAfterMaghrib]
 * replaces the Isha angle for interval-based methods (Umm al-Qura).
 */
enum class CalculationMethod(
    val fajrAngle: Double,
    val ishaAngle: Double?,
    val ishaMinutesAfterMaghrib: Int? = null,
) {
    MuslimWorldLeague(18.0, 17.0),
    Egyptian(19.5, 17.5),
    UmmAlQura(18.5, null, 90),
    Karachi(18.0, 18.0),
    NorthAmerica(15.0, 15.0),

    /** "Angle 15°" convention widely used in France — the design's default. */
    France15(15.0, 15.0),
    France12(12.0, 12.0),
}

/** Juristic convention for the Asr shadow length. */
enum class AsrMadhab(val shadowFactor: Int) {
    Shafii(1),
    Hanafi(2),
}

/**
 * Fallback strategy for latitudes where astronomical twilight can be absent.
 * [AngleBased] matches the design's "الطريقة المستندة إلى الزاوية".
 */
enum class HighLatitudeRule {
    None,
    MiddleOfTheNight,
    SeventhOfTheNight,
    AngleBased,
}
