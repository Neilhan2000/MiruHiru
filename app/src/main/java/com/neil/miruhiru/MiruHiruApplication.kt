package com.neil.miruhiru

import android.app.Application
import com.neil.miruhiru.data.source.MiruHiruRepository
import com.neil.miruhiru.util.ServiceLocator
import kotlin.properties.Delegates

class MiruHiruApplication : Application() {

    // Depends on the flavor
    val miruHiruRepository: MiruHiruRepository
        get() = ServiceLocator.provideTasksRepository(this)

    companion object {
        var instance: MiruHiruApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}