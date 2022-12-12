package com.neil.miruhiru.explore

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.FragmentExploreBinding
import com.neil.miruhiru.ext.glideImageCenter

/**
 * Created by Neil Tsai in Dec. 2022.
 *
 * If you want to use mapbox route line api and navigation feature,
 * just checkout to mapbox_navigation branch to see detail.
 */
class ExploreFragment : Fragment() {

    companion object {
        private const val ZOOM_SIZE = 14.0
        private const val ICON_SIZE = 2.0
        private const val ONE_HOUR = 3600
        private const val REQUEST_LOCATION_CODE = 123
    }

    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var binding: FragmentExploreBinding
    private lateinit var mapView: MapView
    private lateinit var mapBoxMap: MapboxMap

    var stage: Int = 1

    private val viewModel: ExploreViewModel by lazy {
        ViewModelProvider(this).get(ExploreViewModel::class.java)
    }

    private val slideUp: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up) }
    private val slideDown: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down) }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().zoom(ZOOM_SIZE).bearing(it).build())
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener {
        mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(it).zoom(ZOOM_SIZE).build())
        mapView.gestures.focalPoint = mapView.getMapboxMap().pixelForCoordinate(it)
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentExploreBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapBoxMap = mapView.getMapboxMap()
        enableLocation()

        binding.myLocationIcon.setOnClickListener {
            initLocationComponent()
            setupGesturesListener()
        }

        // Add challenges to map.
        viewModel.challengeList.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            it.forEach { challenge ->
                addAnnotationToMap(challenge)
            }
        })

        // Observe calculate result and display it.
        viewModel.challengeDistance.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            binding.challengeDistance.text = it
        })

        return binding.root
    }

    /**
     * Setup map.
     */
    private fun onMapReady() {
        mapView.getMapboxMap().loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }

        // Add map click listener, noticing that it will cause conflict with onAnnotationClickListener.
        mapBoxMap.addOnMapClickListener { point ->
            // See current point.
            // Timber.i("location = " + point.latitude() + ", " + point.longitude())

            // Show challenge card view.
            binding.locationCardView.startAnimation(slideDown)
            binding.locationCardView.visibility = View.GONE
            false
        }
    }

    /**
     * Add annotation and set click listener.
     */
    private fun addAnnotationToMap(challenge: Challenge) {
        viewModel.bitmapFromDrawableRes(
            this.requireContext(),
            viewModel.determineChallengeIcon(challenge.type)
        )?.let {
            // Create an instance of the Annotation API and get the PointAnnotationManager.
            val annotationApi = mapView.annotations
            pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(
                    challenge.location.longitude,
                    challenge.location.latitude
                ))
                .withIconImage(it)
                .withIconSize(ICON_SIZE)
                // store data to annotation
                // .withData(JsonParser.parseString(Gson().toJson(locationInfo)))

            // add annotation click listener.
            pointAnnotationManager.addClickListener(OnPointAnnotationClickListener {
                setupLocationCardView(challenge)
                // get data from annotation
                // Gson().fromJson(annotation.getData()?.asJsonObject, LocationInfo::class.java)

                false
            })
            // Add the resulting pointAnnotation to the map.
            pointAnnotationManager.create(pointAnnotationOptions)
        }
    }

    /**
     * Display all needed information on location card view.
     */
    private fun setupLocationCardView(challenge: Challenge) {

        binding.locationCardView.startAnimation(slideUp)
        binding.challengeTitle.text = challenge.name
        binding.challengeStage.text = challenge.stage.toString()
        binding.challengeImage.glideImageCenter(challenge.image, R.drawable.image_placeholder)

        val hours = challenge.timeSpent.div(ONE_HOUR)
        val totalRating = viewModel.roundOffDecimal(challenge.totalRating).toString()
        val commentQuantity = challenge.commentQuantity
        binding.challengeTime.text = getString(R.string.convert_seconds_to_hours, hours)
        binding.challengeRating.text = String.format(getString(R.string.total_rating_and_comment_quantity), totalRating, commentQuantity)

        challenge.location.let { viewModel.calculateAndShowDistance(it) }

        binding.locationCardView.setOnClickListener {
            this@ExploreFragment.findNavController().navigate(NavGraphDirections
                .actionGlobalChallengeDetailFragment(challenge.id))
        }
        binding.locationCardView.visibility = View.VISIBLE
    }

    private fun setupGesturesListener() {
        mapView.gestures.addOnMoveListener(onMoveListener)
    }

    /**
     * Add position change listener and bearing listener.
     */
    private fun initLocationComponent() {
        val locationComponentPlugin = mapView.location
        locationComponentPlugin.updateSettings {
            this.enabled = true
        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
//        Use this to add camera bearing
//        locationComponentPlugin?.addOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
    }

    /**
     * Remove position change listener and bearing listener.
     */
    private fun onCameraTrackingDismissed() {
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.location
            .removeOnIndicatorBearingChangedListener(onIndicatorBearingChangedListener)
        mapView.location
            .removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView.gestures.removeOnMoveListener(onMoveListener)
    }

    /**
     * Check permission and setup map.
     */
    private fun enableLocation() {
        if (PermissionsManager.areLocationPermissionsGranted(this.context)) {
            onMapReady()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }
    }

    // Handle asking permission result.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}