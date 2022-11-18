package com.neil.miruhiru.customdetail.item

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import androidx.core.net.toUri
import androidx.core.widget.addTextChangedListener
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.neil.miruhiru.R
import com.neil.miruhiru.customdetail.BottomSheetTypeFilter
import com.neil.miruhiru.databinding.*


class BottomStepFragment(private val step: BottomSheetTypeFilter, private val viewModel: BottomSheetViewModel) : Fragment() {

    companion object {
        private const val GALLERY_CODE = 1
    }
    private lateinit var binding: Any


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_CODE && resultCode == Activity.RESULT_OK) {
            (binding as FragmentBottomStepBinding).uploadedTaskImage.setImageURI(data?.data)
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

                // set bottom sheet content
                if (URLUtil.isValidUrl(viewModel.task.image)) {
                    Glide.with(step1Binding.uploadedTaskImage.context).load(viewModel.task.image).centerCrop().apply(
                        RequestOptions().placeholder(R.drawable.ic_image_loading).error(R.drawable.ic_image_loading)
                    ).into(step1Binding.uploadedTaskImage)
                } else {
                    step1Binding.uploadedTaskImage.setImageURI(viewModel.task.image.toUri())
                }
                step1Binding.editTextTaskName.setText(viewModel.task.name)
                step1Binding.editTextIntroduction.setText(viewModel.task.introduction)


                // listener
                step1Binding.editTextTaskName.addTextChangedListener {
                    viewModel.setTaskName(it.toString())
                }
                step1Binding.editTextIntroduction.addTextChangedListener {
                    viewModel.setTaskIntroduction(it.toString())
                }
                step1Binding.uploadedTaskImage.setOnClickListener {
                    viewModel.selectImage(this)
                }

                step1Binding.root

            }
            else -> {
                binding =  FragmentBottomStep2Binding.inflate(inflater, container, false)
                val step2Binding = binding as FragmentBottomStep2Binding

                // set bottom sheet content
                step2Binding.editTextGuide.setText(viewModel.task.guide)
                step2Binding.editTextQuestion.setText(viewModel.task.question)
                step2Binding.editTextAns.setText(viewModel.task.answer)

                // listener
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