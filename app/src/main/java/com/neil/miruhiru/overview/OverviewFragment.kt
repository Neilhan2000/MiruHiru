package com.neil.miruhiru.overview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.challengesuccess.ChallengeSuccessFragmentDirections
import com.neil.miruhiru.databinding.FragmentOverviewBinding
import com.neil.miruhiru.network.LoadingStatus
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*


class OverviewFragment : Fragment() {

    private lateinit var binding: FragmentOverviewBinding
    private val viewModel: OverviewViewModel by lazy {
        ViewModelProvider(this).get(OverviewViewModel::class.java)
    }
    private val scope = CoroutineScope(Job() + Dispatchers.Main)
    private lateinit var job: Job
    private var jobInitialized = false
    private lateinit var taskAdapter: OverViewAdapter
    private val simpleCallback = object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), 0) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val startPosition = viewHolder.adapterPosition
            val endPosition = target.adapterPosition

            Collections.swap(viewModel.customTaskList.value, startPosition, endPosition)
            recyclerView.itemAnimator = DefaultItemAnimator()

            // prevent from cancelling current move animation
            if (jobInitialized) { job.cancel() }
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)

            Timber.i("animation duration ${recyclerView.itemAnimator?.moveDuration}")
            recyclerView.adapter?.itemCount?.let { itemCount ->
                scope.launch {
                    job = launch {
                        jobInitialized = true
                        delay(250)
                        recyclerView.itemAnimator = null
                        recyclerView.adapter?.notifyItemRangeChanged(0, itemCount)
                        this.cancel()
                    }
                }
            }

            setStartButtonToUpdate()
            return true
        }
        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOverviewBinding.inflate(inflater, container, false)
        viewModel.customChallengeId = OverviewFragmentArgs.fromBundle(requireArguments()).customChallengeId

        val itemTouchHelper = ItemTouchHelper(simpleCallback)
        itemTouchHelper.attachToRecyclerView(binding.customTaskRecycler)
        taskAdapter = OverViewAdapter(viewModel)  { task ->
            // send task and navigate to custom detail page
            setFragmentResult("fromOverview", bundleOf("task" to task))
            UserManager.customCurrentStage = null
            UserManager.customTotalStage = null
            this.findNavController().navigate(NavGraphDirections.actionGlobalCustomDetailFragment(viewModel.customChallengeId))
        }
        binding.customTaskRecycler.adapter = taskAdapter

        viewModel.customTaskList.observe(viewLifecycleOwner, Observer {
            taskAdapter.submitList(it)
        })
        viewModel.loadCustomTasks()

        viewModel.resetStartButton.observe(viewLifecycleOwner, Observer { updateSuccess ->
            if (updateSuccess) {
                resetStartButton()
                viewModel.resetStartButtonCompleted()
            }
        })

        // observe editing status to determine button feature
        viewModel.editingCompleted.observe(viewLifecycleOwner, Observer { editingCompleted ->
            if (editingCompleted) {
                binding.editOrUploadButton.text = getString(R.string.upload)
                binding.editOrUploadButton.setOnClickListener {
                    viewModel.uploadCustomChallengeBeVerified()
                }

            } else {
                binding.editOrUploadButton.text = getString(R.string.edit)
                UserManager.customCurrentStage = null
                UserManager.customTotalStage = null
                binding.editOrUploadButton.setOnClickListener {
                    this.findNavController().navigate(NavGraphDirections.actionGlobalCustomDetailFragment(viewModel.customChallengeId))
                }
                binding.startCustomButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
                binding.startCustomButton.setOnClickListener(null)
            }
        })

        // observe upload to change button
        viewModel.uploadToBeVerified.observe(viewLifecycleOwner, Observer { uploaded ->
            if (uploaded) {
                binding.editOrUploadButton.text = getString(R.string.uploaded)
                binding.editOrUploadButton.setOnClickListener(null)
            }
        })

        binding.startCustomButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeDetailFragment(viewModel.customChallengeId))
        }

        binding.completeCustomButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalCustomFragment(1))
        }

        // handle back press
        requireActivity().onBackPressedDispatcher
            .addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val previousFragment = findNavController().previousBackStackEntry?.destination
                    if (previousFragment?.id == R.id.customDetailFragment) {
                        this@OverviewFragment.findNavController().navigate(NavGraphDirections.actionGlobalCustomFragment(1))
                    } else {
                        this@OverviewFragment.findNavController().navigateUp()
                    }
                }
            })

        // observe loading status and show progress bar
        viewModel.loadingStatus.observe(viewLifecycleOwner, Observer { status ->
            when (status) {
                LoadingStatus.LOADING -> {
                    MainActivity.getInstanceFromMainActivity().window.setFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.VISIBLE
                }
                LoadingStatus.DONE -> {
                    MainActivity.getInstanceFromMainActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.GONE
                }
                LoadingStatus.ERROR -> {
                    MainActivity.getInstanceFromMainActivity().window.clearFlags(
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    )
                    binding.progressBar2.visibility = View.GONE
                    Toast.makeText(requireContext(), "loading error", Toast.LENGTH_SHORT).show()
                }
            }
        })

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    private fun setStartButtonToUpdate() {
        binding.startCustomButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
        binding.startCustomButton.text = getString(R.string.update)
        binding.startCustomButton.setOnClickListener(null)
        binding.startCustomButton.setOnClickListener {
            viewModel.updateCustomChallenge()
        }
    }

    private fun resetStartButton() {
        Timber.i("reset ${viewModel.editingCompleted.value}")
        if (viewModel.editingCompleted.value == true) {
            binding.startCustomButton.text = getString(R.string.challenge)
            binding.startCustomButton.setOnClickListener(null)
            binding.startCustomButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
            binding.startCustomButton.setOnClickListener {
                // navigate to challenge detail
                this.findNavController().navigate(NavGraphDirections.actionGlobalChallengeDetailFragment(viewModel.customChallengeId))
            }
        } else {
            binding.startCustomButton.text = getString(R.string.challenge)
            binding.startCustomButton.setOnClickListener(null)
            binding.startCustomButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
        }
    }
}