package com.neil.miruhiru.taskdetail

import android.os.Bundle
import android.view.Display
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.UserManager
import com.neil.miruhiru.data.LocationInfo
import com.neil.miruhiru.databinding.FragmentTaskDetailBinding
import com.neil.miruhiru.ext.glideImageCenter
import timber.log.Timber

class TaskDetailFragment : Fragment() {

    companion object {
        private const val DISTANCE_RESTRICTION = 30
    }
    private lateinit var binding: FragmentTaskDetailBinding
    private lateinit var locationInfo: LocationInfo
    private val viewModel: TaskDetailViewModel by lazy {
        ViewModelProvider(this).get(TaskDetailViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        locationInfo = TaskDetailFragmentArgs.fromBundle(requireArguments()).locationInfo
        binding.taskImage.glideImageCenter(locationInfo.image, R.drawable.image_placeholder)
        binding.TaskTitle.text = locationInfo.name
        if (locationInfo.distance != 0) {
            binding.distance.text = getString(R.string.distance_to_task, locationInfo.distance)
        } else {
            binding.distance.text = getString(R.string.no_gps)
        }

        binding.introductionContent.text = locationInfo.introduction
        if (locationInfo.distance > DISTANCE_RESTRICTION) {

            // allow test account to remove distance restriction
            if (UserManager.userId == getString(R.string.my_gmail) ||
                UserManager.userId == getString(R.string.miru_gmail) ||
                UserManager.userId == getString(R.string.hiru_gmail)) {

                binding.answerButton.setOnClickListener {
                    this.findNavController().navigate(NavGraphDirections.actionGlobalTaskDialogFragment(locationInfo))
                }
            } else {
                binding.answerButton.text = getString(R.string.go_last_page)
                binding.answerButton.setOnClickListener {
                    this.findNavController().navigateUp()
                }
                Toast.makeText(requireContext(), getString(R.string.distance_too_far), Toast.LENGTH_LONG).show()
            }
        } else if (UserManager.userId == getString(R.string.my_gmail) ||
            UserManager.userId == getString(R.string.miru_gmail) ||
            UserManager.userId == getString(R.string.hiru_gmail)) {

            binding.answerButton.setOnClickListener {
                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskDialogFragment(locationInfo))
            }
        } else if (binding.distance.text == getString(R.string.no_gps)) {

            binding.answerButton.text = getString(R.string.go_last_page)
            binding.answerButton.setOnClickListener {
                this.findNavController().navigateUp()
            }
            Toast.makeText(requireContext(), getString(R.string.need_to_open_gps), Toast.LENGTH_LONG).show()
        } else {

            binding.answerButton.setOnClickListener {
                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskDialogFragment(locationInfo))
            }
        }

        return binding.root
    }
}