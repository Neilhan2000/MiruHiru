package com.neil.miruhiru.util

import android.content.Context
import androidx.annotation.VisibleForTesting
import com.neil.miruhiru.data.source.DefaultMiruHiruRepository
import com.neil.miruhiru.data.source.MiruHiruDataSource
import com.neil.miruhiru.data.source.MiruHiruRepository
import com.neil.miruhiru.data.source.local.MiruHiruLocalDataSource
import com.neil.miruhiru.data.source.remote.MiruHiruRemoteDataSource

object ServiceLocator {
    @Volatile
    var miruHiruRepository: MiruHiruRepository? = null
        @VisibleForTesting set

    fun provideTasksRepository(context: Context): MiruHiruRepository {
        synchronized(this) {
            return miruHiruRepository
                ?: createMiruHiruRepository(context)
        }
    }

    private fun createMiruHiruRepository(context: Context): MiruHiruRepository {
        return DefaultMiruHiruRepository(
            MiruHiruRemoteDataSource,
            createLocalDataSource(context)
        )
    }

    private fun createLocalDataSource(context: Context): MiruHiruDataSource {
        return MiruHiruLocalDataSource(context)
    }
}