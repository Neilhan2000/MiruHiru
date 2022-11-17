package com.neil.miruhiru.overview

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.neil.miruhiru.databinding.FragmentOverviewBinding


class OverviewFragment : Fragment() {

    private lateinit var binding: FragmentOverviewBinding
    private val viewModel: OverviewViewModel by lazy {
        ViewModelProvider(this).get(OverviewViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentOverviewBinding.inflate(inflater, container, false)
        viewModel.customChallengeId = OverviewFragmentArgs.fromBundle(requireArguments()).customChallengeId


        
        viewModel.customTaskList.observe(viewLifecycleOwner, Observer {
            // submit list
        })





        return binding.root
    }

}