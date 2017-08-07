package com.inlacou.byvapps.maplistlib.business

import com.inlacou.byvapps.maplist.MapListElementModel

/**
 * Created by inlacou on 01/08/17.
 */
class ExampleItem(override val latitude: Any?, override val longitude: Any?, override val markerResource: Int?, override val selectedMarkerResource: Int? = null) : MapListElementModel {
}