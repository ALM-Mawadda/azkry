package com.azkry.app.core.models

import androidx.annotation.StringRes
import com.azkry.app.R

/**
 * The daily prayer events the app tracks and computes times for. [Sunrise]
 * is not a prayer but bounds Fajr and anchors Duha, so it travels with the
 * set everywhere times are displayed.
 */
enum class Prayer {
    Fajr,
    Sunrise,
    Dhuhr,
    Asr,
    Maghrib,
    Isha,
    ;

    val isObligatory: Boolean
        get() = this != Sunrise

    companion object {
        val obligatory: List<Prayer> = entries.filter { it.isObligatory }
    }
}

@StringRes
fun Prayer.labelRes(): Int = when (this) {
    Prayer.Fajr -> R.string.prayer_fajr
    Prayer.Sunrise -> R.string.prayer_sunrise
    Prayer.Dhuhr -> R.string.prayer_dhuhr
    Prayer.Asr -> R.string.prayer_asr
    Prayer.Maghrib -> R.string.prayer_maghrib
    Prayer.Isha -> R.string.prayer_isha
}
