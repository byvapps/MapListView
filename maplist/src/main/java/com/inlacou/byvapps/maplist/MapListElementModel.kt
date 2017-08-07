package com.inlacou.byvapps.maplist

/**
 * Created by inlacou on 14/07/17.
 */
open interface MapListElementModel{
	open val latitude: Any?
	open val longitude: Any?
	open val markerResource: Int?
	open val selectedMarkerResource: Int?
}