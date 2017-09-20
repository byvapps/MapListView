package com.inlacou.byvapps.maplist

import android.support.annotation.RequiresPermission
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.inlacou.byvapps.maplist.clustering.MyRenderer
import com.inlacou.byvapps.maplist.clustering.SelectableMarker
import com.inlacou.byvapps.maplist.rx.GoogleMapCameraMoveObs
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit

/**
 * Created by inlacou on 14/07/17.
 */
class MapListViewCtrl<T: MapListElementModel> {

	private var view: MapListView<T>
	private var model: MapListViewModel<T>

	//MapList
	private var linearLayoutManagerHorizontal: LinearLayoutManager? = null
	private var linearLayoutManagerVertical: LinearLayoutManager? = null
	private var adapterVertical: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
	private var adapterHorizontal: RecyclerView.Adapter<RecyclerView.ViewHolder>? = null
	private var loading = false
	private var currentPosition: Int? = null

	private var mMap: GoogleMap? = null
	private var clusterManager: ClusterManager<SelectableMarker>? = null

	private var selected_marker_id: Int = R.drawable.pin_selected
	private var unselected_marker_id: Int = R.drawable.pin
	private var moveCameraOnMarkerFocusChange: Boolean = true
	///MapList

	//RX
	private val disposables: MutableList<Disposable> = mutableListOf()
	private val doOnMainThreadObs: Observable<Int> = Observable.just(1).observeOn(AndroidSchedulers.mainThread())
	///RX

	companion object {
		private val DEBUG_TAG = MapListViewCtrl::class.java.simpleName
	}

	constructor(view: MapListView<T>, model: MapListViewModel<T>,
	            horizontalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>,
	            verticalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>) {
		this.view = view
		this.model = model
		saveData()

		linearLayoutManagerHorizontal = LinearLayoutManager(view.context, LinearLayoutManager.HORIZONTAL, false)
		linearLayoutManagerVertical = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
		view.recyclerViewVertical!!.layoutManager = linearLayoutManagerVertical
		view.recyclerViewHorizontal!!.layoutManager = linearLayoutManagerHorizontal
		view.recyclerViewVertical!!.isVerticalFadingEdgeEnabled = false
		view.recyclerViewHorizontal!!.isVerticalFadingEdgeEnabled = false
		view.recyclerViewVertical!!.isHorizontalFadingEdgeEnabled = false
		view.recyclerViewHorizontal!!.isHorizontalFadingEdgeEnabled = false
		StartSnapHelper().attachToRecyclerView(view.recyclerViewVertical)
		StartSnapHelper().attachToRecyclerView(view.recyclerViewHorizontal)
		adapterVertical = verticalAdapter
		adapterHorizontal = horizontalAdapter
		view.recyclerViewHorizontal!!.adapter = adapterHorizontal
		view.recyclerViewVertical!!.adapter = adapterVertical
		view.vMap?.getMapAsync { t ->
			mMap = t
			mMap?.setOnMapLoadedCallback {
				configureClusterManager()
				saveData()
				update()
				mMap?.setPadding(0,0, 0,view.recyclerViewHorizontal!!.height+view.tvChangeMode!!.height)
				view.mCallback?.onReady()
			}
		}
	}

	fun adjustBoundsToPoints() {
		if(model.itemList.size==0){
			return
		}
		val llbb = LatLngBounds.Builder()
		(0..model.itemList.size-1)
				.map { model.itemList[it] }
				.map { if(it.latitude is Double && it.longitude is Double){
						LatLng(it.latitude as Double, it.longitude as Double)
					}else{
						LatLng((it.latitude as String).toDouble(), (it.longitude as String).toDouble())
					}
				}
				.forEach { llbb.include(it) }
		mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(llbb.build(), 32))
	}

	internal fun saveData() {
		Log.d(DEBUG_TAG +".saveData", "model.itemList.size: " + model.itemList.size)
		Log.d(DEBUG_TAG +".saveData", "model.backupList.size: " + model.backupList.size)
		model.backupList.clear()
		(0 until model.itemList.size)
				.map { model.itemList[it] }
				.forEach { model.backupList.add(it) }
	}

	private fun configureClusterManager() {
		clusterManager = ClusterManager(view.context, mMap)
		clusterManager?.renderer = MyRenderer(context = view.context, map = mMap!!, clusterManager = clusterManager)
		(clusterManager?.renderer as MyRenderer).selected_marker_id = selected_marker_id
		(clusterManager?.renderer as MyRenderer).unselected_marker_id = unselected_marker_id
		clusterManager?.renderer?.setOnClusterClickListener { cluster ->
			val b = LatLngBounds.Builder()
			for (m in cluster.items) {
				b.include(m.getPosition())
			}
			val bounds = b.build()
			//Change the padding as per needed
			val cu = CameraUpdateFactory.newLatLngBounds(bounds, 32)
			mMap?.animateCamera(cu)
			true
		}

		clusterManager?.setOnClusterItemClickListener { marker ->
			scrollTo(marker.position)
			selectMarker(marker, moveCameraOnMarkerFocusChange)
			true
		}

		mMap?.setOnCameraIdleListener(clusterManager)
		mMap?.setOnInfoWindowClickListener(clusterManager)
		mMap?.setOnMarkerClickListener(clusterManager)

		GoogleMapCameraMoveObs.init(mMap!!)
				.debounce(200, TimeUnit.MILLISECONDS)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribeOn(AndroidSchedulers.mainThread())
				.subscribe(object: Observer<LatLng>{
			override fun onNext(t: LatLng?) {
				val vRegion = mMap!!.projection.visibleRegion
				val shouthWest = vRegion.latLngBounds.southwest
				val northEast = vRegion.latLngBounds.northeast
				if(model.mapMode== MapListViewModel.MapMode.EXPLORATION) {
					applyFilter(northEast, shouthWest)
				}
			}

			override fun onComplete() {
			}

			override fun onError(e: Throwable?) {
			}

			override fun onSubscribe(d: Disposable) {
				disposables.add(d)
			}

		})
	}

	private fun applyFilter(northEast: LatLng, shouthWest: LatLng) {
		Log.d(DEBUG_TAG +".applyFilter", "northEast: " + northEast.latitude + ", " + northEast.longitude)
		Log.d(DEBUG_TAG +".applyFilter", "shouthWest: " + shouthWest.latitude + ", " + shouthWest.longitude)
		model.itemList.clear()
		(0 until model.backupList.size)
				.map { model.backupList[it] }
				.filter { Log.d(DEBUG_TAG +".applyFilter", "item: " + it.latitude + ", " + it.longitude)
						it.latitude.toString().toDouble()<northEast.latitude && it.latitude.toString().toDouble()>shouthWest.latitude &&
						it.longitude.toString().toDouble()<northEast.longitude && it.longitude.toString().toDouble()>shouthWest.longitude}
				.forEach { model.itemList.add(it) }
		update()
	}

	private fun selectMarker(marker: SelectableMarker?, moveTo: Boolean = true) {
		Log.d(DEBUG_TAG+".selectMarker", "marker: $marker | moveTo: $moveTo")
		if(marker==null || mMap==null){
			return
		}
		disposables.add(doOnMainThreadObs.subscribe({
			if(moveTo) MapUtils.moveMapTo(mMap!!, marker.lat, marker.lon, animate = true)
			if(clusterManager==null){ return@subscribe }
			val renderer = clusterManager!!.renderer as MyRenderer
			if(renderer.selectedMarker!=marker) {
				val mapMarker = (clusterManager!!.renderer as MyRenderer).getMarker(marker)
				val oldMapMarker = (clusterManager!!.renderer as MyRenderer).getMarker(renderer.selectedMarker)
				oldMapMarker?.setIcon(BitmapDescriptorFactory.fromResource(renderer.selectedMarker!!.item.markerResource ?: renderer.unselected_marker_id))
				if(mapMarker!=null){
					mapMarker.setIcon(BitmapDescriptorFactory.fromResource(marker.item.selectedMarkerResource ?: renderer.selected_marker_id))
					renderer.selectedMarker = marker
				}
			}
		}))
	}

	private fun selectMarker(position: Int? = null, moveTo: Boolean = true) {
		Log.d(DEBUG_TAG+".selectMarker", "position: $position | moveTo: $moveTo")
		try {
			selectMarker(model.clusterItems[position ?: 0], moveTo)
		}catch (ioobe: IndexOutOfBoundsException){
			Log.e(DEBUG_TAG+".selectMarker", "position: $position | moveTo: $moveTo | IndexOutOfBoundsException | ${model.clusterItems.size}")
		}
	}

	fun scrollTo(position: Int) {
		if(currentPosition!=null) {
			view.recyclerViewHorizontal?.scrollToPosition(position)
		}else{
			view.recyclerViewHorizontal?.scrollToPosition(position)
		}
	}

	fun changeMode(mode: MapListViewModel.DisplayMode? = null) {
		if(mode==null) {
			when (model.displayMode) {
				MapListViewModel.DisplayMode.MAP -> model.displayMode = MapListViewModel.DisplayMode.LIST
				MapListViewModel.DisplayMode.LIST -> model.displayMode = MapListViewModel.DisplayMode.MAP
			}
		}else{
			when (mode) {
				MapListViewModel.DisplayMode.MAP -> model.displayMode = MapListViewModel.DisplayMode.MAP
				MapListViewModel.DisplayMode.LIST -> model.displayMode = MapListViewModel.DisplayMode.LIST
			}
		}
		view.onModeChanged()
	}

	fun onHorizontalScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) { //TODO add debounce
		//Look for shown element
		val position = (recyclerView?.layoutManager as LinearLayoutManager)
				.findFirstCompletelyVisibleItemPosition()
		if (position >= 0 && position != currentPosition) {
			currentPosition = position
			selectMarker(currentPosition, moveCameraOnMarkerFocusChange)
		}
	}

	fun onUpdated() {
		Log.d(DEBUG_TAG +".onUpdated", "model.itemList.size: " + model.itemList!!.size)
		loading = false
		if (model.itemList.size > 0) {
			view.onContent()
		} else {
			view.onEmpty()
		}
		adapterHorizontal!!.notifyDataSetChanged()
		adapterVertical!!.notifyDataSetChanged()
		doOnMainThreadObs.delay(500, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(object: Observer<Int>{
			override fun onSubscribe(d: Disposable) {
				disposables.add(d)
			}

			override fun onNext(t: Int) {
				mMap?.setPadding(0,0, 0,view.recyclerViewHorizontal!!.height+view.tvChangeMode!!.height)
				clusterManager?.cluster()
			}

			override fun onComplete() {
			}

			override fun onError(e: Throwable?) {
			}
		})
	}

	fun update() {
		if (loading) {
			return
		} else {
			loading = true
			model.clusterItems.clear()
			clusterManager?.clearItems()
			(0 until model.itemList.size)
					.map { model.itemList[it] }
					.filter {
						try{
							it.latitude.toString().toDouble()
							it.longitude.toString().toDouble()
							true
						}catch (nfe: NumberFormatException){
							Log.e(DEBUG_TAG, "Invalid lat/lng on element: $it")
							//TODO should filter it from other list too!
							false
						}
					}
					.forEachIndexed { index, item ->
						val marker = SelectableMarker(
								item.latitude.toString().toDouble(),
								item.longitude.toString().toDouble(),
								item,
								index
						)
						model.clusterItems.add(marker)
						clusterManager?.addItem(marker)
					}

			disposables.add(doOnMainThreadObs.subscribe({
				onUpdated() }))
		}
	}

	fun setMarkers(pin_selected: Int, pin_unselected: Int) {
		selected_marker_id = pin_selected
		unselected_marker_id = pin_unselected
	}

	fun onDestroy() {
		(0 until disposables.size)
				.map { disposables[it] }
				.forEach { it.dispose() }
	}

	fun setClusteringEnabled(b: Boolean = true){
		(clusterManager?.renderer as MyRenderer).active = b
		clusterManager?.cluster()
	}

	fun setClusterMinSize(min: Int){
		(clusterManager?.renderer as MyRenderer).minimum = min
		clusterManager?.cluster()
	}

	fun setMoveCameraOnMarkerFocusChange(b: Boolean) {
		moveCameraOnMarkerFocusChange = b
	}

	fun setOnBeforeClusterRenderedListener(clusterRenderedListener: MyRenderer.onBeforeClusterRenderedListener) {
		(clusterManager?.renderer as MyRenderer).clusterRenderedListener = clusterRenderedListener
		clusterManager?.cluster()
	}

	@RequiresPermission(anyOf = arrayOf("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"))
	fun setMyLocationEnabled(enabled: Boolean, show: Boolean){
		mMap?.isMyLocationEnabled = enabled
		mMap?.uiSettings?.isMyLocationButtonEnabled = show
	}

	fun moveMapTo(bounds: LatLngBounds, animate: Boolean = false): Boolean {
		return MapUtils.moveMapTo(mMap, bounds, animate)
	}

	fun moveMapTo(latLng: LatLng, zoom: Float? = null, animate: Boolean = false): Boolean {
		return MapUtils.moveMapTo(mMap, latLng.latitude, latLng.longitude, zoom, animate)
	}

}