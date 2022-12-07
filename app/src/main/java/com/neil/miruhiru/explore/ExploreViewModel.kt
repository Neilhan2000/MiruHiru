package com.neil.miruhiru.explore

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Point
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.R
import com.neil.miruhiru.data.Challenge
import timber.log.Timber
import java.math.RoundingMode
import java.text.DecimalFormat
import kotlin.math.roundToInt

class ExploreViewModel(private val modelApplication: Application): AndroidViewModel(modelApplication) {

    init {
        loadChallenges()
    }

    private val _challengeList = MutableLiveData<List<Challenge>>()
    val challengeList: LiveData<List<Challenge>>
        get() = _challengeList

    private val _challengeDistance = MutableLiveData<String>()
    val challengeDistance: LiveData<String>
        get() = _challengeDistance

    private fun loadChallenges() {
        val db = Firebase.firestore
        val challengeList = mutableListOf<Challenge>()

        db.collection(modelApplication.getString(R.string.challenges))
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val challenge = document.toObject<Challenge>()
                    challengeList.add(challenge)
                }
                _challengeList.value = challengeList
            }
            .addOnFailureListener { exception ->

            }

    }

    /**
     * Calculate distance between current point and destination.
     */
    private fun calculateDistance(currentPoint: Point?, destinationPoint: GeoPoint): Float {
        val current = Location(modelApplication.getString(R.string.current))
        currentPoint?.latitude()?.let { current.latitude = it }
        currentPoint?.longitude()?.let { current.longitude = it }

        val destination = Location(modelApplication.getString(R.string.current))
        destination.latitude = destinationPoint.latitude
        destination.longitude = destinationPoint.longitude

        val result =  floatArrayOf(0.0F)
        val distance = currentPoint?.let {
            Location.distanceBetween(
                currentPoint.latitude(), currentPoint.longitude(),
            destinationPoint.latitude, destinationPoint.longitude, result)
        }
        return result.first()
    }

    /**
     * Round number to one decimal place.
     */
    fun roundOffDecimal(number: Float): Float {
        val df = DecimalFormat(modelApplication.getString(R.string.one_decimal_format))
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toFloat()
    }

    /**
     * Determine annotation icon by challenge type.
     */
    fun determineChallengeIcon(type: String): Int {
        return when (type) {
            modelApplication.getString(R.string.food) -> R.drawable.ic_food_location
            modelApplication.getString(R.string.couple) -> R.drawable.ic_love_location
            modelApplication.getString(R.string.history) -> R.drawable.ic_histoty_location
            modelApplication.getString(R.string.travel) -> R.drawable.ic_travel_location
            modelApplication.getString(R.string.special) -> R.drawable.ic_special_location
            else -> R.drawable.anya_icon2
        }
    }

    fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
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

    /**
     * Calculate distance between current point and destination, and set it to challengeDistance
     */
    @SuppressLint("MissingPermission")
    fun calculateAndShowDistance(destination: GeoPoint) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(modelApplication)
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                if (location == null) {
                    _challengeDistance.value = modelApplication.getString(R.string.no_gps)
                } else {
                    val currentPoint = Point.fromLngLat(
                            location.longitude,
                            location.latitude
                        )
                    val distance = calculateDistance(currentPoint, destination)

                    _challengeDistance.value = modelApplication.getString(R.string.convert_distances_to_int
                        , distance.roundToInt()
                    )
                }
            }
            .addOnFailureListener { exception ->
                Timber.i("${exception.message}")
            }

    }
}