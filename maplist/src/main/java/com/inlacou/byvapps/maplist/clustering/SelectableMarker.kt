package com.inlacou.byvapps.galdakao.clustering

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

/**
 * Created by inlacou on 27/06/17.
 */
class SelectableMarker(
        val lat: Double
        , val lon: Double
        , val item: Any
        , val position: Int
        , var selected: Boolean = false
) : ClusterItem {
    companion object{
        private val DEBUG_TAG = SelectableMarker::class.java.simpleName
    }

    override fun getSnippet(): String {
        return "Holi"
    }

    override fun getTitle(): String {
        return "Holi"
    }

    override fun getPosition(): LatLng {
        return LatLng(lat, lon)
    }
}