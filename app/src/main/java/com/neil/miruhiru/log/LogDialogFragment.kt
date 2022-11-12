package com.neil.miruhiru.log

import android.Manifest
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.neil.miruhiru.R
import com.neil.miruhiru.databinding.FragmentLogDialogBinding
import timber.log.Timber
import java.io.ByteArrayOutputStream


class LogDialogFragment : DialogFragment() {

    companion object {
        private const val GALLERY_CODE = 1
        private const val CAMERA_CODE = 2
    }

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

         Timber.i("$requestCode $resultCode data ${data}")
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK) {
            data?.data?.let {
                viewModel.imageUri = it
                binding.uploadedImage.setImageURI(viewModel.imageUri)
            }
        } else if (requestCode == CAMERA_CODE  && resultCode == RESULT_OK) {
            data?.extras?.let {
                val imageBitmap = it.get("data") as Bitmap
                val uri = getImageUriFromBitmap(imageBitmap)
                viewModel.imageUri = uri
                binding.uploadedImage.setImageURI(uri)
            }
        }
    }

    private fun selectImage() {
        val intent = Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
        startActivityForResult(Intent.createChooser(intent,"gallery"), GALLERY_CODE)
    }

    private fun takePhoto() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), 123)
        } else {
            // take photo
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(Intent.createChooser(intent,"camera"), CAMERA_CODE)
        }
    }

    private fun getImageUriFromBitmap(bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    private fun setupScreen() {

        val builder = AlertDialog.Builder(requireActivity())
        builder.setView(binding.root)
        dialog = builder.create()
        dialog.window!!.setBackgroundDrawableResource(R.drawable.dialog_border)
        binding.uploadButton.setOnClickListener {
            if (isInputValid()) {
                // update data then close dialog
                viewModel.uploadImageAndText()
            }
        }
        binding.backToTaskSuccessButton.setOnClickListener {
            this.findNavController().navigateUp()
        }

        binding.editTextLog.addTextChangedListener {
            binding.logSuccessIcon.visibility = View.GONE
            binding.uploadButton.text = "留下紀錄"
            viewModel.text = it.toString()
        }

        binding.uploadedImage.setOnClickListener {
            val language = arrayOf("從相簿上傳", "開啟相機")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("選擇上傳方式")
            builder.setSingleChoiceItems(language, -1) { dialog, which ->
                if (which == 0) {
                    selectImage()
                } else if (which == 1) {
                    takePhoto()
                }
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }

        // observe upload status and update screen
        viewModel.uploadStatus.observe(this, Observer { uploadSuccess ->
            if (uploadSuccess) {
                binding.logSuccessIcon.visibility = View.VISIBLE
                binding.backToTaskSuccessButton.text = "完成"
            } else {
                binding.logSuccessIcon.visibility = View.GONE
                binding.uploadButton.text = "再試一次"
                binding.backToTaskSuccessButton.text = "完成"
            }
        })


    }

    private fun isInputValid(): Boolean {
        return if (binding.editTextLog.text.toString().isEmpty()) {
            Toast.makeText(requireContext(), "至少要輸入文字歐歐歐", Toast.LENGTH_SHORT).show()
            false
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 123) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(requireContext(), "camera permission granted", Toast.LENGTH_SHORT).show()
                // take photo
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(Intent.createChooser(intent,"camera"), CAMERA_CODE)
            } else {
                Toast.makeText(requireContext(), "camera permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}