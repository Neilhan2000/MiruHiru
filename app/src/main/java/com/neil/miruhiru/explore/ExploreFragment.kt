package com.neil.miruhiru.explore

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.google.gson.*
//import com.mapbox.android.core.location.LocationEngineListener
//import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.api.directions.v5.models.Bearing
import com.mapbox.api.directions.v5.models.RouteOptions

import com.mapbox.geojson.Point
import com.mapbox.maps.*
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
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.extensions.applyDefaultNavigationOptions
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.base.route.*
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.trip.session.RouteProgressObserver
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineApi
import com.mapbox.navigation.ui.maps.route.line.api.MapboxRouteLineView
import com.mapbox.navigation.ui.maps.route.line.model.MapboxRouteLineOptions
import com.neil.miruhiru.BuildConfig
import com.neil.miruhiru.NavGraphDirections


import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.LocationInfo
import com.neil.miruhiru.databinding.FragmentExploreBinding
import kotlin.math.roundToInt


class ExploreFragment : Fragment(), PermissionsListener {

    private val viewModel: ExploreViewModel by lazy {
        ViewModelProvider(this).get(ExploreViewModel::class.java)
    }

    private lateinit var permissionManager: PermissionsManager
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var binding: FragmentExploreBinding

    private var mapView: MapView? = null
    private var mapBoxMap: MapboxMap? = null
    private var routeLineApi: MapboxRouteLineApi? = null
    private var routeLineView: MapboxRouteLineView? = null
//    private var mapboxNavigation: MapboxNavigation? = null
    var stage: Int = 1

    // listeners and observers
    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(it).build())
        mapView?.gestures?.focalPoint = mapView?.getMapboxMap()?.pixelForCoordinate(it)
    }

    private val onMoveListener = object : OnMoveListener {
        override fun onMoveBegin(detector: MoveGestureDetector) {
            onCameraTrackingDismissed()
        }

        override fun onMove(detector: MoveGestureDetector): Boolean {
            return false
        }

        override fun onMoveEnd(detector: MoveGestureDetector) {}
    }

    // route progress observer
    val routeProgressObserver = object : RouteProgressObserver {
        override fun onRouteProgressChanged(routeProgress: RouteProgress) {
            routeProgress.currentState?.let { currentState ->
                Log.i("neil", "fetch route state = $currentState")
            }
        }
    }
    // arrival observer
    val arrivalObserver = object : ArrivalObserver {

        override fun onWaypointArrival(routeProgress: RouteProgress) {
            Toast.makeText(this@ExploreFragment.context, "arrive waypoint", Toast.LENGTH_SHORT).show()
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            Toast.makeText(this@ExploreFragment.context, "go to next waypoint", Toast.LENGTH_SHORT).show()
        }

        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            Toast.makeText(this@ExploreFragment.context, "arrive destination", Toast.LENGTH_SHORT).show()
            Log.i("neil", "stage $stage completed")
            Toast.makeText(this@ExploreFragment.context, "stage ${stage++} completed", Toast.LENGTH_SHORT).show()
//            mapboxNavigation?.unregisterRouteProgressObserver(routeProgressObserver)
            val secondWaypoint = Point.fromLngLat(121.532658603193, 24.038928111026806)

            startNavigation(secondWaypoint)


//            removeMarkerFromMap(point1)
//            removeMarkerFromMap(point2)
//            removeMarkerFromMap(point3)
            // can't remove annotation, try old version annotation plugin
            for (annotation in pointAnnotationManager.annotations) {
                Log.i("neil", "new annotation id = ${annotation}")
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentExploreBinding.inflate(inflater, container, false)

        mapView = binding.mapView
        mapBoxMap = mapView?.getMapboxMap()
        enableLocation()
        viewModel.challengeList

//        mapboxNavigation =
//            if (MapboxNavigationProvider.isCreated()) {
//                MapboxNavigationProvider.retrieve()
//            } else {
//                this.context?.let { it1 ->
//                    NavigationOptions.Builder(it1)
//                        .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
//                        .build()
//                }?.let { it2 ->
//                    MapboxNavigationProvider.create(
//                        it2
//                    )
//                }
//            }
        val routeLineOptions = this.context?.let { it1 -> MapboxRouteLineOptions.Builder(it1).build() }
        routeLineApi = routeLineOptions?.let { it1 -> MapboxRouteLineApi(it1) }
        routeLineView = routeLineOptions?.let { it1 -> MapboxRouteLineView(it1) }

        binding.button.setOnClickListener {
//            val destination = Point.fromLngLat(121.53234019230837, 25.03844419092961)
//            startNavigation(destination)
        }

        binding.myLocationIcon.setOnClickListener {
            initLocationComponent()
            setupGesturesListener()
        }

        viewModel.challengeList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it.forEach { challenge ->
                addAnnotationToMap(challenge)
            }
        })


        return binding.root
    }

    // set up map
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

        // add map click listener, it will cause conflict with onAnnotationClickListener
        mapBoxMap?.addOnMapClickListener { point ->
            Log.i("neil", "location = ${point.latitude()}, ${point.longitude()}")
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
            binding.locationCardView.animation = animation
            animation.start()
            binding.locationCardView.visibility = View.GONE
            false
        }


    }

    private fun determineChallengeIcon(type: String): Int {
        return when (type) {
            getString(R.string.food) -> R.drawable.ic_food_location
            getString(R.string.couple) -> R.drawable.ic_love_location
            else -> R.drawable.anya_icon2
        }
    }

    // add annotation
    private fun addAnnotationToMap(challenge: Challenge) {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            this.requireContext(),
            determineChallengeIcon(challenge.type!!)
        )?.let {
            val annotationApi = mapView?.annotations
            pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)!!
            // this data will be store in annotation
//            val locationInfo = LocationInfo(challenge.name!!, challenge.location!!, challenge.stage!!,
//            challenge.totalRating!!, challenge.timeSpent!!, challenge.image!!)
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(
                    challenge.location?.longitude!!,
                    challenge.location?.latitude!!
                ))
                .withIconImage(it)
                .withIconSize(2.0)
                // store data to annotation
//                .withData(JsonParser.parseString(Gson().toJson(locationInfo)))
            // add annotation click listener.
            pointAnnotationManager?.addClickListener(object : OnPointAnnotationClickListener {
                override fun onAnnotationClick(annotation: PointAnnotation): Boolean {

                    val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                    binding.locationCardView.animation = animation
                    animation.start()
                    // get data from annotation
//                    Toast.makeText(requireContext(), "${Gson().fromJson(annotation.getData()?.asJsonObject, LocationInfo::class.java)}", Toast.LENGTH_SHORT).show()
                    binding.challengeTitle.text = challenge.name
                    binding.challengeRating.text = "${challenge.totalRating?.let { it1 ->
                        viewModel.roundOffDecimal(
                            it1
                        )
                    }} (${challenge.commentQuantity})"
                    binding.challengeStage.text = challenge.stage.toString()
                    binding.challengeTime.text = "${challenge.timeSpent?.div(3600)} Hrs"
                    Glide.with(binding.challengeImage.context).load(challenge.image).centerCrop().apply(
                        RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
                    ).into(binding.challengeImage)
                    // this function will show distance the between your location and challenge in the cardView
                    challenge.location?.let { calculateAndShowDistance(it) }
                    binding.locationCardView.setOnClickListener {
                        this@ExploreFragment.findNavController().navigate(NavGraphDirections.actionGlobalChallengeDetailFragment(challenge.id ?: "null"))
                    }
                    binding.locationCardView.visibility = View.VISIBLE
                    return false
                }
            })
            // Add the resulting pointAnnotation to the map.
            pointAnnotationManager?.create(pointAnnotationOptions)
        }
    }
    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            // copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    // delete marker
    private fun removeMarkerFromMap(point: Point) {

        val pointAnnotationOptions: PointAnnotationOptions =
            PointAnnotationOptions().withPoint(point)

        var annotationID : Long = -1
        pointAnnotationManager.annotations.forEach {
            if (it.point == point) {
                annotationID = it.id
            }
        }

        pointAnnotationManager
            .update(pointAnnotationOptions
                .build(
                    annotationID,
                    pointAnnotationManager
                )
            )
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
//        Toast.makeText(this.context, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView?.location
            ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.location
            ?.removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
//        mapboxNavigation?.registerArrivalObserver(arrivalObserver)
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
//        mapboxNavigation?.unregisterArrivalObserver(arrivalObserver)
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

        routeLineApi?.cancel()
        routeLineView?.cancel()

//        mapboxNavigation?.unregisterRouteProgressObserver(routeProgressObserver)

    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(this.context, "if you want to use map you need to agree permission", Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocation()
        }
    }

    // check permission and set up map
    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
            onMapReady()
        } else {
            permissionManager = PermissionsManager(this)
            permissionManager.requestLocationPermissions(this.activity)
            onMapReady()
        }
    }

    // navigation
    private fun startNavigation(destination: Point) {
        val originLocation = Location("test").apply {
            longitude = 121.53231116772025
            latitude = 25.038436316242397
            bearing = 10f
        }
        val originPoint = Point.fromLngLat(
            originLocation.longitude,
            originLocation.latitude
        )

//        mapboxNavigation?.requestRoutes(
//            RouteOptions.builder()
//                .applyDefaultNavigationOptions()
//                .coordinatesList(listOf(originPoint, destination))
//                .bearingsList(
//                    listOf(
//                        Bearing.builder()
//                            .angle(originLocation.bearing.toDouble())
//                            .degrees(45.0)
//                            .build(),
//                        null
//                    )
//                )
//                .build(),
//            object : NavigationRouterCallback {
//                override fun onCanceled(
//                    routeOptions: RouteOptions,
//                    routerOrigin: RouterOrigin
//                ) {
//                    Log.i("neil", "fetch route cancel")
//                }
//
//                override fun onFailure(
//                    reasons: List<RouterFailure>,
//                    routeOptions: RouteOptions
//                ) {
//                    Log.i("neil", "fetch route failure $reasons")
//                }
//
//                @SuppressLint("MissingPermission")
//                override fun onRoutesReady(
//                    routes: List<NavigationRoute>,
//                    routerOrigin: RouterOrigin
//                ) {
//                    Log.i("neil", "fetch route ready")
//                    routeLineApi?.setNavigationRoutes(routes) { value ->
//                        routeLineView?.renderRouteDrawData(mapBoxMap?.getStyle()!!, value)
//                    }
//                    mapboxNavigation!!.setNavigationRoutes(routes)
//                    mapboxNavigation!!.registerRouteProgressObserver(routeProgressObserver)
//                    mapboxNavigation!!.startTripSession(withForegroundService = false)
//
//                }
//            }
//        )
    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation(): Point? {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireContext())
        var currentPoint: Point? = null
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.i("neil", "current location = ${location?.latitude}, ${location?.longitude}")
                currentPoint = Point.fromLngLat(
                    location!!.longitude,
                    location!!.latitude
                )
            }
        return currentPoint
    }

    @SuppressLint("MissingPermission")
    private fun calculateAndShowDistance(destination: GeoPoint) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireContext())
        var currentPoint: Point? = null
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                Log.i("neil", "current location = ${location?.latitude}, ${location?.longitude}")
                currentPoint = Point.fromLngLat(
                    location!!.longitude,
                    location!!.latitude
                )
                val distance = viewModel.calculateDistance(currentPoint, destination)
                binding.challengeDistance.text = "${distance.roundToInt()} Ms"
            }
    }


//    override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
//        binding.locationCardView.visibility = View.VISIBLE
//        return true
//    }

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

// navigation sample code
//{
//
//    private companion object {
//        private const val BUTTON_ANIMATION_DURATION = 1500L
//    }
//
//    /**
//     * Debug tool used to play, pause and seek route progress events that can be used to produce mocked location updates along the route.
//     */
//    private val mapboxReplayer = MapboxReplayer()
//
//    /**
//     * Debug tool that mocks location updates with an input from the [mapboxReplayer].
//     */
//    private val replayLocationEngine = ReplayLocationEngine(mapboxReplayer)
//
//    /**
//     * Debug observer that makes sure the replayer has always an up-to-date information to generate mock updates.
//     */
//    private val replayProgressObserver = ReplayProgressObserver(mapboxReplayer)
//
//    /**
//     * Bindings to the example layout.
//     */
//    private lateinit var binding: FragmentExploreBinding
//
//    /**
//     * Mapbox Maps entry point obtained from the [MapView].
//     * You need to get a new reference to this object whenever the [MapView] is recreated.
//     */
//    private lateinit var mapboxMap: MapboxMap
//
//    /**
//     * Mapbox Navigation entry point. There should only be one instance of this object for the app.
//     * You can use [MapboxNavigationProvider] to help create and obtain that instance.
//     */
//    private lateinit var mapboxNavigation: MapboxNavigation
//
//    /**
//     * Used to execute camera transitions based on the data generated by the [viewportDataSource].
//     * This includes transitions from route overview to route following and continuously updating the camera as the location changes.
//     */
//    private lateinit var navigationCamera: NavigationCamera
//
//    /**
//     * Produces the camera frames based on the location and routing data for the [navigationCamera] to execute.
//     */
//    private lateinit var viewportDataSource: MapboxNavigationViewportDataSource
//
//    /*
//    * Below are generated camera padding values to ensure that the route fits well on screen while
//    * other elements are overlaid on top of the map (including instruction view, buttons, etc.)
//    */
//    private val pixelDensity = Resources.getSystem().displayMetrics.density
//    private val overviewPadding: EdgeInsets by lazy {
//        EdgeInsets(
//            140.0 * pixelDensity,
//            40.0 * pixelDensity,
//            120.0 * pixelDensity,
//            40.0 * pixelDensity
//        )
//    }
//    private val landscapeOverviewPadding: EdgeInsets by lazy {
//        EdgeInsets(
//            30.0 * pixelDensity,
//            380.0 * pixelDensity,
//            110.0 * pixelDensity,
//            20.0 * pixelDensity
//        )
//    }
//    private val followingPadding: EdgeInsets by lazy {
//        EdgeInsets(
//            180.0 * pixelDensity,
//            40.0 * pixelDensity,
//            150.0 * pixelDensity,
//            40.0 * pixelDensity
//        )
//    }
//    private val landscapeFollowingPadding: EdgeInsets by lazy {
//        EdgeInsets(
//            30.0 * pixelDensity,
//            380.0 * pixelDensity,
//            110.0 * pixelDensity,
//            40.0 * pixelDensity
//        )
//    }
//
//    /**
//     * Generates updates for the [MapboxManeuverView] to display the upcoming maneuver instructions
//     * and remaining distance to the maneuver point.
//     */
//    private lateinit var maneuverApi: MapboxManeuverApi
//
//    /**
//     * Generates updates for the [MapboxTripProgressView] that include remaining time and distance to the destination.
//     */
//    private lateinit var tripProgressApi: MapboxTripProgressApi
//
//    /**
//     * Generates updates for the [routeLineView] with the geometries and properties of the routes that should be drawn on the map.
//     */
//    private lateinit var routeLineApi: MapboxRouteLineApi
//
//    /**
//     * Draws route lines on the map based on the data from the [routeLineApi]
//     */
//    private lateinit var routeLineView: MapboxRouteLineView
//
//    /**
//     * Generates updates for the [routeArrowView] with the geometries and properties of maneuver arrows that should be drawn on the map.
//     */
//    private val routeArrowApi: MapboxRouteArrowApi = MapboxRouteArrowApi()
//
//    /**
//     * Draws maneuver arrows on the map based on the data [routeArrowApi].
//     */
//    private lateinit var routeArrowView: MapboxRouteArrowView
//
//
//    private val navigationLocationProvider = NavigationLocationProvider()
//
//    /**
//     * Gets notified with location updates.
//     *
//     * Exposes raw updates coming directly from the location services
//     * and the updates enhanced by the Navigation SDK (cleaned up and matched to the road).
//     */
//    private val locationObserver = object : LocationObserver {
//        var firstLocationUpdateReceived = false
//
//        override fun onNewRawLocation(rawLocation: Location) {
//// not handled
//        }
//
//        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
//            val enhancedLocation = locationMatcherResult.enhancedLocation
//// update location puck's position on the map
//            navigationLocationProvider.changePosition(
//                location = enhancedLocation,
//                keyPoints = locationMatcherResult.keyPoints,
//            )
//
//// update camera position to account for new location
//            viewportDataSource.onLocationChanged(enhancedLocation)
//            viewportDataSource.evaluate()
//
//// if this is the first location update the activity has received,
//// it's best to immediately move the camera to the current user location
//            if (!firstLocationUpdateReceived) {
//                firstLocationUpdateReceived = true
//                navigationCamera.requestNavigationCameraToOverview(
//                    stateTransitionOptions = NavigationCameraTransitionOptions.Builder()
//                        .maxDuration(0) // instant transition
//                        .build()
//                )
//            }
//        }
//    }
//
//    /**
//     * Gets notified with progress along the currently active route.
//     */
//    private val routeProgressObserver = RouteProgressObserver { routeProgress ->
//// update the camera position to account for the progressed fragment of the route
//        viewportDataSource.onRouteProgressChanged(routeProgress)
//        viewportDataSource.evaluate()
//
//// draw the upcoming maneuver arrow on the map
//        val style = mapboxMap.getStyle()
//        if (style != null) {
//            val maneuverArrowResult = routeArrowApi.addUpcomingManeuverArrow(routeProgress)
//            routeArrowView.renderManeuverUpdate(style, maneuverArrowResult)
//        }
//
//// update top banner with maneuver instructions
//        val maneuvers = maneuverApi.getManeuvers(routeProgress)
//        maneuvers.fold(
//            { error ->
//                Toast.makeText(
//                    this.context,
//                    error.errorMessage,
//                    Toast.LENGTH_SHORT
//                ).show()
//            },
//            {
//
//            }
//        )
//
//// update bottom trip progress summary
//
//    }
//
//    /**
//     * Gets notified whenever the tracked routes change.
//     *
//     * A change can mean:
//     * - routes get changed with [MapboxNavigation.setRoutes]
//     * - routes annotations get refreshed (for example, congestion annotation that indicate the live traffic along the route)
//     * - driver got off route and a reroute was executed
//     */
//    private val routesObserver = RoutesObserver { routeUpdateResult ->
//        if (routeUpdateResult.routes.isNotEmpty()) {
//// generate route geometries asynchronously and render them
//            val routeLines = routeUpdateResult.routes.map { RouteLine(it, null) }
//
//            routeLineApi.setRoutes(
//                routeLines
//            ) { value ->
//                mapboxMap.getStyle()?.apply {
//                    routeLineView.renderRouteDrawData(this, value)
//                }
//            }
//
//// update the camera position to account for the new route
//            viewportDataSource.onRouteChanged(routeUpdateResult.routes.first())
//            viewportDataSource.evaluate()
//        } else {
//// remove the route line and route arrow from the map
//            val style = mapboxMap.getStyle()
//            if (style != null) {
//                routeLineApi.clearRouteLine { value ->
//                    routeLineView.renderClearRouteLineValue(
//                        style,
//                        value
//                    )
//                }
//                routeArrowView.render(style, routeArrowApi.clearArrows())
//            }
//
//// remove the route reference from camera position evaluations
//            viewportDataSource.clearRouteData()
//            viewportDataSource.evaluate()
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        binding = FragmentExploreBinding.inflate(inflater, container, false)
//
//        mapboxMap = binding.mapView.getMapboxMap()
//
//// initialize the location puck
//        binding.mapView.location.apply {
//            this.locationPuck = LocationPuck2D(
//                bearingImage = this@ExploreFragment.context?.let {
//                    ContextCompat.getDrawable(
//                        it,
//                        R.drawable.rsz_love_locatoin_icon
//                    )
//                }
//            )
//            setLocationProvider(navigationLocationProvider)
//            enabled = true
//        }
//
//// initialize Mapbox Navigation
//        mapboxNavigation = if (MapboxNavigationProvider.isCreated()) {
//            MapboxNavigationProvider.retrieve()
//        } else ({
//            this.context?.let {
//                NavigationOptions.Builder(it)
//                    .accessToken(getString(R.string.mapbox_access_token))
//                    // comment out the location engine setting block to disable simulation
//                    .locationEngine(replayLocationEngine)
//                    .build()
//            }?.let {
//                MapboxNavigationProvider.create(
//                    it
//                )
//            }
//        })!!
//
//// initialize Navigation Camera
//        viewportDataSource = MapboxNavigationViewportDataSource(mapboxMap)
//        navigationCamera = NavigationCamera(
//            mapboxMap,
//            binding.mapView.camera,
//            viewportDataSource
//        )
//// set the animations lifecycle listener to ensure the NavigationCamera stops
//// automatically following the user location when the map is interacted with
//        binding.mapView.camera.addCameraAnimationsLifecycleListener(
//            NavigationBasicGesturesHandler(navigationCamera)
//        )
//        navigationCamera.registerNavigationCameraStateChangeObserver { navigationCameraState ->
//
//// set the padding values depending on screen orientation and visible view layout
//            if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                viewportDataSource.overviewPadding = landscapeOverviewPadding
//            } else {
//                viewportDataSource.overviewPadding = overviewPadding
//            }
//            if (this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//                viewportDataSource.followingPadding = landscapeFollowingPadding
//            } else {
//                viewportDataSource.followingPadding = followingPadding
//            }
//
//// make sure to use the same DistanceFormatterOptions across different features
//            val distanceFormatterOptions =
//                mapboxNavigation.navigationOptions.distanceFormatterOptions
//
//// initialize maneuver api that feeds the data to the top banner maneuver view
//            maneuverApi = MapboxManeuverApi(
//                MapboxDistanceFormatter(distanceFormatterOptions)
//            )
//
//
//// initialize route line, the withRouteLineBelowLayerId is specified to place
//// the route line below road labels layer on the map
//// the value of this option will depend on the style that you are using
//// and under which layer the route line should be placed on the map layers stack
//            val mapboxRouteLineOptions = this.context?.let {
//                MapboxRouteLineOptions.Builder(it)
//                    .withRouteLineBelowLayerId("road-label")
//                    .build()
//            }
//            routeLineApi = mapboxRouteLineOptions?.let { MapboxRouteLineApi(it) }!!
//            routeLineView = mapboxRouteLineOptions?.let { MapboxRouteLineView(it) }
//
//// initialize maneuver arrow view to draw arrows on the map
//            val routeArrowOptions = this.context?.let { RouteArrowOptions.Builder(it).build() }
//            routeArrowView = routeArrowOptions?.let { MapboxRouteArrowView(it) }!!
//
//// load map style
//            mapboxMap.loadStyleUri(
//                Style.MAPBOX_STREETS
//            ) {
//// add long click listener that search for a route to the clicked destination
//                binding.mapView.gestures.addOnMapLongClickListener { point ->
//                    findRoute(point)
//                    true
//                }
//            }
//
//// start the trip session to being receiving location updates in free drive
//// and later when a route is set also receiving route progress updates
//            mapboxNavigation.startTripSession()
//
//        }
//        return binding.root
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//// register event listeners
//        mapboxNavigation.registerRoutesObserver(routesObserver)
//        mapboxNavigation.registerRouteProgressObserver(routeProgressObserver)
//        mapboxNavigation.registerLocationObserver(locationObserver)
//        mapboxNavigation.registerRouteProgressObserver(replayProgressObserver)
//
//        if (mapboxNavigation.getRoutes().isEmpty()) {
//// if simulation is enabled (ReplayLocationEngine set to NavigationOptions)
//// but we're not simulating yet,
//// push a single location sample to establish origin
//            mapboxReplayer.pushEvents(
//                listOf(
//                    ReplayRouteMapper.mapToUpdateLocation(
//                        eventTimestamp = 0.0,
//                        point = Point.fromLngLat(-122.39726512303575, 37.785128345296805)
//                    )
//                )
//            )
//            mapboxReplayer.playFirstLocation()
//        }
//    }
//
//    override fun onStop() {
//        super.onStop()
//
//// unregister event listeners to prevent leaks or unnecessary resource consumption
//        mapboxNavigation.unregisterRoutesObserver(routesObserver)
//        mapboxNavigation.unregisterRouteProgressObserver(routeProgressObserver)
//        mapboxNavigation.unregisterLocationObserver(locationObserver)
//        mapboxNavigation.unregisterRouteProgressObserver(replayProgressObserver)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        MapboxNavigationProvider.destroy()
//        mapboxReplayer.finish()
//        maneuverApi.cancel()
//        routeLineApi.cancel()
//        routeLineView.cancel()
//
//    }
//
//    fun findRoute(destination: Point) {
//        val originLocation = navigationLocationProvider.lastLocation
//        val originPoint = originLocation?.let {
//            Point.fromLngLat(it.longitude, it.latitude)
//        } ?: return
//
//// execute a route request
//// it's recommended to use the
//// applyDefaultNavigationOptions and applyLanguageAndVoiceUnitOptions
//// that make sure the route request is optimized
//// to allow for support of all of the Navigation SDK features
//        mapboxNavigation.requestRoutes(
//            RouteOptions.builder()
//                .applyDefaultNavigationOptions()
//                .coordinatesList(listOf(originPoint, destination))
//// provide the bearing for the origin of the request to ensure
//// that the returned route faces in the direction of the current user movement
//                .bearingsList(
//                    listOf(
//                        Bearing.builder()
//                            .angle(originLocation.bearing.toDouble())
//                            .degrees(45.0)
//                            .build(),
//                        null
//                    )
//                )
//                .layersList(listOf(mapboxNavigation.getZLevel(), null))
//                .build(),
//            object : RouterCallback {
//                override fun onRoutesReady(
//                    routes: List<DirectionsRoute>,
//                    routerOrigin: RouterOrigin
//                ) {
//                    setRouteAndStartNavigation(routes)
//                }
//
//                override fun onFailure(
//                    reasons: List<RouterFailure>,
//                    routeOptions: RouteOptions
//                ) {
//// no impl
//                }
//
//                override fun onCanceled(routeOptions: RouteOptions, routerOrigin: RouterOrigin) {
//// no impl
//                }
//            }
//        )
//    }
//
//    fun setRouteAndStartNavigation(routes: List<DirectionsRoute>) {
//// set routes, where the first route in the list is the primary route that
//// will be used for active guidance
//        mapboxNavigation.setRoutes(routes)
//
//// start location simulation along the primary route
//        startSimulation(routes.first())
//
//// move the camera to overview when new route is available
//        navigationCamera.requestNavigationCameraToOverview()
//    }
//
//    fun clearRouteAndStopNavigation() {
//// clear
//        mapboxNavigation.setRoutes(listOf())
//
//// stop simulation
//        mapboxReplayer.stop()
//
//// hide UI elements
//    }
//
//    fun startSimulation(route: DirectionsRoute) {
//        mapboxReplayer.run {
//            stop()
//            clearEvents()
//            val replayEvents = ReplayRouteMapper().mapDirectionsRouteGeometry(route)
//            pushEvents(replayEvents)
//            seekTo(replayEvents.first())
//            play()
//        }
//    }
//}