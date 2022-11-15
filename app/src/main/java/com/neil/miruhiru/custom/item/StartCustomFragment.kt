package com.neil.miruhiru.custom.item

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentStartCustomBinding


class StartCustomFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentStartCustomBinding.inflate(inflater, container, false)

        binding.startCreateButton.setOnClickListener {
            this.findNavController().navigate(NavGraphDirections.actionGlobalCustomDetailFragment())
        }


        return binding.root
    }
}