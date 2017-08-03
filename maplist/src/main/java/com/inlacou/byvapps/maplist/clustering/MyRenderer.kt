package com.inlacou.byvapps.galdakao.clustering

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.inlacou.byvapps.maplist.R

/**
 * Created by inlacou on 27/06/17.
 * Guide: https://github.com/googlemaps/android-maps-utils/blob/master/demo/src/com/google/maps/android/utils/demo/CustomMarkerClusteringDemoActivity.java
 */
class MyRenderer(
		context: Context?
		, map: GoogleMap?
		, clusterManager: ClusterManager<SelectableMarker>?
) : DefaultClusterRenderer<SelectableMarker>(context, map, clusterManager){
	companion object {
		private val DEBUG_TAG = MyRenderer::class.java.simpleName
	}
	var selectedMarker: SelectableMarker? = null
	var selected_marker_id = R.drawable.pin_selected
	var unselected_marker_id = R.drawable.pin
	var centerPin = false
	var clusterRenderedListener: onBeforeClusterRenderedListener? = null
	var minimum: Int = 5
	var active: Boolean = true

	override fun onBeforeClusterItemRendered(marker: SelectableMarker, markerOptions: MarkerOptions) {
		///Log.d(DEBUG_TAG+".onBeforeClusterItemRdr", "marker: " + Gson().toJson(marker))
		// Draw a single person.
		// Set the info window to show their name.
		//val icon = mIconGenerator.makeIcon()
		//markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title(marker.enterprise.nombre)
		if(marker.selected) {
			markerOptions.icon(BitmapDescriptorFactory.fromResource(marker.item.selectedMarkerResource ?: selected_marker_id))
					.zIndex(1.0f)
			selectedMarker = marker
		}else {
			markerOptions.icon(BitmapDescriptorFactory.fromResource(marker.item.markerResource ?: unselected_marker_id))
		}
		if(centerPin){
			markerOptions.anchor(0.5f, 0.5f)
		}
	}

	override fun setOnClusterItemClickListener(listener: ClusterManager.OnClusterItemClickListener<SelectableMarker>?) {
		super.setOnClusterItemClickListener(listener)
	}

	override fun onBeforeClusterRendered(cluster: Cluster<SelectableMarker>, markerOptions: MarkerOptions) {
		if(clusterRenderedListener==null || clusterRenderedListener?.onBeforeClusterRendered(cluster, markerOptions)==false){
			when(cluster.size){
				in 5..9 -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_5))
				in 10..14 -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_10))
				in 15..19 -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_15))
				in 20..24 -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_20))
				in 25..49 -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_25))
				in 50..99 -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_50))
				in 100..499 -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_100))
				in 500..999 -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_500))
				else -> markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_1000))
			}
		}
	}

	override fun shouldRenderAsCluster(cluster: Cluster<SelectableMarker>): Boolean {
		return active && cluster.size >= minimum
	}

	interface onBeforeClusterRenderedListener {
		/**
		 * example: markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.cluster_5))
		 */
		fun onBeforeClusterRendered(cluster: Cluster<SelectableMarker>, markerOptions: MarkerOptions): Boolean
	}
}