package com.neil.miruhiru

import android.app.Application
import kotlin.properties.Delegates

class MiruHiruApplication : Application() {

    // Depends on the flavor,
//    val stylishRepository: StylishRepository
//        get() = ServiceLocator.provideTasksRepository(this)

    companion object {
        var instance: MiruHiruApplication by Delegates.notNull()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}