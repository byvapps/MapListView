# MapListView

Add it to your layout:
```xml
<com.inlacou.byvapps.galdakao.ui.views.common.maplist.MapListView
			android:id="@+id/maplist"
			android:layout_width="match_parent"
			android:layout_height="match_parent"
			app:layout_behavior="@string/appbar_scrolling_view_behavior" //if inside CoordinatorLayout
			style="@style/MapTheme1"/>
```

Style it:
```xml
<!--Set general Map Theme-->
	<style name="MapTheme1">
		<item name="verticalListBackColor">@color/colorPrimary</item>
		<!--This styles slightly the change mode button, and has priority over complete theme customization-->
		<!--<item name="changeModeBackColor">@color/colorPrimaryDark</item>-->
		<!--<item name="changeModeTextColor">@color/colorAccent</item>-->
	</style>

	<!--This completely styles the change mode button-->
	<style name="mapListChangeModeButtonTheme">
		<item name="android:textColor">@color/red</item>
		<item name="android:textStyle">bold</item>
		<item name="android:textSize">14sp</item>
		<item name="android:padding">@dimen/general_all_half</item>
		<item name="android:background">@color/white</item>
		<item name="android:gravity">center_horizontal</item>
	</style>
```
Initialize and configure it:
``` kotlin
 override fun onCreate(savedInstanceState: Bundle?) {
    
    //Get MapList through butterKnife or old school.
		mapList = findViewById<MapListView<ExampleItem>>(R.id.maplist)
 
		//This list will hold your items
    val items = mutableListOf<ExampleItem>()
    //Add items to list
    //...something
    //Create MapList model
    val mapModel = MapListViewModel(itemList = items)

    ///Configure your adapters for vertical and horizontal lists
		//List for the adapters
    val itemList = mutableListOf<ExampleItemViewModel>()
		(0..items.size-1)
				.map { items[it] }
				.forEach { itemList.add(ExampleItemViewModel(it)) }
    //Callback for adapters' items
		val callback = object : EnterpriseRvAdapter.Callbacks {
			override fun onItemClick(item: ExampleItemViewModel) {
				Log.d(DEBUG_TAG, "Not implemented") //TODO not implemented
			}

			override fun onItemDeleted(item: ExampleItemViewModel) {
				Log.d(DEBUG_TAG, "Not implemented") //TODO not implemented
			}
		}
    
    //Finally create the adapters
		val adapterHorizontal = EnterpriseRvAdapter(this, itemList = itemList,
				orientation = EnterpriseRvAdapter.Orientation.HORIZONTAL, callbacks = callback)
		val adapterVertical = EnterpriseRvAdapter(this, itemList = itemList,
				orientation = EnterpriseRvAdapter.Orientation.VERTICAL, callbacks = callback)

    //Finally configure the MapList
		mapList.setCallback(object: MapListView.Callbacks<ExampleItem>{
			override val data: MapListViewModel<ExampleItem> //Give him the model created before
				get() = mapModel
			override val horizontalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> //Horizontal adapter
				get() = adapterHorizontal
			override val verticalAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder> //Vertical adapter
				get() = adapterVertical
			override fun onReady() { //Some other configurations
				mapList.setClusteringEnabled(true) //Do clusters even appear?
				mapList.setClusterMinSize(5) //When do start clusters appearing
				mapList.setMoveCameraOnMarkerFocusChange(false) //Move camera position on marker focus change: click marker or move horizontal list
				mapList.setOnBeforeClusterRenderedListener(object: MyRenderer.onBeforeClusterRenderedListener{ //Redefine cluster icons
					override fun onBeforeClusterRendered(cluster: Cluster<SelectableMarker>, markerOptions: MarkerOptions): Boolean {
						val icon = IconGenerator(applicationContext).makeIcon(cluster.size.toString())
						markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon))
						return true
					}
				})
			}
		})
    
		mapList.onCreate(savedInstanceState) //Google Maps things
}

override fun onPause() { //Google Maps things
    super.onPause()
    mapList.onPause()
}

override fun onResume() { //Google Maps things
    super.onResume()
    mapList.onResume()
}

override fun onStart() { //Google Maps things
    super.onStart()
    mapList.onStart()
}

override fun onStop() { //Google Maps things
    super.onStop()
    mapList.onStop()
}
```
Oh, and dont forget to add your Google Maps API key!
```xml
<string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">YOUR_KEY_HERE</string>
```










