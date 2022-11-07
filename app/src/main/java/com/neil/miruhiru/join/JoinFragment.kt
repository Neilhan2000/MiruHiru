package com.neil.miruhiru.join

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentJoinBinding

class JoinFragment : Fragment() {

    private lateinit var binding: FragmentJoinBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoinBinding.inflate(inflater, container, false)
        binding.openCameraButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalScanFragment())
        }

        return binding.root
    }

}