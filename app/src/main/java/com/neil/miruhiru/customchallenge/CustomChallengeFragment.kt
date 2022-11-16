package com.neil.miruhiru.customchallenge

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import android.widget.Toast
import androidx.core.view.children
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.chip.Chip
import com.mapbox.maps.extension.style.expressions.dsl.generated.format
import com.neil.miruhiru.NavGraphDirections
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentBottomStepBinding
import com.neil.miruhiru.databinding.FragmentCustomChallengeBinding
import com.neil.miruhiru.taskdetail.TaskDetailViewModel
import timber.log.Timber


class CustomChallengeFragment : Fragment() {


    private lateinit var binding: FragmentCustomChallengeBinding
    private val viewModel: CustomChallengeViewModel by lazy {
        ViewModelProvider(this).get(CustomChallengeViewModel::class.java)
    }
    companion object {
        private const val GALLERY_CODE = 1
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            binding.uploadedChallengeImage.setImageURI(data?.data)
            data?.data?.let { viewModel.setTaskImage(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomChallengeBinding.inflate(inflater, container, false)

        binding.stageNumberPicker.maxValue = 5
        binding.stageNumberPicker.minValue = 1
        binding.stageNumberPicker.setOnValueChangedListener { numberPicker, last, current ->
            viewModel.challenge.stage = current
        }

        binding.hourNumberPicker.maxValue = 10
        binding.hourNumberPicker.minValue = 0
        binding.hourNumberPicker.setOnValueChangedListener { _, _, current ->
            viewModel.challenge.timeSpent = (current * 3600 + binding.minuteNumberPicker.value * 600).toLong()
        }

        binding.minuteNumberPicker.maxValue = 5
        binding.minuteNumberPicker.minValue = 0
        binding.minuteNumberPicker.setFormatter(object : NumberPicker.Formatter {
            override fun format(value: Int): String {
                val temp = value * 10
                return "" + temp
            }
        })
        binding.minuteNumberPicker.setOnValueChangedListener { _, _, current ->
            viewModel.challenge.timeSpent = (current * 600 + binding.hourNumberPicker.value * 3600).toLong()
        }

        binding.ChallnegeNextButton.setOnClickListener {
            if (viewModel.isInputValid()) {
                viewModel.postChallenge()
            }
        }

        binding.editTextChallengeName.addTextChangedListener {
            viewModel.challenge.name = it.toString()
        }

        binding.editTextChallengeDescription.addTextChangedListener {
            viewModel.challenge.description = it.toString()
        }

        binding.chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            group.children
                .toList()
                .filter { (it as Chip).isChecked }
                .forEach {
                    viewModel.challenge.type = it.tag.toString()
                }
        }

        binding.uploadedChallengeImage.setOnClickListener {
            viewModel.selectImage(this)
        }

        viewModel.navigateToCustomDetailFragment.observe(viewLifecycleOwner, Observer { postChallengeSuccess ->
            if (postChallengeSuccess) {
                this.findNavController().navigate(NavGraphDirections.actionGlobalCustomDetailFragment())
                viewModel.navigateToCustomDetailFragmentCompleted()
            }
        })


        return binding.root
    }
}