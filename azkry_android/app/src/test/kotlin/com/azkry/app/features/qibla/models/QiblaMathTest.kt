package com.azkry.app.features.qibla.models

import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QiblaMathTest {
    @Test
    fun `paris qibla points southeast`() {
        val bearing = QiblaMath.bearingToKaaba(latitude = 48.8566, longitude = 2.3522)
        // Reference value ≈ 119.2° from true north.
        assertTrue("was $bearing", abs(bearing - 119.2) < 1.0)
    }

    @Test
    fun `jakarta qibla points west-northwest`() {
        val bearing = QiblaMath.bearingToKaaba(latitude = -6.2088, longitude = 106.8456)
        // Reference value ≈ 295.1° from true north.
        assertTrue("was $bearing", abs(bearing - 295.1) < 1.5)
    }

    @Test
    fun `due south of the kaaba the bearing is north`() {
        val bearing = QiblaMath.bearingToKaaba(latitude = 0.0, longitude = QiblaMath.KAABA_LONGITUDE)
        assertEquals(0.0, bearing, 0.5)
    }

    @Test
    fun `needle rotation wraps around north`() {
        assertEquals(20.0, QiblaMath.needleRotation(bearingDegrees = 10.0, azimuthDegrees = 350.0), 1e-9)
        assertEquals(350.0, QiblaMath.needleRotation(bearingDegrees = 340.0, azimuthDegrees = 350.0), 1e-9)
    }
}
