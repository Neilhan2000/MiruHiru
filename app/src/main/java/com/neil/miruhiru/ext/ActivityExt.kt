package com.neil.miruhiru.ext

import android.app.Activity
import com.neil.miruhiru.MiruHiruApplication
import com.neil.miruhiru.factory.ViewModelFactory

fun Activity.getVmFactory(): ViewModelFactory {
    val repository = (applicationContext as MiruHiruApplication).miruHiruRepository
    return ViewModelFactory(repository)
}