package com.inlacou.byvapps.maplist

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil



/**
 * Created by inlacou on 9/02/17.
 */

object MapUtils {

    fun moveMapTo(mMap: GoogleMap?, bounds: LatLngBounds, animate: Boolean = false): Boolean {
        if(mMap==null){
            return false
        }
        val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 16)
	    if (animate) {
            mMap.animateCamera(cameraUpdate)
        } else {
            mMap.moveCamera(cameraUpdate)
        }
	    return true
    }

    fun moveMapTo(mMap: GoogleMap?, lat: Double, lon: Double, zoom: Float? = null, animate: Boolean = false): Boolean {
	    if(mMap==null){
		    return false
	    }
	    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                LatLng(
                        lat,
                        lon
                ),
                zoom ?: mMap.cameraPosition.zoom)
        if (animate) {
            mMap.animateCamera(cameraUpdate)
        } else {
            mMap.moveCamera(cameraUpdate)
        }
	    return true
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. Uses Haversine method as its base.

     * lat1, lon1 Start point lat2, lon2 End point
     * @returns Distance in Meters
     */
    fun distance(lat1: Double, lat2: Double, lon1: Double,
                 lon2: Double): Double {

        val R = 6371 // Radius of the earth

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R.toDouble() * c * 1000.0 // convert to meters

        distance = Math.pow(distance, 2.0)

        return Math.sqrt(distance)
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.

     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    fun distance(lat1: Double, lat2: Double, lon1: Double,
                 lon2: Double, el1: Double, el2: Double): Double {

        val R = 6371 // Radius of the earth

        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R.toDouble() * c * 1000.0 // convert to meters

        val height = el1 - el2

        distance = Math.pow(distance, 2.0) + Math.pow(height, 2.0)

        return Math.sqrt(distance)
    }

    fun toBounds(center: LatLng, radius: Double): LatLngBounds {
        val southwest = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 225.0)
        val northeast = SphericalUtil.computeOffset(center, radius * Math.sqrt(2.0), 45.0)
        return LatLngBounds(southwest, northeast)
    }

}
