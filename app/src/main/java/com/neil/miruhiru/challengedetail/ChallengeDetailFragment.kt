package com.neil.miruhiru.challengedetail

import android.annotation.SuppressLint
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.FragmentChallengeDetailBinding
import com.neil.miruhiru.explore.ExploreViewModel
import kotlin.math.roundToInt

class ChallengeDetailFragment : Fragment() {

    private val viewModel: ChallengeDetailViewModel by lazy {
        ViewModelProvider(this).get(ChallengeDetailViewModel::class.java)
    }
    private lateinit var binding: FragmentChallengeDetailBinding
    private lateinit var mapView: MapView

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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengeDetailBinding.inflate(inflater, container, false)
        mapView = binding.mapView


        viewModel.challenge.observe(viewLifecycleOwner, Observer {
            setupScreen(it)
        })

        onMapReady()

        // set camera to current location
        binding.myLocation.setOnClickListener {
            initLocationComponent()
            setupGesturesListener()
        }



        return binding.root
    }
    private fun setupScreen(challenge: Challenge) {
        binding.ChallengeTitle.text = challenge.name
        Glide.with(binding.challengeMainImage.context).load(challenge.image).centerCrop().apply(
            RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
        ).into(binding.challengeMainImage)
        binding.ratingBar.rating = challenge.totalRating!!
        binding.ratingText.text = challenge.totalRating.toString()
        binding.stageText.text = challenge.stage.toString()
        binding.timeText.text = "${challenge.timeSpent?.div(3600)} Hrs"
        binding.challengeDescription.text = challenge.description
        binding.typeText.text = challenge.type
        calculateAndShowDistance(challenge.location!!)
    }


    @SuppressLint("MissingPermission")
    private fun calculateAndShowDistance(destination: GeoPoint) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireContext())
        var currentPoint: Point? = null
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
//                Log.i("neil", "current location = ${location?.latitude}, ${location?.longitude}")
                currentPoint = Point.fromLngLat(
                    location!!.longitude,
                    location!!.latitude
                )
                val distance = viewModel.calculateDistance(currentPoint, destination)
                binding.distanceText.text = "距離起點 ${distance.roundToInt()} Ms"
            }
    }

    // set up map
    private fun onMapReady() {
        mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder()
                .zoom(16.0)
                .build()
        )
        mapView?.getMapboxMap()?.loadStyleUri(
            Style.MAPBOX_STREETS
        ) {
            initLocationComponent()
            setupGesturesListener()
        }
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView?.location
        locationComponentPlugin?.updateSettings {
            this.enabled = true
        }
        locationComponentPlugin?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    private fun setupGesturesListener() {
        mapView?.gestures?.addOnMoveListener(onMoveListener)
    }

    private fun onCameraTrackingDismissed() {
//        Toast.makeText(this.context, "onCameraTrackingDismissed", Toast.LENGTH_SHORT).show()
        mapView?.location
            ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
    }
}