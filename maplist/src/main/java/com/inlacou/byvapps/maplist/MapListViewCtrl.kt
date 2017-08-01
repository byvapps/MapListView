package com.inlacou.byvapps.galdakao.ui.views.common.maplist

import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.clustering.ClusterManager
import com.inlacou.byvapps.galdakao.clustering.MyRenderer
import com.inlacou.byvapps.galdakao.clustering.SelectableMarker
import com.inlacou.byvapps.galdakao.general.common.MapUtils
import com.inlacou.byvapps.galdakao.rx.GoogleMapCameraMoveObs
import com.inlacou.byvapps.maplist.R
import com.inlacou.byvapps.maplist.StartSnapHelper
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
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
				if(model.initialPosition!=null)	MapUtils.moveMapTo(mMap!!, model.initialPosition)
				else adjustBoundsToPoints()
			}
		}
	}

	private fun adjustBoundsToPoints() {
		val llbb = LatLngBounds.Builder()
		(0..model.itemList.size-1)
				.map { model.itemList[it] }
				.forEach { llbb.include(LatLng(it.latitude as Double, it.longitude as Double)) }
		mMap?.moveCamera(CameraUpdateFactory.newLatLngBounds(llbb.build(), 32))
	}

	internal fun saveData() {
		Log.d(DEBUG_TAG+".saveData", "model.itemList.size: " + model.itemList.size)
		Log.d(DEBUG_TAG+".saveData", "model.backupList.size: " + model.backupList.size)
		model.backupList.clear()
		(0..model.itemList.size-1)
				.map { model.itemList[it] }
				.forEach { model.backupList.add(it) }
	}

	private fun configureClusterManager() {
		clusterManager = ClusterManager<SelectableMarker>(view.context, mMap)
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
			selectMarker(marker, model.mapMode!=MapListViewModel.MapMode.EXPLORATION)
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
				if(model.mapMode==MapListViewModel.MapMode.EXPLORATION) {
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
		Log.d(DEBUG_TAG+".applyFilter", "northEast: " + northEast.latitude + ", " + northEast.longitude)
		Log.d(DEBUG_TAG+".applyFilter", "shouthWest: " + shouthWest.latitude + ", " + shouthWest.longitude)
		model.itemList.clear()
		(0..model.backupList.size-1)
				.map { model.backupList[it] }
				.filter { Log.d(DEBUG_TAG+".applyFilter", "item: " + it.latitude + ", " + it.longitude)
						it.latitude.toString().toDouble()<northEast.latitude && it.latitude.toString().toDouble()>shouthWest.latitude &&
						it.longitude.toString().toDouble()<northEast.longitude && it.longitude.toString().toDouble()>shouthWest.longitude}
				.forEach { model.itemList.add(it) }
		update()
	}

	private fun selectMarker(marker: SelectableMarker?, moveTo: Boolean = true) {
		if(marker==null || mMap==null){
			return
		}
		disposables.add(doOnMainThreadObs.subscribe({
			if(moveTo) MapUtils.moveMapTo(mMap!!, marker.lat, marker.lon, animate = true)
			val renderer = clusterManager!!.renderer as MyRenderer
			if(renderer.selectedMarker!=marker) {
				val mapMarker = (clusterManager!!.renderer as MyRenderer).getMarker(marker)
				val oldMapMarker = (clusterManager!!.renderer as MyRenderer).getMarker(renderer.selectedMarker)
				oldMapMarker?.setIcon(BitmapDescriptorFactory.fromResource(renderer.unselected_marker_id))
				if(mapMarker!=null){
					mapMarker.setIcon(BitmapDescriptorFactory.fromResource(renderer.selected_marker_id))
					renderer.selectedMarker = marker
				}
			}
		}))
	}

	private fun selectMarker(position: Int? = null, moveTo: Boolean = true) {
		Log.d(DEBUG_TAG+".selectMarker", "position: " + position)
		try {
			selectMarker(model.clusterItems[position ?: 0], moveTo)
		}catch (ioobe: IndexOutOfBoundsException){}
	}

	fun scrollTo(position: Int) {
		if(currentPosition!=null) {
			view.recyclerViewHorizontal?.scrollToPosition(position)
		}else{
			view.recyclerViewHorizontal?.scrollToPosition(position)
		}
	}

	fun onModeChangeClick() {
		when(model.displayMode){
			MapListViewModel.DisplayMode.MAP -> model.displayMode = MapListViewModel.DisplayMode.LIST
			MapListViewModel.DisplayMode.LIST -> model.displayMode = MapListViewModel.DisplayMode.MAP
		}
		view.onModeChanged()
	}

	fun onHorizontalScrolled(recyclerView: RecyclerView?, dx: Int, dy: Int) { //TODO add debounce
		//Look for shown element
		val position = (recyclerView?.layoutManager as LinearLayoutManager)
				.findFirstCompletelyVisibleItemPosition()
		if (position >= 0 && position != currentPosition) {
			currentPosition = position
			selectMarker(currentPosition, model.mapMode!=MapListViewModel.MapMode.EXPLORATION)
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
		Observable.timer(1000, TimeUnit.MILLISECONDS)
				.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe(object: Observer<Long> {
					override fun onSubscribe(d: Disposable) {
						disposables.add(d)
					}

					override fun onError(e: Throwable?) {
						Log.d(DEBUG_TAG, "onError")
					}

					override fun onComplete() {
						Log.d(DEBUG_TAG, "onComplete")
					}

					override fun onNext(t: Long?) {
						Log.d(DEBUG_TAG + ".onNext", "padding1: " + view.recyclerViewHorizontal!!.height)
						Log.d(DEBUG_TAG + ".onNext", "padding2: " + view.tvChangeMode!!.height)
						mMap!!.setPadding(0,0, 0,view.recyclerViewHorizontal!!.height+view.tvChangeMode!!.height)
						//TODO Maybe more configuration for this? if(model.mapMode!=MapListViewModel.MapMode.EXPLORATION) selectMarker(0)
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
			(0..model.itemList.size - 1)
					.map { model.itemList[it] }
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
				clusterManager?.cluster()
				onUpdated() }))
		}
	}

	fun setMarkers(pin_selected: Int, pin_unselected: Int) {
		selected_marker_id = pin_selected
		unselected_marker_id = pin_unselected
	}

	fun onDestroy() {
		(0..disposables.size-1)
				.map { disposables[it] }
				.forEach { it.dispose() }
	}
}