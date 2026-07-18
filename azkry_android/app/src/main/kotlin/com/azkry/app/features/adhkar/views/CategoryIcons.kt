package com.azkry.app.features.adhkar.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bed
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.ChildCare
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.DoneAll
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material.icons.outlined.WavingHand
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Resolves a [com.azkry.app.core.models.DhikrCategory.iconKey] to its icon.
 * Unknown keys fall back to a sparkle so new seeded categories never crash
 * the list.
 */
fun categoryIcon(iconKey: String): ImageVector = when (iconKey) {
    "sun" -> Icons.Outlined.WbSunny
    "moon" -> Icons.Outlined.DarkMode
    "bed" -> Icons.Outlined.Bed
    "alarm" -> Icons.Outlined.Alarm
    "prayer" -> Icons.Outlined.Mosque
    "after_prayer" -> Icons.Outlined.DoneAll
    "tasbih" -> Icons.Outlined.AutoAwesome
    "sparkle" -> Icons.Outlined.AutoAwesome
    "hamd" -> Icons.Outlined.WavingHand
    "istighfar" -> Icons.Outlined.VolunteerActivism
    "quran" -> Icons.AutoMirrored.Outlined.MenuBook
    "bookmark" -> Icons.Outlined.Bookmark
    "shield" -> Icons.Outlined.Shield
    "shield_quran" -> Icons.Outlined.FavoriteBorder
    "kaaba" -> Icons.Outlined.Mosque
    "family" -> Icons.Outlined.Groups
    "kids" -> Icons.Outlined.ChildCare
    else -> Icons.Outlined.AutoAwesome
}
