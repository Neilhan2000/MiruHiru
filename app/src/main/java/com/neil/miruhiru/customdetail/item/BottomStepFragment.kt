package com.neil.miruhiru.customdetail.item

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.neil.miruhiru.R
import com.neil.miruhiru.customdetail.BottomSheetTypeFilter
import com.neil.miruhiru.databinding.*


class BottomStepFragment(private val step: BottomSheetTypeFilter) : Fragment() {

    private lateinit var binding: Any

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return when (step) {
            BottomSheetTypeFilter.STEP1 -> {
                binding = FragmentBottomStepBinding.inflate(inflater, container, false)
                binding as FragmentBottomStepBinding

                (binding as FragmentBottomStepBinding).editTextChallengeName.setText(R.string.app_name)

                (binding as FragmentBottomStepBinding).root

            }
            else -> {
                binding =  FragmentBottomStep2Binding.inflate(inflater, container, false)
                binding as FragmentBottomStep2Binding
                (binding as FragmentBottomStep2Binding).root
            }
        }
    }
}