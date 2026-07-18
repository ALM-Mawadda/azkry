package com.azkry.app.features.prayertimes.services

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import com.azkry.app.core.prayertimes.GeoLocation
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

data class ResolvedLocation(
    val location: GeoLocation,
    val cityName: String?,
)

interface LocationService {
    val hasPermission: Boolean

    /** Null when permission is missing or no fix could be obtained. */
    suspend fun currentLocation(): ResolvedLocation?
}

@Singleton
class FusedLocationService @Inject constructor(
    @param:ApplicationContext private val context: Context,
) : LocationService {
    override val hasPermission: Boolean
        get() = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    override suspend fun currentLocation(): ResolvedLocation? {
        if (!hasPermission) return null
        val client = LocationServices.getFusedLocationProviderClient(context)
        val fix = suspendCancellableCoroutine<android.location.Location?> { continuation ->
            @Suppress("MissingPermission")
            client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { location -> continuation.resume(location) }
                .addOnFailureListener { continuation.resume(null) }
        } ?: return null

        val geo = GeoLocation(latitude = fix.latitude, longitude = fix.longitude)
        return ResolvedLocation(location = geo, cityName = reverseGeocodeCity(geo))
    }

    private suspend fun reverseGeocodeCity(location: GeoLocation): String? {
        if (!Geocoder.isPresent()) return null
        return withContext(Dispatchers.IO) {
            runCatching {
                val geocoder = Geocoder(context, Locale.forLanguageTag("ar"))
                // The T+ listener API is asynchronous; the deprecated blocking
                // call remains functional and is the simplest correct choice
                // off the main thread.
                @Suppress("DEPRECATION")
                val address = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                address?.firstOrNull()?.let { it.locality ?: it.subAdminArea ?: it.adminArea }
            }.getOrNull()
        }
    }
}
