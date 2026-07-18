package com.azkry.app.app

import androidx.datastore.preferences.core.stringPreferencesKey

enum class AppLanguage(val code: String?) {
    Default(null),
    Arabic("ar"),
    ;

    companion object {
        fun fromCode(code: String?): AppLanguage {
            val normalized = code?.trim()?.takeIf { it.isNotEmpty() } ?: return Default
            if (normalized.equals("system", ignoreCase = true) ||
                normalized.equals("default", ignoreCase = true)
            ) {
                return Default
            }
            return entries.firstOrNull { language ->
                language.code?.equals(normalized, ignoreCase = true) == true ||
                    language.name.equals(normalized, ignoreCase = true)
            } ?: Default
        }
    }
}

enum class Appearance {
    Dark,
    Light,
    System,
    ;

    companion object {
        fun fromName(name: String?): Appearance =
            entries.firstOrNull { it.name == name } ?: Dark
    }
}

object AppStateKeys {
    val AppLanguage = stringPreferencesKey("appLanguage")
    val Appearance = stringPreferencesKey("appearance")
    val HijriOffsetDays = stringPreferencesKey("hijriOffsetDays")
}
