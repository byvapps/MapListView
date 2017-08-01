package com.inlacou.byvapps.maplistlib.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import com.inlacou.byvapps.maplistlib.R

/**
 * Created by inlacou on 01/08/17.
 */
class ExampleItemView : FrameLayout {
	private var mCallback: Callbacks? = null
	private var cont: Context? = null

	var surfaceLayout: View? = null
	lateinit private var model: ExampleItemViewModel
	lateinit private var controller: ExampleItemViewCtrl

	constructor(context: Context) : super(context) {
		this.cont = context
		init()
	}

	constructor(context: Context, callbacks: Callbacks) : super(context) {
		this.cont = context
		mCallback = callbacks
		init()
	}

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.cont = context
		init()
	}

	constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		this.cont = context
		init()
	}

	fun setCallback(mCallback: Callbacks) {
		this.mCallback = mCallback
	}

	private fun init() {
		getData()
		initialize()
		setListeners()
	}

	fun getData() {
		if (mCallback != null) {
			model = mCallback!!.data
			controller = ExampleItemViewCtrl(view = this, model = model)
		}
	}

	fun initialize(view: View) {
		surfaceLayout = view.findViewById(R.id.view_base_layout_surface)
	}

	protected fun initialize() {
		val rootView = View.inflate(context, R.layout.view_example_item, this)
		initialize(rootView)
	}

	fun populate() {

	}

	private fun setListeners() {
		surfaceLayout!!.setOnClickListener { mCallback!!.onSurfaceClick(model) }
	}

	fun inAnimation() {
		val animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_in)
		surfaceLayout!!.startAnimation(animation)
	}

	fun outAnimation() {
		val animation = AnimationUtils.loadAnimation(context, android.R.anim.fade_out)
		surfaceLayout!!.startAnimation(animation)
		animation.setAnimationListener(object : Animation.AnimationListener {
			override fun onAnimationStart(animation: Animation) {

			}

			override fun onAnimationEnd(animation: Animation) {
				mCallback!!.onDelete(model)
			}

			override fun onAnimationRepeat(animation: Animation) {

			}
		})
	}

	interface Callbacks {
		fun onSurfaceClick(item: ExampleItemViewModel)
		fun onDelete(item: ExampleItemViewModel)
		val data: ExampleItemViewModel
	}

	companion object {
		private val DEBUG_TAG = ExampleItemView::class.java.simpleName
	}

}