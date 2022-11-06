package com.neil.miruhiru.taskdetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.data.LocationInfo
import com.neil.miruhiru.databinding.FragmentTaskDetailBinding

class TaskDetailFragment : Fragment() {

    private lateinit var binding: FragmentTaskDetailBinding
    private lateinit var locationInfo: LocationInfo

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        locationInfo = TaskDetailFragmentArgs.fromBundle(requireArguments()).locationInfo
        setupScreen()

        return binding.root
    }
    private fun setupScreen() {
        Glide.with(binding.taskImage.context).load(locationInfo.image).centerCrop().apply(
            RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
        ).into(binding.taskImage)
        binding.TaskTitle.text = locationInfo.name
        binding.distance.text = "距離任務點 ${locationInfo.distance} Ms"
        binding.introductionContent.text = locationInfo.introduction
        if (locationInfo.distance > 50) {
            binding.answerButton.text = "回上一頁"
            binding.answerButton.setOnClickListener {
                this.findNavController().navigateUp()
            }
            Toast.makeText(requireContext(), "距離任務點太遠囉，靠近一點在回答八OuO", Toast.LENGTH_LONG).show()
        } else {
            binding.answerButton.setOnClickListener {
                this.findNavController().navigate(NavGraphDirections.actionGlobalTaskDialogFragment(locationInfo))
            }
        }

    }

}