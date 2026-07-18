package com.azkry.app.core.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material.icons.outlined.WbTwilight
import androidx.compose.ui.graphics.vector.ImageVector
import com.azkry.app.core.models.Prayer

/** The design's per-prayer glyphs on time cells and rows. */
fun prayerIcon(prayer: Prayer): ImageVector = when (prayer) {
    Prayer.Fajr -> Icons.Outlined.WbTwilight
    Prayer.Sunrise -> Icons.Outlined.WbSunny
    Prayer.Dhuhr -> Icons.Outlined.LightMode
    Prayer.Asr -> Icons.Outlined.Cloud
    Prayer.Maghrib -> Icons.Outlined.WbTwilight
    Prayer.Isha -> Icons.Outlined.DarkMode
}
