package com.azkry.app.features.qibla.models

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

object QiblaMath {
    const val KAABA_LATITUDE = 21.4225
    const val KAABA_LONGITUDE = 39.8262

    /**
     * Initial great-circle bearing from ([latitude], [longitude]) to the
     * Kaaba, in degrees clockwise from true north, normalized to [0, 360).
     */
    fun bearingToKaaba(latitude: Double, longitude: Double): Double {
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val deltaLng = Math.toRadians(KAABA_LONGITUDE - longitude)

        val y = sin(deltaLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLng)
        return Math.toDegrees(atan2(y, x)).mod(360.0)
    }

    /**
     * Rotation to apply to the qibla needle given the device's current
     * compass [azimuthDegrees] (clockwise from north), in [0, 360).
     */
    fun needleRotation(bearingDegrees: Double, azimuthDegrees: Double): Double =
        (bearingDegrees - azimuthDegrees).mod(360.0)
}
