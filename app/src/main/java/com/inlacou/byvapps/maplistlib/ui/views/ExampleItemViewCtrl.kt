package com.inlacou.byvapps.maplistlib.ui.views


/**
 * Created by inlacou on 01/08/17.
 */
class ExampleItemViewCtrl {

	private var view: ExampleItemView
	private var model: ExampleItemViewModel

	companion object {
		private val DEBUG_TAG = ExampleItemViewCtrl::class.java.simpleName
	}

	constructor(view: ExampleItemView, model: ExampleItemViewModel) {
		this.view = view
		this.model = model
	}
}