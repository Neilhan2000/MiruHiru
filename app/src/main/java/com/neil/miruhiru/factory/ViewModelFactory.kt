package com.neil.miruhiru.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.neil.miruhiru.MainViewModel
import com.neil.miruhiru.data.source.MiruHiruRepository

/**
 * Created by Neil Tsai in Dec. 2022.
 *
 * Factory for all ViewModels.
 */
@Suppress("UNCHECKED_CAST")
class ViewModelFactory constructor(
    private val miruHiruRepository: MiruHiruRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>) =
        with(modelClass) {
            when {
                isAssignableFrom(MainViewModel::class.java) ->
                    MainViewModel(miruHiruRepository)

                else ->
                    throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        } as T
}