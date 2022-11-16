package com.neil.miruhiru.customdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.neil.miruhiru.R
import com.neil.miruhiru.customdetail.item.BottomSheetViewModel
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.databinding.FragmentCustomBottomSheetBinding
import timber.log.Timber

class CustomBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: BottomSheetViewModel by lazy {
        ViewModelProvider(this).get(BottomSheetViewModel::class.java)
    }
    private lateinit var binding: FragmentCustomBottomSheetBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)

        setFragmentResultListener("bottomSheet") { requestKey, bundle ->
            // any type that can be put in a Bundle is supported
            val result = bundle.getParcelable<Task>("task")
            Timber.i("result $result")
            if (result != null) {
                viewModel.task = result

            }
        }
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
                    // pass task to CustomDetailFragment
                    val result = viewModel.task
                    setFragmentResult("customDetail", bundleOf("task" to result))
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