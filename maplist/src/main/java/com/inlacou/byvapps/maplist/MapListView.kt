package com.inlacou.byvapps.maplist

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.annotation.RequiresPermission
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView

import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.inlacou.byvapps.maplist.clustering.MyRenderer
import com.inlacou.byvapps.maplist.rx.RvScrollObs
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


/**
 * Created by inlacou on 14/07/17.
 */
class MapListView<T: MapListElementModel> : FrameLayout {
	internal var mCallback: Callbacks<T>? = null
	var modeChangeListener: ModeChangedListener? = null
	private var cont: Context? = null

	//MapList
	var recyclerViewHorizontal: RecyclerView? = null
	var recyclerViewVertical: RecyclerView? = null
	var tvChangeMode: TextView? = null
	var vMap: MapView? = null
	///MapList

	//RX
	private val disposables: MutableList<Disposable> = mutableListOf()
	///RX

	var surfaceLayout: View? = null
	lateinit private var model: MapListViewModel<T>
	lateinit private var controller: MapListViewCtrl<T>

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.cont = context
		init()
		readAttrs(attrs)
	}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		this.cont = context
		init()
		readAttrs(attrs)
	}

	protected fun readAttrs(attrs: AttributeSet) {
		run {
			val ta = context.obtainStyledAttributes(attrs, R.styleable.ChangeMode, 0, 0)
			try {
				//if (ta.hasValue(R.styleable.ChangeMode_changeModeTextSize)) setText(ta.getString(R.styleable.ThreeDimensionalButton_text))
				if (ta.hasValue(R.styleable.ChangeMode_changeModeTextColor)) setChangeModeTextColor(ta.getColor(R.styleable.ChangeMode_changeModeTextColor, -1))
				if (ta.hasValue(R.styleable.ChangeMode_changeModeBackColor)) setChangeModeBackColor(ta.getColor(R.styleable.ChangeMode_changeModeBackColor, -1))
				//if (ta.hasValue(R.styleable.ChangeMode_changeModeListText)) setDrawableLeft(ta.getDrawable(R.styleable.ThreeDimensionalButton_drawableLeft))
				//if (ta.hasValue(R.styleable.ChangeMode_changeModeMapText)) setDrawableRight(ta.getDrawable(R.styleable.ThreeDimensionalButton_drawableRight))
				//if (ta.hasValue(R.styleable.ChangeMode_changeModeEnabled)) setDrawableTop(ta.getDrawable(R.styleable.ThreeDimensionalButton_drawableTop))
				//if (ta.hasValue(R.styleable.ChangeMode_changeModeVisible)) setChangeModeTextColor(ta.getColorStateList(R.styleable.ChangeMode_changeModeVisible))
			} finally {
				ta.recycle()
			}
		}
		run {
			val ta = context.obtainStyledAttributes(attrs, R.styleable.VerticalList, 0, 0)
			try {
				if (ta.hasValue(R.styleable.VerticalList_verticalListBackColor)) setVerticalListBackColor(ta.getColor(R.styleable.VerticalList_verticalListBackColor, -1))
			} finally {
				ta.recycle()
			}
		}
	}

	fun setChangeModeTextColor(colorStateList: ColorStateList?) {
		tvChangeMode?.setTextColor(colorStateList)
	}

	fun  setChangeModeTextColor(color: Int) {
		tvChangeMode?.setTextColor(color)
	}

	fun  setChangeModeBackColor(color: Int) {
		tvChangeMode?.setBackgroundColor(color)
	}

	fun  setVerticalListBackColor(color: Int) {
		recyclerViewVertical?.setBackgroundColor(color)
	}

	fun setClusteringEnabled(b: Boolean = true){
		controller.setClusteringEnabled(b)
	}

	fun setOnBeforeClusterRenderedListener(clusterRenderedListener: MyRenderer.onBeforeClusterRenderedListener){
		controller.setOnBeforeClusterRenderedListener(clusterRenderedListener)
	}

	fun setClusterMinSize(min: Int){
		controller.setClusterMinSize(min)
	}

	fun setCallback(mCallback: Callbacks<T>) {
		this.mCallback = mCallback
		model = mCallback.data
		controller = MapListViewCtrl(view = this, model = model,
				horizontalAdapter = mCallback.horizontalAdapter,
				verticalAdapter = mCallback.verticalAdapter)
		onModeChanged()
	}

	fun setMoveCameraOnMarkerFocusChange(b: Boolean) {
		controller.setMoveCameraOnMarkerFocusChange(b)
	}

	private fun init() {
		initialize()
		setListeners()
	}

	protected fun initialize() {
		val rootView = View.inflate(context, R.layout.view_maplist, this)
		initialize(rootView)
	}

	fun initialize(view: View) {
		surfaceLayout = view.findViewById(R.id.view_base_layout_surface)
		recyclerViewHorizontal = view.findViewById(R.id.recyclerview_horizontal)
		recyclerViewVertical = view.findViewById(R.id.recyclerview_vertical)
		tvChangeMode = view.findViewById(R.id.change_mode)
		vMap = view.findViewById(R.id.map)
	}

	fun update(){
		controller.saveData()
		controller.update()
	}

	fun onCreate(savedInstanceState: Bundle?){
		vMap?.onCreate(savedInstanceState)
	}

	fun onResume() {
		vMap?.onResume()
	}

	fun onStart() {
		vMap?.onResume()
	}

	fun onStop() {
		vMap?.onStop()
	}

	fun onPause() {
		vMap?.onPause()
	}

	fun onDestroy() {
		(0 until disposables.size)
				.map { disposables[it] }
				.forEach { it.dispose() }
		controller.onDestroy()
		vMap?.onDestroy()
	}

	fun populate() {
	}

	fun onEmpty() {
		tvChangeMode?.visibility = View.GONE
	}

	fun onContent() {
		tvChangeMode?.visibility = View.VISIBLE
	}

	private fun setListeners() {
		tvChangeMode?.setOnClickListener(View.OnClickListener { controller.changeMode() })
		RvScrollObs.create(recyclerViewHorizontal)
				//.subscribeOn(Schedulers.newThread())
				.observeOn(AndroidSchedulers.mainThread())
				.debounce(200, TimeUnit.MILLISECONDS)
				.subscribe(object: Observer<RvScrollObs.Result>{
					override fun onError(e: Throwable?) {
						Log.d(DEBUG_TAG +".onError", "e: " + e)
					}

					override fun onSubscribe(d: Disposable) {
						disposables.add(d)
					}

					override fun onComplete() {
						//Never gonna happen
					}

					override fun onNext(t: RvScrollObs.Result) {
						controller.onHorizontalScrolled(t.recyclerView, t.dx, t.dy)
					}
				})
	}

	fun onModeChanged(){
		when(model.displayMode){
			MapListViewModel.DisplayMode.MAP -> {
				tvChangeMode?.text = context.getString(R.string.See_on_list)
				recyclerViewHorizontal?.visibility = View.VISIBLE
				recyclerViewVertical?.visibility = View.GONE
				modeChangeListener?.onModeChanged(MapListViewModel.DisplayMode.MAP)
			}
			MapListViewModel.DisplayMode.LIST -> {
				tvChangeMode?.text = context.getString(R.string.See_on_map)
				recyclerViewVertical?.visibility = View.VISIBLE
				recyclerViewHorizontal?.visibility = View.GONE
				modeChangeListener?.onModeChanged(MapListViewModel.DisplayMode.LIST)
			}
		}
	}

	interface Callbacks<T: MapListElementModel> {
		val data: MapListViewModel<T>
		val horizontalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
		val verticalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
		fun onReady()
	}

	companion object {
		private val DEBUG_TAG = MapListView::class.java.simpleName
	}

	fun setMarkers(pin_selected: Int, pin_unselected: Int) {
		controller.setMarkers(pin_selected, pin_unselected)
	}

	fun moveMapTo(bounds: LatLngBounds, animate: Boolean = false): Boolean {
		return controller.moveMapTo(bounds, animate)
	}

	fun moveMapTo(latLng: LatLng, zoom: Float? = null, animate: Boolean = false): Boolean {
		return controller.moveMapTo(latLng, zoom, animate)
	}

	fun selectItem(item: T){
		return controller.selectItem(item)
	}

	fun adjustBoundsToPoints() {
		controller.adjustBoundsToPoints()
	}

	fun changeMode(mode: MapListViewModel.DisplayMode? = null) {
		controller.changeMode(mode)
	}

	@RequiresPermission(anyOf = arrayOf("android.permission.ACCESS_COARSE_LOCATION", "android.permission.ACCESS_FINE_LOCATION"))
	fun setMyLocationEnabled(enabled: Boolean, show: Boolean = true){
		controller.setMyLocationEnabled(enabled, show)
	}

	interface ModeChangedListener {
		fun onModeChanged(mode: MapListViewModel.DisplayMode)
	}
}