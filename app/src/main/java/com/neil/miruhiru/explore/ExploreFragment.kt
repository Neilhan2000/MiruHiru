package com.neil.miruhiru.explore

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.material.navigation.NavigationView
import com.mapbox.android.core.location.LocationEngine
//import com.mapbox.android.core.location.LocationEngineListener
//import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector

import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
//import com.mapbox.mapboxsdk.Mapbox
//import com.mapbox.mapboxsdk.annotations.IconFactory
//import com.mapbox.mapboxsdk.annotations.Marker
//import com.mapbox.mapboxsdk.annotations.MarkerOptions
//import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
//import com.mapbox.mapboxsdk.geometry.LatLng
//import com.mapbox.mapboxsdk.maps.MapView
//import com.mapbox.mapboxsdk.maps.MapboxMap
//import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin
//import com.mapbox.mapboxsdk.plugins.locationlayer.modes.CameraMode
//import com.mapbox.mapboxsdk.plugins.locationlayer.modes.RenderMode
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.expressions.dsl.generated.interpolate
import com.mapbox.maps.plugin.LocationPuck2D
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location


import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentExploreBinding


class ExploreFragment : Fragment(), PermissionsListener {


    private lateinit var permissionManager: PermissionsManager


    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(it).build())
        mapView?.gestures?.focalPoint = mapView?.getMapboxMap()?.pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
//            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }
    var mapView: MapView? = null
//    val arrivalObserver = object : ArrivalObserver()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentExploreBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        enableLocation()git 



        return binding.root
    }


    private fun onMapReady() {
        mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder()
                .zoom(14.0)
                .build()
        )
        mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }

    private fun setupGesturesListener() {
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView?.location
        locationComponentPlugin?.updateSettings {
            this.enabled = true
//            use this to custom location puck
//            this.locationPuck = LocationPuck2D(
//                bearingImage = this@ExploreFragment.context?.let {
//                    AppCompatResources.getDrawable(
//                        it,
//                        R.drawable.rsz_love_locatoin_icon,
//                    )
//                },
//                shadowImage = this@ExploreFragment.context?.let {
//                    AppCompatResources.getDrawable(
//                        it,
//                        R.drawable.rsz_love_locatoin_icon,
//                    )
//                },
//                scaleExpression = interpolate {
//                    linear()
//                    zoom()
//                    stop {
//                        literal(0.0)
//                        literal(0.6)
//                    }
//                    stop {
//                        literal(20.0)
//                        literal(1.0)
//                    }
//                }.toJson()
//            )
        }
        locationComponentPlugin?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        use this to add camera bearing
//        locationComponentPlugin?.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    private fun onCameraTrackingDismissed() {
        Toast.makeText(this.context, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView?.location
            ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.location
            ?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.location
            ?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView?.location
            ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this.context, "if you want to use map you need to agree permission", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocation()
        }
    }

    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
            onMapReady()
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this.activity)
        }
    }


}

// old version code
//class ExploreFragment : Fragment(), PermissionsListener, LocationEngineListener, MapboxMap.OnMapClickListener {
//
//    private lateinit var mapView: MapView
//    private lateinit var map: MapboxMap
//    private lateinit var permissionManager: PermissionsManager
//    private lateinit var originLocation: Location
//    private lateinit var originPosition: Point
//    private lateinit var destinationPosition: Point
//
//    private var locationEngine: LocationEngine? = null
//    private var locationLayerPlugin: LocationLayerPlugin? = null
//    private var destinationMarker: Marker? = null
//
//    //    val arrivalObserver = object : ArrivalObserver()
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        this.context?.let { Mapbox.getInstance(it, getString(R.string.mapbox_access_token)) }
//        val binding = FragmentExploreBinding.inflate(inflater, container, false)
//        mapView = binding.mapView
//        mapView.onCreate(savedInstanceState)
//        mapView.getMapAsync { mapboxMap ->
//            map = mapboxMap
//            enableLocation()
//            map.addOnMapClickListener { point ->
//                val icon = this.context?.let { IconFactory.getInstance(it).fromResource(R.drawable.rsz_love_locatoin_icon) }
//
//                destinationMarker = map.addMarker(
//                    MarkerOptions().setTitle("location").position(point).setIcon(icon))
//                destinationPosition = Point.fromLngLat(point.longitude, point.latitude)
//                originPosition = Point.fromLngLat(originLocation.longitude, originLocation.latitude)
//            }
//        }
//
//
//
//        return binding.root
//    }
//
//    private fun enableLocation() {
//        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
//            initializeLocationEngine()
//            initializeLocationLayer()
//        } else {
//            permissionManager = PermissionsManager(this)
//            permissionManager.requestLocationPermissions(this.activity)
//        }
//    }
//
//    @SuppressWarnings("MissingPermission")
//    private fun initializeLocationEngine() {
//        locationEngine = LocationEngineProvider(this.context).obtainBestLocationEngineAvailable()
//        locationEngine?.priority = LocationEnginePriority.HIGH_ACCURACY
//        locationEngine?.activate()
//
//        val lastLocation = locationEngine?.lastLocation
//        Log.i("neil", "location = ${lastLocation}")
//        if (lastLocation != null) {
//            originLocation = lastLocation
//            setCameraPosition(lastLocation)
//        } else {
//            locationEngine?.addLocationEngineListener(this)
//        }
//    }
//
//    @SuppressWarnings("MissingPermission")
//    private fun initializeLocationLayer() {
//        locationLayerPlugin = LocationLayerPlugin(mapView, map, locationEngine)
//        locationLayerPlugin?.isLocationLayerEnabled = true
//        locationLayerPlugin?.cameraMode = CameraMode.TRACKING
//        locationLayerPlugin?.renderMode = RenderMode.NORMAL
//    }
//
//    private fun setCameraPosition(location: Location) {
//        map.animateCamera(CameraUpdateFactory.newLatLngZoom(
//            LatLng(location.latitude, location.longitude), 15.0))
//    }
//
//    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
//        Toast.makeText(this.context, "if you want to use map you need to agree permission", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun onPermissionResult(granted: Boolean) {
//        if (granted) {
//            enableLocation()
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        permissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//
//    @SuppressWarnings("MissingPermission")
//    override fun onConnected() {
//        locationEngine?.requestLocationUpdates()
//    }
//
//    override fun onLocationChanged(location: Location?) {
//        location?.let {
//            originLocation = location
//            setCameraPosition(location)
//        }
//    }
//
//    @SuppressWarnings("MissingPermission")
//    override fun onStart() {
//        super.onStart()
//        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
//            locationEngine?.requestLocationUpdates()
//            locationLayerPlugin?.onStart()
//        }
//        mapView.onStart()
//    }
//
//    override fun onResume() {
//        super.onResume()
//        mapView.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//        mapView.onPause()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        locationEngine?.removeLocationUpdates()
//        locationLayerPlugin?.onStop()
//        mapView?.onStop()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mapView?.onDestroy()
//        locationEngine?.deactivate()
//    }
//
//    override fun onLowMemory() {
//        super.onLowMemory()
//        mapView?.onLowMemory()
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        if (outState != null) {
//            mapView.onSaveInstanceState(outState)
//        }
//    }
//
//    override fun onMapClick(point: LatLng) {
//        destinationMarker = map.addMarker(MarkerOptions().position(point))
//        destinationPosition = Point.fromLngLat(point.longitude, point.latitude)
//        originPosition = Point.fromLngLat(originLocation.longitude, originLocation.latitude)
//    }
//
//
//}