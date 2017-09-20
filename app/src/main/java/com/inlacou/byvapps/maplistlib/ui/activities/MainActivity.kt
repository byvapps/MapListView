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
import android.widget.Toast
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.Cluster
import com.inlacou.byvapps.maplistlib.R
import com.inlacou.byvapps.maplistlib.adapter.ExampleRvAdapter
import com.inlacou.byvapps.maplistlib.business.ExampleItem
import com.inlacou.byvapps.maplistlib.ui.views.ExampleItemViewModel
import kotlinx.android.synthetic.main.activity_main.*
import com.google.maps.android.ui.IconGenerator
import com.hulab.debugkit.DebugFunction
import com.hulab.debugkit.DevTool
import com.hulab.debugkit.DevToolFragment
import com.inlacou.byvapps.maplist.MapListView
import com.inlacou.byvapps.maplist.MapListViewModel
import com.inlacou.byvapps.maplist.MapUtils
import com.inlacou.byvapps.maplist.clustering.MyRenderer
import com.inlacou.byvapps.maplist.clustering.SelectableMarker
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

	companion object {
		final val DEBUG_TAG = MainActivity.javaClass.simpleName
	}

	private lateinit var mapList: MapListView<ExampleItem>

	private var items: MutableList<ExampleItem> = mutableListOf()
	private var itemList: MutableList<ExampleItemViewModel> = mutableListOf()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		setSupportActionBar(toolbar)

		//MapList
		val initialPosition = MapUtils.toBounds(LatLng(0.0,0.0), 100.0)
		//0
		/*
		items.add(ExampleItem(43.2480085,-2.9222963))
		items.add(ExampleItem(43.2440709,-2.9431234))
		items.add(ExampleItem(43.3467236,-3.0121161))
		items.add(ExampleItem(43.2459541,-2.9321773))

		items.add(ExampleItem(43.3680085,-2.8222963))
		items.add(ExampleItem(43.3640709,-2.8431234))
		items.add(ExampleItem(43.4667236,-2.9121161))
		items.add(ExampleItem(43.3659541,-2.8321773))

		items.add(ExampleItem(43.1680085,-3.0222963))
		items.add(ExampleItem(43.1640709,-3.0431234))
		items.add(ExampleItem(43.1667236,-3.1121161))
		items.add(ExampleItem(43.1659541,-3.0321773))

		items.add(ExampleItem(43.2880085,-2.7222963))
		items.add(ExampleItem(43.2840709,-2.7431234))
		items.add(ExampleItem(43.3867236,-2.8121161))
		items.add(ExampleItem(43.2859541,-2.7321773))

		items.add(ExampleItem(43.2480085,-3.0222963))
		items.add(ExampleItem(43.2440709,-3.0431234))
		items.add(ExampleItem(43.3467236,-3.1121161))
		items.add(ExampleItem(43.2459541,-3.0321773))
		*/

		val mapModel = MapListViewModel(itemList = items)

		mapList = findViewById<MapListView<ExampleItem>>(R.id.maplist)

		val callback = object : ExampleRvAdapter.Callbacks {
			override fun onItemClick(item: ExampleItemViewModel, orientation: ExampleRvAdapter.Orientation) {
				if(orientation==ExampleRvAdapter.Orientation.VERTICAL){
					mapList.changeMode(MapListViewModel.DisplayMode.MAP)
					mapList.selectItem(item.item)
					Observable.timer(1000, TimeUnit.MILLISECONDS)
							.observeOn(AndroidSchedulers.mainThread())
							.subscribe {
								Toast.makeText(this@MainActivity, "Open detail view", Toast.LENGTH_SHORT).show()
							}
				}else{
					Toast.makeText(this@MainActivity, "Open detail view", Toast.LENGTH_SHORT).show()
				}
			}
		}

		val adapterHorizontal = ExampleRvAdapter(this, itemList = itemList,
				orientation = ExampleRvAdapter.Orientation.HORIZONTAL, callbacks = callback)
		val adapterVertical = ExampleRvAdapter(this, itemList = itemList,
				orientation = ExampleRvAdapter.Orientation.VERTICAL, callbacks = callback)

		mapList.setCallback(object: MapListView.Callbacks<ExampleItem>{
			override val data: MapListViewModel<ExampleItem>
				get() = mapModel
			override val horizontalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
				get() = adapterHorizontal
			override val verticalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
				get() = adapterVertical
			override fun onReady() {
				mapList.setClusteringEnabled(true)
				mapList.setClusterMinSize(5)
				mapList.setMoveCameraOnMarkerFocusChange(true)
				mapList.setOnBeforeClusterRenderedListener(object: MyRenderer.onBeforeClusterRenderedListener{
					override fun onBeforeClusterRendered(cluster: Cluster<SelectableMarker>, markerOptions: MarkerOptions): Boolean {
						val icon = IconGenerator(applicationContext).makeIcon(cluster.size.toString())
						markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
						return true
					}
				})
				mapList.setMyLocationEnabled(true, true)
			}
		})

		mapList.onCreate(savedInstanceState)
		///MapList

		fab.setOnClickListener { view ->
			Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
					.setAction("Action", null).show()
		}

		val toggle = ActionBarDrawerToggle(
				this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
		drawer_layout.addDrawerListener(toggle)
		toggle.syncState()

		nav_view.setNavigationItemSelectedListener(this)

		Observable.timer(5000, TimeUnit.MILLISECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe({ addItems() })

		debugKit()
	}

	private fun debugKit() {
		val builder = DevTool.Builder(this)

		builder.addFunction(object : DebugFunction() {
			@Throws(Exception::class)
			override fun call(): String {
				log("do nothing")
				return "did nothing"
			}
		})

		// optional, DevToolFragment.DevToolTheme.DARK is set by default
		builder.setTheme(DevToolFragment.DevToolTheme.DARK)
				.build()
	}

	private fun addItems() {
		items.add(ExampleItem(43.2680085,-2.9222963, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.2640709,-2.9431234, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.3667236,-3.0121161, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.2659541,-2.9321773, R.drawable.pin, R.drawable.pin_farmacia_selected))
		//4
		items.add(ExampleItem(43.3680085,-2.9222963, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.3640709,-2.9431234, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.4667236,-3.0121161, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.3659541,-2.9321773, R.drawable.pin, R.drawable.pin_farmacia_selected))
		//8
		items.add(ExampleItem(43.1680085,-2.9222963, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.1640709,-2.9431234, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.1667236,-3.0121161, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.1659541,-2.9321773, R.drawable.pin, R.drawable.pin_farmacia_selected))
		//12
		items.add(ExampleItem(43.2880085,-2.9222963, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.2840709,-2.9431234, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.3867236,-3.0121161, R.drawable.pin, R.drawable.pin_farmacia_selected))
		items.add(ExampleItem(43.2859541,-2.9321773, R.drawable.pin, R.drawable.pin_farmacia_selected))

		(0 until items.size)
				.map { items[it] }
				.forEach { itemList.add(ExampleItemViewModel(it)) }


		mapList.modeChangeListener = object: MapListView.ModeChangedListener{
			override fun onModeChanged(mode: MapListViewModel.DisplayMode) {
				Log.d(DEBUG_TAG+".onModeChanged", "Mode changed to $mode")
			}
		}
		mapList.update()
		mapList.adjustBoundsToPoints()
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
