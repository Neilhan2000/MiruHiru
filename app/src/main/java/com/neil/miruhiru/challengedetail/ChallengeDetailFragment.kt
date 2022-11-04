package com.neil.miruhiru.challengedetail

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentChallengeDetailBinding
import kotlin.math.roundToInt

class ChallengeDetailFragment : Fragment() {

    private val viewModel: ChallengeDetailViewModel by lazy {
        ViewModelProvider(this).get(ChallengeDetailViewModel::class.java)
    }
    private lateinit var binding: FragmentChallengeDetailBinding
    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var firstStagePoint: Point
    private var show = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengeDetailBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        // observer challenge data and setup screen
        viewModel.challenge.observe(viewLifecycleOwner, Observer {
            setupScreen(it)
        })

        // observer task data to add annotation to map and setup map camera
        viewModel.taskList.observe(viewLifecycleOwner, Observer {
            it.forEach {
                addAnnotationToMap(it)
            }
            firstStagePoint = Point.fromLngLat(
                it[0].location?.longitude!!,
                it[0].location?.latitude!!
            )
            setLocation()
        })

        // set camera to current location
        binding.myLocation.setOnClickListener {
            setLocation()
        }


        // click to show and hide comments
        binding.seeComment.setOnClickListener {
            viewModel.loadComments("2WBySSd68w3VrA08eLGj")
            if (show) {
                binding.recyclerComment.visibility = View.VISIBLE
            } else {
                binding.recyclerComment.visibility = View.GONE
            }
            show = !show
        }

        // set recyclerView adapter
        val adapter = CommentAdapter(
            viewModel
        ) { index ->
            reportUser(index)
        }
        binding.recyclerComment.adapter = adapter

        // observer commentUsers and show in recyclerView
        viewModel.commentUsers.observe(viewLifecycleOwner, Observer {
            adapter.submitList(viewModel.commentList.value)
        })

        return binding.root
    }

    private fun reportUser(userIndex: Int) {
        Toast.makeText(requireContext(), "report user $userIndex", Toast.LENGTH_SHORT).show()
    }

    private fun setLocation() {
        if (this::firstStagePoint.isInitialized) {
            mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(firstStagePoint).build())
        }
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

        // calculate and show distance
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
//                Log.i("neil", "current location = ${location?.latitude}, ${location?.longitude}")
                currentPoint = Point.fromLngLat(
                    location!!.longitude,
                    location!!.latitude
                )
                val distance = viewModel.calculateDistance(currentPoint, destination)
                binding.distanceText.text = "距離起點 ${distance.roundToInt()} Ms"
            }.addOnFailureListener { exception ->
                binding.distanceText.text = "${exception.message}"
            }
    }


    // add annotation
    private fun addAnnotationToMap(task: Task) {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            this.requireContext(),
            determineTaskIcon(task.stage!!)
        )?.let {
            val annotationApi = mapView?.annotations
            pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)!!
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(
                    task.location?.longitude!!,
                    task.location?.latitude!!
                ))
                .withIconImage(it)
                .withIconSize(2.0)

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

    private fun determineTaskIcon(stage: Int): Int {
        val iconRes = when (stage) {
            1 -> R.drawable.anya_icon
            else -> R.drawable.anya_icon2
        }
        return iconRes
    }
}