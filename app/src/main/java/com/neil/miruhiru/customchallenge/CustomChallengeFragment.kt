package com.neil.miruhiru.customchallenge

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.NumberPicker
import com.mapbox.maps.extension.style.expressions.dsl.generated.format
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentCustomChallengeBinding


class CustomChallengeFragment : Fragment() {


    private lateinit var binding: FragmentCustomChallengeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomChallengeBinding.inflate(inflater, container, false)

        binding.stageNumberPicker.maxValue = 5
        binding.stageNumberPicker.minValue = 1

        binding.hourNumberPicker.maxValue = 10
        binding.hourNumberPicker.minValue = 0

        binding.minuteNumberPicker.maxValue = 60
        binding.minuteNumberPicker.minValue = 0
        binding.minuteNumberPicker.setFormatter(object : NumberPicker.Formatter {
            override fun format(p0: Int): String {
                TODO("Not yet implemented")
            }

        })

        val minuteNumberPickerFormatter = NumberPicker.Formatter()










        return binding.root
    }
}