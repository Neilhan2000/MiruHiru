package com.neil.miruhiru.customdetail

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.neil.miruhiru.R
import com.neil.miruhiru.chat.ChatDialogViewModel
import com.neil.miruhiru.customdetail.item.BottomSheetViewModel
import com.neil.miruhiru.databinding.FragmentCustomBottomSheetBinding

class CustomBottomSheetFragment : BottomSheetDialogFragment() {

    private val viewModel: BottomSheetViewModel by lazy {
        ViewModelProvider(this).get(BottomSheetViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentCustomBottomSheetBinding.inflate(inflater, container, false)

        binding.viewPager.adapter = BottomSheetPageAdapter(this)
        binding.viewPager.isUserInputEnabled = false

        binding.editNextButton.setOnClickListener {
            binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1)
        }

        if (binding.viewPager.currentItem == 1) {
            binding.editNextButton.text = "完成"
            binding.editNextButton.setOnClickListener(null)
            binding.editNextButton.setOnClickListener {
                this.dismiss()
            }
        }


        return binding.root
    }
}