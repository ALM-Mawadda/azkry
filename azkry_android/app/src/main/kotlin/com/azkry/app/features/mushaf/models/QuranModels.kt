package com.azkry.app.features.mushaf.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One entry of the bundled surah index (`assets/quran/index.json`). */
@Serializable
data class SurahInfo(
    @SerialName("n") val number: Int,
    val name: String,
    @SerialName("en") val englishName: String,
    /** "Meccan" or "Medinan" as shipped by the source data. */
    val type: String,
    @SerialName("count") val ayahCount: Int,
    /** Madani mushaf page of the surah's first ayah. */
    val page: Int,
    val juz: Int,
) {
    val isMeccan: Boolean get() = type == "Meccan"
}

/** First ayah of each juz (`assets/quran/index.json`). */
@Serializable
data class JuzStart(
    @SerialName("n") val number: Int,
    val surah: Int,
    val ayah: Int,
    val page: Int,
)

@Serializable
data class QuranIndex(
    val surahs: List<SurahInfo>,
    val juzs: List<JuzStart>,
)

@Serializable
data class QuranAyah(
    @SerialName("n") val numberInSurah: Int,
    @SerialName("t") val text: String,
    @SerialName("p") val page: Int,
    @SerialName("j") val juz: Int,
)

/** One surah's full text (`assets/quran/surah_<n>.json`). */
@Serializable
data class SurahContent(
    @SerialName("n") val number: Int,
    val name: String,
    val ayahs: List<QuranAyah>,
)

/** The reader's last position, persisted so the mushaf home can resume. */
data class LastRead(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val page: Int,
    val timestampMillis: Long,
)

@Serializable
data class QuranBookmark(
    val surahNumber: Int,
    val surahName: String,
    val page: Int,
)

/** A khatmah plan: read the whole mushaf across [totalDays] days. */
data class Khatmah(
    val startDateKey: String,
    val totalDays: Int,
) {
    companion object {
        const val TOTAL_PAGES = 604
    }
}

private const val ARABIC_INDIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"

fun Int.toArabicIndicDigits(): String =
    toString().map { ch ->
        if (ch.isDigit()) ARABIC_INDIC_DIGITS[ch - '0'] else ch
    }.joinToString("")
