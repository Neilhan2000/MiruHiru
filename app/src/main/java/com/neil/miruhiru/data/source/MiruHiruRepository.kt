package com.neil.miruhiru.data.source

import androidx.lifecycle.MutableLiveData
import com.neil.miruhiru.data.*
import com.neil.miruhiru.data.Notification

interface MiruHiruRepository {

    suspend fun cleanEventSingle(): Result<String>

    suspend fun cleanEventMultiple(): Result<String>

    suspend fun cleanUserCurrentEvent(userDocumentId: String): Result<Boolean>

    suspend fun getUserDocumentId(): Result<String>

    suspend fun removeUserProgress(userDocumentId: String): Result<Boolean>

    fun detectNotifications(userDocumentId: String): MutableLiveData<List<Notification>>

}