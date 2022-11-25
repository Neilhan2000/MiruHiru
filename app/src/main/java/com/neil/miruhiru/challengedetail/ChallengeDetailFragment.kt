package com.neil.miruhiru.challengedetail

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.ChallengeInfo
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentChallengeDetailBinding
import com.neil.miruhiru.factory.ChallengeDetailViewModelFactory
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class ChallengeDetailFragment : Fragment() {

    private lateinit var factory: ChallengeDetailViewModelFactory
    private val viewModel: ChallengeDetailViewModel by lazy {
        ViewModelProvider(this, factory).get(ChallengeDetailViewModel::class.java)
    }

    private lateinit var binding: FragmentChallengeDetailBinding
    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var firstStagePoint: Point
    private lateinit var challengeId: String
    private var like by Delegates.notNull<Boolean>()
    private var show = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChallengeDetailBinding.inflate(inflater, container, false)
        mapView = binding.mapView

        // get args from last fragment and pass it to viewModel
        challengeId = ChallengeDetailFragmentArgs.fromBundle(requireArguments()).challengeId
        factory = ChallengeDetailViewModelFactory(challengeId)
        val previousFragmentId  = this.findNavController().previousBackStackEntry?.destination?.id
        if (previousFragmentId == R.id.exploreFragment || previousFragmentId == R.id.likeChallengeFragment ||
                previousFragmentId == R.id.communityFragment) {
            viewModel.loadChallenge(challengeId)
            UserManager.isPersonal = false
        } else if (previousFragmentId == R.id.overviewFragment) {
            viewModel.loadPersonalChallenge(challengeId)
            binding.seeComment.visibility = View.GONE
            binding.ratingBar.visibility = View.GONE
            binding.ratingText.visibility = View.GONE
            binding.likeIcon.visibility = View.GONE
            binding.unlikeIcon.visibility = View.GONE
            binding.likeClick.visibility = View.GONE
            UserManager.isPersonal = true
        }

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
                it[0].location.longitude,
                it[0].location.latitude
            )
            setLocation()
        })

        // set camera to current location
        binding.myLocation.setOnClickListener {
            setLocation()
        }


        // click to show and hide comments
        binding.seeComment.setOnClickListener {
            viewModel.loadComments()
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
            adapter.notifyDataSetChanged()
            if (adapter.itemCount > 2) {
                binding.recyclerComment.layoutParams.height = 450
            }
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
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
        challenge.totalRating?.let { binding.ratingBar.rating  = it }
        binding.ratingText.text = "${challenge.totalRating?.let { viewModel.roundOffDecimal(it) }} (${challenge.commentQuantity})"
        binding.stageText.text = challenge.stage.toString()
        binding.timeText.text = "${challenge.timeSpent?.toDouble().toBigDecimal().div(3600.toDouble().toBigDecimal())} Hrs"
        binding.challengeDescription.text = challenge.description
        binding.author.text = "created by ${challenge.author}"
        binding.typeText.text = challenge.type
        binding.typeText.setBackgroundResource(when (challenge.type) {
            getString(R.string.food) -> R.drawable.type_text_border
            getString(R.string.history) -> R.drawable.type_history_border
            getString(R.string.couple) -> R.drawable.type_couple_border
            getString(R.string.travel) -> R.drawable.type_travel_border
            getString(R.string.special) -> R.drawable.type_special_border
            else -> R.drawable.type_text_border
        })

        // like or unlike challenge
        challenge.location?.let { calculateAndShowDistance(it) }

        challenge.likeList.forEach { likeUser ->
            if (likeUser == UserManager.userId) {
                like = true
                binding.likeIcon.visibility = View.VISIBLE
            } else {
                like = false
                binding.likeIcon.visibility = View.INVISIBLE
            }
        }
        binding.likeClick.setOnClickListener {
            if (like) {
                binding.likeIcon.visibility = View.INVISIBLE
                viewModel.unLikeChallenge(challengeId)
            } else {
                binding.likeIcon.visibility = View.VISIBLE
                viewModel.likeChallenge(challengeId)
            }
            like = !like
        }

        binding.startButton.setOnClickListener {
            viewModel.checkHasCurrentEvent(challengeId)
        }

        // observe if has uncompleted event
        viewModel.hasCurrentEvent.observe(viewLifecycleOwner, Observer { hasEvent ->
            if (hasEvent) {
                val defaultBuilder = AlertDialog.Builder(requireContext())
                    .setTitle("發現上次儲存的紀錄")
                    .setMessage("要繼續上次的挑戰嗎?")
                    .setPositiveButton("確定", object: DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            this@ChallengeDetailFragment.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
                        }
                    })
                    .setNeutralButton("清除紀錄", object: DialogInterface.OnClickListener{
                        override fun onClick(p0: DialogInterface?, p1: Int) {
                            viewModel.cleanEventSingle()
                        }
                    }) .show()
                defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                defaultBuilder.getButton(DialogInterface.BUTTON_NEUTRAL)
                    .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
            } else {
                this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeTypeFragment(
                    ChallengeInfo(challenge.id, challenge.stage)))
            }
        })
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
            1 -> R.drawable.ic_stage_one_on
            2 -> R.drawable.ic_stage_two_on
            3 -> R.drawable.ic_stage_three_on
            4 -> R.drawable.ic_stage_four_on
            else -> R.drawable.ic_stage_five_on
        }
        return iconRes
    }
}