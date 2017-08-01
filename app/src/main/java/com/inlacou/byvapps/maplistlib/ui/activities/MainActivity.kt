package com.inlacou.byvapps.maplistlib.ui.activities

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.inlacou.byvapps.galdakao.general.common.MapUtils
import com.inlacou.byvapps.galdakao.ui.views.common.maplist.MapListView
import com.inlacou.byvapps.galdakao.ui.views.common.maplist.MapListViewModel
import com.inlacou.byvapps.maplistlib.R
import com.inlacou.byvapps.maplistlib.adapter.EnterpriseRvAdapter
import com.inlacou.byvapps.maplistlib.business.ExampleItem
import com.inlacou.byvapps.maplistlib.ui.views.ExampleItemViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

	companion object {
		final val DEBUG_TAG = MainActivity.javaClass.simpleName
	}

	private lateinit var mapList: MapListView<ExampleItem>

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(toolbar)

		val initialPosition = MapUtils.toBounds(LatLng(0.0,0.0), 100.0)
		val items = mutableListOf<ExampleItem>()
		items.add(ExampleItem(43.2680085,-2.9222963))
		items.add(ExampleItem(43.2640709,-2.9431234))
		items.add(ExampleItem(43.3667236,-3.0121161))
		items.add(ExampleItem(43.2659541,-2.9321773))

		val mapModel = MapListViewModel(itemList = items)

		mapList = findViewById<MapListView<ExampleItem>>(R.id.maplist)

		val itemList = mutableListOf<ExampleItemViewModel>()
		(0..items.size-1)
				.map { items[it] }
				.forEach { itemList.add(ExampleItemViewModel(it)) }

		val callback = object : EnterpriseRvAdapter.Callbacks {
			override fun onItemClick(item: ExampleItemViewModel) {
				Log.d(DEBUG_TAG, "Not implemented") //TODO not implemented
			}

			override fun onItemDeleted(item: ExampleItemViewModel) {
				Log.d(DEBUG_TAG, "Not implemented") //TODO not implemented
			}
		}

		val adapterHorizontal = EnterpriseRvAdapter(this, itemList = itemList,
				orientation = EnterpriseRvAdapter.Orientation.HORIZONTAL, callbacks = callback)
		val adapterVertical = EnterpriseRvAdapter(this, itemList = itemList,
				orientation = EnterpriseRvAdapter.Orientation.VERTICAL, callbacks = callback)

		mapList.setCallback(object: MapListView.Callbacks<ExampleItem>{
			override val data: MapListViewModel<ExampleItem>
				get() = mapModel
			override val horizontalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
				get() = adapterHorizontal
			override val verticalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
				get() = adapterVertical
			override val defaultLocation: LatLngBounds
				get() = initialPosition
		})
		mapList.getData()

		fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
					.setAction("Action", null).show()
		}

		val toggle = ActionBarDrawerToggle(
				this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
		drawer_layout.addDrawerListener(toggle)
		toggle.syncState()

		nav_view.setNavigationItemSelectedListener(this)

		mapList.onCreate(savedInstanceState)
	}

	override fun onPause() {
		super.onPause()
		mapList.onPause()
	}

	override fun onResume() {
		super.onResume()
		mapList.onResume()
	}

	override fun onStart() {
		super.onStart()
		mapList.onStart()
	}

	override fun onStop() {
		super.onStop()
		mapList.onStop()
	}

	override fun onBackPressed() {
		if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
			drawer_layout.closeDrawer(GravityCompat.START)
		} else {
			super.onBackPressed()
		}
	}

	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		// Inflate the menu; this adds items to the action bar if it is present.
		menuInflater.inflate(R.menu.main, menu)
		return true
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		when (item.itemId) {
			R.id.action_settings -> return true
			else -> return super.onOptionsItemSelected(item)
		}
	}

	override fun onNavigationItemSelected(item: MenuItem): Boolean {
		// Handle navigation view item clicks here.
		when (item.itemId) {
			R.id.nav_camera -> {
				// Handle the camera action
			}
			R.id.nav_gallery -> {

			}
			R.id.nav_slideshow -> {

			}
			R.id.nav_manage -> {

			}
			R.id.nav_share -> {

			}
			R.id.nav_send -> {

			}
		}

		drawer_layout.closeDrawer(GravityCompat.START)
		return true
	}
}