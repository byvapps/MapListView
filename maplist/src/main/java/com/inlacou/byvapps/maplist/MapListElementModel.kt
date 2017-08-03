package com.inlacou.byvapps.maplist

/**
 * Created by inlacou on 14/07/17.
 */
open class MapListElementModel(open val latitude: Any? = null
                               , open val longitude: Any? = null
                               , open val markerResource: Int? = null
                               , open val selectedMarkerResource: Int? = null
) //Fill in the brackets, because the model must be a POJO and no more