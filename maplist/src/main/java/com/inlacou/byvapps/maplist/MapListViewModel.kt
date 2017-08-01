package com.inlacou.byvapps.galdakao.ui.views.common.maplist

import com.google.android.gms.maps.model.LatLngBounds
import com.inlacou.byvapps.galdakao.clustering.SelectableMarker

/**
 * Created by inlacou on 14/07/17.
 */
data class MapListViewModel<T: MapListElementModel>(val itemList: MutableList<T>
                                                    , val backupList: MutableList<T> = mutableListOf()
                                                    , val initialPosition: LatLngBounds? = null
                                                    , var displayMode: DisplayMode = DisplayMode.MAP
                                                    , val clusterItems: MutableList<SelectableMarker> = mutableListOf<SelectableMarker>()
                                                    , val mapMode: MapMode = MapMode.FINITE) {
    enum class DisplayMode {
        MAP, LIST
    }

    enum class MapMode {
        FINITE, EXPLORATION
    }
} //Fill in the brackets, because the model must be a POJO and no more