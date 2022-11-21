package com.neil.miruhiru.customdetail.item

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint
import com.mapbox.geojson.Point
import com.neil.miruhiru.data.Challenge
import com.neil.miruhiru.data.Task
import com.neil.miruhiru.log.LogDialogFragment

class BottomSheetViewModel : ViewModel() {

    companion object {
        private const val GALLERY_CODE = 1
    }

    val challenge = Challenge()

    var task = Task()

    fun setTaskName(name: String) {
        task.name = name
    }

    fun setTaskIntroduction(introduction: String) {
        task.introduction = introduction
    }

    fun setTaskImage(uri: Uri) {
        task.image = uri.toString()
    }

    fun setTaskGuide(guide: String) {
        task.guide = guide
    }

    fun setTaskQuestion(question: String) {
        task.question = question
    }

    fun setTaskAnswer(answer: String) {
        task.answer = answer
    }

    fun selectImage(fragment: BottomStepFragment) {
        val intent = Intent().setType("image/*").setAction(Intent.ACTION_GET_CONTENT)
        fragment.startActivityForResult(Intent.createChooser(intent,"gallery"), GALLERY_CODE)
    }





}