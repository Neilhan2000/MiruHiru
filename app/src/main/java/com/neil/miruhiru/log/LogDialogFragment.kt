package com.neil.miruhiru.log

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.graphics.TypefaceCompatUtil
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.mapbox.android.core.permissions.PermissionsListener
import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentLogDialogBinding
import com.neil.miruhiru.tasksuccess.TaskSAndLogDViewModel
import timber.log.Timber


class LogDialogFragment : DialogFragment() {

    lateinit var binding: FragmentLogDialogBinding
    private lateinit var dialog: AlertDialog
    private val viewModel: LogViewModel by lazy {
        ViewModelProvider(this).get(LogViewModel::class.java)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = FragmentLogDialogBinding.inflate(LayoutInflater.from(context))

        setupScreen()

        return dialog
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

         Timber.i("$requestCode $resultCode data ${data?.data}")
        if (requestCode == 105 && resultCode == RESULT_OK) {
            data?.data?.let {
                viewModel.imageUri = it
                binding.uploadedImage.setImageURI(viewModel.imageUri)
                viewModel.uploadImage()
            }
        }
    }

    private fun selectImage() {
        val intent = Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent,"picture"), 105)
    }

    private fun setupScreen() {

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_border)
        binding.uploadButton.setOnClickListener {
            if (isInputValid()) {
                // update data then close dialog
                viewModel.uploadImage()
                binding.logSuccessIcon.visibility = View.VISIBLE
                binding.uploadedImage.visibility = View.VISIBLE
                binding.uploadButton.text = "上傳成功"

            }
        }

        binding.editTextLog.addTextChangedListener {
            binding.logSuccessIcon.visibility = View.GONE
            binding.uploadButton.text = "留下紀錄"
        }

        binding.uploadedImage.setOnClickListener {
            selectImage()
        }
    }

    private fun isInputValid(): Boolean {
        return if (binding.editTextLog.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "至少要輸入文字歐歐歐", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }
}