package com.azkry.app.features.settings.models

import androidx.annotation.StringRes
import com.azkry.app.R

/**
 * A third-party work the app redistributes, with the attribution its licence
 * requires. Author names, licence names and URIs are legal text: they stay in
 * English exactly as the licensor published them and are never translated.
 */
data class AppLicense(
    @param:StringRes val headingRes: Int,
    val work: String,
    val copyright: String,
    val license: String,
    val url: String,
)

/**
 * Every third-party work bundled in the APK. The CC BY-SA adhan recording and
 * the OFL fonts both require this notice to travel with the app, so the screen
 * that renders this list is not optional chrome.
 */
val APP_LICENSES: List<AppLicense> = listOf(
    AppLicense(
        headingRes = R.string.license_quran,
        work = "Tanzil Quran Text (Uthmani)",
        copyright = "Copyright © 2007–2024 Tanzil Project",
        license = "Creative Commons Attribution 3.0",
        url = "https://tanzil.net/docs/tanzil_license",
    ),
    AppLicense(
        headingRes = R.string.license_font_almarai,
        work = "Almarai",
        copyright = "Copyright © 2019 The Almarai Project Authors",
        license = "SIL Open Font License 1.1",
        url = "https://openfontlicense.org",
    ),
    AppLicense(
        headingRes = R.string.license_font_amiri,
        work = "Amiri, Amiri Quran",
        copyright = "Copyright © 2010–2022 The Amiri Project Authors",
        license = "SIL Open Font License 1.1",
        url = "https://openfontlicense.org",
    ),
    AppLicense(
        headingRes = R.string.license_adhan,
        work = "The Adhan — Muslim Call to Prayer, by Aaqib Azeez (Wikimedia Commons)",
        copyright = "Copyright © Aaqib Azeez",
        license = "Creative Commons Attribution-ShareAlike 4.0",
        url = "https://creativecommons.org/licenses/by-sa/4.0/",
    ),
)
