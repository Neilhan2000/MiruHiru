package com.neil.miruhiru.customdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.neil.miruhiru.R
import com.neil.miruhiru.customdetail.item.CustomViewModel
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.databinding.FragmentCustomBottomSheetBinding
import timber.log.Timber

class CustomBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: CustomViewModel by lazy {
        ViewModelProvider(this).get(CustomViewModel::class.java)
    }
    private lateinit var binding: FragmentCustomBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }


    private var currentItem = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomBottomSheetBinding.inflate(inflater, container, false)

        binding.viewPager.adapter = BottomSheetPageAdapter(this, viewModel)
        binding.viewPager.isUserInputEnabled = false
        setupButton()



        return binding.root
    }

    private fun setupButton() {
        binding.editNextButton.setOnClickListener {

            if (currentItem == 0) {
                binding.editNextButton.text = "完成"
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1)
                binding.editBackButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                currentItem ++
                binding.editNextButton.setOnClickListener {
                    // post task
                    this.dismiss()
                }
            }
        }

        binding.editBackButton.setOnClickListener {

            if (currentItem == 1) {
                binding.editNextButton.text = "下一步"
                binding.editBackButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey))
                currentItem --
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() - 1)

                binding.editNextButton.setOnClickListener {
                    if (currentItem == 0) {
                        binding.editNextButton.text = "完成"
                        binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1)
                        binding.editBackButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.deep_yellow))
                        currentItem ++
                        binding.editNextButton.setOnClickListener {
                            // post task
                            this.dismiss()
                        }
                    }
                }
            }
        }
    }
}