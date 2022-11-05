package com.neil.miruhiru.taskdetail

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentTaskDetailBinding

class TaskDetailFragment : Fragment() {

    private lateinit var binding: FragmentTaskDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskDetailBinding.inflate(inflater, container, false)
        val locationInfo = TaskDetailFragmentArgs.fromBundle(requireArguments()).locationInfo

        Glide.with(binding.taskImage.context).load(locationInfo.image).centerCrop().apply(
            RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
        ).into(binding.taskImage)







        return binding.root
    }

}