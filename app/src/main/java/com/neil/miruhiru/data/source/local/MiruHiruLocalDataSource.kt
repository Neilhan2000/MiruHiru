package com.neil.miruhiru.data.source.local

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.neil.miruhiru.data.Notification
import com.neil.miruhiru.data.Result
import com.neil.miruhiru.data.source.MiruHiruDataSource

class MiruHiruLocalDataSource(val context: Context) : MiruHiruDataSource {

    override suspend fun cleanEventSingle(): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun cleanEventMultiple(): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun cleanUserCurrentEvent(userDocumentId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override suspend fun getUserDocumentId(): Result<String> {
        TODO("Not yet implemented")
    }

    override suspend fun removeUserProgress(userDocumentId: String): Result<Boolean> {
        TODO("Not yet implemented")
    }

    override fun detectNotifications(userDocumentId: String): MutableLiveData<List<Notification>> {
        TODO("Not yet implemented")
    }
}