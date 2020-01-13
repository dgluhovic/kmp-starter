package com.kmp_starter.core.base

import android.location.Location
import com.kmp_starter.data.LatLng

actual fun distanceMeters(
    loc: LatLng,
    otherLoc: LatLng
): Double = loc.toLocation().distanceTo(otherLoc.toLocation()).toDouble()

private fun LatLng.toLocation() = Location("").also {
    it.latitude = latitude
    it.longitude = longitude
}