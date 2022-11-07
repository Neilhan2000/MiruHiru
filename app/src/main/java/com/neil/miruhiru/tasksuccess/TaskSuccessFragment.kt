package com.neil.miruhiru.tasksuccess

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentTaskSuccessBinding

class TaskSuccessFragment : Fragment() {

    private lateinit var binding: FragmentTaskSuccessBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskSuccessBinding.inflate(inflater, container, false)
        binding.logButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalLogDialogFragment())
        }
        binding.continueButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalTaskFragment())
        }



        return binding.root
    }
}