package com.neil.miruhiru.customdetail

import android.content.Context
import android.content.DialogInterface
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.activity.OnBackPressedCallback
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.addOnMapClickListener
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentCustomDetailBinding
import timber.log.Timber

class CustomDetailFragment : Fragment() {

    private lateinit var mapView: MapView
    private lateinit var pointAnnotationManager: PointAnnotationManager
    private lateinit var mapBoxMap: MapboxMap
    private lateinit var binding: FragmentCustomDetailBinding
    private val viewModel: CustomDetailViewModel by lazy {
        ViewModelProvider(this).get(CustomDetailViewModel::class.java)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFragmentResultListener("customDetail") { requestKey, bundle ->
            val result = bundle.getParcelable<Task>("task")
            Timber.i("result $result")
            if (result != null) {
                viewModel.task = result

                if (viewModel.isInputValid()) {
                    binding.nextButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))

                    Timber.i("original task${viewModel.originalTask} normal task ${viewModel.task}")
                    // if the task do not change we don't change the button
                    if (viewModel.originalTask != viewModel.task && binding.nextButton.text == getString(R.string.update)) {
                        viewModel.needToUpdate()
                        binding.nextButton.setOnClickListener {
                            viewModel.updateTask()

                            viewModel.originalTask.id = result.id
                            // we set the image when we upload the image to firebase (in viewModel not here)
                            viewModel.originalTask.stage = result.stage
                            viewModel.originalTask.name = result.name
                            viewModel.originalTask.question = result.question
                            viewModel.originalTask.answer = result.answer
                            viewModel.originalTask.location = result.location
                            viewModel.originalTask.introduction = result.introduction
                            viewModel.originalTask.guide = result.guide
                        }

                    } else if (viewModel.originalTask == viewModel.task && binding.nextButton.text == getString(R.string.update)){
                        binding.nextButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
                    }
                } else {
                    binding.nextButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
                }
            }
        }

        setFragmentResultListener("fromOverview") { requestKey, bundle ->
            val result = bundle.getParcelable<Task>("task")
            Timber.i("result from overview $result")
            if (result != null) {

                viewModel.task = result

                viewModel.originalTask.id = result.id
                viewModel.originalTask.image = result.image
                viewModel.originalTask.stage = result.stage
                viewModel.originalTask.name = result.name
                viewModel.originalTask.question = result.question
                viewModel.originalTask.answer = result.answer
                viewModel.originalTask.location = result.location
                viewModel.originalTask.introduction = result.introduction
                viewModel.originalTask.guide = result.guide

                addAnnotationToMap(Point.fromLngLat(result.location.longitude, result.location.latitude))
                binding.editButton.isEnabled = true
                binding.editButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                binding.editCancelButton.isEnabled = true
                binding.editCancelButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))

                binding.nextButton.text = getString(R.string.update)
                binding.nextButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
                binding.nextButton.setOnClickListener(null)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomDetailBinding.inflate(inflater, container, false)
        mapView = binding.mapView
        mapBoxMap = mapView.getMapboxMap()
        val annotationApi = mapView.annotations
        pointAnnotationManager = annotationApi.createPointAnnotationManager(mapView)
        viewModel.customChallengeId = CustomDetailFragmentArgs.fromBundle(requireArguments()).customChallengeId

        mapBoxMap.addOnMapClickListener { point ->
            pointAnnotationManager.deleteAll()
            addAnnotationToMap(point)
            changeButtonStatus()
            if (binding.nextButton.text == getString(R.string.update)) {
                viewModel.setOriginalTaskLocation(point)
                viewModel.setTaskLocation(point)

                binding.nextButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                binding.nextButton.setOnClickListener(null)
                Timber.i("task image ${viewModel.task.image} original image ${viewModel.originalTask.image}")
                viewModel.needToUpdate()
                binding.nextButton.setOnClickListener {
                    viewModel.updateTask()
                }
            } else {
                viewModel.setTaskLocation(point)
            }
            false
        }

        binding.closeGuideIcon.setOnClickListener {
            binding.guideCardView.visibility = View.GONE
        }
        binding.editButton.setOnClickListener {
            val result = viewModel.task
            // pass data to BottomSheetFragment
            setFragmentResult("bottomSheet", bundleOf("task" to result))
            this.findNavController().navigate(NavGraphDirections.actionGlobalCustomBottomSheetFragment())
        }
        binding.editCancelButton.setOnClickListener {
            pointAnnotationManager.deleteAll()
            changeButtonStatus()
            viewModel.deleteTask()
        }
        binding.nextButton.setOnClickListener {
            if (viewModel.isInputValid()) {
                viewModel.postTask()
            }
        }

        viewModel.navigateToCustomDetailFragment.observe(viewLifecycleOwner, Observer { postTaskSuccess ->
            if (postTaskSuccess) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalCustomDetailFragment(viewModel.customChallengeId))
                viewModel.navigateToCustomDetailFragmentCompleted()
            }
        })

        viewModel.navigateToOverviewFragment.observe(viewLifecycleOwner, Observer { postLastTaskSuccess ->
            if (postLastTaskSuccess) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalOverviewFragment(viewModel.customChallengeId))
                viewModel.navigateToOverviewFragmentCompleted()
            }
        })

        // continuing unfinished editing will not show guide text
        if (this.findNavController().previousBackStackEntry?.destination?.id == R.id.overviewFragment) {
            binding.guideCardView.visibility = View.GONE
        }
        // update current stage value from 0 to total stage(excluding total 1),
        // and determine should we need to load firebase data(only first time and continue unfinished editing)
        when (UserManager.customCurrentStage) {
            -1 -> {
                viewModel.loadFirstOrUnfinishedEditing()
                Timber.i("-1 -> current stage ${UserManager.customCurrentStage}, total stage ${UserManager.customTotalStage}")
                binding.customGuideText.text = getString(R.string.customGuideTextOne) + "1" + getString(R.string.customGuideTextTwo)
            }
            else -> {
                UserManager.customCurrentStage = UserManager.customCurrentStage?.plus(1)
                Timber.i("else -> current stage ${UserManager.customCurrentStage}, total stage ${UserManager.customTotalStage}")
                binding.customGuideText.text = getString(R.string.customGuideTextOne) + "${UserManager.customCurrentStage}" + getString(R.string.customGuideTextTwo)

                if (UserManager.customCurrentStage == UserManager.customTotalStage) {
                    viewModel.setLastStage()
                }
            }
        }

        // in the editing of last stage, we change the next button content via live data
        // is not valid url means that the task is come from overview page, we don't change the next button here
        viewModel.isLastStage.observe(viewLifecycleOwner, Observer { isLastStage ->
            if (isLastStage && !URLUtil.isValidUrl(viewModel.task.image)) {
                binding.nextButton.text = getString(R.string.complete)
                Timber.i("is last stage -> current stage ${UserManager.customCurrentStage}, total stage ${UserManager.customTotalStage}")
            }
        })

        // observe the isUpdated value and change the next button
        viewModel.isUpdated.observe(viewLifecycleOwner, Observer { updateSuccess ->
            if (updateSuccess) {
                binding.nextButton.setOnClickListener(null)
                binding.nextButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
            }
        })


        // override back press
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    UserManager.customCurrentStage = null
                    UserManager.customTotalStage = null
                    if (binding.nextButton.text == getString(R.string.update)) {
                        if (viewModel.isUpdated.value == false) {
                            val defaultBuilder = AlertDialog.Builder(requireContext())
                                .setTitle("尚未儲存")
                                .setMessage("編輯尚未更新，確定要退出嗎?")
                                .setPositiveButton("確定", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        this@CustomDetailFragment.findNavController().navigateUp()
                                    }
                                })
                                .setNegativeButton("取消", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                    }
                                }).show()
                            defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
                                .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                            defaultBuilder.getButton(DialogInterface.BUTTON_NEGATIVE)
                                .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                        } else {
                            this@CustomDetailFragment.findNavController().navigateUp()
                        }
                    } else {
                        if (this@CustomDetailFragment.findNavController().previousBackStackEntry?.destination?.id ==
                                R.id.overviewFragment) {
                            val defaultBuilder = AlertDialog.Builder(requireContext())
                                .setTitle("暫停編輯")
                                .setMessage("進度會自動儲存，要暫時退出嗎?")
                                .setPositiveButton("確定", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        this@CustomDetailFragment.findNavController().navigateUp()
                                    }
                                })
                                .setNegativeButton("取消", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                    }
                                })
                                .setNeutralButton("清除挑戰", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        viewModel.cleanCustomChallenge(viewModel.customChallengeId)
                                        this@CustomDetailFragment.findNavController().navigate(NavGraphDirections.actionGlobalCustomFragment())
                                    }
                                })
                                .show()
                            defaultBuilder.getButton(DialogInterface.BUTTON_POSITIVE)
                                .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                            defaultBuilder.getButton(DialogInterface.BUTTON_NEGATIVE)
                                .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                            defaultBuilder.getButton(DialogInterface.BUTTON_NEUTRAL)
                                .setTextColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                        } else {
                            val defaultBuilder = AlertDialog.Builder(requireContext())
                                .setTitle("暫停編輯")
                                .setMessage("進度會自動儲存，要暫時退出嗎?")
                                .setPositiveButton("確定", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        this@CustomDetailFragment.findNavController().navigate(NavGraphDirections.actionGlobalCustomFragment())
                                    }
                                })
                                .setNegativeButton("取消", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                    }
                                })
                                .setNeutralButton("清除挑戰", object : DialogInterface.OnClickListener {
                                    override fun onClick(p0: DialogInterface?, p1: Int) {
                                        viewModel.cleanCustomChallenge(viewModel.customChallengeId)
                                        this@CustomDetailFragment.findNavController().navigate(NavGraphDirections.actionGlobalCustomFragment())
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
                    }
                }
            })
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