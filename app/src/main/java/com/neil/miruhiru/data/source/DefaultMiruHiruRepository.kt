package com.neil.miruhiru.data.source

import androidx.lifecycle.MutableLiveData
import com.neil.miruhiru.data.*
import com.neil.miruhiru.data.Notification

class DefaultMiruHiruRepository(
    private val miruHiruRemoteDataSource: MiruHiruDataSource,
    private val miruHiruLocalDataSource: MiruHiruDataSource
) : MiruHiruRepository {
    override suspend fun cleanEventSingle(): Result<String> {
        return miruHiruRemoteDataSource.cleanEventSingle()
    }

    override suspend fun cleanEventMultiple(): Result<String> {
        return miruHiruRemoteDataSource.cleanEventMultiple()
    }

    override suspend fun cleanUserCurrentEvent(userDocumentId: String): Result<Boolean> {
        return miruHiruRemoteDataSource.cleanUserCurrentEvent(userDocumentId)
    }

    override suspend fun getUserDocumentId(): Result<String> {
        return miruHiruRemoteDataSource.getUserDocumentId()
    }

    override suspend fun removeUserProgress(userDocumentId: String): Result<Boolean> {
        return miruHiruRemoteDataSource.removeUserProgress(userDocumentId)
    }

    override fun detectNotifications(userDocumentId: String): MutableLiveData<List<Notification>> {
        return miruHiruRemoteDataSource.detectNotifications(userDocumentId)
    }

}