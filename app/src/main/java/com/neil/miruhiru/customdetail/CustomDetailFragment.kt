package com.neil.miruhiru.customdetail

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentCustomDetailBinding
import kotlin.concurrent.fixedRateTimer

class CustomDetailFragment : Fragment() {


    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var mapBoxMap: MapboxMap
    private lateinit var binding: FragmentCustomDetailBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomDetailBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapBoxMap = mapView.getMapboxMap()
        val annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)

        mapBoxMap.addOnMapClickListener { point ->
            pointAnnotationManager.deleteAll()
            addAnnotationToMap(point)
            changeButtonStatus()
            false
        }

        binding.editButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalCustomBottomSheetFragment())
        }
        binding.editCancelButton.setOnClickListener {
            pointAnnotationManager.deleteAll()
            changeButtonStatus()
        }
        binding.nextButton.setOnClickListener {

        }


        return binding.root
    }

    private fun changeButtonStatus() {
        if (pointAnnotationManager.annotations.isNotEmpty()) {
            binding.editButton.isEnabled = true
            binding.editButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))

            binding.editCancelButton.isEnabled = true
            binding.editCancelButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
        } else {
            binding.editButton.isEnabled = false
            binding.editButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))

            binding.editCancelButton.isEnabled = false
            binding.editCancelButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
        }
    }

    // add annotation
    private fun addAnnotationToMap(point: Point) {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            this.requireContext(),
            R.drawable.ic_red_location
        )?.let {

            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
                .withIconSize(2.0)

            // Add the resulting pointAnnotation to the map.
            pointAnnotationManager.create(pointAnnotationOptions)
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
}