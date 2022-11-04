package com.neil.miruhiru.task

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentTaskBinding

class TaskFragment : Fragment() {

    // animation
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_form_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(), R.anim.fab_to_bottom_anim) }
    private var clicked = false

    private lateinit var binding: FragmentTaskBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)

        // floating action button
        binding.fabAdd.setOnClickListener {
            onAddButtonClick()
        }
        binding.fabChat.setOnClickListener {
            Toast.makeText(requireContext(), "chat clicked", Toast.LENGTH_SHORT).show()
        }
        binding.fabAndroid.setOnClickListener {
            Toast.makeText(requireContext(), "android clicked", Toast.LENGTH_SHORT).show()
        }

        return binding.root
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
        } else {
            binding.fabChat.visibility = View.INVISIBLE
            binding.fabAndroid.visibility = View.INVISIBLE
        }
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.fabChat.animation = fromBottom
            binding.fabAndroid.animation = fromBottom
            binding.fabAdd.startAnimation(rotateOpen)
        } else {
            binding.fabChat.animation = toBottom
            binding.fabAndroid.animation = toBottom
            binding.fabAdd.startAnimation(rotateClose)
        }
    }
}