package com.neil.miruhiru.log

import android.annotation.SuppressLint
import android.app.Application
import android.app.ProgressDialog
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.location.Location
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.graphics.TypefaceCompatUtil.getTempFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

import com.neil.miruhiru.MainActivity
import com.neil.miruhiru.data.Challenge
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class LogViewModel(application: Application) : AndroidViewModel(application) {

    private val viewModelApplication = application

    private val _uploadStatus = MutableLiveData<Boolean>()
    val uploadStatus: LiveData<Boolean>
        get() = _uploadStatus
    lateinit var imageUri: Uri

    fun loadImage() {
        val db = Firebase.firestore



    }


    fun selectImage() {
        val intent = Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
            .addFlags(FLAG_ACTIVITY_NEW_TASK)
        MainActivity.getInstanceFromViewModel()?.startActivityForResult(intent, 100)
    }

    fun uploadImage() {
        val progressDialog = ProgressDialog(MainActivity.getInstanceFromViewModel())
        progressDialog.setMessage("上傳中...")
        progressDialog.setCancelable(false)
        progressDialog.show()

        val formatter = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)
        val storageReference = FirebaseStorage.getInstance().getReference("images/$fileName")
        storageReference.putFile(imageUri)
            .addOnSuccessListener {
                Toast.makeText(viewModelApplication, "上傳成功", Toast.LENGTH_SHORT).show()
                if (progressDialog.isShowing) progressDialog.dismiss()
                _uploadStatus.value = true
            }
            .addOnFailureListener {
                Toast.makeText(viewModelApplication, "上傳失敗，原因:${it.message}", Toast.LENGTH_SHORT).show()
                if (progressDialog.isShowing) progressDialog.dismiss()
                _uploadStatus.value = false
            }


    }
}