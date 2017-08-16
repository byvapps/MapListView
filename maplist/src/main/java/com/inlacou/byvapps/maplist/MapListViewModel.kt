package com.inlacou.byvapps.maplist

import com.inlacou.byvapps.maplist.clustering.SelectableMarker

/**
 * Created by inlacou on 14/07/17.
 */
data class MapListViewModel<T: MapListElementModel>(val itemList: MutableList<T>
                                                    , val backupList: MutableList<T> = mutableListOf()
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