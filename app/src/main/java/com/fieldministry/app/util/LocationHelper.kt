package com.fieldministry.app.util

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class GpsPoint(val latitude: Double, val longitude: Double)

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): GpsPoint? = suspendCancellableCoroutine { cont ->
        val client = LocationServices.getFusedLocationProviderClient(context)
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .build()

        client.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    cont.resume(GpsPoint(location.latitude, location.longitude))
                } else {
                    cont.resume(null)
                }
            }
            .addOnFailureListener {
                cont.resume(null)
            }
    }
}
