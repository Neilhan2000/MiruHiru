package com.neil.miruhiru.task

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearSnapHelper
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.GeoPoint
import com.google.gson.Gson
import com.google.gson.JsonParser
import com.mapbox.android.gestures.MoveGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.LocationInfo
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentTaskBinding
import kotlinx.coroutines.*
import timber.log.Timber

class TaskFragment : Fragment() {

    private val viewModel: TaskViewModel by lazy {
        ViewModelProvider(this).get(TaskViewModel::class.java)
    }
    // animation
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_form_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_to_bottom_anim) }
    private var clicked = false
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
    private val scope = CoroutineScope(Job() + Dispatchers.IO)

    private lateinit var binding: FragmentTaskBinding
    private lateinit var taskAdapter: TaskAdapter
    private lateinit var guideAdapter: TaskGuideAdapter
    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)

        // factory

        // floating action button
        binding.fabAdd.setOnClickListener {
            onAddButtonClick()
        }
        binding.fabChat.setOnClickListener {
            Toast.makeText(requireContext(), "chat clicked", Toast.LENGTH_SHORT).show()
        }
        binding.fabAndroid.setOnClickListener {
            binding.guideTextRecycler.scrollToPosition(1)
            binding.guideTextRecycler.visibility = View.VISIBLE
        }
        binding.fabLocation.setOnClickListener {
            initLocationComponent()
        }

        // task and guide adapter
        taskAdapter = TaskAdapter(viewModel)
        guideAdapter = TaskGuideAdapter()
        binding.TaskRecycler.adapter = taskAdapter
        binding.guideTextRecycler.adapter = guideAdapter
        LinearSnapHelper().apply {
            attachToRecyclerView(binding.guideTextRecycler)
        }
        LinearSnapHelper().apply {
            attachToRecyclerView(binding.TaskRecycler)
        }

        // detect scroll position and hide guideTextRecycler
        binding.guideTextRecycler.setOnScrollChangeListener { view, _, _, _, _ ->
            val scrollToBottom = (binding.guideTextRecycler.computeHorizontalScrollRange()) / 3 * 2
//            val alr = (((binding.guideTextRecycler.computeHorizontalScrollOffset()) - 1080).toFloat() / 1080F)
//            view.alpha = alr
            if (binding.guideTextRecycler.computeHorizontalScrollOffset() == scrollToBottom ||
                binding.guideTextRecycler.computeHorizontalScrollOffset() == 0) {
                view.visibility = View.GONE
            }
        }


        // observe taskList and setup screen
        viewModel.taskList.observe(viewLifecycleOwner, Observer {
            taskAdapter.submitList(it)
            taskAdapter.notifyDataSetChanged()
            binding.progressBar.layoutParams.width = ((binding.progressBar.width) / viewModel.totalStage) * viewModel.currentStage
            binding.progressText.text = "${viewModel.currentStage} / ${viewModel.totalStage}"

            val guildTextList = listOf<Task>(Task(), it[0], Task())
            guideAdapter.submitList(guildTextList)
            binding.guideTextRecycler.scrollToPosition(1)

        })

        viewModel.annotationList.observe(viewLifecycleOwner, Observer {
            for (task in it) {
                addAnnotationToMap(task, viewModel.currentStage)
            }
        })
        viewModel.loadEventsWithTask(UserManager.userChallengeDocumentId ?: "null",
            UserManager.user.currentEvent)

        // setup map
        mapView = binding.mapView
        initLocationComponent()
        mapView?.gestures?.addOnMoveListener(onMoveListener)

        // override back press
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                @SuppressLint("ResourceAsColor")
                override fun handleOnBackPressed() {

                    if (viewModel.isMultiple == true) {
                        val defaultBuilder = AlertDialog.Builder(requireContext())
                            .setTitle("要退出挑戰嗎?")
                            .setMessage("發現挑戰有其他玩家，退出後將無法再參與歐")
                            .setPositiveButton("確定", object: DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    viewModel.cleanEventMultiple()
                                }
                            })
                            .setNegativeButton("取消", object: DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    // do nothing
                                }
                            })
                            .show()
                        defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                        defaultBuilder.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))

                    } else if (viewModel.isMultiple == false) {
                        val defaultBuilder = AlertDialog.Builder(requireContext())
                            .setTitle("要退出挑戰嗎?")
                            .setMessage("退出後進度會自動儲存歐")
                            .setPositiveButton("確定", object: DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    this@TaskFragment.findNavController().navigate(NavGraphDirections.actionGlobalExploreFragment())
                                }
                            })
                            .setNeutralButton("不要儲存", object: DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    viewModel.cleanEventSingle()
                                }
                            })
                            .setNegativeButton("取消", object: DialogInterface.OnClickListener{
                                override fun onClick(p0: DialogInterface?, p1: Int) {
                                    // do nothing
                                }
                            })
                            .show()
                        defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                        defaultBuilder.getButton(DialogInterface.BUTTON_NEGATIVE)
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                        defaultBuilder.getButton(DialogInterface.BUTTON_NEUTRAL)
                            .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                    }
//                    // if you want onBackPressed() to be called as normal afterwards
//                    if (isEnabled) {
//                        isEnabled = false
//                        requireActivity().onBackPressed()
//                    }
                }
            })

        // observer clean user current event
        viewModel.navigateUp.observe(viewLifecycleOwner, Observer { backPressSuccess ->
            if (backPressSuccess) {
                this@TaskFragment.findNavController().navigate(NavGraphDirections.actionGlobalExploreFragment())
                viewModel.navigateUpCompleted()
            }
        })

        // observer if user is kicked out of challenge
        viewModel.isKicked.observe(viewLifecycleOwner, Observer { isKicked ->
            if (isKicked) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalExploreFragment())
                Toast.makeText(requireContext(), "你已被移出挑戰", Toast.LENGTH_SHORT).show()
            }
        })
        viewModel.detectUserKicked()


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.gestures?.removeOnMoveListener(onMoveListener)
        scope.cancel()
    }

    private fun onAddButtonClick() {
        setVisibility(clicked)
        setAnimation(clicked)
        clicked = !clicked
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.fabChat.visibility = View.VISIBLE
            binding.fabAndroid.visibility = View.VISIBLE
            binding.fabLocation.visibility = View.VISIBLE
        } else {
            binding.fabChat.visibility = View.INVISIBLE
            binding.fabAndroid.visibility = View.INVISIBLE
            binding.fabLocation.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.fabChat.startAnimation(fromBottom)
            binding.fabAndroid.startAnimation(fromBottom)
            binding.fabLocation.startAnimation(fromBottom)
            binding.fabAdd.startAnimation(rotateOpen)
        } else {
            binding.fabChat.startAnimation(toBottom)
            binding.fabAndroid.startAnimation(toBottom)
            binding.fabLocation.startAnimation(toBottom)
            binding.fabAdd.startAnimation(rotateClose)
        }
    }

    private fun initLocationComponent() {
        val locationComponentPlugin = mapView?.location
        locationComponentPlugin?.updateSettings {
            this.enabled = true
        }
        locationComponentPlugin?.addOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    private fun onCameraTrackingDismissed() {
        mapView?.location
            ?.removeOnIndicatorPositionChangedListener(onIndicatorPositionChangedListener)
    }

    private fun detectArrival(): String {
        // use timer and calculate arrival
        val distance = 0
        if (distance <= 50) {
            return "ARRIVAL"
        } else if (distance >= 1000) {
            return "OUTOFRANGE"
        } else {
            return "TRACKING"
        }
    }

    private fun determineTaskIcon(taskStage: Int, currentStage: Int): Int {
        if (taskStage > currentStage) {
            return when (taskStage) {
                2 -> R.drawable.ic_stage_two_off
                3 -> R.drawable.ic_stage_three_off
                4 -> R.drawable.ic_stage_four_off
                else -> R.drawable.ic_stage_five_off
            }
        } else if (taskStage == currentStage) {
            return when (taskStage) {
                1 -> R.drawable.ic_stage_one_on
                2 -> R.drawable.ic_stage_two_on
                3 -> R.drawable.ic_stage_three_on
                4 -> R.drawable.ic_stage_four_on
                else -> R.drawable.ic_stage_five_on
            }
        } else {
            return R.drawable.ic_success
        }
    }

    // add annotation
    private fun addAnnotationToMap(task: Task, currentStage: Int) {
        // Create an instance of the Annotation API and get the PointAnnotationManager.
        bitmapFromDrawableRes(
            this.requireContext(),
            determineTaskIcon(task.stage, currentStage)
        )?.let {
            val annotationApi = mapView?.annotations
            pointAnnotationManager = annotationApi?.createPointAnnotationManager(mapView!!)!!

            // Set options for the resulting symbol layer.
            val pointAnnotationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(
                    Point.fromLngLat(
                        task.location?.longitude!!,
                        task.location?.latitude!!
                    )
                )
                .withIconImage(it)
                .withIconSize(2.0)

            // add annotation click listener.
            pointAnnotationManager?.addClickListener(object : OnPointAnnotationClickListener {
                override fun onAnnotationClick(annotation: PointAnnotation): Boolean {
                    val position = currentStage - task.stage
                    if (position >= 0) {
                        binding.TaskRecycler.smoothScrollToPosition(position)
                    }
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


}