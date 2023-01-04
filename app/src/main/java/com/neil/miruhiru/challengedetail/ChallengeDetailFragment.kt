package com.neil.miruhiru.challengedetail

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.ChallengeInfo
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentChallengeDetailBinding
import com.neil.miruhiru.ext.glideImageCenter
import com.neil.miruhiru.network.LoadingStatus
import com.neil.miruhiru.util.Util.showDialog2OptionsNeu
import timber.log.Timber
import kotlin.math.roundToInt
import kotlin.properties.Delegates

private const val RECYCLER_HEIGHT = 450
private const val ONE_HOUR: Double = 3600.0
private const val ICON_SIZE = 2.0

class ChallengeDetailFragment : Fragment() {

    private val viewModel: ChallengeDetailViewModel by lazy {
        ViewModelProvider(this).get(ChallengeDetailViewModel::class.java)
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

        // show or hide rating system
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

        // go back
        binding.detailBackArrow.setOnClickListener {
            this.findNavController().navigateUp()
        }

        // observe loading status and show progress bar
        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer { status ->
            if (status == LoadingStatus.LOADING) {
                MainActivity.getInstanceFromMainActivity().window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                binding.progressBar2.visibility = View.VISIBLE
            } else if (status == LoadingStatus.DONE) {
                MainActivity.getInstanceFromMainActivity().window.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                binding.progressBar2.visibility = View.GONE
            } else {
                MainActivity.getInstanceFromMainActivity().window.clearFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                )
                Toast.makeText(requireContext(), "loading error", Toast.LENGTH_SHORT).show()
            }
        })

        // observer challenge data and setup screen
        viewModel.challenge.observe(viewLifecycleOwner, Observer {
            setupChallenge(it)
        })

        // observer task data to add annotation to map then setup map camera
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

        // set camera to original location
        binding.originalLocation.setOnClickListener {
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
        val adapter = CommentAdapter(viewModel) { position ->
            showDialog(position)
        }
        binding.recyclerComment.adapter = adapter

        // observer commentUsers and show in recyclerView
        viewModel.commentUsers.observe(viewLifecycleOwner, Observer {
            adapter.submitList(viewModel.commentList.value)
            Timber.i("comment List ${viewModel.commentList.value}")
            adapter.notifyDataSetChanged()
            if (adapter.itemCount > 2) {
                binding.recyclerComment.layoutParams.height = RECYCLER_HEIGHT
            } else {
                binding.recyclerComment.layoutParams.height = RecyclerView.LayoutParams.WRAP_CONTENT
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

    private fun showDialog(position: Int) {
        val dialog = Dialog(requireContext(), R.style.fullScreenDialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_report)
        val reportButton = dialog.findViewById<Button>(R.id.reportButton)
        val blockButton = dialog.findViewById<Button>(R.id.blockButton)
        val backButton = dialog.findViewById<Button>(R.id.backButton)

        reportButton.setOnClickListener {
            Toast.makeText(requireContext(), "report user", Toast.LENGTH_SHORT).show()
        }
        blockButton.setOnClickListener {
            Toast.makeText(requireContext(), "block user", Toast.LENGTH_SHORT).show()
            viewModel.blockUser(position)
        }
        backButton.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setGravity(Gravity.BOTTOM)
    }

    private fun setLocation() {
        if (this::firstStagePoint.isInitialized) {
            mapView.getMapboxMap().setCamera(CameraOptions.Builder().center(firstStagePoint).build())
        }
    }

    private fun setupChallenge(challenge: Challenge) {
        binding.ChallengeTitle.text = challenge.name
        binding.challengeMainImage.glideImageCenter(challenge.image, R.drawable.image_placeholder)
        challenge.totalRating?.let { binding.ratingBar.rating  = it }

        val totalRating = viewModel.roundOffDecimal(challenge.totalRating).toString()
        val commentQuantity = challenge.commentQuantity
        binding.ratingText.text = String.format(getString(R.string.total_rating_and_comment_quantity), totalRating, commentQuantity)
        binding.stageText.text = challenge.stage.toString()

        val hours = challenge.timeSpent.toDouble().toBigDecimal().div(ONE_HOUR.toBigDecimal()).toString()
        binding.timeText.text = getString(R.string.convert_seconds_to_hours_dem, hours)
        binding.challengeDescription.text = challenge.description

        binding.typeText.text = challenge.type
        binding.typeText.setBackgroundResource(when (challenge.type) {
            getString(R.string.food) -> R.drawable.type_text_border
            getString(R.string.history) -> R.drawable.type_history_border
            getString(R.string.couple) -> R.drawable.type_couple_border
            getString(R.string.travel) -> R.drawable.type_travel_border
            getString(R.string.special) -> R.drawable.type_special_border
            else -> R.drawable.type_text_border
        })

        challenge.location?.let { calculateAndShowDistance(it) }
        // like or unlike challenge

        if (viewModel.isLike(challenge)) {
            like = true
            binding.likeIcon.visibility = View.VISIBLE
        } else {
            like = false
            binding.likeIcon.visibility = View.INVISIBLE
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
        // observe to display author name
        viewModel.authorName.observe(viewLifecycleOwner, Observer {
            binding.author.text = getString(R.string.created_by, it)
        })

        // observe if has uncompleted event
        viewModel.hasCurrentEvent.observe(viewLifecycleOwner, Observer { hasEvent ->
            if (hasEvent == true) {
                showDialog2OptionsNeu(
                    getString(R.string.find_record),
                    getString(R.string.recover_last_record),
                    positiveFun = { this@ChallengeDetailFragment.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment()) },
                    neutralFun = { viewModel.cleanEventSingle() }
                )

            } else if (hasEvent == false){
                this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeTypeFragment(
                    ChallengeInfo(challenge.id, challenge.stage)))
                viewModel.navigateToChallengeTypeFragmentCompleted()
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
                location?.let {
                    currentPoint = Point.fromLngLat(
                        location.longitude,
                        location.latitude
                    )
                    val distance = viewModel.calculateDistance(currentPoint, destination)
                    binding.distanceText.text = getString(R.string.convert_distances_to_int, distance.roundToInt())
                }
            }

    }


    // add annotation
    private fun addAnnotationToMap(task: Task) {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            this.requireContext(),
            determineTaskIcon(task.stage)
        )?.let {
            val annotationApi = mapView.annotations
            pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)
            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(Point.fromLngLat(
                    task.location.longitude,
                    task.location.latitude
                ))
                .withIconImage(it)
                .withIconSize(ICON_SIZE)

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