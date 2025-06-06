package ru.yandex.buggyweatherapp.data.repository

import android.location.Geocoder
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import ru.yandex.buggyweatherapp.data.model.Location
import ru.yandex.buggyweatherapp.ui.api.LocationRepository
import javax.inject.Inject

class LocationRepositoryImpl @Inject constructor(
    private val geocoder: Geocoder,
    private val fusedLocationClient: FusedLocationProviderClient
): LocationRepository {
    
    override fun getCurrentLocation(callback: (Location?) -> Unit) {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val userLocation = Location(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        callback(userLocation)
                    } else {
                        requestLocationUpdates(callback)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("LocationRepository", "Error getting location", e)
                    callback(null)
                }
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            callback(null)
        }
    }
    
    
    private fun requestLocationUpdates(callback: (Location?) -> Unit) {
        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .build()
            
            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        val userLocation = Location(
                            latitude = location.latitude,
                            longitude = location.longitude
                        )
                        callback(userLocation)
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            callback(null)
        }
    }
    
    
    override fun getCityNameFromLocation(location: Location): String? {
        try {

            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            return if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                if (address.locality != null) {
                    address.locality
                } else if (address.subAdminArea != null) {
                    address.subAdminArea
                } else {
                    address.adminArea
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting city name", e)
            return null
        }
    }


    override fun startLocationTracking() {
//        LocationTracker.getInstance(context).startTracking()
    }
    
    
}