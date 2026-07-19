package com.azkry.app.core.models

import androidx.annotation.StringRes
import com.azkry.app.R

enum class WorshipSection {
    Quran,
    Daily,
    Rawatib,
}

/**
 * The fixed set of trackable non-prayer worship tasks. [key] is persisted in
 * `worship_logs` and must remain stable across package moves and obfuscation.
 */
enum class WorshipTask(
    val key: String,
    val section: WorshipSection,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
) {
    QuranDaily("QuranDaily", WorshipSection.Quran, R.string.tracking_quran_daily, R.string.tracking_quran_daily_subtitle),
    Kahf("Kahf", WorshipSection.Quran, R.string.tracking_kahf, R.string.tracking_kahf_subtitle),
    Duha("Duha", WorshipSection.Daily, R.string.tracking_duha, R.string.tracking_duha_subtitle),
    RawatibFajr(
        "RawatibFajr",
        WorshipSection.Rawatib,
        R.string.tracking_rawatib_fajr,
        R.string.tracking_rawatib_fajr_subtitle,
    ),
    RawatibDhuhrBefore(
        "RawatibDhuhrBefore",
        WorshipSection.Rawatib,
        R.string.tracking_rawatib_dhuhr_before,
        R.string.tracking_rawatib_dhuhr_before_subtitle,
    ),
    RawatibDhuhrAfter(
        "RawatibDhuhrAfter",
        WorshipSection.Rawatib,
        R.string.tracking_rawatib_dhuhr_after,
        R.string.tracking_rawatib_dhuhr_after_subtitle,
    ),
    RawatibMaghrib(
        "RawatibMaghrib",
        WorshipSection.Rawatib,
        R.string.tracking_rawatib_maghrib,
        R.string.tracking_rawatib_maghrib_subtitle,
    ),
    RawatibIsha(
        "RawatibIsha",
        WorshipSection.Rawatib,
        R.string.tracking_rawatib_isha,
        R.string.tracking_rawatib_isha_subtitle,
    ),
    Qiyam("Qiyam", WorshipSection.Rawatib, R.string.tracking_qiyam, R.string.tracking_qiyam_subtitle),
    Witr("Witr", WorshipSection.Rawatib, R.string.tracking_witr, R.string.tracking_witr_subtitle),
    ;

    companion object {
        fun fromKey(key: String): WorshipTask? = entries.firstOrNull { it.key == key }
    }
}
