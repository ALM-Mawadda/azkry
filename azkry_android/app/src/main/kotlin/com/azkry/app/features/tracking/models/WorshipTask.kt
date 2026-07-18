package com.azkry.app.features.tracking.models

import androidx.annotation.StringRes
import com.azkry.app.R

enum class WorshipSection {
    Quran,
    Daily,
    Rawatib,
}

/**
 * The fixed set of trackable non-prayer worship tasks shown on the tracking
 * screen. The enum name is the stable `taskKey` stored in `worship_logs`.
 */
enum class WorshipTask(
    val section: WorshipSection,
    @param:StringRes val titleRes: Int,
    @param:StringRes val subtitleRes: Int,
) {
    QuranDaily(WorshipSection.Quran, R.string.tracking_quran_daily, R.string.tracking_quran_daily_subtitle),
    Kahf(WorshipSection.Quran, R.string.tracking_kahf, R.string.tracking_kahf_subtitle),
    Duha(WorshipSection.Daily, R.string.tracking_duha, R.string.tracking_duha_subtitle),
    RawatibFajr(WorshipSection.Rawatib, R.string.tracking_rawatib_fajr, R.string.tracking_rawatib_fajr_subtitle),
    RawatibDhuhrBefore(
        WorshipSection.Rawatib,
        R.string.tracking_rawatib_dhuhr_before,
        R.string.tracking_rawatib_dhuhr_before_subtitle,
    ),
    RawatibDhuhrAfter(
        WorshipSection.Rawatib,
        R.string.tracking_rawatib_dhuhr_after,
        R.string.tracking_rawatib_dhuhr_after_subtitle,
    ),
    RawatibMaghrib(WorshipSection.Rawatib, R.string.tracking_rawatib_maghrib, R.string.tracking_rawatib_maghrib_subtitle),
    RawatibIsha(WorshipSection.Rawatib, R.string.tracking_rawatib_isha, R.string.tracking_rawatib_isha_subtitle),
    Qiyam(WorshipSection.Rawatib, R.string.tracking_qiyam, R.string.tracking_qiyam_subtitle),
    Witr(WorshipSection.Rawatib, R.string.tracking_witr, R.string.tracking_witr_subtitle),
    ;

    companion object {
        fun fromKey(key: String): WorshipTask? = entries.firstOrNull { it.name == key }
    }
}
