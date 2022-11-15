package com.neil.miruhiru.customdetail.item

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import com.neil.miruhiru.customdetail.BottomSheetTypeFilter
import com.neil.miruhiru.databinding.*
import com.neil.miruhiru.log.LogDialogFragment


class BottomStepFragment(private val step: BottomSheetTypeFilter, private val viewModel: CustomViewModel) : Fragment() {

    companion object {
        private const val GALLERY_CODE = 1
    }
    private lateinit var binding: Any


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            (binding as FragmentBottomStepBinding).uploadedChallengeImage.setImageURI(data?.data)
            data?.data?.let { viewModel.setTaskImage(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return when (step) {
            BottomSheetTypeFilter.STEP1 -> {
                binding = FragmentBottomStepBinding.inflate(inflater, container, false)
                val step1Binding = binding as FragmentBottomStepBinding

                step1Binding.editTextChallengeName.addTextChangedListener {
                    viewModel.setTaskName(it.toString())
                }
                step1Binding.editTextIntroduction.addTextChangedListener {
                    viewModel.setTaskIntroduction(it.toString())
                }
                step1Binding.uploadedChallengeImage.setOnClickListener {
                    viewModel.selectImage(this)
                }

                step1Binding.root

            }
            else -> {
                binding =  FragmentBottomStep2Binding.inflate(inflater, container, false)
                val step2Binding = binding as FragmentBottomStep2Binding

                step2Binding.editTextGuide.addTextChangedListener {
                    viewModel.setTaskGuide(it.toString())
                }
                step2Binding.editTextQuestion.addTextChangedListener {
                    viewModel.setTaskQuestion(it.toString())
                }
                step2Binding.editTextAns.addTextChangedListener {
                    viewModel.setTaskAnswer(it.toString())
                }



                step2Binding.root
            }
        }
    }
}